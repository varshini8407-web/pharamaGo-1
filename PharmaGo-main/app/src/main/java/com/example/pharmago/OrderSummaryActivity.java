package com.example.pharmago;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class OrderSummaryActivity extends AppCompatActivity {

    TextView tvSummaryMedicine, tvSummaryPharmacy;
    TextView tvSummaryOrderType, tvSummaryPrescription, tvTotalBill;
    LinearLayout layoutDeliveryAddress;
    EditText etDeliveryAddress;
    Button btnProceedPayment;

    String medicineName, pharmacyName, price, orderType;
    Long medicineId, pharmacyId, prescriptionId;
    boolean prescriptionRequired, prescriptionUploaded;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_summary);

        tvSummaryMedicine = findViewById(R.id.tvSummaryMedicine);
        tvSummaryPharmacy = findViewById(R.id.tvSummaryPharmacy);
        tvSummaryOrderType = findViewById(R.id.tvSummaryOrderType);
        tvSummaryPrescription = findViewById(R.id.tvSummaryPrescription);
        tvTotalBill = findViewById(R.id.tvTotalBill);
        layoutDeliveryAddress = findViewById(R.id.layoutDeliveryAddress);
        etDeliveryAddress = findViewById(R.id.etDeliveryAddress);
        btnProceedPayment = findViewById(R.id.btnProceedPayment);

        Intent intent = getIntent();

        medicineName = intent.getStringExtra("medicineName");
        pharmacyName = intent.getStringExtra("pharmacyName");
        price = intent.getStringExtra("price");
        orderType = intent.getStringExtra("orderType");
        medicineId = intent.getLongExtra("medicineId", -1L);
        pharmacyId = intent.getLongExtra("pharmacyId", -1L);
        prescriptionId = intent.hasExtra("prescriptionId") ? intent.getLongExtra("prescriptionId", -1L) : null;
        
        prescriptionRequired = intent.getBooleanExtra("prescriptionRequired", false);
        prescriptionUploaded = intent.getBooleanExtra("prescriptionUploaded", false);

        tvSummaryMedicine.setText("Medicine: " + (medicineName == null ? "Not selected" : medicineName));
        tvSummaryPharmacy.setText("Pharmacy: " + (pharmacyName == null ? "Not selected" : pharmacyName));
        tvSummaryOrderType.setText("Order Type: " + (orderType == null ? "Not selected" : orderType));

        String prescriptionStatus;
        if (!prescriptionRequired) {
            prescriptionStatus = "Not required";
        } else if (prescriptionUploaded) {
            prescriptionStatus = "Uploaded - Pending verification";
        } else {
            prescriptionStatus = "Required - Not uploaded";
        }

        tvSummaryPrescription.setText("Prescription Status: " + prescriptionStatus);
        tvTotalBill.setText("Total Bill: " + (price == null ? "₹0" : price));

        // Handle delivery address visibility
        if ("DELIVERY".equalsIgnoreCase(orderType) || (orderType != null && orderType.toLowerCase().contains("delivery"))) {
            layoutDeliveryAddress.setVisibility(View.VISIBLE);
            SharedPreferences prefs = getSharedPreferences("PharmaGoPrefs", MODE_PRIVATE);
            String savedAddress = prefs.getString("userAddress", "");
            etDeliveryAddress.setText(savedAddress);
        } else {
            layoutDeliveryAddress.setVisibility(View.GONE);
        }

        btnProceedPayment.setOnClickListener(v -> {
            String deliveryAddress = etDeliveryAddress.getText().toString().trim();
            
            Intent paymentIntent = new Intent(OrderSummaryActivity.this, PaymentActivity.class);
            paymentIntent.putExtra("medicineName", medicineName);
            paymentIntent.putExtra("pharmacyName", pharmacyName);
            paymentIntent.putExtra("price", price);
            paymentIntent.putExtra("orderType", orderType);
            paymentIntent.putExtra("medicineId", medicineId);
            paymentIntent.putExtra("pharmacyId", pharmacyId);
            paymentIntent.putExtra("prescriptionId", prescriptionId);
            paymentIntent.putExtra("prescriptionRequired", prescriptionRequired);
            paymentIntent.putExtra("prescriptionUploaded", prescriptionUploaded);
            paymentIntent.putExtra("deliveryAddress", deliveryAddress);
            startActivity(paymentIntent);
        });
    }
}