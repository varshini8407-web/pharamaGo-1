package com.example.pharmago;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

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
        requiresPrescription = intent.getBooleanExtra("requiresPrescription", false);

        if (medicineName == null) medicineName = "Not selected";
        if (pharmacyName == null) pharmacyName = "Selected Pharmacy";
        if (price == null) price = "Not available";

        tvMedicineName.setText("Medicine: " + medicineName);
        tvPharmacyName.setText("Pharmacy: " + pharmacyName);
        tvPrice.setText("Price: " + price);

        btnTakeaway.setOnClickListener(v -> openPrescriptionPage("TAKEAWAY"));
        btnDelivery.setOnClickListener(v -> openPrescriptionPage("DELIVERY"));
    }

    private void openPrescriptionPage(String orderType) {
        Intent intent = new Intent(OrderTypeActivity.this, PrescriptionUploadActivity.class);
        intent.putExtra("medicineName", medicineName);
        intent.putExtra("pharmacyName", pharmacyName);
        intent.putExtra("price", price);
        intent.putExtra("medicineId", medicineId);
        intent.putExtra("pharmacyId", pharmacyId);
        intent.putExtra("orderType", orderType);
        intent.putExtra("requiresPrescription", requiresPrescription);
        startActivity(intent);
    }
}