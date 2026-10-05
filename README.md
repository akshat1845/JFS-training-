[README.md](https://github.com/user-attachments/files/33043986/README.md)
# JFS Training Practicals & Capstone Modules

Welcome to the Java Full Stack (JFS) Training Practical Projects repository. This repository houses two comprehensive enterprise-grade Java practical modules covering Core Java, Object-Oriented Design, Concurrency, Stream API, and Data Persistence.

---

## 📂 Repository Structure

```text
JFS-training-/
├── README.md
└── Module Project/
    ├── Module - 1/
    │   └── InventoryManagementSystem/
    │       ├── data/
    │       │   └── inventory_data.csv
    │       ├── src/
    │       │   ├── Main.java
    │       │   ├── model/
    │       │   │   ├── Category.java
    │       │   │   ├── Product.java
    │       │   │   └── StockTransaction.java
    │       │   ├── service/
    │       │   │   ├── InventoryService.java
    │       │   │   └── InventoryServiceImpl.java
    │       │   ├── exception/
    │       │   │   ├── ProductNotFoundException.java
    │       │   │   ├── InsufficientStockException.java
    │       │   │   └── InvalidProductDataException.java
    │       │   ├── util/
    │       │   │   └── FileStorageUtil.java
    │       │   └── report/
    │       │       └── InventoryReportGenerator.java
    │       ├── inventory_audit_report.txt
    │       ├── run.sh
    │       └── run.bat
    └── Module - 2/
        └── MultiThreadedFileProcessor/
            ├── data/
            │   ├── sales_q1_north.csv
            │   ├── sales_q2_europe.csv
            │   ├── sales_q3_apac.csv
            │   └── sales_online_global.csv
            ├── src/
            │   ├── Main.java
            │   ├── config/
            │   │   └── ProcessorConfig.java
            │   ├── model/
            │   │   ├── SalesTransaction.java
            │   │   └── ProcessingSummary.java
            │   ├── processor/
            │   │   ├── CsvFileProcessor.java
            │   │   └── SampleDataGenerator.java
            │   └── report/
            │       └── AnalyticsReportAggregator.java
            ├── analytics_report.txt
            ├── category_summary.csv
            ├── run.sh
            └── run.bat
```

---

## 📦 Module 1: Smart Inventory Management & Audit System

An enterprise-ready inventory tracking and audit system with real-time stock valuation, reorder threshold alerts, transaction histories, and file persistence.

### Key Highlights:
- **Object-Oriented Design & Clean Architecture:** Modular separation across `model`, `service`, `exception`, `util`, and `report` packages.
- **Stock Management:** Real-time stock-in (procurement) and stock-out (sales) validation with custom exceptions (`ProductNotFoundException`, `InsufficientStockException`).
- **Low-Stock Alert Monitor:** Automatically flags products falling below their dynamic reorder threshold.
- **Financial Analytics & Reporting:** Calculates inventory valuation at cost vs retail price, profit margins, and category distributions.
- **Data Persistence:** Automatic CSV storage synchronization (`FileStorageUtil`) and audit log tracking.

### Running Module 1:
```bash
cd "Module Project/Module - 1/InventoryManagementSystem"
# On Linux/macOS
./run.sh
# To run automated demo simulation
./run.sh --demo

# On Windows
run.bat
```

---

## ⚡ Module 2: High-Performance Multi-Threaded Sales Data Processor

An asynchronous, parallel file parsing and revenue analytics engine utilizing Java Concurrency utilities.

### Key Highlights:
- **Design Patterns:** Fluent **Builder Pattern** (`ProcessorConfig.Builder`) for custom thread counts, delimiters, filters, and output paths.
- **Java Concurrency Utilities:** Uses `ExecutorService`, `Callable<ProcessingSummary>`, and non-blocking `Future<T>` tokens for concurrent CSV partition processing.
- **Fault-Tolerant Parsing:** Robust error containment with malformed row detection and thread-level benchmarks.
- **Multi-Dimensional Business Analytics:**
  - Executive Financial Summary (Gross, Discounts, Net Revenue, Tax, Average Order Value).
  - Regional Performance (APAC, North America, Europe, Global Online).
  - Category Share & Units breakdown.
  - Payment Method and Customer Segmentation distributions.
  - Top 5 Products by Revenue.
- **Dual Export Options:** Exports human-readable analytics text report (`analytics_report.txt`) and tabular CSV summary (`category_summary.csv`).

### Running Module 2:
```bash
cd "Module Project/Module - 2/MultiThreadedFileProcessor"
# On Linux/macOS
./run.sh

# On Windows
run.bat
```

---

## 🛠️ Technology Stack
- **Language:** Java 17 / Java 21
- **Concurrency:** `java.util.concurrent` (`ExecutorService`, `Callable`, `Future`, `AtomicInteger`)
- **Functional Programming:** Java 8+ Stream API & Lambdas
- **I/O:** Java NIO & Buffered I/O Streams
