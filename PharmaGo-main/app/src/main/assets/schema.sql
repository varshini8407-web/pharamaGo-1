-- PharmaGo Enterprise Healthcare Application Schema (MySQL / MariaDB)
-- Production Grade Multi-Role Platform: Patient (Member 1), Pharmacist (Member 2), Delivery Agent (Member 3)

SET FOREIGN_KEY_CHECKS = 0;

-- 1. Users Table
CREATE TABLE IF NOT EXISTS users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    full_name VARCHAR(100) NOT NULL,
    phone VARCHAR(20) NOT NULL,
    address TEXT,
    role ENUM('PATIENT', 'PHARMACIST', 'DELIVERY') NOT NULL DEFAULT 'PATIENT',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 2. Pharmacies Table
CREATE TABLE IF NOT EXISTS pharmacies (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    owner_id BIGINT,
    name VARCHAR(150) NOT NULL,
    store_type VARCHAR(50) DEFAULT 'PHARMACY',
    license_no VARCHAR(100) UNIQUE,
    address TEXT NOT NULL,
    latitude DOUBLE DEFAULT 13.0827,
    longitude DOUBLE DEFAULT 80.2707,
    phone VARCHAR(20) NOT NULL,
    is_open BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (owner_id) REFERENCES users(id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 3. Medicines Catalog Table
CREATE TABLE IF NOT EXISTS medicines (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    pharmacy_id BIGINT DEFAULT 1,
    brand_name VARCHAR(100) NOT NULL,
    generic_name VARCHAR(150) NOT NULL,
    dosage VARCHAR(50) NOT NULL,
    unit_price DOUBLE NOT NULL,
    stock_qty INT NOT NULL DEFAULT 0,
    batch_no VARCHAR(50),
    expiry_date VARCHAR(50),
    is_rx_required BOOLEAN DEFAULT FALSE,
    is_available BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (pharmacy_id) REFERENCES pharmacies(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 4. Active Carts (Swiggy/Zomato Store Isolation)
CREATE TABLE IF NOT EXISTS active_carts (
    patient_id BIGINT PRIMARY KEY,
    pharmacy_id BIGINT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (patient_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (pharmacy_id) REFERENCES pharmacies(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 5. Active Cart Items Table
CREATE TABLE IF NOT EXISTS active_cart_items (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    patient_id BIGINT NOT NULL,
    medicine_id BIGINT NOT NULL,
    quantity INT NOT NULL DEFAULT 1,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uq_patient_medicine (patient_id, medicine_id),
    FOREIGN KEY (patient_id) REFERENCES active_carts(patient_id) ON DELETE CASCADE,
    FOREIGN KEY (medicine_id) REFERENCES medicines(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 6. Orders Table
CREATE TABLE IF NOT EXISTS orders (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    patient_id BIGINT NOT NULL,
    pharmacy_id BIGINT DEFAULT 1,
    delivery_agent_id BIGINT NULL,
    order_type ENUM('TAKEAWAY', 'DELIVERY') NOT NULL,
    status ENUM('PENDING_APPROVAL', 'APPROVED', 'REJECTED', 'READY_FOR_PICKUP', 'READY_FOR_DELIVERY', 'OUT_FOR_DELIVERY', 'COMPLETED', 'CANCELLED') NOT NULL DEFAULT 'PENDING_APPROVAL',
    rejection_reason VARCHAR(255) NULL,
    prescription_image_path TEXT NULL,
    delivery_address TEXT NULL,
    delivery_lat DOUBLE DEFAULT 13.0827,
    delivery_lng DOUBLE DEFAULT 80.2707,
    pickup_qr_payload TEXT NULL,
    delivery_otp VARCHAR(10) NULL,
    subtotal DOUBLE DEFAULT 0.0,
    cgst DOUBLE DEFAULT 0.0,
    sgst DOUBLE DEFAULT 0.0,
    delivery_fee DOUBLE DEFAULT 0.0,
    total_amount DOUBLE DEFAULT 0.0,
    payment_mode VARCHAR(50) DEFAULT 'ONLINE_UPI',
    payment_status VARCHAR(50) DEFAULT 'PENDING',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    approved_at TIMESTAMP NULL,
    completed_at TIMESTAMP NULL,
    FOREIGN KEY (patient_id) REFERENCES users(id),
    FOREIGN KEY (pharmacy_id) REFERENCES pharmacies(id),
    FOREIGN KEY (delivery_agent_id) REFERENCES users(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 7. Order Items Table
CREATE TABLE IF NOT EXISTS order_items (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_id BIGINT NOT NULL,
    medicine_id BIGINT NOT NULL,
    medicine_name VARCHAR(150) NOT NULL,
    quantity INT NOT NULL,
    unit_price DOUBLE NOT NULL,
    total_price DOUBLE NOT NULL,
    FOREIGN KEY (order_id) REFERENCES orders(id) ON DELETE CASCADE,
    FOREIGN KEY (medicine_id) REFERENCES medicines(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 8. Delivery Trips Table
CREATE TABLE IF NOT EXISTS delivery_trips (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_id BIGINT NOT NULL,
    agent_id BIGINT NOT NULL,
    distance_km DOUBLE NOT NULL DEFAULT 3.2,
    delivery_fee_earned DOUBLE NOT NULL DEFAULT 75.0,
    tip_amount DOUBLE NOT NULL DEFAULT 15.0,
    status ENUM('ASSIGNED', 'REJECTED', 'DELIVERED') NOT NULL DEFAULT 'ASSIGNED',
    completed_at TIMESTAMP NULL,
    FOREIGN KEY (order_id) REFERENCES orders(id) ON DELETE CASCADE,
    FOREIGN KEY (agent_id) REFERENCES users(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

SET FOREIGN_KEY_CHECKS = 1;

-- =========================================================================
-- MOCK SEED DATA (Cleanly Executable Without Foreign Key Mismatches)
-- =========================================================================

-- Seed 1: Mandatory Users (Patient, Pharmacist, Delivery Driver + Demo Logins)
INSERT IGNORE INTO users (id, username, password_hash, full_name, phone, address, role) VALUES
(101, 'patient_demo', 'pass123', 'John Doe', '9876543210', '12 Hospital Rd, Chennai', 'PATIENT'),
(102, 'ITA_PHARM_01', 'pass123', 'Apex Pharmacy Staff', '9876543211', '10 Main St, Chennai', 'PHARMACIST'),
(103, 'driver_demo', 'pass123', 'Suresh Kumar', '9876543212', 'Chennai Hub', 'DELIVERY'),
(1, 'patient', 'admin', 'Priya Patel', '+91 98112 34567', '12 Hospital Rd, Chennai', 'PATIENT'),
(2, 'ITA1024', 'admin', 'Apex Pharmacy Staff', '+91 94441 23456', '10 Main St, Chennai', 'PHARMACIST'),
(3, 'AGENT-402', 'admin', 'Suresh Kumar (Delivery Driver)', '+91 98765 43210', 'Chennai Hub', 'DELIVERY'),
(402, 'DEL402', 'admin', 'Suresh Kumar', '+91 98765 43212', 'Chennai Hub', 'DELIVERY'),
(4, 'admin', 'admin', 'System Administrator', '+91 99999 88888', 'Metro Healthcare Headquarters', 'PATIENT');

-- Seed 2: Pharmacies (Apollo Clinical Pharmacy & City Care Hospital Dispensary)
INSERT IGNORE INTO pharmacies (id, owner_id, name, store_type, license_no, address, latitude, longitude, phone, is_open) VALUES
(1, 102, 'Apollo Clinical Pharmacy', 'PHARMACY', 'DL-TN-CH-00129', '10 Main St, Thousand Lights, Chennai', 13.0610, 80.2520, '044-28290123', TRUE),
(2, 2, 'City Care Hospital Dispensary', 'HOSPITAL', 'DL-TN-CH-00456', '88 Greams Road, Chennai', 13.0625, 80.2555, '044-28293344', TRUE);

-- Seed 3: 6 Medicines Split Across Stores (At least 2 requiring prescriptions)
INSERT IGNORE INTO medicines (id, pharmacy_id, brand_name, generic_name, dosage, unit_price, stock_qty, batch_no, expiry_date, is_rx_required, is_available) VALUES
(1, 1, 'Paracetamol 500mg', 'Acetaminophen', '500 mg Tablet', 15.00, 150, 'APL-PCM-801', '12/2027', FALSE, TRUE),
(2, 1, 'Amoxicillin 500mg', 'Amoxicillin Trihydrate', '500 mg Capsule', 45.00, 80, 'APL-AMX-502', '08/2026', TRUE, TRUE),
(3, 1, 'Azithromycin 250mg', 'Azithromycin', '250 mg Tablet', 95.00, 65, 'APL-AZI-303', '10/2026', TRUE, TRUE),
(4, 2, 'Cetirizine 10mg', 'Cetirizine Hydrochloride', '10 mg Tablet', 12.00, 200, 'CCH-CTZ-104', '05/2028', FALSE, TRUE),
(5, 2, 'Metformin 500mg', 'Metformin HCl', '500 mg Extended Release', 28.00, 110, 'CCH-MET-705', '03/2027', TRUE, TRUE),
(6, 2, 'Pantoprazole 40mg', 'Pantoprazole Sodium', '40 mg Gastro-resistant', 38.00, 130, 'CCH-PAN-407', '11/2027', FALSE, TRUE);
