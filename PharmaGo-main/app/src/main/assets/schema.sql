-- PharmaGo Healthcare Application Schema (MySQL / MariaDB)
-- Integrates Member 1 (Frontend/Patient), Member 2 (Backend/Database/Pharmacist), and Member 3 (Smart Features/Delivery)

CREATE TABLE IF NOT EXISTS users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    full_name VARCHAR(100) NOT NULL,
    phone VARCHAR(20) NOT NULL,
    role VARCHAR(20) NOT NULL DEFAULT 'PATIENT',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS medicines (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    brand_name VARCHAR(100) NOT NULL,
    generic_name VARCHAR(150) NOT NULL,
    dosage VARCHAR(50) NOT NULL,
    unit_price DOUBLE NOT NULL,
    stock_qty INT NOT NULL DEFAULT 0,
    batch_no VARCHAR(50),
    expiry_date VARCHAR(50),
    is_rx_required INT DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS orders (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    patient_id BIGINT NOT NULL,
    order_type VARCHAR(20) NOT NULL,
    status VARCHAR(30) NOT NULL DEFAULT 'PENDING_VERIFICATION',
    delivery_address TEXT,
    prescription_image_path TEXT,
    qr_payload TEXT,
    delivery_otp VARCHAR(10),
    delivery_agent_id BIGINT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS invoices (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_id BIGINT NOT NULL,
    subtotal DOUBLE NOT NULL,
    tax DOUBLE NOT NULL,
    total_amount DOUBLE NOT NULL,
    payment_mode VARCHAR(50) DEFAULT 'CASH',
    payment_status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS order_items (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_id BIGINT NOT NULL,
    medicine_id BIGINT NOT NULL,
    quantity INT NOT NULL,
    unit_price DOUBLE NOT NULL
);

-- Seed Initial Default Users (Password: admin)
INSERT IGNORE INTO users (id, username, password_hash, full_name, phone, role) VALUES
(1, 'patient', 'admin', 'Priya Patel', '+91 98112 34567', 'PATIENT'),
(2, 'ITA1024', 'admin', 'Dr. Arvind Sharma (Pharmacist)', '+91 94441 23456', 'PHARMACIST'),
(3, 'AGENT-402', 'admin', 'Rajesh Kumar (Delivery Courier)', '+91 98765 43210', 'DELIVERY'),
(4, 'admin', 'admin', 'System Administrator', '+91 99999 88888', 'PATIENT');

-- Seed Standard Medicines
INSERT IGNORE INTO medicines (id, brand_name, generic_name, dosage, unit_price, stock_qty, batch_no, expiry_date, is_rx_required) VALUES
(1, 'Paracetamol 500mg', 'Acetaminophen', '500 mg Tablet', 15.00, 140, 'APL-PCM-801', '12/2027', 0),
(2, 'Amoxicillin 500mg', 'Amoxicillin Trihydrate', '500 mg Capsule', 45.00, 85, 'APL-AMX-502', '08/2026', 1),
(3, 'Azithromycin 250mg', 'Azithromycin', '250 mg Tablet', 95.00, 60, 'APL-AZI-303', '10/2026', 1),
(4, 'Cetirizine 10mg', 'Cetirizine Hydrochloride', '10 mg Tablet', 12.00, 220, 'APL-CTZ-104', '05/2028', 0),
(5, 'Metformin 500mg', 'Metformin HCl', '500 mg Extended Release', 28.00, 95, 'APL-MET-705', '03/2027', 1),
(6, 'Atorvastatin 10mg', 'Atorvastatin Calcium', '10 mg Tablet', 65.00, 70, 'APL-ATV-906', '09/2026', 1),
(7, 'Pantoprazole 40mg', 'Pantoprazole Sodium', '40 mg Gastro-resistant', 38.00, 110, 'APL-PAN-407', '11/2027', 0),
(8, 'Ibuprofen 400mg', 'Ibuprofen', '400 mg Film-coated', 22.00, 130, 'APL-IBU-208', '07/2027', 0);
