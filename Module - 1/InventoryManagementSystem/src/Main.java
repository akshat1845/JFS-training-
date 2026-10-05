import exception.InsufficientStockException;
import exception.InvalidProductDataException;
import exception.ProductNotFoundException;
import model.Category;
import model.Product;
import report.InventoryReportGenerator;
import service.InventoryService;
import service.InventoryServiceImpl;
import util.FileStorageUtil;

import java.io.File;
import java.util.List;
import java.util.Scanner;

/**
 * ====================================================================
 *  Module 1: Enterprise Smart Inventory Management & Audit System
 * ====================================================================
 *
 * Core Capabilities:
 *  1. Full CRUD for Products with categorized stock modeling
 *  2. Stock In (Procurement) & Stock Out (Sales Dispatch)
 *  3. Dynamic Low-Stock & Reorder Alerts
 *  4. Advanced Search (by keyword, SKU, category)
 *  5. Financial Valuation & Profitability Analytics
 *  6. CSV Data Persistence & Transaction Audit Trail
 */
public class Main {
    private static final String DATA_FILE = "data/inventory_data.csv";
    private static final String AUDIT_REPORT_FILE = "inventory_audit_report.txt";

    public static void main(String[] args) {
        InventoryService inventoryService = new InventoryServiceImpl();

        System.out.println("===============================================================");
        System.out.println("  SMART INVENTORY MANAGEMENT & AUDIT SYSTEM (MODULE 1)");
        System.out.println("===============================================================");

        // Load existing inventory data
        try {
            File file = new File(DATA_FILE);
            if (file.exists()) {
                FileStorageUtil.loadInventoryFromCsv(inventoryService, DATA_FILE);
                System.out.println("[SYSTEM] Successfully loaded " + inventoryService.getAllProducts().size() + " products from " + DATA_FILE);
            } else {
                seedInitialData(inventoryService);
                FileStorageUtil.saveInventoryToCsv(inventoryService, DATA_FILE);
                System.out.println("[SYSTEM] Initialized fresh catalog and saved to " + DATA_FILE);
            }
        } catch (Exception e) {
            System.err.println("[ERROR] Failed to load CSV data: " + e.getMessage());
        }

        // If run with demo flag or non-interactive
        if (args.length > 0 && "--demo".equalsIgnoreCase(args[0])) {
            runAutomatedDemonstration(inventoryService);
            return;
        }

        // Interactive Menu CLI
        runInteractiveCLI(inventoryService);
    }

    private static void runInteractiveCLI(InventoryService service) {
        Scanner scanner = new Scanner(System.in);
        boolean running = true;

        while (running) {
            System.out.println("\n---------------------------------------------------------------");
            System.out.println("                      MAIN MENU");
            System.out.println("---------------------------------------------------------------");
            System.out.println(" 1. View All Products");
            System.out.println(" 2. Search Products (Keyword / SKU / Category)");
            System.out.println(" 3. View Low-Stock Alerts");
            System.out.println(" 4. Add New Product");
            System.out.println(" 5. Restock Product (Stock IN)");
            System.out.println(" 6. Record Product Sale (Stock OUT)");
            System.out.println(" 7. View Stock Transaction Audit Trail");
            System.out.println(" 8. Generate & Export Analytics Report");
            System.out.println(" 9. Run Automated Test Scenario");
            System.out.println(" 0. Save & Exit");
            System.out.print("Select an option [0-9]: ");

            String input = scanner.hasNextLine() ? scanner.nextLine().trim() : "0";

            switch (input) {
                case "1":
                    displayProducts(service.getAllProducts());
                    break;
                case "2":
                    System.out.print("Enter search keyword: ");
                    String kw = scanner.nextLine().trim();
                    displayProducts(service.searchProducts(kw));
                    break;
                case "3":
                    List<Product> low = service.getLowStockProducts();
                    if (low.isEmpty()) {
                        System.out.println("\n[OK] All products have adequate inventory levels.");
                    } else {
                        System.out.println("\n[!] ATTENTION: Low stock detected on " + low.size() + " item(s):");
                        displayProducts(low);
                    }
                    break;
                case "4":
                    handleAddNewProduct(scanner, service);
                    break;
                case "5":
                    handleRestock(scanner, service);
                    break;
                case "6":
                    handleSale(scanner, service);
                    break;
                case "7":
                    System.out.println("\n--- TRANSACTION AUDIT LOG ---");
                    service.getTransactionHistory().forEach(System.out::println);
                    break;
                case "8":
                    String report = InventoryReportGenerator.generateComprehensiveReport(service);
                    System.out.println(report);
                    try {
                        InventoryReportGenerator.saveReportToFile(service, AUDIT_REPORT_FILE);
                        System.out.println("[SAVED] Exported audit report to: " + AUDIT_REPORT_FILE);
                    } catch (Exception e) {
                        System.err.println("[ERROR] Could not save report: " + e.getMessage());
                    }
                    break;
                case "9":
                    runAutomatedDemonstration(service);
                    break;
                case "0":
                case "":
                    try {
                        FileStorageUtil.saveInventoryToCsv(service, DATA_FILE);
                        System.out.println("\n[SAVED] Inventory data safely saved to " + DATA_FILE);
                    } catch (Exception e) {
                        System.err.println("[ERROR] Failed to save inventory: " + e.getMessage());
                    }
                    System.out.println("Exiting Smart Inventory System. Goodbye!");
                    running = false;
                    break;
                default:
                    System.out.println("Invalid option. Please choose between 0 and 9.");
            }
        }
    }

