package com.example.pharmago;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.materialswitch.MaterialSwitch;
import com.google.android.material.textfield.TextInputEditText;

/**
 * Member 2: Pharmacist Profile Screen
 * Displays clinical staff identity, pharmacy branch, counter active toggle, metrics, and edit dialog.
 */
public class PharmacistProfileActivity extends AppCompatActivity {

    private ImageView btnBackPharmacistProfile, btnEditPharmacistProfileHeader;
    private TextView tvPharmacistFullName, tvPharmacistStaffId, tvPharmacistDesignation;
    private TextView tvPharmacistBranchName, tvPharmacistLicenseNo, tvPharmacistPhone, tvPharmacistEmail;
    private TextView tvCounterStatusTitle, tvMetricDispensedOrders, tvMetricTakeaways;
    private TextView btnEditContactAction;
    private MaterialSwitch switchPharmacistStatus;
    private MaterialButton btnProfileOpenScanner, btnProfileOpenOrders, btnPharmacistSignOut;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pharmacist_profile);

        btnBackPharmacistProfile = findViewById(R.id.btnBackPharmacistProfile);
        btnEditPharmacistProfileHeader = findViewById(R.id.btnEditPharmacistProfileHeader);
        tvPharmacistFullName = findViewById(R.id.tvPharmacistFullName);
        tvPharmacistStaffId = findViewById(R.id.tvPharmacistStaffId);
        tvPharmacistDesignation = findViewById(R.id.tvPharmacistDesignation);
        tvPharmacistBranchName = findViewById(R.id.tvPharmacistBranchName);
        tvPharmacistLicenseNo = findViewById(R.id.tvPharmacistLicenseNo);
        tvPharmacistPhone = findViewById(R.id.tvPharmacistPhone);
        tvPharmacistEmail = findViewById(R.id.tvPharmacistEmail);
        tvCounterStatusTitle = findViewById(R.id.tvCounterStatusTitle);
        tvMetricDispensedOrders = findViewById(R.id.tvMetricDispensedOrders);
        tvMetricTakeaways = findViewById(R.id.tvMetricTakeaways);
        btnEditContactAction = findViewById(R.id.btnEditContactAction);
        switchPharmacistStatus = findViewById(R.id.switchPharmacistStatus);
        btnProfileOpenScanner = findViewById(R.id.btnProfileOpenScanner);
        btnProfileOpenOrders = findViewById(R.id.btnProfileOpenOrders);
        btnPharmacistSignOut = findViewById(R.id.btnPharmacistSignOut);

        if (btnBackPharmacistProfile != null) {
            btnBackPharmacistProfile.setOnClickListener(v -> finish());
        }

        loadPharmacistData();

        View.OnClickListener editListener = v -> showEditProfileDialog();
        if (btnEditPharmacistProfileHeader != null) btnEditPharmacistProfileHeader.setOnClickListener(editListener);
        if (btnEditContactAction != null) btnEditContactAction.setOnClickListener(editListener);

        if (switchPharmacistStatus != null) {
            switchPharmacistStatus.setOnCheckedChangeListener((buttonView, isChecked) -> {
                SharedPreferences prefs = getSharedPreferences("PharmaGoPrefs", MODE_PRIVATE);
                prefs.edit().putBoolean("counter_active", isChecked).apply();
                if (isChecked) {
                    tvCounterStatusTitle.setText("Counter Active (Online)");
                    Toast.makeText(this, "Takeaway Counter is Online", Toast.LENGTH_SHORT).show();
                } else {
                    tvCounterStatusTitle.setText("Counter Inactive (Paused)");
                    Toast.makeText(this, "Takeaway Counter is Paused", Toast.LENGTH_SHORT).show();
                }
            });
        }

        if (btnProfileOpenScanner != null) {
            btnProfileOpenScanner.setOnClickListener(v -> {
                Intent intent = new Intent(PharmacistProfileActivity.this, QRScanCounterActivity.class);
                startActivity(intent);
            });
        }

        if (btnProfileOpenOrders != null) {
            btnProfileOpenOrders.setOnClickListener(v -> {
                Intent intent = new Intent(PharmacistProfileActivity.this, PharmacistOrdersActivity.class);
                startActivity(intent);
            });
        }

        if (btnPharmacistSignOut != null) {
            btnPharmacistSignOut.setOnClickListener(v -> {
                SharedPreferences prefs = getSharedPreferences("PharmaGoPrefs", MODE_PRIVATE);
                if (prefs != null) {
                    prefs.edit().clear().apply();
                }
                Intent intent = new Intent(PharmacistProfileActivity.this, LoginActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            });
        }
    }

    private void loadPharmacistData() {
        SharedPreferences prefs = getSharedPreferences("PharmaGoPrefs", MODE_PRIVATE);
        String username = prefs.getString("username", "ITA1024");
        String fullName = prefs.getString("fullName", "Dr. Arvind Sharma");
        String phone = prefs.getString("phone", "+91 94441 23456");
        String email = prefs.getString("email", "pharmacist.ita1024@apollo.hospital");
        String branch = prefs.getString("branch", "Apollo Hospital Metro Pharmacy (Ground Floor Counter B)");
        boolean counterActive = prefs.getBoolean("counter_active", true);

        if (tvPharmacistFullName != null) tvPharmacistFullName.setText(fullName);
        if (tvPharmacistStaffId != null) tvPharmacistStaffId.setText("Staff ID: " + username);
        if (tvPharmacistPhone != null) tvPharmacistPhone.setText(phone);
        if (tvPharmacistEmail != null) tvPharmacistEmail.setText(email);
        if (tvPharmacistBranchName != null) tvPharmacistBranchName.setText(branch);
        if (switchPharmacistStatus != null) switchPharmacistStatus.setChecked(counterActive);
    }

    private void showEditProfileDialog() {
        SharedPreferences prefs = getSharedPreferences("PharmaGoPrefs", MODE_PRIVATE);
        String curName = prefs.getString("fullName", "Dr. Arvind Sharma");
        String curPhone = prefs.getString("phone", "+91 94441 23456");
        String curEmail = prefs.getString("email", "pharmacist.ita1024@apollo.hospital");

        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_edit_profile, null);
        TextInputEditText etName = dialogView.findViewById(R.id.etEditName);
        TextInputEditText etPhone = dialogView.findViewById(R.id.etEditPhone);
        TextInputEditText etEmail = dialogView.findViewById(R.id.etEditEmail);
        TextInputEditText etAddress = dialogView.findViewById(R.id.etEditAddress);

        if (etName != null) etName.setText(curName);
        if (etPhone != null) etPhone.setText(curPhone);
        if (etEmail != null) etEmail.setText(curEmail);
        if (etAddress != null) {
            etAddress.setHint("Assigned Pharmacy Branch");
            etAddress.setText(prefs.getString("branch", "Apollo Hospital Metro Pharmacy (Ground Floor Counter B)"));
        }

        AlertDialog dialog = new MaterialAlertDialogBuilder(this)
                .setTitle("Edit Pharmacist Details")
                .setView(dialogView)
                .setPositiveButton("Save Changes", null)
                .setNegativeButton("Cancel", null)
                .create();

        dialog.setOnShowListener(d -> {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
                String newName = etName != null && etName.getText() != null ? etName.getText().toString().trim() : "";
                String newPhone = etPhone != null && etPhone.getText() != null ? etPhone.getText().toString().trim() : "";
                String newEmail = etEmail != null && etEmail.getText() != null ? etEmail.getText().toString().trim() : "";
                String newBranch = etAddress != null && etAddress.getText() != null ? etAddress.getText().toString().trim() : "";

                if (newName.isEmpty()) {
                    if (etName != null) etName.setError("Name cannot be empty");
                    return;
                }
                if (newPhone.isEmpty()) {
                    if (etPhone != null) etPhone.setError("Phone cannot be empty");
                    return;
                }

                prefs.edit()
                        .putString("fullName", newName)
                        .putString("phone", newPhone)
                        .putString("email", newEmail)
                        .putString("branch", newBranch)
                        .apply();

                loadPharmacistData();
                Toast.makeText(this, "Profile updated successfully!", Toast.LENGTH_SHORT).show();
                dialog.dismiss();
            });
        });

        dialog.show();
    }
}
