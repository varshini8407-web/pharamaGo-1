package com.example.pharmago;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class PaymentActivity extends AppCompatActivity {

    TextView tvPaymentDetails;
    RadioGroup rgPaymentMethod;
    Button btnPayNow;

    String medicineName, pharmacyName, price, orderType, deliveryAddress;
    Long medicineId, pharmacyId, prescriptionId;
    boolean prescriptionRequired, prescriptionUploaded;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment);

        tvPaymentDetails = findViewById(R.id.tvPaymentDetails);
        rgPaymentMethod = findViewById(R.id.rgPaymentMethod);
        btnPayNow = findViewById(R.id.btnPayNow);

        Intent intent = getIntent();
        if (intent != null) {
            medicineName = intent.getStringExtra("medicineName");
            pharmacyName = intent.getStringExtra("pharmacyName");
            price = intent.getStringExtra("price");
            orderType = intent.getStringExtra("orderType");
            deliveryAddress = intent.getStringExtra("deliveryAddress");
            medicineId = intent.getLongExtra("medicineId", -1L);
            pharmacyId = intent.getLongExtra("pharmacyId", -1L);
            prescriptionId = intent.hasExtra("prescriptionId") ? intent.getLongExtra("prescriptionId", -1L) : null;

            prescriptionRequired = intent.getBooleanExtra("prescriptionRequired", false);
            prescriptionUploaded = intent.getBooleanExtra("prescriptionUploaded", false);
        }

        tvPaymentDetails.setText(
                "Medicine: " + medicineName +
                        "\nPharmacy: " + pharmacyName +
                        "\nOrder Type: " + orderType +
                        "\nAmount to Pay: " + price
        );

        btnPayNow.setOnClickListener(v -> {

            int selectedMethod = rgPaymentMethod.getCheckedRadioButtonId();

            if (selectedMethod == -1) {
                Toast.makeText(
                        this,
                        "Please select a payment method.",
                        Toast.LENGTH_SHORT
                ).show();
                return;
            }

            String paymentMethod;

            if (selectedMethod == R.id.rbUpi) {
                paymentMethod = "UPI";
            } else if (selectedMethod == R.id.rbCard) {
                paymentMethod = "Debit/Credit Card";
            } else {
                paymentMethod = "Cash on Delivery";
            }

            Intent confirmationIntent = new Intent(
                    PaymentActivity.this,
                    OrderConfirmationActivity.class
            );

            confirmationIntent.putExtra("medicineName", medicineName);
            confirmationIntent.putExtra("pharmacyName", pharmacyName);
            confirmationIntent.putExtra("price", price);
            confirmationIntent.putExtra("orderType", orderType);
            confirmationIntent.putExtra("paymentMethod", paymentMethod);
            confirmationIntent.putExtra("deliveryAddress", deliveryAddress);
            confirmationIntent.putExtra("medicineId", medicineId);
            confirmationIntent.putExtra("pharmacyId", pharmacyId);
            confirmationIntent.putExtra("prescriptionId", prescriptionId);
            confirmationIntent.putExtra("prescriptionRequired", prescriptionRequired);
            confirmationIntent.putExtra("prescriptionUploaded", prescriptionUploaded);

            startActivity(confirmationIntent);
        });
    }
}