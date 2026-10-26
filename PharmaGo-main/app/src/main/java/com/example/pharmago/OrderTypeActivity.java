package com.example.pharmago;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.pharmago.utils.CartManager;

/**
 * Order Type Selection Screen: Takeaway (Pickup) vs Doorstep Delivery.
 *
 * Bug Fix: Previously both buttons always routed to PrescriptionUploadActivity,
 * causing the Rx RadioGroup "select one" validation error for OTC-only carts.
 *
 * Fix:
 *  - If cart requires prescription (Rx item present)  → PrescriptionUploadActivity
 *    (where user also re-confirms the order type via the radio buttons)
 *  - If OTC-only cart → directly proceed to OrderConfirmationActivity with orderType
 */
public class OrderTypeActivity extends AppCompatActivity {

    TextView tvMedicineName, tvPharmacyName, tvPrice;
    Button btnTakeaway, btnDelivery;

    String medicineName, pharmacyName, price;
    Long medicineId, pharmacyId;
    boolean requiresPrescription;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_type);

        tvMedicineName = findViewById(R.id.tvMedicineName);
        tvPharmacyName = findViewById(R.id.tvPharmacyName);
        tvPrice = findViewById(R.id.tvPrice);
        btnTakeaway = findViewById(R.id.btnTakeaway);
        btnDelivery = findViewById(R.id.btnDelivery);

        Intent intent = getIntent();
        medicineName = intent.getStringExtra("medicineName");
        pharmacyName = intent.getStringExtra("pharmacyName");
        price = intent.getStringExtra("price");
        medicineId = intent.getLongExtra("medicineId", -1L);
        pharmacyId = intent.getLongExtra("pharmacyId", -1L);
        // Also check CartManager live state
        requiresPrescription = intent.getBooleanExtra("requiresPrescription",
                CartManager.getInstance().isRequiresPrescription());

        if (medicineName == null || medicineName.equals("-1")) medicineName = "Selected Medicines";
        if (pharmacyName == null) pharmacyName = "Selected Pharmacy";
        if (price == null || price.isEmpty()) {
            double cartTotal = CartManager.getInstance().getTotalAmount();
            price = cartTotal > 0 ? String.format(java.util.Locale.getDefault(), "\u20b9%.2f", cartTotal) : "See cart";
        }

        if (tvMedicineName != null) tvMedicineName.setText("Cart: " + medicineName);
        if (tvPharmacyName != null) tvPharmacyName.setText("Pharmacy: " + pharmacyName);
        if (tvPrice != null) tvPrice.setText("Total: " + price);

        if (btnTakeaway != null) btnTakeaway.setOnClickListener(v -> onOrderTypeSelected("TAKEAWAY"));
        if (btnDelivery != null) btnDelivery.setOnClickListener(v -> onOrderTypeSelected("DELIVERY"));
    }

    private void onOrderTypeSelected(String orderType) {
        // Visual feedback: highlight the selected button
        if (btnTakeaway != null && btnDelivery != null) {
            int selectedColor = ContextCompat.getColor(this, R.color.primary_clinical_blue);
            int normalColor = ContextCompat.getColor(this, R.color.neutral_muted);
            if ("TAKEAWAY".equals(orderType)) {
                btnTakeaway.setBackgroundColor(selectedColor);
                btnTakeaway.setTextColor(Color.WHITE);
                btnDelivery.setBackgroundColor(normalColor);
                btnDelivery.setTextColor(Color.WHITE);
            } else {
                btnDelivery.setBackgroundColor(selectedColor);
                btnDelivery.setTextColor(Color.WHITE);
                btnTakeaway.setBackgroundColor(normalColor);
                btnTakeaway.setTextColor(Color.WHITE);
            }
        }

        if (requiresPrescription) {
            // Rx cart: go to prescription upload screen (user also selects order type there)
            Intent intent = new Intent(OrderTypeActivity.this, PrescriptionUploadActivity.class);
            intent.putExtra("pharmacyName", pharmacyName);
            intent.putExtra("pharmacyId", pharmacyId != null ? pharmacyId : 1L);
            intent.putExtra("orderType", orderType);
            intent.putExtra("requiresPrescription", true);
            startActivity(intent);
        } else {
            // OTC cart: proceed directly to order confirmation — no prescription needed
            Intent intent = new Intent(OrderTypeActivity.this, OrderConfirmationActivity.class);
            intent.putExtra("pharmacyName", pharmacyName);
            intent.putExtra("pharmacyId", pharmacyId != null ? pharmacyId : 1L);
            intent.putExtra("orderType", orderType);
            intent.putExtra("price", price);
            intent.putExtra("requiresPrescription", false);
            startActivity(intent);
        }
    }
}