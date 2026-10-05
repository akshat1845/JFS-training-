package service;

import exception.InsufficientStockException;
import exception.InvalidProductDataException;
import exception.ProductNotFoundException;
import model.Category;
import model.Product;
import model.StockTransaction;

import java.util.List;
import java.util.Map;

public interface InventoryService {
    void addProduct(Product product) throws InvalidProductDataException;
    void updateProduct(String productId, String name, Double costPrice, Double sellingPrice, Integer reorderLevel, String supplier) throws ProductNotFoundException;
    void deleteProduct(String productId) throws ProductNotFoundException;
    Product getProductById(String productId) throws ProductNotFoundException;
    Product getProductBySku(String sku) throws ProductNotFoundException;
    
    List<Product> getAllProducts();
    List<Product> getLowStockProducts();
    List<Product> searchProducts(String keyword);
    List<Product> getProductsByCategory(Category category);
    
    void restockProduct(String productId, int quantity, String note) throws ProductNotFoundException, IllegalArgumentException;
    void sellProduct(String productId, int quantity, String note) throws ProductNotFoundException, InsufficientStockException;
    
    List<StockTransaction> getTransactionHistory();
    
    double calculateTotalInventoryCost();
    double calculateTotalInventoryRetailValue();
    double calculateTotalExpectedProfit();
    
    Map<Category, Long> getProductCountByCategory();
    Map<Category, Double> getInventoryValueByCategory();
}
