package processor;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

/**
 * Utility to generate realistic, multi-partition sales CSV datasets for testing parallel processing.
 */
public class SampleDataGenerator {
    private static final String HEADER = "transactionId,date,region,customerType,category,product,unitsSold,unitPrice,discountRate,paymentMethod";

    public static void generateSampleDatasetsIfEmpty(String folderPath) {
        File dir = new File(folderPath);
        if (!dir.exists()) {
            dir.mkdirs();
        }

        File[] files = dir.listFiles((d, n) -> n.toLowerCase().endsWith(".csv"));
        if (files != null && files.length > 0) {
            return; // Already populated
        }

        System.out.println("[DATA-GEN] Generating realistic multi-region sample sales files in: " + folderPath);

        writeDataset(new File(dir, "sales_q1_north.csv"), new String[]{
                "TX-1001,2026-01-10,North America,Retail,Electronics,MacBook Pro M3,4,1999.00,0.05,Credit Card",
                "TX-1002,2026-01-14,North America,Wholesale,Furniture,Ergonomic Office Chair,25,180.00,0.15,Bank Transfer",
                "TX-1003,2026-01-20,North America,Retail,Apparel,Thermal Winter Jacket,12,120.00,0.10,PayPal",
                "TX-1004,2026-02-05,North America,Enterprise,Electronics,Dell UltraSharp 32,8,650.00,0.08,Credit Card",
                "TX-1005,2026-02-18,North America,Retail,Groceries,Premium Coffee Beans,80,14.50,0.00,UPI",
                "TX-1006,2026-03-02,North America,Wholesale,Healthcare,N95 Mask Packs,150,18.00,0.20,Bank Transfer",
                "TX-1007,2026-03-15,North America,Retail,Electronics,Wireless Headphones,15,199.99,0.05,Credit Card",
                "TX-1008,2026-03-28,North America,Retail,Furniture,Motorized Standing Desk,6,450.00,0.00,PayPal"
        });

        writeDataset(new File(dir, "sales_q2_europe.csv"), new String[]{
                "TX-2001,2026-04-02,Europe,Retail,Electronics,Sony WH-1000XM5,10,349.99,0.05,Credit Card",
                "TX-2002,2026-04-12,Europe,Wholesale,Groceries,Organic Olive Oil Batch,120,12.00,0.10,Bank Transfer",
                "TX-2003,2026-05-01,Europe,Retail,Apparel,Waterproof Hiking Boots,18,140.00,0.00,PayPal",
                "TX-2004,2026-05-18,Europe,Enterprise,Furniture,Conference Table Setup,4,1200.00,0.12,Bank Transfer",
                "TX-2005,2026-06-04,Europe,Retail,Electronics,4K OLED Gaming Monitor,7,799.00,0.05,Credit Card",
                "TX-2006,2026-06-20,Europe,Retail,Healthcare,Smart Health Tracker,30,89.50,0.05,UPI",
                "TX-2007,2026-06-25,Europe,Wholesale,Office Supplies,Ergonomic Keyboard Bundles,40,65.00,0.15,Bank Transfer"
        });

        writeDataset(new File(dir, "sales_q3_apac.csv"), new String[]{
                "TX-3001,2026-07-08,Asia-Pacific,Retail,Electronics,iPad Pro 12.9,14,1099.00,0.05,UPI",
                "TX-3002,2026-07-19,Asia-Pacific,Enterprise,Electronics,Server Rack Switches,5,2200.00,0.10,Bank Transfer",
                "TX-3003,2026-08-04,Asia-Pacific,Retail,Apparel,Dry-Fit Athletic Jersey,65,35.00,0.05,Credit Card",
                "TX-3004,2026-08-22,Asia-Pacific,Wholesale,Groceries,Green Tea Extract Cartons,200,9.00,0.15,Bank Transfer",
                "TX-3005,2026-09-02,Asia-Pacific,Retail,Furniture,Mesh Drafting Stool,15,110.00,0.00,UPI",
                "TX-3006,2026-09-17,Asia-Pacific,Retail,Electronics,Mechanical Keyboards,45,120.00,0.08,PayPal",
                "TX-3007,2026-09-29,Asia-Pacific,Wholesale,Healthcare,Infrared Thermometers,80,24.00,0.12,Credit Card"
        });

        writeDataset(new File(dir, "sales_online_global.csv"), new String[]{
                "TX-4001,2026-09-30,Global-Online,Retail,Electronics,Noise Cancelling Earbuds,55,149.00,0.10,Credit Card",
                "TX-4002,2026-10-01,Global-Online,Retail,Apparel,Merino Wool Sweaters,35,85.00,0.05,PayPal",
                "TX-4003,2026-10-02,Global-Online,Retail,Office Supplies,Aluminum Monitor Arms,48,55.00,0.05,Credit Card",
                "TX-4004,2026-10-03,Global-Online,Retail,Groceries,Artisan Spice Kits,90,22.50,0.00,UPI",
                "TX-4005,2026-10-04,Global-Online,Retail,Healthcare,Aromatherapy Diffusers,60,38.00,0.10,PayPal",
                "TX-4006,2026-10-05,Global-Online,Wholesale,Electronics,USB-C Docking Stations,60,95.00,0.18,Bank Transfer"
        });
    }

    private static void writeDataset(File targetFile, String[] rows) {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(targetFile))) {
            bw.write(HEADER);
            bw.newLine();
            for (String r : rows) {
                bw.write(r);
                bw.newLine();
            }
        } catch (IOException e) {
            System.err.println("Failed to write dataset " + targetFile.getName() + ": " + e.getMessage());
        }
    }
}
