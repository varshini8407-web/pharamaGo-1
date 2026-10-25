package com.example.pharmago.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.example.pharmago.models.DeliveryOrderModel;
import com.example.pharmago.models.InventoryItem;
import com.example.pharmago.models.Invoice;
import com.example.pharmago.models.OrderModel;
import com.example.pharmago.models.OrderStatus;
import com.example.pharmago.models.Payment;
import com.example.pharmago.models.PrescriptionMedicineItem;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import com.mysql.cj.jdbc.exceptions.CommunicationsException;

/**
 * Member 2 Database Connection & Persistence Layer
 * Provides direct MySQL/JDBC connectivity and local fallback persistence.
 */
public class DatabaseConnection extends SQLiteOpenHelper {
    private static final String TAG = "DatabaseConnection";
    private static final String DATABASE_NAME = "pharmago_hospital.db";
    private static final int DATABASE_VERSION = 5;

    private static DatabaseConnection instance;

    // ==========================================
    // MySQL JDBC Connectivity Configuration
    // ==========================================
    // "10.0.2.2" maps to localhost of host machine in Android Emulator
    // For physical devices on Wi-Fi, change to host PC IP (e.g., "10.143.137.47" or Wi-Fi IP)
    public static volatile String DB_HOST = "10.0.2.2";
    public static volatile int DB_PORT = 3306;
    public static volatile String DB_NAME = "pharmago";
    public static volatile String DB_USER = "root";
    public static volatile String DB_PASSWORD = "";

    public static void configureDatabase(String host, int port, String dbName, String user, String password) {
        if (host != null && !host.trim().isEmpty()) DB_HOST = host.trim();
        if (port > 0) DB_PORT = port;
        if (dbName != null && !dbName.trim().isEmpty()) DB_NAME = dbName.trim();
        if (user != null) DB_USER = user.trim();
        if (password != null) DB_PASSWORD = password;
        Log.i(TAG, "Configured MySQL target: jdbc:mysql://" + DB_HOST + ":" + DB_PORT + "/" + DB_NAME);
    }

    // Table: inventory
    public static final String TABLE_INVENTORY = "inventory";
    public static final String COL_MED_ID = "medicine_id";
    public static final String COL_MED_NAME = "medicine_name";
    public static final String COL_MED_DOSAGE = "dosage";
    public static final String COL_MED_STOCK = "available_stock";
    public static final String COL_MED_PRICE = "unit_price";
    public static final String COL_MED_CATEGORY = "category";
    public static final String COL_MED_BATCH = "batch_no";
    public static final String COL_MED_EXPIRY = "expiry_date";
    public static final String COL_MED_DISPENSED = "dispensed_count";

    // Table: pharmacist_orders
    public static final String TABLE_ORDERS = "pharmacist_orders";
    public static final String COL_ORDER_ID = "order_id";
    public static final String COL_PATIENT_NAME = "patient_name";
    public static final String COL_PATIENT_PHONE = "patient_phone";
    public static final String COL_ORDER_TYPE = "order_type";
    public static final String COL_STATUS = "status";
    public static final String COL_PRESCRIPTION_URL = "prescription_url";
    public static final String COL_TOTAL_AMOUNT = "total_amount";
    public static final String COL_OTP = "otp";
    public static final String COL_QR_STRING = "qr_string";
    public static final String COL_QR_BYTES = "qr_bytes";
    public static final String COL_TIMESTAMP = "order_timestamp";
    public static final String COL_ADDRESS = "delivery_address";
    public static final String COL_INVOICE_ID = "invoice_id";
    public static final String COL_PAYMENT_STATUS = "payment_status";
    public static final String COL_REJECTION_REASON = "rejection_reason";
    public static final String COL_RX_REQUIRED = "prescription_required";

    // Table: invoices
    public static final String TABLE_INVOICES = "invoices";
    public static final String COL_INV_ID = "invoice_id";
    public static final String COL_INV_ORDER_ID = "order_id";
    public static final String COL_INV_PHARMACIST = "pharmacist_id";
    public static final String COL_INV_SUBTOTAL = "subtotal";
    public static final String COL_INV_TAX = "tax";
    public static final String COL_INV_PLATFORM_FEE = "platform_fee";
    public static final String COL_INV_GRAND_TOTAL = "grand_total";
    public static final String COL_INV_TIMESTAMP = "timestamp";

    // Table: payments
    public static final String TABLE_PAYMENTS = "payments";
    public static final String COL_PAY_ID = "payment_id";
    public static final String COL_PAY_ORDER_ID = "order_id";
    public static final String COL_PAY_AMOUNT = "amount";
    public static final String COL_PAY_STATUS = "payment_status";
    public static final String COL_PAY_METHOD = "payment_method";
    public static final String COL_PAY_TIMESTAMP = "timestamp";

    public static synchronized DatabaseConnection getInstance(Context context) {
        if (instance == null) {
            instance = new DatabaseConnection(context != null ? context.getApplicationContext() : null);
        }
        return instance;
    }

    public static synchronized DatabaseConnection getInstance() {
        return instance;
    }

    public static boolean isEmulator() {
        return (android.os.Build.BRAND != null && android.os.Build.BRAND.startsWith("generic") && android.os.Build.DEVICE != null && android.os.Build.DEVICE.startsWith("generic"))
                || (android.os.Build.FINGERPRINT != null && (android.os.Build.FINGERPRINT.startsWith("generic") || android.os.Build.FINGERPRINT.startsWith("unknown")))
                || (android.os.Build.HARDWARE != null && (android.os.Build.HARDWARE.contains("goldfish") || android.os.Build.HARDWARE.contains("ranchu")))
                || (android.os.Build.MODEL != null && (android.os.Build.MODEL.contains("google_sdk") || android.os.Build.MODEL.contains("Emulator") || android.os.Build.MODEL.contains("Android SDK built for x86")))
                || (android.os.Build.PRODUCT != null && (android.os.Build.PRODUCT.contains("sdk_google") || android.os.Build.PRODUCT.contains("google_sdk") || android.os.Build.PRODUCT.contains("sdk")));
    }

    public static String getEffectiveHost() {
        if (!isEmulator() && "10.0.2.2".equals(DB_HOST)) {
            return "10.143.137.47";
        }
        return DB_HOST;
    }

    /**
     * Establishes a direct JDBC Connection to MySQL using com.mysql.cj.jdbc.Driver
     */
    public Connection getConnection() throws SQLException, CommunicationsException {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            Log.e(TAG, "MySQL JDBC Driver class not found: " + e.getMessage(), e);
            throw new SQLException("MySQL JDBC Driver missing from runtime classpath", e);
        }

        String targetHost = getEffectiveHost();
        // Requirement 1.2: URL parameters ?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true
        String jdbcUrl = "jdbc:mysql://" + targetHost + ":" + DB_PORT + "/" + DB_NAME
                + "?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true&connectTimeout=5000&socketTimeout=8000";

