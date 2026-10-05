package service;

import exception.InsufficientStockException;
import exception.InvalidProductDataException;
import exception.ProductNotFoundException;
import model.Category;
import model.Product;
import model.StockTransaction;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class InventoryServiceImpl implements InventoryService {
    private final Map<String, Product> productMap = new ConcurrentHashMap<>();
    private final List<StockTransaction> transactions = Collections.synchronizedList(new ArrayList<>());
    private final AtomicInteger txCounter = new AtomicInteger(1000);

    @Override
    public void addProduct(Product product) throws InvalidProductDataException {
        if (product == null) throw new InvalidProductDataException("Product cannot be null");
        if (product.getId() == null || product.getId().trim().isEmpty()) {
            throw new InvalidProductDataException("Product ID cannot be empty");
        }
        if (product.getName() == null || product.getName().trim().isEmpty()) {
            throw new InvalidProductDataException("Product name cannot be empty");
        }
        if (product.getCostPrice() < 0 || product.getSellingPrice() < 0) {
            throw new InvalidProductDataException("Prices cannot be negative");
        }
        if (product.getQuantity() < 0) {
            throw new InvalidProductDataException("Initial quantity cannot be negative");
        }
        if (productMap.containsKey(product.getId())) {
            throw new InvalidProductDataException("Product with ID " + product.getId() + " already exists");
        }

        productMap.put(product.getId(), product);
        
        if (product.getQuantity() > 0) {
            transactions.add(new StockTransaction(
                    "TX-" + txCounter.incrementAndGet(),
                    product.getId(),
                    product.getName(),
                    StockTransaction.Type.STOCK_IN,
                    product.getQuantity(),
                    product.getCostPrice(),
                    "Initial stock"
            ));
        }
    }

    @Override
    public void updateProduct(String productId, String name, Double costPrice, Double sellingPrice, Integer reorderLevel, String supplier) throws ProductNotFoundException {
        Product product = getProductById(productId);
        if (name != null && !name.trim().isEmpty()) product.setName(name.trim());
        if (costPrice != null && costPrice >= 0) product.setCostPrice(costPrice);
        if (sellingPrice != null && sellingPrice >= 0) product.setSellingPrice(sellingPrice);
        if (reorderLevel != null && reorderLevel >= 0) product.setReorderLevel(reorderLevel);
        if (supplier != null) product.setSupplier(supplier.trim());
        product.touch();
    }

    @Override
    public void deleteProduct(String productId) throws ProductNotFoundException {
        if (!productMap.containsKey(productId)) {
            throw new ProductNotFoundException("Product not found with ID: " + productId);
        }
        productMap.remove(productId);
    }

    @Override
    public Product getProductById(String productId) throws ProductNotFoundException {
        Product p = productMap.get(productId);
        if (p == null) {
            throw new ProductNotFoundException("Product with ID '" + productId + "' not found.");
        }
        return p;
    }

    @Override
    public Product getProductBySku(String sku) throws ProductNotFoundException {
        return productMap.values().stream()
                .filter(p -> p.getSku().equalsIgnoreCase(sku))
                .findFirst()
                .orElseThrow(() -> new ProductNotFoundException("Product with SKU '" + sku + "' not found."));
    }

    @Override
    public List<Product> getAllProducts() {
        return productMap.values().stream()
                .sorted(Comparator.comparing(Product::getName))
                .collect(Collectors.toList());
    }

    @Override
    public List<Product> getLowStockProducts() {
        return productMap.values().stream()
                .filter(Product::isLowStock)
                .sorted(Comparator.comparingInt(Product::getQuantity))
                .collect(Collectors.toList());
    }

    @Override
    public List<Product> searchProducts(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) return getAllProducts();
        String lower = keyword.trim().toLowerCase();
        return productMap.values().stream()
                .filter(p -> p.getName().toLowerCase().contains(lower) ||
                             p.getSku().toLowerCase().contains(lower) ||
                             p.getCategory().name().toLowerCase().contains(lower) ||
                             (p.getSupplier() != null && p.getSupplier().toLowerCase().contains(lower)))
                .sorted(Comparator.comparing(Product::getName))
                .collect(Collectors.toList());
    }

    @Override
    public List<Product> getProductsByCategory(Category category) {
        return productMap.values().stream()
                .filter(p -> p.getCategory() == category)
                .sorted(Comparator.comparing(Product::getName))
                .collect(Collectors.toList());
    }

    @Override
    public void restockProduct(String productId, int quantity, String note) throws ProductNotFoundException, IllegalArgumentException {
        if (quantity <= 0) throw new IllegalArgumentException("Restock quantity must be positive");
        Product product = getProductById(productId);
        product.setQuantity(product.getQuantity() + quantity);
        product.touch();

        transactions.add(new StockTransaction(
                "TX-" + txCounter.incrementAndGet(),
                product.getId(),
                product.getName(),
                StockTransaction.Type.STOCK_IN,
                quantity,
                product.getCostPrice(),
                note != null && !note.isEmpty() ? note : "Restock replenishment"
        ));
    }

    @Override
    public void sellProduct(String productId, int quantity, String note) throws ProductNotFoundException, InsufficientStockException {
        if (quantity <= 0) throw new IllegalArgumentException("Sell quantity must be positive");
        Product product = getProductById(productId);
        if (product.getQuantity() < quantity) {
            throw new InsufficientStockException(String.format(
                    "Insufficient stock for product '%s'. Requested: %d, Available: %d",
                    product.getName(), quantity, product.getQuantity()
            ));
        }
        product.setQuantity(product.getQuantity() - quantity);
        product.touch();

        transactions.add(new StockTransaction(
                "TX-" + txCounter.incrementAndGet(),
                product.getId(),
                product.getName(),
                StockTransaction.Type.STOCK_OUT,
                quantity,
                product.getSellingPrice(),
                note != null && !note.isEmpty() ? note : "Customer sale"
        ));
    }

    @Override
    public List<StockTransaction> getTransactionHistory() {
        return new ArrayList<>(transactions);
    }

    @Override
    public double calculateTotalInventoryCost() {
        return productMap.values().stream().mapToDouble(Product::getTotalCostValue).sum();
    }

    @Override
    public double calculateTotalInventoryRetailValue() {
        return productMap.values().stream().mapToDouble(Product::getTotalRetailValue).sum();
    }

    @Override
    public double calculateTotalExpectedProfit() {
        return productMap.values().stream().mapToDouble(Product::getExpectedProfit).sum();
    }

    @Override
    public Map<Category, Long> getProductCountByCategory() {
        return productMap.values().stream()
                .collect(Collectors.groupingBy(Product::getCategory, TreeMap::new, Collectors.counting()));
    }

    @Override
    public Map<Category, Double> getInventoryValueByCategory() {
        return productMap.values().stream()
                .collect(Collectors.groupingBy(
                        Product::getCategory,
                        TreeMap::new,
                        Collectors.summingDouble(Product::getTotalRetailValue)
                ));
    }
}
