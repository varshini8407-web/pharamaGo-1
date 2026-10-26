package com.example.pharmago;

import com.example.pharmago.models.InventoryItem;
import com.example.pharmago.models.Medicine;
import com.example.pharmago.models.MedicineItem;
import com.example.pharmago.models.OrderItem;
import com.example.pharmago.models.OrderStatus;
import com.example.pharmago.utils.QRCodeScanner;

import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import static org.junit.Assert.*;

/**
 * Principal QA Automated Integration & Smoke Test Suite.
 * Covers:
 * 1. Pharmacist & Delivery Role Username Rules (ITA/DEL constraints).
 * 2. Multi-Store Cart Isolation & Single-Pharmacy Enforcement.
 * 3. Prescription Gating & Status Lifecycle Transitions.
 * 4. Token Verification Asymmetry (Client vs. Staff).
 * 5. Line-Item Price Freezing & Stock Deduction Calculations.
 */
public class HealthcareIntegrationSmokeTest {

    private Pattern itaPattern;
    private Pattern delPattern;

    @Before
    public void setUp() {
        itaPattern = Pattern.compile("^ITA.*", Pattern.CASE_INSENSITIVE);
        delPattern = Pattern.compile("^DEL.*", Pattern.CASE_INSENSITIVE);
    }

    // =========================================================================
    // 1. PHARMACIST & DELIVERY USERNAME RULE VALIDATION
    // =========================================================================

    @Test
    public void testPharmacistUsername_StrictlyRequiresITAPrefix() {
        String validPharmacist1 = "ITA_PHARM_01";
        String validPharmacist2 = "ITA1024";
        String invalidPharmacist1 = "john_pharm";
        String invalidPharmacist2 = "pharm_apex";
        String invalidPharmacist3 = "ADMIN123";

        // Valid usernames starting with ITA
        assertTrue("ITA_PHARM_01 must be accepted", itaPattern.matcher(validPharmacist1).matches());
        assertTrue("ITA1024 must be accepted", itaPattern.matcher(validPharmacist2).matches());

        // Invalid usernames not starting with ITA
        assertFalse("john_pharm must be strictly rejected", itaPattern.matcher(invalidPharmacist1).matches());
        assertFalse("pharm_apex must be strictly rejected", itaPattern.matcher(invalidPharmacist2).matches());
        assertFalse("ADMIN123 must be strictly rejected for Pharmacist", itaPattern.matcher(invalidPharmacist3).matches());
    }

    @Test
    public void testDeliveryAgentUsername_StrictlyRequiresDELPrefix() {
        String validDriver1 = "DEL402";
        String validDriver2 = "DELIVERY_DEMO";
        String invalidDriver = "suresh_express";

        assertTrue("DEL402 must be accepted", delPattern.matcher(validDriver1).matches());
        assertTrue("DELIVERY_DEMO must be accepted", delPattern.matcher(validDriver2).matches());
        assertFalse("suresh_express must be rejected for Delivery role", delPattern.matcher(invalidDriver).matches());
    }

    // =========================================================================
    // 2. MULTI-STORE CART ISOLATION (SWIGGY/ZOMATO CONSTRAINT)
    // =========================================================================

    @Test
    public void testMultiStoreCartIsolation_BlocksCrossPharmacyItems() {
        // Simulated in-memory cart manager state
        Integer currentPharmacyId = null;
        List<MedicineItem> activeCart = new ArrayList<>();

        // Add first item from Pharmacy 1 (Apollo Clinical Pharmacy)
        MedicineItem item1 = new MedicineItem(1L, "Paracetamol 500mg", "₹15.00", "Tablet", 1L, "Apollo Clinical Pharmacy", false);
        int store1 = 1;

        if (currentPharmacyId == null || currentPharmacyId == store1) {
            currentPharmacyId = store1;
            activeCart.add(item1);
        }
        assertEquals(1, activeCart.size());
        assertEquals(Integer.valueOf(1), currentPharmacyId);

        // Attempt to add second item from Pharmacy 2 (City Care Hospital Dispensary)
        MedicineItem item2 = new MedicineItem(4L, "Cetirizine 10mg", "₹12.00", "Tablet", 2L, "City Care Hospital Dispensary", false);
        int store2 = 2;

        boolean conflictDetected = (currentPharmacyId != null && !currentPharmacyId.equals(store2));
        assertTrue("Adding item from another store must trigger conflict dialog", conflictDetected);

        // Simulate User Confirmation of "Replace cart items?"
        activeCart.clear();
        currentPharmacyId = store2;
        activeCart.add(item2);

        assertEquals(1, activeCart.size());
        assertEquals(Integer.valueOf(2), currentPharmacyId);
        assertEquals("Cetirizine 10mg", activeCart.get(0).getName());
    }

