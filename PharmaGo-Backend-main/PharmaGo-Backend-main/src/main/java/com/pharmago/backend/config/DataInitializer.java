package com.pharmago.backend.config;

import com.pharmago.backend.entity.*;
import com.pharmago.backend.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Component
public class DataInitializer implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(DataInitializer.class);

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PharmacyRepository pharmacyRepository;

    @Autowired
    private MedicineRepository medicineRepository;

    @Autowired
    private InventoryRepository inventoryRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private DeliveryRepository deliveryRepository;

    @Autowired
    private InvoiceRepository invoiceRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        logger.info("=================================================");
        logger.info("Initializing PharmaGo Live Database Data...");
        logger.info("=================================================");

        // 1. Seed Users
        User patient = initUser("patient", "John Doe", "9876543210", "patient@pharmago.com",
                "admin", "42 Richmond Road, Bangalore", User.Role.CUSTOMER);

        User pharmacist = initUser("ITA1024", "Pharmacist Anita Sharma", "9876543211", "pharmacist.ita1024@apollo.hospital",
                "admin", "Metro Branch, Bangalore", User.Role.PHARMACIST);

        User deliveryAgent = initUser("AGENT-402", "Rajesh Kumar", "9876543212", "agent.rajesh@pharmago.com",
                "admin", "Bangalore Central Hub", User.Role.DELIVERY);

        User deliveryAgentDel = initUser("DEL402", "Rajesh Kumar", "9876543214", "del402@pharmago.com",
                "admin", "Bangalore Central Hub", User.Role.DELIVERY);

        User admin = initUser("admin", "System Administrator", "9876543213", "admin@pharmago.com",
                "admin", "Headquarters", User.Role.ADMIN);

        // 2. Seed Pharmacies
        Pharmacy apollo = initPharmacy("Apollo Hospital Pharmacy, Metro Branch", "MG Road Metro Station, Bangalore",
                "+91 80 2558 1000", 12.9716, 77.5946);

        Pharmacy medplus = initPharmacy("MedPlus Wellness Clinic Pharmacy", "Indiranagar 100ft Road, Bangalore",
                "+91 80 2520 2000", 12.9750, 77.6000);

        Pharmacy fortis = initPharmacy("Fortis Care Pharmacy Counter", "Bannerghatta Main Road, Bangalore",
                "+91 80 6621 4444", 12.9800, 77.6100);

        // 3. Seed Medicines
        Medicine m1 = initMedicine("Paracetamol 500mg", "Effective fever and mild to moderate pain relief.",
                "GSK", "Pain Relief", 45.00, false);
        Medicine m2 = initMedicine("Amoxicillin 500mg", "Broad-spectrum antibiotic for bacterial infections.",
                "Pfizer", "Antibiotic", 120.00, true);
        Medicine m3 = initMedicine("Azithromycin 250mg", "Macrolide antibiotic for respiratory and throat infections.",
                "Cipla", "Antibiotic", 150.00, true);
        Medicine m4 = initMedicine("Cetirizine 10mg", "Non-drowsy antihistamine for allergy and cold symptoms.",
                "Dr. Reddy's", "Antihistamine", 35.00, false);
        Medicine m5 = initMedicine("Metformin 500mg", "First-line oral medication for controlling blood sugar in type 2 diabetes.",
                "Sun Pharma", "Antidiabetic", 85.00, false);
        Medicine m6 = initMedicine("Atorvastatin 10mg", "Statin medication for cholesterol reduction and cardiovascular protection.",
                "Lupin", "Cardiovascular", 110.00, true);
        Medicine m7 = initMedicine("Pantoprazole 40mg", "Proton pump inhibitor for acidity, heartburn, and reflux relief.",
                "Alkem", "Antacid", 65.00, false);
        Medicine m8 = initMedicine("Ibuprofen 400mg", "Nonsteroidal anti-inflammatory for toothache, back pain, and swelling.",
                "Abbott", "NSAID", 40.00, false);
        Medicine m9 = initMedicine("Cough Syrup DX", "Soothing cough suppressant for relief from dry and irritating cough.",
                "Dabur", "Respiratory", 90.00, false);
        Medicine m10 = initMedicine("Vitamin C 500mg", "Chewable daily immune booster with citrus bioflavonoids.",
                "Himalaya", "Supplements", 55.00, false);

        // 4. Seed Inventory for each pharmacy
        List<Medicine> allMeds = List.of(m1, m2, m3, m4, m5, m6, m7, m8, m9, m10);
        for (Pharmacy p : List.of(apollo, medplus, fortis)) {
            for (Medicine med : allMeds) {
                initInventory(p, med, 100, BigDecimal.valueOf(med.getPrice() != null ? med.getPrice() : 50.00));
            }
        }

        // 5. Seed Orders, Deliveries, and Invoices
        if (orderRepository.count() == 0) {
            // Order 101: Takeaway, PENDING_VERIFICATION
            Order o1 = Order.builder()
                    .user(patient)
                    .pharmacy(apollo)
                    .type(Order.OrderType.TAKEAWAY)
                    .status(Order.OrderStatus.PENDING_VERIFICATION)
                    .totalAmount(BigDecimal.valueOf(80.00))
                    .pickupToken("ORDER:101:INV:1101")
                    .items(new ArrayList<>())
                    .build();
            o1.getItems().add(OrderItem.builder().order(o1).medicine(m1).quantity(1).priceAtOrder(BigDecimal.valueOf(45.00)).build());
            o1.getItems().add(OrderItem.builder().order(o1).medicine(m4).quantity(1).priceAtOrder(BigDecimal.valueOf(35.00)).build());
            orderRepository.save(o1);

            // Order 102: Takeaway, READY_FOR_PICKUP
            Order o2 = Order.builder()
                    .user(patient)
                    .pharmacy(apollo)
                    .type(Order.OrderType.TAKEAWAY)
                    .status(Order.OrderStatus.READY_FOR_PICKUP)
                    .totalAmount(BigDecimal.valueOf(135.00))
                    .pickupToken("ORDER:102:INV:1102")
                    .items(new ArrayList<>())
                    .build();
            o2.getItems().add(OrderItem.builder().order(o2).medicine(m1).quantity(1).priceAtOrder(BigDecimal.valueOf(45.00)).build());
            o2.getItems().add(OrderItem.builder().order(o2).medicine(m9).quantity(1).priceAtOrder(BigDecimal.valueOf(90.00)).build());
            orderRepository.save(o2);

            Invoice inv102 = Invoice.builder()
                    .order(o2)
                    .invoiceNumber("INV:1102")
                    .amount(BigDecimal.valueOf(135.00))
                    .build();
            invoiceRepository.save(inv102);

            // Order 8090: Takeaway, READY_FOR_PICKUP
            Order o8090 = Order.builder()
                    .user(patient)
                    .pharmacy(apollo)
                    .type(Order.OrderType.TAKEAWAY)
                    .status(Order.OrderStatus.READY_FOR_PICKUP)
                    .totalAmount(BigDecimal.valueOf(90.00))
                    .pickupToken("ORDER:8090:INV:1040")
                    .items(new ArrayList<>())
                    .build();
            o8090.getItems().add(OrderItem.builder().order(o8090).medicine(m1).quantity(2).priceAtOrder(BigDecimal.valueOf(45.00)).build());
            orderRepository.save(o8090);

            Invoice inv8090 = Invoice.builder()
                    .order(o8090)
                    .invoiceNumber("INV:1040")
                    .amount(BigDecimal.valueOf(90.00))
                    .build();
            invoiceRepository.save(inv8090);

            // Order 8091: Takeaway, READY_FOR_PICKUP
            Order o8091 = Order.builder()
                    .user(patient)
                    .pharmacy(medplus)
                    .type(Order.OrderType.TAKEAWAY)
                    .status(Order.OrderStatus.READY_FOR_PICKUP)
                    .totalAmount(BigDecimal.valueOf(120.00))
                    .pickupToken("ORDER:8091:INV:1041")
                    .items(new ArrayList<>())
                    .build();
            o8091.getItems().add(OrderItem.builder().order(o8091).medicine(m2).quantity(1).priceAtOrder(BigDecimal.valueOf(120.00)).build());
            orderRepository.save(o8091);

            Invoice inv8091 = Invoice.builder()
                    .order(o8091)
                    .invoiceNumber("INV:1041")
                    .amount(BigDecimal.valueOf(120.00))
                    .build();
            invoiceRepository.save(inv8091);

            // Order 8093: Delivery, OUT_FOR_DELIVERY
            Order o8093 = Order.builder()
                    .user(patient)
                    .pharmacy(apollo)
                    .type(Order.OrderType.DELIVERY)
                    .status(Order.OrderStatus.OUT_FOR_DELIVERY)
                    .deliveryAddress("Apt 4B, Green Glen Layout, Bellandur, Bangalore")
                    .totalAmount(BigDecimal.valueOf(210.00))
                    .items(new ArrayList<>())
                    .build();
            o8093.getItems().add(OrderItem.builder().order(o8093).medicine(m5).quantity(1).priceAtOrder(BigDecimal.valueOf(85.00)).build());
            o8093.getItems().add(OrderItem.builder().order(o8093).medicine(m2).quantity(1).priceAtOrder(BigDecimal.valueOf(120.00)).build());
            orderRepository.save(o8093);

            Delivery del8093 = Delivery.builder()
                    .order(o8093)
                    .deliveryPersonName("Rajesh Kumar")
                    .deliveryPersonPhone("+91 98765 43210")
                    .status(Delivery.DeliveryStatus.OUT_FOR_DELIVERY)
                    .deliveryAddress("Apt 4B, Green Glen Layout, Bellandur, Bangalore")
                    .assignedAt(LocalDateTime.now().minusMinutes(25))
                    .pickedUpAt(LocalDateTime.now().minusMinutes(12))
                    .build();
            deliveryRepository.save(del8093);

            Invoice inv8093 = Invoice.builder()
                    .order(o8093)
                    .invoiceNumber("INV:1043")
                    .amount(BigDecimal.valueOf(210.00))
                    .build();
            invoiceRepository.save(inv8093);

            logger.info("Successfully seeded live sample orders, invoices, and deliveries.");
        }

        logger.info("=================================================");
        logger.info("PharmaGo Live Database Seeding Completed!");
        logger.info("Users: patient, ITA1024, AGENT-402, admin");
        logger.info("=================================================");
    }

    private User initUser(String username, String fullName, String phone, String email,
                          String rawPassword, String address, User.Role role) {
        return userRepository.findByUsername(username).orElseGet(() -> {
            User u = User.builder()
                    .username(username)
                    .fullName(fullName)
                    .phoneNumber(phone)
                    .email(email)
                    .password(passwordEncoder.encode(rawPassword))
                    .address(address)
                    .role(role)
                    .build();
            return userRepository.save(u);
        });
    }

    private Pharmacy initPharmacy(String name, String address, String phone, double lat, double lng) {
        return pharmacyRepository.findAll().stream()
                .filter(p -> p.getName().equalsIgnoreCase(name))
                .findFirst()
                .orElseGet(() -> {
                    Pharmacy p = Pharmacy.builder()
                            .name(name)
                            .address(address)
                            .phoneNumber(phone)
                            .latitude(lat)
                            .longitude(lng)
                            .active(true)
                            .build();
                    return pharmacyRepository.save(p);
                });
    }

    private Medicine initMedicine(String name, String desc, String manufacturer, String category, double price, boolean rx) {
        return medicineRepository.findByNameContainingIgnoreCase(name).stream()
                .findFirst()
                .orElseGet(() -> {
                    Medicine m = Medicine.builder()
                            .name(name)
                            .description(desc)
                            .manufacturer(manufacturer)
                            .category(category)
                            .price(price)
                            .requiresPrescription(rx)
                            .build();
                    return medicineRepository.save(m);
                });
    }

    private void initInventory(Pharmacy p, Medicine m, int qty, BigDecimal price) {
        inventoryRepository.findByPharmacyAndMedicine(p, m).orElseGet(() -> {
            Inventory inv = Inventory.builder()
                    .pharmacy(p)
                    .medicine(m)
                    .quantity(qty)
                    .price(price)
                    .build();
            return inventoryRepository.save(inv);
        });
    }
}