    private static void displayProducts(List<Product> products) {
        if (products.isEmpty()) {
            System.out.println("\n(No products found)");
            return;
        }
        System.out.println("\n" + "=".repeat(100));
        System.out.printf("%-6s | %-25s | %-12s | %-15s | %-5s | %-8s | %-8s | %s\n",
                "ID", "Product Name", "SKU", "Category", "Qty", "Cost", "Price", "Status");
        System.out.println("-".repeat(100));
        for (Product p : products) {
            System.out.printf("%-6s | %-25s | %-12s | %-15s | %5d | $%7.2f | $%7.2f | %s\n",
                    p.getId(),
                    p.getName().length() > 25 ? p.getName().substring(0, 22) + "..." : p.getName(),
                    p.getSku(),
                    p.getCategory(),
                    p.getQuantity(),
                    p.getCostPrice(),
                    p.getSellingPrice(),
                    p.isLowStock() ? "[LOW STOCK]" : "OK");
        }
        System.out.println("=".repeat(100));
    }

    private static void handleAddNewProduct(Scanner scanner, InventoryService service) {
        try {
            System.out.print("Product ID (e.g. P113): ");
            String id = scanner.nextLine().trim();
            System.out.print("Product Name: ");
            String name = scanner.nextLine().trim();
            System.out.print("SKU (e.g. TECH-XYZ): ");
            String sku = scanner.nextLine().trim();
            System.out.print("Category (ELECTRONICS, GROCERIES, APPAREL, FURNITURE, HEALTHCARE, AUTOMOTIVE, OFFICE_SUPPLIES): ");
            Category cat = Category.valueOf(scanner.nextLine().trim().toUpperCase());
            System.out.print("Cost Price: ");
            double cost = Double.parseDouble(scanner.nextLine().trim());
            System.out.print("Selling Price: ");
            double sell = Double.parseDouble(scanner.nextLine().trim());
            System.out.print("Initial Quantity: ");
            int qty = Integer.parseInt(scanner.nextLine().trim());
            System.out.print("Reorder Threshold: ");
            int threshold = Integer.parseInt(scanner.nextLine().trim());
            System.out.print("Supplier: ");
            String supplier = scanner.nextLine().trim();

            Product p = new Product(id, name, sku, cat, cost, sell, qty, threshold, supplier);
            service.addProduct(p);
            System.out.println("[SUCCESS] Added product: " + p.getName());
        } catch (Exception e) {
            System.err.println("[ERROR] Failed to add product: " + e.getMessage());
        }
    }

    private static void handleRestock(Scanner scanner, InventoryService service) {
        try {
            System.out.print("Enter Product ID to Restock: ");
            String id = scanner.nextLine().trim();
            System.out.print("Quantity to add: ");
            int qty = Integer.parseInt(scanner.nextLine().trim());
            System.out.print("Note / PO Reference: ");
            String note = scanner.nextLine().trim();

            service.restockProduct(id, qty, note);
            Product p = service.getProductById(id);
            System.out.println("[SUCCESS] Restocked! New quantity for '" + p.getName() + "' is " + p.getQuantity());
        } catch (Exception e) {
            System.err.println("[ERROR] Restock failed: " + e.getMessage());
        }
    }