        Log.d(TAG, "Opening JDBC connection to " + jdbcUrl);
        try {
            Connection conn = DriverManager.getConnection(jdbcUrl, DB_USER, DB_PASSWORD);
            ensureSchemaInitialized(conn);
            return conn;
        } catch (SQLException e) {
            if (!"10.0.2.2".equals(targetHost)) {
                Log.w(TAG, "Failed connecting to " + targetHost + ", trying 10.0.2.2 fallback");
                String fallbackUrl = "jdbc:mysql://10.0.2.2:" + DB_PORT + "/" + DB_NAME
                        + "?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true&connectTimeout=3000&socketTimeout=5000";
                try {
                    Connection conn = DriverManager.getConnection(fallbackUrl, DB_USER, DB_PASSWORD);
                    ensureSchemaInitialized(conn);
                    return conn;
                } catch (Exception ignored) {}
            }
            throw e;
        }
    }

    private static volatile boolean schemaInitialized = false;

    /**
     * Requirement 1.3: Schema Verification & Table Creation
     * Creates users, medicines, orders, invoices, and order_items tables if they do not exist.
     */
    public synchronized void ensureSchemaInitialized(Connection conn) {
        if (schemaInitialized || conn == null) return;
        try (java.sql.Statement stmt = conn.createStatement()) {
            stmt.execute("CREATE TABLE IF NOT EXISTS users (" +
                    "id BIGINT AUTO_INCREMENT PRIMARY KEY, " +
                    "username VARCHAR(50) NOT NULL UNIQUE, " +
                    "password_hash VARCHAR(255) NOT NULL, " +
                    "full_name VARCHAR(100) NOT NULL, " +
                    "phone VARCHAR(20) NOT NULL, " +
                    "role VARCHAR(20) NOT NULL DEFAULT 'PATIENT', " +
                    "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP)");

            stmt.execute("CREATE TABLE IF NOT EXISTS medicines (" +
                    "id BIGINT AUTO_INCREMENT PRIMARY KEY, " +
                    "brand_name VARCHAR(100) NOT NULL, " +
                    "generic_name VARCHAR(150) NOT NULL, " +
                    "dosage VARCHAR(50) NOT NULL, " +
                    "unit_price DOUBLE NOT NULL, " +
                    "stock_qty INT NOT NULL DEFAULT 0, " +
                    "batch_no VARCHAR(50), " +
                    "expiry_date VARCHAR(50), " +
                    "is_rx_required INT DEFAULT 0, " +
                    "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP)");

            stmt.execute("CREATE TABLE IF NOT EXISTS orders (" +
                    "id BIGINT AUTO_INCREMENT PRIMARY KEY, " +
                    "patient_id BIGINT NOT NULL, " +
                    "order_type VARCHAR(20) NOT NULL, " +
                    "status VARCHAR(30) NOT NULL DEFAULT 'PENDING_VERIFICATION', " +
                    "delivery_address TEXT, " +
                    "prescription_image_path TEXT, " +
                    "qr_payload TEXT, " +
                    "delivery_otp VARCHAR(10), " +
                    "delivery_agent_id BIGINT, " +
                    "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                    "updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP)");

            stmt.execute("CREATE TABLE IF NOT EXISTS invoices (" +
                    "id BIGINT AUTO_INCREMENT PRIMARY KEY, " +
                    "order_id BIGINT NOT NULL, " +
                    "subtotal DOUBLE NOT NULL, " +
                    "tax DOUBLE NOT NULL, " +
                    "total_amount DOUBLE NOT NULL, " +
                    "payment_mode VARCHAR(50) DEFAULT 'CASH', " +
                    "payment_status VARCHAR(20) NOT NULL DEFAULT 'PENDING', " +
                    "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP)");

            stmt.execute("CREATE TABLE IF NOT EXISTS order_items (" +
                    "id BIGINT AUTO_INCREMENT PRIMARY KEY, " +
                    "order_id BIGINT NOT NULL, " +
                    "medicine_id BIGINT NOT NULL, " +
                    "quantity INT NOT NULL, " +
                    "unit_price DOUBLE NOT NULL)");

            // Pre-seed default demo users if not present
            stmt.execute("INSERT IGNORE INTO users (id, username, password_hash, full_name, phone, role) VALUES " +
                    "(1, 'patient', 'admin', 'Priya Patel', '+91 98112 34567', 'PATIENT'), " +
                    "(2, 'ITA1024', 'admin', 'Dr. Arvind Sharma', '+91 94441 23456', 'PHARMACIST'), " +
                    "(3, 'AGENT-402', 'admin', 'Rajesh Kumar', '+91 98765 43210', 'DELIVERY'), " +
                    "(4, 'admin', 'admin', 'System Admin', '+91 99999 88888', 'PATIENT'), " +
                    "(5, 'DEL402', 'admin', 'Rajesh Kumar (Delivery Courier)', '+91 98765 43214', 'DELIVERY')");

            // Pre-seed standard clinical medicines
            stmt.execute("INSERT IGNORE INTO medicines (id, brand_name, generic_name, dosage, unit_price, stock_qty, batch_no, expiry_date, is_rx_required) VALUES " +
                    "(1, 'Paracetamol 500mg', 'Acetaminophen', '500 mg Tablet', 15.00, 140, 'APL-PCM-801', '12/2027', 0), " +
                    "(2, 'Amoxicillin 500mg', 'Amoxicillin Trihydrate', '500 mg Capsule', 45.00, 85, 'APL-AMX-502', '08/2026', 1), " +
                    "(3, 'Azithromycin 250mg', 'Azithromycin', '250 mg Tablet', 95.00, 60, 'APL-AZI-303', '10/2026', 1), " +
                    "(4, 'Cetirizine 10mg', 'Cetirizine Hydrochloride', '10 mg Tablet', 12.00, 220, 'APL-CTZ-104', '05/2028', 0), " +
                    "(5, 'Metformin 500mg', 'Metformin HCl', '500 mg Extended Release', 28.00, 95, 'APL-MET-705', '03/2027', 1), " +
                    "(6, 'Atorvastatin 10mg', 'Atorvastatin Calcium', '10 mg Tablet', 65.00, 70, 'APL-ATV-906', '09/2026', 1), " +
                    "(7, 'Pantoprazole 40mg', 'Pantoprazole Sodium', '40 mg Gastro-resistant', 38.00, 110, 'APL-PAN-407', '11/2027', 0), " +
                    "(8, 'Ibuprofen 400mg', 'Ibuprofen', '400 mg Film-coated', 22.00, 130, 'APL-IBU-208', '07/2027', 0)");

            schemaInitialized = true;
            Log.i(TAG, "MySQL Schema successfully verified and initialized.");
        } catch (SQLException e) {
            Log.w(TAG, "Schema auto-init note: " + e.getMessage());
        }
    }

    private DatabaseConnection(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Create Inventory Table
        db.execSQL("CREATE TABLE " + TABLE_INVENTORY + " (" +
                COL_MED_ID + " INTEGER PRIMARY KEY, " +
                COL_MED_NAME + " TEXT, " +
                COL_MED_DOSAGE + " TEXT, " +
                COL_MED_STOCK + " INTEGER, " +
                COL_MED_PRICE + " REAL, " +
                COL_MED_CATEGORY + " TEXT, " +
                COL_MED_BATCH + " TEXT, " +
                COL_MED_EXPIRY + " TEXT, " +
                COL_MED_DISPENSED + " INTEGER DEFAULT 0)");

        // Create Orders Table
        db.execSQL("CREATE TABLE " + TABLE_ORDERS + " (" +
                COL_ORDER_ID + " INTEGER PRIMARY KEY, " +
                COL_PATIENT_NAME + " TEXT, " +
                COL_PATIENT_PHONE + " TEXT, " +
                COL_ORDER_TYPE + " TEXT, " +
                COL_STATUS + " TEXT, " +
                COL_PRESCRIPTION_URL + " TEXT, " +
                COL_TOTAL_AMOUNT + " REAL, " +
                COL_OTP + " TEXT, " +
                COL_QR_STRING + " TEXT, " +
                COL_QR_BYTES + " BLOB, " +
                COL_TIMESTAMP + " TEXT, " +
                COL_ADDRESS + " TEXT, " +
                COL_INVOICE_ID + " INTEGER, " +
                COL_PAYMENT_STATUS + " TEXT, " +
                COL_REJECTION_REASON + " TEXT, " +
                COL_RX_REQUIRED + " INTEGER DEFAULT 1)");

        // Create Invoices Table
        db.execSQL("CREATE TABLE " + TABLE_INVOICES + " (" +
                COL_INV_ID + " INTEGER PRIMARY KEY, " +
                COL_INV_ORDER_ID + " INTEGER, " +
                COL_INV_PHARMACIST + " TEXT, " +
                COL_INV_SUBTOTAL + " REAL, " +
                COL_INV_TAX + " REAL, " +
                COL_INV_PLATFORM_FEE + " REAL, " +
                COL_INV_GRAND_TOTAL + " REAL, " +
                COL_INV_TIMESTAMP + " TEXT)");

        // Create Payments Table
        db.execSQL("CREATE TABLE " + TABLE_PAYMENTS + " (" +
                COL_PAY_ID + " INTEGER PRIMARY KEY, " +
                COL_PAY_ORDER_ID + " INTEGER, " +
                COL_PAY_AMOUNT + " REAL, " +
                COL_PAY_STATUS + " TEXT, " +
                COL_PAY_METHOD + " TEXT, " +
                COL_PAY_TIMESTAMP + " TEXT)");

        // Seed initial inventory
        seedInventory(db);
        // Seed initial hospital orders
        seedOrders(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_INVENTORY);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_ORDERS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_INVOICES);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_PAYMENTS);
        onCreate(db);
    }

    private void seedInventory(SQLiteDatabase db) {
        insertInventoryItem(db, 201L, "Amoxicillin 500mg", "Capsule • Strip of 10", 45, 90.00, "Antibiotics", "BATCH-AMX-2026", "12/2027", 120);
        insertInventoryItem(db, 202L, "Paracetamol 650mg (Dolo)", "Tablet • Strip of 15", 80, 35.00, "Pain Relief", "BATCH-PCM-2026", "08/2028", 240);
        insertInventoryItem(db, 203L, "Azithromycin 500mg", "Tablet • Strip of 3", 8, 115.00, "Antibiotics", "BATCH-AZM-2026", "05/2027", 65);
        insertInventoryItem(db, 204L, "Pantoprazole 40mg", "Tablet • Strip of 10", 50, 85.00, "Gastro", "BATCH-PAN-2026", "11/2027", 150);
        insertInventoryItem(db, 205L, "Cetirizine 10mg", "Tablet • Strip of 10", 65, 25.00, "Allergy", "BATCH-CTZ-2026", "03/2028", 85);
        insertInventoryItem(db, 206L, "Metformin 500mg", "Tablet • Strip of 20", 14, 60.00, "Cardiovascular", "BATCH-MET-2026", "09/2027", 95);
        insertInventoryItem(db, 207L, "Atorvastatin 20mg", "Tablet • Strip of 10", 35, 140.00, "Cardiovascular", "BATCH-ATV-2026", "10/2027", 50);
        insertInventoryItem(db, 208L, "Vitamin C & Zinc 500mg", "Chewable • Strip of 15", 90, 75.00, "Vitamins", "BATCH-VIT-2026", "01/2029", 180);
        insertInventoryItem(db, 209L, "Ibuprofen 400mg", "Tablet • Strip of 10", 5, 42.00, "Pain Relief", "BATCH-IBU-2026", "04/2027", 40);
    }

    private void insertInventoryItem(SQLiteDatabase db, long id, String name, String dosage, int stock,
                                     double price, String category, String batch, String expiry, int dispensed) {
        ContentValues cv = new ContentValues();
        cv.put(COL_MED_ID, id);
        cv.put(COL_MED_NAME, name);
        cv.put(COL_MED_DOSAGE, dosage);
        cv.put(COL_MED_STOCK, stock);
        cv.put(COL_MED_PRICE, price);
        cv.put(COL_MED_CATEGORY, category);
        cv.put(COL_MED_BATCH, batch);
        cv.put(COL_MED_EXPIRY, expiry);
        cv.put(COL_MED_DISPENSED, dispensed);
        db.insert(TABLE_INVENTORY, null, cv);
    }

    private void seedOrders(SQLiteDatabase db) {
        // Order 8090: Direct Takeaway ready for pickup with QR code
        ContentValues cv0 = new ContentValues();
        cv0.put(COL_ORDER_ID, 8090L);
        cv0.put(COL_PATIENT_NAME, "Vikram Verma");
        cv0.put(COL_PATIENT_PHONE, "+91 98450 12345");
        cv0.put(COL_ORDER_TYPE, "TAKEAWAY");
        cv0.put(COL_STATUS, OrderStatus.READY_FOR_PICKUP.name());
        cv0.put(COL_PRESCRIPTION_URL, "");
        cv0.put(COL_TOTAL_AMOUNT, 185.00);
        cv0.put(COL_TIMESTAMP, "Today, 03:15 PM");
        cv0.put(COL_ADDRESS, "Hospital Ground Floor Counter B");
        cv0.put(COL_INVOICE_ID, 1040L);
        cv0.put(COL_QR_STRING, "ORDER:8090:INV:1040");
        cv0.put(COL_PAYMENT_STATUS, "PAID ONLINE (UPI)");
        cv0.put(COL_RX_REQUIRED, 0);
        db.insert(TABLE_ORDERS, null, cv0);

        // Order 8091: Direct Takeaway awaiting pickup (Cash payment pending)
        ContentValues cv1 = new ContentValues();
        cv1.put(COL_ORDER_ID, 8091L);
        cv1.put(COL_PATIENT_NAME, "Sunita Rao");
        cv1.put(COL_PATIENT_PHONE, "+91 97401 23456");
        cv1.put(COL_ORDER_TYPE, "TAKEAWAY");
        cv1.put(COL_STATUS, OrderStatus.READY_FOR_PICKUP.name());
        cv1.put(COL_PRESCRIPTION_URL, "");
        cv1.put(COL_TOTAL_AMOUNT, 120.00);
        cv1.put(COL_TIMESTAMP, "Today, 03:45 PM");
        cv1.put(COL_ADDRESS, "Hospital Ground Floor Counter B");
        cv1.put(COL_INVOICE_ID, 1041L);
        cv1.put(COL_QR_STRING, "ORDER:8091:INV:1041");
        cv1.put(COL_PAYMENT_STATUS, "CASH ON PICKUP (Unpaid)");
        cv1.put(COL_RX_REQUIRED, 0);
        db.insert(TABLE_ORDERS, null, cv1);

        // Order 8092: Takeaway awaiting prescription verification
        ContentValues cv2 = new ContentValues();
        cv2.put(COL_ORDER_ID, 8092L);
        cv2.put(COL_PATIENT_NAME, "Rahul Sharma");
        cv2.put(COL_PATIENT_PHONE, "+91 98765 43210");
        cv2.put(COL_ORDER_TYPE, "TAKEAWAY");
        cv2.put(COL_STATUS, OrderStatus.PENDING_VERIFICATION.name());
        cv2.put(COL_PRESCRIPTION_URL, "sample_rx_1");
        cv2.put(COL_TOTAL_AMOUNT, 215.00);
        cv2.put(COL_TIMESTAMP, "Today, 04:30 PM");
        cv2.put(COL_ADDRESS, "Hospital Ground Floor Counter B");
        cv2.put(COL_PAYMENT_STATUS, "PAID ONLINE (UPI)");
        cv2.put(COL_RX_REQUIRED, 1);
        db.insert(TABLE_ORDERS, null, cv2);

        // Order 8093: New Assignment (Ready for delivery pickup)
        ContentValues cv3 = new ContentValues();
        cv3.put(COL_ORDER_ID, 8093L);
        cv3.put(COL_PATIENT_NAME, "Priya Patel");
        cv3.put(COL_PATIENT_PHONE, "+91 98112 34567");
        cv3.put(COL_ORDER_TYPE, "DELIVERY");
        cv3.put(COL_STATUS, OrderStatus.READY_FOR_DELIVERY.name());
        cv3.put(COL_PRESCRIPTION_URL, "sample_rx_2");
        cv3.put(COL_TOTAL_AMOUNT, 235.00);
        cv3.put(COL_TIMESTAMP, "Today, 04:45 PM");
        cv3.put(COL_ADDRESS, "Flat 402, Lotus Orchid, Sector 4, Metro City");
        cv3.put(COL_OTP, "549123");
        cv3.put(COL_PAYMENT_STATUS, "PAID ONLINE (UPI)");
        cv3.put(COL_RX_REQUIRED, 1);
        db.insert(TABLE_ORDERS, null, cv3);

        // Order 8094: In-Transit / Active Delivery
        ContentValues cv4 = new ContentValues();
        cv4.put(COL_ORDER_ID, 8094L);
        cv4.put(COL_PATIENT_NAME, "Dr. Ananya Sen");
        cv4.put(COL_PATIENT_PHONE, "+91 97234 56789");
        cv4.put(COL_ORDER_TYPE, "DELIVERY");
        cv4.put(COL_STATUS, OrderStatus.OUT_FOR_DELIVERY.name());
        cv4.put(COL_PRESCRIPTION_URL, "sample_rx_3");
        cv4.put(COL_TOTAL_AMOUNT, 110.00);
        cv4.put(COL_TIMESTAMP, "Today, 05:10 PM");
        cv4.put(COL_ADDRESS, "Villa 12, Palm Meadows, Whitefield, Metro City");
        cv4.put(COL_OTP, "827419");
        cv4.put(COL_PAYMENT_STATUS, "CASH_ON_DELIVERY");
        cv4.put(COL_RX_REQUIRED, 1);
        db.insert(TABLE_ORDERS, null, cv4);

        // Order 8088: Completed Delivery History
        ContentValues cv6 = new ContentValues();
        cv6.put(COL_ORDER_ID, 8088L);
        cv6.put(COL_PATIENT_NAME, "Karthik Nair");
        cv6.put(COL_PATIENT_PHONE, "+91 98451 98765");
        cv6.put(COL_ORDER_TYPE, "DELIVERY");
        cv6.put(COL_STATUS, OrderStatus.DELIVERED.name());
        cv6.put(COL_PRESCRIPTION_URL, "");
        cv6.put(COL_TOTAL_AMOUNT, 280.00);
        cv6.put(COL_TIMESTAMP, "Today, 10:45 AM");
        cv6.put(COL_ADDRESS, "Block C, 204, Green Glen Layout, Bellandur, Metro City");
        cv6.put(COL_OTP, "619284");
        cv6.put(COL_PAYMENT_STATUS, "PAID ONLINE (UPI)");
        cv6.put(COL_RX_REQUIRED, 1);
        db.insert(TABLE_ORDERS, null, cv6);

        // Order 8089: Completed Takeaway Order
        ContentValues cv5 = new ContentValues();
        cv5.put(COL_ORDER_ID, 8089L);
        cv5.put(COL_PATIENT_NAME, "Rajesh Kumar");
        cv5.put(COL_PATIENT_PHONE, "+91 99887 76655");
        cv5.put(COL_ORDER_TYPE, "TAKEAWAY");
        cv5.put(COL_STATUS, OrderStatus.COMPLETED.name());
        cv5.put(COL_PRESCRIPTION_URL, "");
        cv5.put(COL_TOTAL_AMOUNT, 310.00);
        cv5.put(COL_TIMESTAMP, "Today, 02:00 PM");
        cv5.put(COL_ADDRESS, "Hospital Ground Floor Counter B");
        cv5.put(COL_INVOICE_ID, 1039L);
        cv5.put(COL_PAYMENT_STATUS, "PAID ONLINE (UPI)");
        cv5.put(COL_RX_REQUIRED, 0);
        db.insert(TABLE_ORDERS, null, cv5);

        // Order 101: Takeaway Ready for Pickup
        ContentValues cv101 = new ContentValues();
        cv101.put(COL_ORDER_ID, 101L);
        cv101.put(COL_PATIENT_NAME, "Priya Patel");
        cv101.put(COL_PATIENT_PHONE, "+91 98112 34567");
        cv101.put(COL_ORDER_TYPE, "TAKEAWAY");
        cv101.put(COL_STATUS, OrderStatus.READY_FOR_PICKUP.name());
        cv101.put(COL_PRESCRIPTION_URL, "");
        cv101.put(COL_TOTAL_AMOUNT, 80.00);
        cv101.put(COL_TIMESTAMP, "Today, 11:00 AM");
        cv101.put(COL_ADDRESS, "Hospital Ground Floor Counter B");
        cv101.put(COL_INVOICE_ID, 1101L);
        cv101.put(COL_QR_STRING, "ORDER:101:INV:1101");
        cv101.put(COL_PAYMENT_STATUS, "PAID ONLINE (UPI)");
        cv101.put(COL_RX_REQUIRED, 0);
        db.insert(TABLE_ORDERS, null, cv101);

        // Order 102: Takeaway Ready for Pickup (Standard Client Pickup Demo)
        ContentValues cv102 = new ContentValues();
        cv102.put(COL_ORDER_ID, 102L);
        cv102.put(COL_PATIENT_NAME, "Priya Patel");
        cv102.put(COL_PATIENT_PHONE, "+91 98112 34567");
        cv102.put(COL_ORDER_TYPE, "TAKEAWAY");
        cv102.put(COL_STATUS, OrderStatus.READY_FOR_PICKUP.name());
        cv102.put(COL_PRESCRIPTION_URL, "");
        cv102.put(COL_TOTAL_AMOUNT, 135.00);
        cv102.put(COL_TIMESTAMP, "Today, 11:30 AM");
        cv102.put(COL_ADDRESS, "Hospital Ground Floor Counter B");
        cv102.put(COL_INVOICE_ID, 1102L);
        cv102.put(COL_QR_STRING, "ORDER:102:INV:1102");
        cv102.put(COL_PAYMENT_STATUS, "PAID ONLINE (UPI)");
        cv102.put(COL_RX_REQUIRED, 0);
        db.insert(TABLE_ORDERS, null, cv102);

        // Order 105: Takeaway Ready for Pickup
        ContentValues cv105 = new ContentValues();
        cv105.put(COL_ORDER_ID, 105L);
        cv105.put(COL_PATIENT_NAME, "Priya Patel");
        cv105.put(COL_PATIENT_PHONE, "+91 98112 34567");
        cv105.put(COL_ORDER_TYPE, "TAKEAWAY");
        cv105.put(COL_STATUS, OrderStatus.READY_FOR_PICKUP.name());
        cv105.put(COL_PRESCRIPTION_URL, "");
        cv105.put(COL_TOTAL_AMOUNT, 95.00);
        cv105.put(COL_TIMESTAMP, "Today, 12:15 PM");
        cv105.put(COL_ADDRESS, "Hospital Ground Floor Counter B");
        cv105.put(COL_INVOICE_ID, 1105L);
        cv105.put(COL_QR_STRING, "ORDER:105:INV:1105");
        cv105.put(COL_PAYMENT_STATUS, "PAID ONLINE (UPI)");
        cv105.put(COL_RX_REQUIRED, 0);
        db.insert(TABLE_ORDERS, null, cv105);

        // Order 1: Live Spring Boot Sync Takeaway
        ContentValues cvL1 = new ContentValues();
        cvL1.put(COL_ORDER_ID, 1L);
        cvL1.put(COL_PATIENT_NAME, "Priya Patel");
        cvL1.put(COL_PATIENT_PHONE, "+91 98112 34567");
        cvL1.put(COL_ORDER_TYPE, "TAKEAWAY");
        cvL1.put(COL_STATUS, OrderStatus.READY_FOR_PICKUP.name());
        cvL1.put(COL_PRESCRIPTION_URL, "");
        cvL1.put(COL_TOTAL_AMOUNT, 80.00);
        cvL1.put(COL_TIMESTAMP, "Today, 01:00 PM");
        cvL1.put(COL_ADDRESS, "Hospital Ground Floor Counter B");
        cvL1.put(COL_INVOICE_ID, 1001L);
        cvL1.put(COL_QR_STRING, "ORDER:1:INV:1001");
        cvL1.put(COL_PAYMENT_STATUS, "PAID ONLINE (UPI)");
        cvL1.put(COL_RX_REQUIRED, 0);
        db.insert(TABLE_ORDERS, null, cvL1);

        // Order 3: Live Spring Boot Sync Takeaway
        ContentValues cvL3 = new ContentValues();
        cvL3.put(COL_ORDER_ID, 3L);
        cvL3.put(COL_PATIENT_NAME, "Sunita Rao");
        cvL3.put(COL_PATIENT_PHONE, "+91 97401 23456");
        cvL3.put(COL_ORDER_TYPE, "TAKEAWAY");
        cvL3.put(COL_STATUS, OrderStatus.READY_FOR_PICKUP.name());
        cvL3.put(COL_PRESCRIPTION_URL, "");
        cvL3.put(COL_TOTAL_AMOUNT, 90.00);
        cvL3.put(COL_TIMESTAMP, "Today, 02:30 PM");
        cvL3.put(COL_ADDRESS, "Hospital Ground Floor Counter B");
        cvL3.put(COL_INVOICE_ID, 1003L);
        cvL3.put(COL_QR_STRING, "ORDER:3:INV:1003");
        cvL3.put(COL_PAYMENT_STATUS, "PAID ONLINE (UPI)");
        cvL3.put(COL_RX_REQUIRED, 0);
        db.insert(TABLE_ORDERS, null, cvL3);
    }

    // ==========================================
    // Screen 1: Dashboard Metrics
    // ==========================================
    public synchronized Map<String, Object> getDashboardMetrics() {
        Map<String, Object> metrics = new HashMap<>();
        SQLiteDatabase db = getReadableDatabase();

        // 1. Pending Prescriptions
        int pendingCount = 0;
        Cursor c1 = db.rawQuery("SELECT COUNT(*) FROM " + TABLE_ORDERS +
                " WHERE " + COL_STATUS + " = ?", new String[]{OrderStatus.PENDING_VERIFICATION.name()});
        if (c1 != null) {
            if (c1.moveToFirst()) pendingCount = c1.getInt(0);
            c1.close();
        }

        // 2. Today's Revenue (from orders ready, delivered, or completed)
        double todayRevenue = 0.0;
        Cursor c2 = db.rawQuery("SELECT SUM(" + COL_TOTAL_AMOUNT + ") FROM " + TABLE_ORDERS +
                " WHERE " + COL_STATUS + " IN (?, ?, ?, ?)",
                new String[]{OrderStatus.READY_FOR_PICKUP.name(), OrderStatus.READY_FOR_DELIVERY.name(),
                        OrderStatus.DELIVERED.name(), OrderStatus.COMPLETED.name()});
        if (c2 != null) {
            if (c2.moveToFirst()) todayRevenue = c2.getDouble(0);
            c2.close();
        }

        // 3. Ready for Pickup
        int pickupCount = 0;
        Cursor c3 = db.rawQuery("SELECT COUNT(*) FROM " + TABLE_ORDERS +
                " WHERE " + COL_STATUS + " = ?", new String[]{OrderStatus.READY_FOR_PICKUP.name()});
        if (c3 != null) {
            if (c3.moveToFirst()) pickupCount = c3.getInt(0);
            c3.close();
        }

        // 4. Low Stock Alerts (< 20 units)
        int lowStockCount = 0;
        Cursor c4 = db.rawQuery("SELECT COUNT(*) FROM " + TABLE_INVENTORY +
                " WHERE " + COL_MED_STOCK + " < 20", null);
        if (c4 != null) {
            if (c4.moveToFirst()) lowStockCount = c4.getInt(0);
            c4.close();
        }

        metrics.put("pendingCount", pendingCount);
        metrics.put("todayRevenue", todayRevenue);
        metrics.put("pickupCount", pickupCount);
        metrics.put("lowStockCount", lowStockCount);

        return metrics;
    }

    // ==========================================
    // Screen 2 & 3: Orders Feed & Queries
    // ==========================================
    public synchronized List<OrderModel> getPharmacistOrders() {
        return getPharmacistOrders("ALL");
    }

    public synchronized List<OrderModel> getPharmacistOrders(String tabFilter) {
        List<OrderModel> orders = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();

        String query = "SELECT * FROM " + TABLE_ORDERS;
        String[] args = null;

        if ("DIRECT".equalsIgnoreCase(tabFilter)) {
            query += " WHERE " + COL_RX_REQUIRED + " = 0 ORDER BY " + COL_ORDER_ID + " DESC";
        } else if ("PRESCRIPTION".equalsIgnoreCase(tabFilter)) {
            query += " WHERE " + COL_RX_REQUIRED + " = 1 ORDER BY " + COL_ORDER_ID + " DESC";
        } else {
            query += " ORDER BY " + COL_ORDER_ID + " DESC";
        }

        Cursor cursor = db.rawQuery(query, args);
        if (cursor != null) {
            while (cursor.moveToNext()) {
                orders.add(parseOrderCursor(cursor));
            }
            cursor.close();
        }
        return orders;
    }

    public synchronized OrderModel getOrderById(long orderId) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_ORDERS + " WHERE " + COL_ORDER_ID + " = ?",
                new String[]{String.valueOf(orderId)});
        OrderModel order = null;
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                order = parseOrderCursor(cursor);
            }
            cursor.close();
        }
        return order;
    }

    public synchronized void insertOrUpdateOrder(OrderModel order) {
        if (order == null || order.getOrderId() == null) return;
        try {
            SQLiteDatabase db = getWritableDatabase();
            ContentValues cv = new ContentValues();
            cv.put(COL_ORDER_ID, order.getOrderId());
            cv.put(COL_PATIENT_NAME, order.getPatientName() != null ? order.getPatientName() : "Patient");
            cv.put(COL_PATIENT_PHONE, order.getPatientPhone() != null ? order.getPatientPhone() : "+91 98112 34567");
            cv.put(COL_ORDER_TYPE, order.getOrderType() != null ? order.getOrderType() : "TAKEAWAY");
            cv.put(COL_STATUS, order.getStatus() != null ? order.getStatus().name() : OrderStatus.READY_FOR_PICKUP.name());
            cv.put(COL_PRESCRIPTION_URL, order.getPrescriptionImageUrl() != null ? order.getPrescriptionImageUrl() : "");
            cv.put(COL_TOTAL_AMOUNT, order.getTotalAmount() != null ? order.getTotalAmount() : 120.00);
            cv.put(COL_TIMESTAMP, order.getOrderTimestamp() != null ? order.getOrderTimestamp() : "Today");
            cv.put(COL_ADDRESS, order.getDeliveryAddress() != null ? order.getDeliveryAddress() : "Hospital Ground Floor Counter B");
            cv.put(COL_INVOICE_ID, order.getInvoiceId() != null ? order.getInvoiceId() : order.getOrderId() + 1000L);
            cv.put(COL_QR_STRING, order.getQrString() != null ? order.getQrString() : "ORDER:" + order.getOrderId() + ":INV:" + (order.getOrderId() + 1000L));
            cv.put(COL_PAYMENT_STATUS, order.getPaymentStatus() != null ? order.getPaymentStatus() : "PAID ONLINE (UPI)");
            cv.put(COL_RX_REQUIRED, order.isPrescriptionRequired() ? 1 : 0);
            db.insertWithOnConflict(TABLE_ORDERS, null, cv, SQLiteDatabase.CONFLICT_REPLACE);
            Log.i(TAG, "Order #" + order.getOrderId() + " synced into SQLite successfully.");
        } catch (Exception e) {
            Log.e(TAG, "Failed to insertOrUpdateOrder #" + order.getOrderId(), e);
        }
    }

    public synchronized OrderModel getOrderByQr(String qrString) {
        if (qrString == null || qrString.trim().isEmpty()) return null;
        qrString = qrString.trim();

        SQLiteDatabase db = getReadableDatabase();

        // 1. Direct query on exact qr_string column
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_ORDERS + " WHERE " + COL_QR_STRING + " = ?",
                new String[]{qrString});
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                OrderModel order = parseOrderCursor(cursor);
                cursor.close();
                return order;
            }
            cursor.close();
        }

        // 2. Parse Order ID with regex helper
        Long parsedId = com.example.pharmago.utils.QRCodeScanner.parseOrderIdFromPayload(qrString);
        if (parsedId != null && parsedId > 0) {
            OrderModel order = getOrderById(parsedId);
            if (order != null) return order;
        }

        // 3. Fallback LIKE search on qr_string column
        if (parsedId != null && parsedId > 0) {
            cursor = db.rawQuery("SELECT * FROM " + TABLE_ORDERS + " WHERE " + COL_QR_STRING + " LIKE ? OR " + COL_ORDER_ID + " = ?",
                    new String[]{"%ORDER:" + parsedId + "%", String.valueOf(parsedId)});
            if (cursor != null) {
                if (cursor.moveToFirst()) {
                    OrderModel order = parseOrderCursor(cursor);
                    cursor.close();
                    return order;
                }
                cursor.close();
            }
        }

        // 4. Case-insensitive substring match
        cursor = db.rawQuery("SELECT * FROM " + TABLE_ORDERS + " WHERE " + COL_QR_STRING + " LIKE ?",
                new String[]{"%" + qrString + "%"});
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                OrderModel order = parseOrderCursor(cursor);
                cursor.close();
                return order;
            }
            cursor.close();
        }

        return null;
    }

    private OrderModel parseOrderCursor(Cursor cursor) {
        long orderId = cursor.getLong(cursor.getColumnIndexOrThrow(COL_ORDER_ID));
        String patientName = cursor.getString(cursor.getColumnIndexOrThrow(COL_PATIENT_NAME));
        String patientPhone = cursor.getString(cursor.getColumnIndexOrThrow(COL_PATIENT_PHONE));
        String orderType = cursor.getString(cursor.getColumnIndexOrThrow(COL_ORDER_TYPE));
        String statusStr = cursor.getString(cursor.getColumnIndexOrThrow(COL_STATUS));
        String rxUrl = cursor.getString(cursor.getColumnIndexOrThrow(COL_PRESCRIPTION_URL));
        double totalAmount = cursor.getDouble(cursor.getColumnIndexOrThrow(COL_TOTAL_AMOUNT));
        String timestamp = cursor.getString(cursor.getColumnIndexOrThrow(COL_TIMESTAMP));
        String address = cursor.getString(cursor.getColumnIndexOrThrow(COL_ADDRESS));
        String otp = cursor.getString(cursor.getColumnIndexOrThrow(COL_OTP));
        String qrString = cursor.getString(cursor.getColumnIndexOrThrow(COL_QR_STRING));
        byte[] qrBytes = cursor.getBlob(cursor.getColumnIndexOrThrow(COL_QR_BYTES));
        long invoiceId = cursor.getLong(cursor.getColumnIndexOrThrow(COL_INVOICE_ID));
        String payStatus = cursor.getString(cursor.getColumnIndexOrThrow(COL_PAYMENT_STATUS));
        String rejReason = cursor.getString(cursor.getColumnIndexOrThrow(COL_REJECTION_REASON));
        boolean rxReq = cursor.getInt(cursor.getColumnIndexOrThrow(COL_RX_REQUIRED)) == 1;

        OrderStatus status;
        try {
            status = OrderStatus.valueOf(statusStr);
        } catch (Exception e) {
            status = OrderStatus.PENDING_VERIFICATION;
        }

        List<PrescriptionMedicineItem> items = getOrderMedicines(orderId);

        OrderModel order = new OrderModel(orderId, patientName, patientPhone, orderType,
                status, rxUrl, totalAmount, timestamp, address, items);
        order.setOtp(otp);
        order.setQrCodeString(qrString);
        order.setQrCodeBytes(qrBytes);
        order.setInvoiceId(invoiceId);
        order.setPaymentStatus(payStatus);
        order.setRejectionReason(rejReason);
        order.setPrescriptionRequired(rxReq);

        return order;
    }

    public synchronized List<PrescriptionMedicineItem> getOrderMedicines(long orderId) {
        List<PrescriptionMedicineItem> list = new ArrayList<>();
        if (orderId == 8090L) {
            list.add(new PrescriptionMedicineItem(202L, "Paracetamol 650mg (Dolo)", "Tablet • Strip of 15", 2, getInventoryStock(202L), 35.00));
            list.add(new PrescriptionMedicineItem(208L, "Vitamin C & Zinc 500mg", "Chewable • Strip of 15", 1, getInventoryStock(208L), 75.00));
        } else if (orderId == 8091L) {
            list.add(new PrescriptionMedicineItem(204L, "Pantoprazole 40mg", "Tablet • Strip of 10", 1, getInventoryStock(204L), 85.00));
            list.add(new PrescriptionMedicineItem(202L, "Paracetamol 650mg (Dolo)", "Tablet • Strip of 15", 1, getInventoryStock(202L), 35.00));
        } else if (orderId == 8092L) {
            list.add(new PrescriptionMedicineItem(201L, "Amoxicillin 500mg", "Capsule • Strip of 10", 2, getInventoryStock(201L), 90.00));
            list.add(new PrescriptionMedicineItem(202L, "Paracetamol 650mg (Dolo)", "Tablet • Strip of 15", 1, getInventoryStock(202L), 35.00));
        } else if (orderId == 8093L) {
            list.add(new PrescriptionMedicineItem(203L, "Azithromycin 500mg", "Tablet • Strip of 3", 1, getInventoryStock(203L), 115.00));
            list.add(new PrescriptionMedicineItem(204L, "Pantoprazole 40mg", "Tablet • Strip of 10", 1, getInventoryStock(204L), 85.00));
            list.add(new PrescriptionMedicineItem(202L, "Paracetamol 650mg (Dolo)", "Tablet • Strip of 15", 1, getInventoryStock(202L), 35.00));
        } else if (orderId == 8089L) {
            list.add(new PrescriptionMedicineItem(201L, "Amoxicillin 500mg", "Capsule • Strip of 10", 2, getInventoryStock(201L), 90.00));
            list.add(new PrescriptionMedicineItem(207L, "Atorvastatin 20mg", "Tablet • Strip of 10", 1, getInventoryStock(207L), 140.00));
        } else {
            list.add(new PrescriptionMedicineItem(204L, "Pantoprazole 40mg", "Tablet • Strip of 10", 1, getInventoryStock(204L), 85.00));
            list.add(new PrescriptionMedicineItem(205L, "Cetirizine 10mg", "Tablet • Strip of 10", 1, getInventoryStock(205L), 25.00));
        }
        return list;
    }

    /**
     * Requirement 2: Dispense takeaway order via QR scan
     */
    public synchronized boolean confirmAndDispenseOrder(long orderId) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(COL_STATUS, OrderStatus.COMPLETED.name());
        cv.put(COL_PAYMENT_STATUS, "PAID ONLINE (Dispensed)");

        int rows = db.update(TABLE_ORDERS, cv, COL_ORDER_ID + " = ?", new String[]{String.valueOf(orderId)});

        // Update payment record to PAID
        ContentValues cvPay = new ContentValues();
        cvPay.put(COL_PAY_STATUS, "PAID");
        db.update(TABLE_PAYMENTS, cvPay, COL_PAY_ORDER_ID + " = ?", new String[]{String.valueOf(orderId)});

        return rows > 0;
    }

    /**
     * Requirement 3: Reject order with reason
     */
    public synchronized boolean rejectOrder(long orderId, String reason) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(COL_STATUS, OrderStatus.CANCELLED.name());
        cv.put(COL_REJECTION_REASON, reason);

        int rows = db.update(TABLE_ORDERS, cv, COL_ORDER_ID + " = ?", new String[]{String.valueOf(orderId)});
        return rows > 0;
    }

    /**
     * Requirement 3: Order Approval
     */
    public synchronized boolean updateOrderApproval(long orderId, OrderStatus status, String otp,
                                                   String qrString, byte[] qrBytes, long invoiceId, double finalAmount) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(COL_STATUS, status.name());
        cv.put(COL_INVOICE_ID, invoiceId);
        cv.put(COL_TOTAL_AMOUNT, finalAmount);
        if (otp != null) cv.put(COL_OTP, otp);
        if (qrString != null) cv.put(COL_QR_STRING, qrString);
        if (qrBytes != null) cv.put(COL_QR_BYTES, qrBytes);

        int rows = db.update(TABLE_ORDERS, cv, COL_ORDER_ID + " = ?", new String[]{String.valueOf(orderId)});
        return rows > 0;
    }

    public synchronized boolean saveInvoice(Invoice invoice) {
        if (invoice == null) return false;
        SQLiteDatabase db = getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(COL_INV_ID, invoice.getInvoiceId());
        cv.put(COL_INV_ORDER_ID, invoice.getOrderId());
        cv.put(COL_INV_PHARMACIST, invoice.getPharmacistId());
        cv.put(COL_INV_SUBTOTAL, invoice.getSubtotal());
        cv.put(COL_INV_TAX, invoice.getTax());
        cv.put(COL_INV_PLATFORM_FEE, invoice.getPlatformFee());
        cv.put(COL_INV_GRAND_TOTAL, invoice.getGrandTotal());
        cv.put(COL_INV_TIMESTAMP, invoice.getTimestamp());
        long result = db.insertWithOnConflict(TABLE_INVOICES, null, cv, SQLiteDatabase.CONFLICT_REPLACE);
        return result != -1;
    }

    public synchronized boolean savePayment(Payment payment) {
        if (payment == null) return false;
        SQLiteDatabase db = getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(COL_PAY_ID, payment.getPaymentId());
        cv.put(COL_PAY_ORDER_ID, payment.getOrderId());
        cv.put(COL_PAY_AMOUNT, payment.getAmount());
        cv.put(COL_PAY_STATUS, payment.getStatus());
        cv.put(COL_PAY_METHOD, payment.getPaymentMethod());
        cv.put(COL_PAY_TIMESTAMP, payment.getTimestamp());
        long result = db.insertWithOnConflict(TABLE_PAYMENTS, null, cv, SQLiteDatabase.CONFLICT_REPLACE);
        return result != -1;
    }

    // ==========================================
    // Screen 4: Clinic Medicine / Inventory Management
    // ==========================================
    public synchronized int getInventoryStock(long medicineId) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT " + COL_MED_STOCK + " FROM " + TABLE_INVENTORY +
                " WHERE " + COL_MED_ID + " = ?", new String[]{String.valueOf(medicineId)});
        int stock = 0;
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                stock = cursor.getInt(0);
            }
            cursor.close();
        }
        return stock;
    }

    public synchronized boolean deductInventoryStock(long medicineId, int quantityToDeduct) {
        SQLiteDatabase db = getWritableDatabase();
        int currentStock = getInventoryStock(medicineId);
        int newStock = Math.max(0, currentStock - quantityToDeduct);

        ContentValues cv = new ContentValues();
        cv.put(COL_MED_STOCK, newStock);
        cv.put(COL_MED_DISPENSED, getDispensedCount(medicineId) + quantityToDeduct);

        int rows = db.update(TABLE_INVENTORY, cv, COL_MED_ID + " = ?", new String[]{String.valueOf(medicineId)});
        return rows > 0;
    }

    public synchronized int getDispensedCount(long medicineId) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT " + COL_MED_DISPENSED + " FROM " + TABLE_INVENTORY +
                " WHERE " + COL_MED_ID + " = ?", new String[]{String.valueOf(medicineId)});
        int count = 0;
        if (cursor != null) {
            if (cursor.moveToFirst()) count = cursor.getInt(0);
            cursor.close();
        }
        return count;
    }

    public synchronized List<InventoryItem> getAllInventory(String search, String category) {
        List<InventoryItem> list = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();

        StringBuilder query = new StringBuilder("SELECT * FROM " + TABLE_INVENTORY + " WHERE 1=1");
        List<String> argsList = new ArrayList<>();

        if (search != null && !search.trim().isEmpty()) {
            query.append(" AND (" + COL_MED_NAME + " LIKE ? OR " + COL_MED_BATCH + " LIKE ?)");
            argsList.add("%" + search.trim() + "%");
            argsList.add("%" + search.trim() + "%");
        }

        if (category != null && !category.equalsIgnoreCase("ALL")) {
            if ("LOW STOCK".equalsIgnoreCase(category)) {
                query.append(" AND " + COL_MED_STOCK + " < 20");
            } else {
                query.append(" AND " + COL_MED_CATEGORY + " = ?");
                argsList.add(category);
            }
        }

        query.append(" ORDER BY " + COL_MED_NAME + " ASC");

        Cursor c = db.rawQuery(query.toString(), argsList.toArray(new String[0]));
        if (c != null) {
            while (c.moveToNext()) {
                long id = c.getLong(c.getColumnIndexOrThrow(COL_MED_ID));
                String name = c.getString(c.getColumnIndexOrThrow(COL_MED_NAME));
                String dosage = c.getString(c.getColumnIndexOrThrow(COL_MED_DOSAGE));
                int stock = c.getInt(c.getColumnIndexOrThrow(COL_MED_STOCK));
                double price = c.getDouble(c.getColumnIndexOrThrow(COL_MED_PRICE));
                String cat = c.getString(c.getColumnIndexOrThrow(COL_MED_CATEGORY));
                String batch = c.getString(c.getColumnIndexOrThrow(COL_MED_BATCH));
                String exp = c.getString(c.getColumnIndexOrThrow(COL_MED_EXPIRY));
                int dispensed = c.getInt(c.getColumnIndexOrThrow(COL_MED_DISPENSED));

                list.add(new InventoryItem(id, name, dosage, stock, price, cat, batch, exp, dispensed));
            }
            c.close();
        }
        return list;
    }

    public synchronized boolean updateInventoryStock(long medicineId, int newStock) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(COL_MED_STOCK, Math.max(0, newStock));
        int rows = db.update(TABLE_INVENTORY, cv, COL_MED_ID + " = ?", new String[]{String.valueOf(medicineId)});
        return rows > 0;
    }

    public synchronized boolean updateMedicineStock(long medicineId, int newQty) {
        return updateInventoryStock(medicineId, newQty);
    }

    public synchronized List<InventoryItem> getInventory(String search, String category) {
        return getAllInventory(search, category);
    }

    public synchronized boolean addInventoryItem(InventoryItem item) {
        if (item == null) return false;
        SQLiteDatabase db = getWritableDatabase();
        ContentValues cv = new ContentValues();
        if (item.getMedicineId() != null && item.getMedicineId() > 0) {
            cv.put(COL_MED_ID, item.getMedicineId());
        }
        cv.put(COL_MED_NAME, item.getMedicineName());
        cv.put(COL_MED_DOSAGE, item.getDosage());
        cv.put(COL_MED_STOCK, item.getAvailableStock());
        cv.put(COL_MED_PRICE, item.getUnitPrice());
        cv.put(COL_MED_CATEGORY, item.getCategory());
        cv.put(COL_MED_BATCH, item.getBatchNo());
        cv.put(COL_MED_EXPIRY, item.getExpiryDate());
        cv.put(COL_MED_DISPENSED, 0);

        long id = db.insertWithOnConflict(TABLE_INVENTORY, null, cv, SQLiteDatabase.CONFLICT_REPLACE);
        return id != -1;
    }

    // ==========================================
    // Screen 5: Reports & Analytics
    // ==========================================
    public synchronized Map<String, Object> getReportsData(String timeFilter) {
        Map<String, Object> data = new HashMap<>();
        SQLiteDatabase db = getReadableDatabase();

        // Multipliers/simulation for time range
        double mult = 1.0;
        if ("THIS WEEK".equalsIgnoreCase(timeFilter)) mult = 4.5;
        if ("THIS MONTH".equalsIgnoreCase(timeFilter)) mult = 18.0;

        // Base revenue from database
        double baseRev = 0.0;
        int totalPatients = 0;
        int takeawayCount = 0;
        int deliveryCount = 0;
        int cashCount = 0;
        int upiCount = 0;

        Cursor cOrders = db.rawQuery("SELECT " + COL_TOTAL_AMOUNT + ", " + COL_ORDER_TYPE + ", " + COL_PAYMENT_STATUS +
                " FROM " + TABLE_ORDERS, null);
        if (cOrders != null) {
            while (cOrders.moveToNext()) {
                totalPatients++;
                baseRev += cOrders.getDouble(0);
                String type = cOrders.getString(1);
                String pay = cOrders.getString(2);
                if ("TAKEAWAY".equalsIgnoreCase(type)) takeawayCount++;
                else deliveryCount++;

                if (pay != null && pay.toUpperCase().contains("CASH")) cashCount++;
                else upiCount++;
            }
            cOrders.close();
        }

        double totalRevenue = baseRev * mult;
        int finalPatients = (int) Math.round(totalPatients * mult);
        int finalTakeaway = (int) Math.round(takeawayCount * mult);
        int finalDelivery = (int) Math.round(deliveryCount * mult);
        int finalCash = (int) Math.round(cashCount * mult);
        int finalUpi = (int) Math.round(upiCount * mult);

        data.put("totalRevenue", totalRevenue);
        data.put("totalPatients", finalPatients);
        data.put("takeawayCount", finalTakeaway);
        data.put("deliveryCount", finalDelivery);
        data.put("cashCount", finalCash);
        data.put("upiCount", finalUpi);

        // Top 5 dispensed medicines
        List<InventoryItem> topMedicines = new ArrayList<>();
        Cursor cTop = db.rawQuery("SELECT * FROM " + TABLE_INVENTORY +
                " ORDER BY " + COL_MED_DISPENSED + " DESC LIMIT 5", null);
        if (cTop != null) {
            while (cTop.moveToNext()) {
                long id = cTop.getLong(cTop.getColumnIndexOrThrow(COL_MED_ID));
                String name = cTop.getString(cTop.getColumnIndexOrThrow(COL_MED_NAME));
                String dosage = cTop.getString(cTop.getColumnIndexOrThrow(COL_MED_DOSAGE));
                int stock = cTop.getInt(cTop.getColumnIndexOrThrow(COL_MED_STOCK));
                double price = cTop.getDouble(cTop.getColumnIndexOrThrow(COL_MED_PRICE));
                String cat = cTop.getString(cTop.getColumnIndexOrThrow(COL_MED_CATEGORY));
                String batch = cTop.getString(cTop.getColumnIndexOrThrow(COL_MED_BATCH));
                String exp = cTop.getString(cTop.getColumnIndexOrThrow(COL_MED_EXPIRY));
                int dispensed = (int) Math.round(cTop.getInt(cTop.getColumnIndexOrThrow(COL_MED_DISPENSED)) * (mult > 1 ? (mult * 0.4) : 1));

                topMedicines.add(new InventoryItem(id, name, dosage, stock, price, cat, batch, exp, dispensed));
            }
            cTop.close();
        }
        data.put("topMedicines", topMedicines);

        return data;
    }

    // ==========================================
    // Member 3: Delivery Agent Methods
    // ==========================================
    public synchronized List<DeliveryOrderModel> getDeliveryOrders(String filter) {
        List<DeliveryOrderModel> list = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();

        String query = "SELECT * FROM " + TABLE_ORDERS + " WHERE " + COL_ORDER_TYPE + " = 'DELIVERY'";
        List<String> args = new ArrayList<>();

        if ("NEW".equalsIgnoreCase(filter)) {
            query += " AND " + COL_STATUS + " = ?";
            args.add(OrderStatus.READY_FOR_DELIVERY.name());
        } else if ("ACTIVE".equalsIgnoreCase(filter) || "IN_TRANSIT".equalsIgnoreCase(filter)) {
            query += " AND " + COL_STATUS + " = ?";
            args.add(OrderStatus.OUT_FOR_DELIVERY.name());
        } else if ("COMPLETED".equalsIgnoreCase(filter) || "HISTORY".equalsIgnoreCase(filter)) {
            query += " AND " + COL_STATUS + " IN (?, ?)";
            args.add(OrderStatus.DELIVERED.name());
            args.add(OrderStatus.COMPLETED.name());
        }

        query += " ORDER BY " + COL_ORDER_ID + " DESC";

        Cursor cursor = db.rawQuery(query, args.toArray(new String[0]));
        if (cursor != null) {
            while (cursor.moveToNext()) {
                long orderId = cursor.getLong(cursor.getColumnIndexOrThrow(COL_ORDER_ID));
                String name = cursor.getString(cursor.getColumnIndexOrThrow(COL_PATIENT_NAME));
                String phone = cursor.getString(cursor.getColumnIndexOrThrow(COL_PATIENT_PHONE));
                String address = cursor.getString(cursor.getColumnIndexOrThrow(COL_ADDRESS));
                double total = cursor.getDouble(cursor.getColumnIndexOrThrow(COL_TOTAL_AMOUNT));
                String statusStr = cursor.getString(cursor.getColumnIndexOrThrow(COL_STATUS));
                String otp = cursor.getString(cursor.getColumnIndexOrThrow(COL_OTP));
                String timestamp = cursor.getString(cursor.getColumnIndexOrThrow(COL_TIMESTAMP));
                String payment = cursor.getString(cursor.getColumnIndexOrThrow(COL_PAYMENT_STATUS));

                OrderStatus status;
                try {
                    status = OrderStatus.valueOf(statusStr);
                } catch (Exception e) {
                    status = OrderStatus.READY_FOR_DELIVERY;
                }

                double lat = 12.9716 + ((orderId % 10) * 0.008);
                double lng = 77.5946 + ((orderId % 10) * 0.006);
                String medSummary = getOrderMedicineSummary(orderId);

                list.add(new DeliveryOrderModel(
                        orderId, name, phone, address,
                        "Apollo Central Pharmacy, Block B",
                        total, payment != null ? payment : "PAID ONLINE",
                        status, otp, lat, lng, timestamp, medSummary
                ));
            }
            cursor.close();
        }
        return list;
    }

    public synchronized String getOrderMedicineSummary(long orderId) {
        List<PrescriptionMedicineItem> items = getOrderMedicines(orderId);
        if (items.isEmpty()) return "Prescribed Medication Package";

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < items.size(); i++) {
            PrescriptionMedicineItem it = items.get(i);
            sb.append(it.getMedicineName()).append(" (").append(it.getRequestedQty()).append("x)");
            if (i < items.size() - 1) sb.append(", ");
        }
        return sb.toString();
    }

    public synchronized boolean updateOrderStatus(long orderId, OrderStatus newStatus) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(COL_STATUS, newStatus.name());
        int rows = db.update(TABLE_ORDERS, cv, COL_ORDER_ID + " = ?", new String[]{String.valueOf(orderId)});
        return rows > 0;
    }

    public synchronized boolean verifyDeliveryOtp(long orderId, String inputOtp) {
        if (inputOtp == null || inputOtp.trim().isEmpty()) return false;
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT " + COL_OTP + ", " + COL_PAYMENT_STATUS + " FROM " + TABLE_ORDERS +
                " WHERE " + COL_ORDER_ID + " = ?", new String[]{String.valueOf(orderId)});
        boolean valid = false;
        String paymentStatus = null;
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                String dbOtp = cursor.getString(0);
                paymentStatus = cursor.getString(1);
                if (dbOtp != null && dbOtp.trim().equals(inputOtp.trim())) {
                    valid = true;
                }
            }
            cursor.close();
        }

        if (valid) {
            SQLiteDatabase wdb = getWritableDatabase();
            ContentValues cv = new ContentValues();
            cv.put(COL_STATUS, OrderStatus.DELIVERED.name());
            if (paymentStatus != null && paymentStatus.toUpperCase().contains("CASH")) {
                cv.put(COL_PAYMENT_STATUS, "PAID (Cash Collected at Doorstep)");
            }
            wdb.update(TABLE_ORDERS, cv, COL_ORDER_ID + " = ?", new String[]{String.valueOf(orderId)});

            ContentValues cvPay = new ContentValues();
            cvPay.put(COL_PAY_STATUS, "PAID");
            wdb.update(TABLE_PAYMENTS, cvPay, COL_PAY_ORDER_ID + " = ?", new String[]{String.valueOf(orderId)});
        }
        return valid;
    }

    public synchronized Map<String, Integer> getDeliverySummaryMetrics() {
        Map<String, Integer> metrics = new HashMap<>();
        SQLiteDatabase db = getReadableDatabase();

        int assigned = 0;
        Cursor c1 = db.rawQuery("SELECT COUNT(*) FROM " + TABLE_ORDERS +
                " WHERE " + COL_ORDER_TYPE + " = 'DELIVERY' AND " + COL_STATUS + " IN (?, ?)",
                new String[]{OrderStatus.READY_FOR_DELIVERY.name(), OrderStatus.OUT_FOR_DELIVERY.name()});
        if (c1 != null) {
            if (c1.moveToFirst()) assigned = c1.getInt(0);
            c1.close();
        }

        int completed = 0;
        Cursor c2 = db.rawQuery("SELECT COUNT(*) FROM " + TABLE_ORDERS +
                " WHERE " + COL_ORDER_TYPE + " = 'DELIVERY' AND " + COL_STATUS + " IN (?, ?)",
                new String[]{OrderStatus.DELIVERED.name(), OrderStatus.COMPLETED.name()});
        if (c2 != null) {
            if (c2.moveToFirst()) completed = c2.getInt(0);
            c2.close();
        }

        metrics.put("assignedCount", assigned);
        metrics.put("completedToday", completed);
        return metrics;
    }
}
