# PharmaGo - Smart Hospital & Pharmacy Care System 🏥💊🛵

PharmaGo is a next-generation healthcare platform that connects Patients, Hospital/Clinic Pharmacists, and Fast-Transit Delivery Couriers into a single synchronized digital ecosystem.

Built with **Material Design 3**, a high-performance **Android Java Client**, and a **Spring Boot 3.4 RESTful Backend** with MySQL and H2 persistence.

---

## Architecture Overview

```
                      ┌───────────────────────────────────────┐
                      │        PharmaGo Android Client        │
                      │  Material Design 3 (#0F56B3, #00A884) │
                      └──────────────────┬────────────────────┘
                                         │
                 ┌───────────────────────┴───────────────────────┐
                 │                                               │
                 ▼                                               ▼
     ┌───────────────────────┐                       ┌───────────────────────┐
     │  Spring Boot Backend  │                       │ MySQL / Local SQLite  │
     │  REST APIs & Security │                       │ Direct JDBC Fallback  │
     └───────────────────────┘                       └───────────────────────┘
```

The system is structured into three coordinated modules:
- **Member 1 (Patient / Customer)**: Browse live medicine catalog, search pharmacies, upload digital prescriptions, place takeaway/delivery orders, track orders with real-time ETA, and access QR pickup tokens.
- **Member 2 (Pharmacist / Clinical Counter)**: Role-gated dashboard (Staff ID `ITA1024`), counter availability toggle, metrics summary, camera-based QR scanner for takeaway verification, inventory stock management, and prescription approvals.
- **Member 3 (Delivery Agent / Smart Transit)**: Role-gated portal (`AGENT-402`), shift status toggle, 3-tab feed (New, Active, History), interactive routing map, turn-by-turn navigation intents, direct customer dialer, and arrival 6-digit OTP authentication.

---

## Project Structure

```
PharmaGo/
├── PharmaGo-main/                 # Native Android Application (Gradle)
│   ├── app/
│   │   ├── src/main/java/com/example/pharmago/
│   │   │   ├── api/              # Retrofit REST Client & Service Interface
│   │   │   ├── database/         # DatabaseConnection (MySQL/JDBC + SQLite)
│   │   │   ├── services/         # Asynchronous BackendService & DeliveryService
│   │   │   ├── models/           # Data models (Orders, Medicines, Invoices)
│   │   │   ├── utils/            # QR Generator/Scanner, NotificationManager
│   │   │   └── *.java            # Activities (Login, Home, Pharmacist, Delivery)
│   │   └── src/main/res/         # Material 3 Layouts, Drawables, Styles
│   ├── build.gradle.kts
│   └── settings.gradle.kts
│
└── PharmaGo-Backend-main/         # Spring Boot 3.4 Backend (Maven)
    └── PharmaGo-Backend-main/
        ├── src/main/java/com/pharmago/backend/
        │   ├── config/           # DataInitializer (Auto-Seeds Live Database)
        │   ├── controller/       # Auth, Medicines, Orders, Pickup, Deliveries
        │   ├── entity/           # User, Medicine, Pharmacy, Order, Delivery
        │   ├── repository/       # Spring Data JPA Repositories
        │   ├── security/         # JWT Security, Token Filters & Roles
        │   └── service/          # Business logic services
        └── pom.xml
```

---

## Live Demo Accounts

| Role | Username | Password | Default Landing Screen |
| :--- | :--- | :--- | :--- |
| **Patient / Customer** | `patient` | `admin` | Home Activity (Catalog, Cart, Orders) |
| **Pharmacist Staff** | `ITA1024` | `admin` | Pharmacist Dashboard & Scan Counter |
| **Delivery Courier** | `AGENT-402` | `admin` | Delivery Agent Dashboard & Routing Map |
| **Administrator** | `admin` | `admin` | Admin & Inventory Management |

---

## Getting Started

### 1. Running the Spring Boot Backend
```bash
cd PharmaGo-Backend-main/PharmaGo-Backend-main
./mvnw clean spring-boot:run
```
- API Base URL: `http://localhost:8081/` (LAN: `http://10.143.137.47:8081/`)
- Health Check: `http://localhost:8081/api/health`
- H2 Console: `http://localhost:8081/h2-console` (`jdbc:h2:file:./data/pharmago`)

### 2. Running the Android Application
1. Open the `PharmaGo-main` folder in Android Studio Ladybug or newer.
2. Sync Gradle dependencies.
3. Build & Run on an Android Emulator or Physical Device (`compileSdk 34`, `minSdk 24`).
   - Emulator automatically connects to `http://10.0.2.2:8081/`.
   - Physical device automatically connects to the host machine LAN IP `http://10.143.137.47:8081/`.

---

## Key Features & Highlights

- **JWT Authentication & Role Gating**: Secure REST sessions with token-based access control.
- **Offline Resilience**: Automatic failover between live Spring Boot REST and local SQLite caching.
- **Real-Time QR Counter Verification**: ZXing embedded camera scanner verifies `ORDER:<id>:INV:<invId>` takeaway tokens in milliseconds.
- **Live Courier Routing**: Google Maps SDK integration with dynamic route polyline, camera bounds, and external turn-by-turn navigation.
- **Automated Clinical Invoicing**: Generates itemized tax and pharmacy invoices for all dispensed medicines.
