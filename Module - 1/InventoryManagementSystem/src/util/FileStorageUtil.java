package util;

import model.Product;
import service.InventoryService;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

public class FileStorageUtil {
    private static final String CSV_HEADER = "id,name,sku,category,costPrice,sellingPrice,quantity,reorderLevel,supplier,lastUpdated";

    public static void saveInventoryToCsv(InventoryService service, String filePath) throws IOException {
        Path path = Paths.get(filePath);
        if (path.getParent() != null) {
            Files.createDirectories(path.getParent());
        }

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath))) {
            writer.write(CSV_HEADER);
            writer.newLine();
            for (Product p : service.getAllProducts()) {
                writer.write(p.toCsvRow());
                writer.newLine();
            }
        }
    }

    public static void loadInventoryFromCsv(InventoryService service, String filePath) throws IOException {
        File file = new File(filePath);
        if (!file.exists()) {
            return;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line = reader.readLine(); // skip header
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) continue;
                try {
                    Product product = Product.fromCsvRow(line);
                    if (product != null) {
                        service.addProduct(product);
                    }
                } catch (Exception e) {
                    System.err.println("Warning: Skipping invalid CSV line: " + line + " (" + e.getMessage() + ")");
                }
            }
        }
    }
}