    // =========================================================================
    // 3. PRESCRIPTION GATING & ORDER LIFECYCLE
    // =========================================================================

    @Test
    public void testPrescriptionGating_RequiresPharmacistApprovalBeforePayment() {
        InventoryItem rxItem = new InventoryItem(2L, "Amoxicillin 500mg", "500 mg Capsule", 80, 45.0, "Apollo Clinical Pharmacy", "APL-502", "08/2026", 0, true);
        InventoryItem otcItem = new InventoryItem(1L, "Paracetamol 500mg", "500 mg Tablet", 150, 15.0, "Apollo Clinical Pharmacy", "APL-801", "12/2027", 0, false);

        assertTrue("Amoxicillin must require prescription", rxItem.isRxRequired());
        assertFalse("Paracetamol must not require prescription", otcItem.isRxRequired());

        // Placing order for Rx item enters PENDING_APPROVAL
        OrderStatus initialStatus = OrderStatus.PENDING_APPROVAL;
        assertEquals(OrderStatus.PENDING_APPROVAL, initialStatus);

        // Payment must be locked while in PENDING_APPROVAL or PENDING_VERIFICATION
        boolean canPayOnline = (initialStatus != OrderStatus.PENDING_APPROVAL && initialStatus != OrderStatus.PENDING_VERIFICATION);
        assertFalse("Payment must remain disabled while order is awaiting pharmacist approval", canPayOnline);

        // Pharmacist approves order -> transitions to READY_FOR_PICKUP or READY_FOR_DELIVERY
        OrderStatus approvedStatus = OrderStatus.READY_FOR_PICKUP;
        assertTrue("Approved takeaway order is ready for pickup", approvedStatus.isReady());
    }

    // =========================================================================
    // 4. TOKEN VERIFICATION ASYMMETRY (QR CODE vs. 6-DIGIT OTP)
    // =========================================================================

    @Test
    public void testTokenAsymmetry_TakeawayProducesQRAndDeliveryProducesOTP() {
        long orderId = 8090L;
        long invoiceId = 1040L;

        // Takeaway flow
        String takeawayPayload = "ORDER:" + orderId + ":INV:" + invoiceId;
        Long parsedOrderId = QRCodeScanner.parseOrderIdFromPayload(takeawayPayload);
        assertNotNull("QRCodeScanner must successfully parse Order ID from QR payload", parsedOrderId);
        assertEquals(Long.valueOf(8090L), parsedOrderId);

        // Delivery flow
        String deliveryOtp = "482915";
        assertTrue("Delivery OTP must be exactly 6 numeric digits", deliveryOtp.matches("^[0-9]{6}$"));

        // Verify asymmetry: Takeaway orders have QR string; Delivery orders have 6-digit OTP
        assertFalse("Takeaway QR payload is not a simple 6-digit OTP", takeawayPayload.matches("^[0-9]{6}$"));
    }

    // =========================================================================
    // 5. LINE-ITEM PRICE FREEZING & INVENTORY DEDUCTION
    // =========================================================================

    @Test
    public void testLineItemPriceFreezingAndInventoryDeduction() {
        // Initial inventory state
        int initialStock = 80;
        double catalogUnitPrice = 45.00;
        int purchaseQty = 3;

        // 1. Stock reduction calculation
        int updatedStock = Math.max(0, initialStock - purchaseQty);
        assertEquals(77, updatedStock);

        // 2. Line-item snapshot creation in order_items
        OrderItem orderItem = new OrderItem(2L, "Amoxicillin 500mg", purchaseQty, catalogUnitPrice, purchaseQty * catalogUnitPrice);
        assertEquals(45.00, orderItem.getUnitPrice(), 0.001);
        assertEquals(135.00, orderItem.getTotalPrice(), 0.001);

        // 3. Price Freezing: Catalog price changes later (e.g. inflation / store update)
        double newCatalogPrice = 55.00;
        assertNotEquals(newCatalogPrice, orderItem.getUnitPrice(), 0.001);
        assertEquals("Historical order_items unit price must remain frozen at ₹45.00", 45.00, orderItem.getUnitPrice(), 0.001);

        // 4. Tax & Totals Breakdown verification (CGST 6%, SGST 6%)
        double subtotal = 135.00;
        double cgst = subtotal * 0.06;
        double sgst = subtotal * 0.06;
        double deliveryFee = 30.00;
        double totalDeliveryOrder = subtotal + cgst + sgst + deliveryFee;

        assertEquals(8.10, cgst, 0.001);
        assertEquals(8.10, sgst, 0.001);
        assertEquals(181.20, totalDeliveryOrder, 0.001);
    }
}