    private static void handleSale(Scanner scanner, InventoryService service) {
        try {
            System.out.print("Enter Product ID Sold: ");
            String id = scanner.nextLine().trim();
            System.out.print("Quantity sold: ");
            int qty = Integer.parseInt(scanner.nextLine().trim());
            System.out.print("Customer / Order ID: ");
            String note = scanner.nextLine().trim();

            service.sellProduct(id, qty, note);
            Product p = service.getProductById(id);
            System.out.println("[SUCCESS] Sale recorded! Remaining stock for '" + p.getName() + "' is " + p.getQuantity());
            if (p.isLowStock()) {
                System.out.println("[WARNING] Item is now below reorder threshold (" + p.getReorderLevel() + ")!");
            }
        } catch (Exception e) {
            System.err.println("[ERROR] Sale transaction failed: " + e.getMessage());
        }
    }

    private static void runAutomatedDemonstration(InventoryService service) {
        System.out.println("\n>>> EXECUTING AUTOMATED SIMULATION & AUDIT <<<");
        try {
            // Perform simulated transactions
            System.out.println("\n1. Simulating restock of Logitech Mice (+15 units)...");
            service.restockProduct("P102", 15, "Batch PO-9921");

            System.out.println("2. Simulating customer purchase of Dell XPS 15 (2 units)...");
            service.sellProduct("P101", 2, "Invoice #INV-2026-001");

            System.out.println("3. Simulating purchase of Dry-Fit Hoodie (1 unit)...");
            service.sellProduct("P108", 1, "Order #ORD-882");

            System.out.println("\n4. Generating Updated Enterprise Audit Report...\n");
            String report = InventoryReportGenerator.generateComprehensiveReport(service);
            System.out.println(report);

            InventoryReportGenerator.saveReportToFile(service, AUDIT_REPORT_FILE);
            FileStorageUtil.saveInventoryToCsv(service, DATA_FILE);
            System.out.println("[DEMO COMPLETE] Simulation saved to disk successfully.");
        } catch (Exception e) {
            System.err.println("[DEMO ERROR] " + e.getMessage());
        }
    }

    private static void seedInitialData(InventoryService service) throws InvalidProductDataException {
        service.addProduct(new Product("P101", "Dell XPS 15 Laptop", "TECH-XPS15", Category.ELECTRONICS, 1100.0, 1499.99, 14, 5, "Dell Direct"));
        service.addProduct(new Product("P102", "Logitech MX Master 3S", "TECH-MXM3S", Category.ELECTRONICS, 65.0, 99.99, 4, 10, "Logitech Global"));
        service.addProduct(new Product("P103", "Samsung 4K 27 Monitor", "TECH-SAM27", Category.ELECTRONICS, 220.0, 329.5, 8, 6, "Samsung Displays"));
        service.addProduct(new Product("P104", "Ergonomic Mesh Chair", "FURN-ERG01", Category.FURNITURE, 140.0, 249.0, 3, 5, "Comfort Seating Co"));
        service.addProduct(new Product("P105", "Solid Oak Standing Desk", "FURN-OAKSD", Category.FURNITURE, 280.0, 499.0, 6, 4, "Nordic Woodworks"));
        service.addProduct(new Product("P106", "Organic Arabica Coffee", "GROC-COF01", Category.GROCERIES, 8.5, 14.99, 65, 20, "Green Valley Organics"));
        service.addProduct(new Product("P107", "Extra Virgin Olive Oil", "GROC-EVOO1", Category.GROCERIES, 12.0, 19.99, 18, 15, "Tuscany Estates"));
        service.addProduct(new Product("P108", "Dry-Fit Sports Hoodie", "APP-HD-BLK", Category.APPAREL, 22.0, 45.0, 2, 8, "Apex Athletics"));
        service.addProduct(new Product("P109", "Merino Wool Socks (Pack)", "APP-SCK-03", Category.APPAREL, 7.0, 16.5, 42, 12, "Highland Mills"));
        service.addProduct(new Product("P110", "Digital Blood Pressure Monitor", "HLTH-BPM01", Category.HEALTHCARE, 32.0, 64.99, 12, 5, "MedTech Innovations"));
        service.addProduct(new Product("P111", "Wireless Mechanical Keyboard", "TECH-KB-MEC", Category.ELECTRONICS, 75.0, 129.99, 5, 6, "Keychron Partners"));
        service.addProduct(new Product("P112", "Aluminum Laptop Stand", "OFFC-LST-01", Category.OFFICE_SUPPLIES, 15.0, 29.99, 25, 10, "Studio Desk Gear"));
    }
}
