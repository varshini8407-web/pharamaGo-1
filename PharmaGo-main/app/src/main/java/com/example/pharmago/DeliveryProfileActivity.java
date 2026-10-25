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
 * Member 3: Delivery Agent Profile Screen
 * Displays courier identity, vehicle credentials, shift toggle, delivery performance stats, and edit dialog.
 */
public class DeliveryProfileActivity extends AppCompatActivity {

    private ImageView btnBackDeliveryProfile, btnEditDeliveryProfileHeader, ivProfileShiftStatusIcon;
    private TextView tvDeliveryProfileFullName, tvDeliveryProfileAgentId, tvDeliveryRolePill;
    private TextView tvProfileShiftTitle, tvProfileShiftSubtext;
    private TextView tvMetricDeliveriesCount, tvDeliveryVehicleInfo, tvDeliveryLicenseNo, tvDeliveryDispatchHub;
    private TextView tvDeliveryPhone, tvDeliveryEmail, btnEditDeliveryContactAction;
    private MaterialSwitch switchProfileDeliveryShift;
    private MaterialButton btnDeliveryOpenMap, btnDeliveryOpenDashboard, btnDeliverySignOut;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_delivery_profile);

        btnBackDeliveryProfile = findViewById(R.id.btnBackDeliveryProfile);
        btnEditDeliveryProfileHeader = findViewById(R.id.btnEditDeliveryProfileHeader);
        ivProfileShiftStatusIcon = findViewById(R.id.ivProfileShiftStatusIcon);
        tvDeliveryProfileFullName = findViewById(R.id.tvDeliveryProfileFullName);
        tvDeliveryProfileAgentId = findViewById(R.id.tvDeliveryProfileAgentId);
        tvDeliveryRolePill = findViewById(R.id.tvDeliveryRolePill);
        tvProfileShiftTitle = findViewById(R.id.tvProfileShiftTitle);
        tvProfileShiftSubtext = findViewById(R.id.tvProfileShiftSubtext);
        tvMetricDeliveriesCount = findViewById(R.id.tvMetricDeliveriesCount);
        tvDeliveryVehicleInfo = findViewById(R.id.tvDeliveryVehicleInfo);
        tvDeliveryLicenseNo = findViewById(R.id.tvDeliveryLicenseNo);
        tvDeliveryDispatchHub = findViewById(R.id.tvDeliveryDispatchHub);
        tvDeliveryPhone = findViewById(R.id.tvDeliveryPhone);
        tvDeliveryEmail = findViewById(R.id.tvDeliveryEmail);
        btnEditDeliveryContactAction = findViewById(R.id.btnEditDeliveryContactAction);
        switchProfileDeliveryShift = findViewById(R.id.switchProfileDeliveryShift);
        btnDeliveryOpenMap = findViewById(R.id.btnDeliveryOpenMap);
        btnDeliveryOpenDashboard = findViewById(R.id.btnDeliveryOpenDashboard);
        btnDeliverySignOut = findViewById(R.id.btnDeliverySignOut);

        if (btnBackDeliveryProfile != null) {
            btnBackDeliveryProfile.setOnClickListener(v -> finish());
        }

        loadDeliveryData();

        View.OnClickListener editListener = v -> showEditProfileDialog();
        if (btnEditDeliveryProfileHeader != null) btnEditDeliveryProfileHeader.setOnClickListener(editListener);
        if (btnEditDeliveryContactAction != null) btnEditDeliveryContactAction.setOnClickListener(editListener);

        if (switchProfileDeliveryShift != null) {
            switchProfileDeliveryShift.setOnCheckedChangeListener((buttonView, isChecked) -> {
                SharedPreferences prefs = getSharedPreferences("PharmaGoPrefs", MODE_PRIVATE);
                prefs.edit().putBoolean("shift_active", isChecked).apply();
                if (isChecked) {
                    if (tvProfileShiftTitle != null) tvProfileShiftTitle.setText("Shift Status: Online");
                    if (tvProfileShiftSubtext != null) tvProfileShiftSubtext.setText("Active for live prescription order assignments");
                    if (ivProfileShiftStatusIcon != null) ivProfileShiftStatusIcon.setColorFilter(0xFF00A884);
                    Toast.makeText(this, "Shift is ONLINE - Ready for Deliveries", Toast.LENGTH_SHORT).show();
                } else {
                    if (tvProfileShiftTitle != null) tvProfileShiftTitle.setText("Shift Status: Offline");
                    if (tvProfileShiftSubtext != null) tvProfileShiftSubtext.setText("Delivery assignments paused");
                    if (ivProfileShiftStatusIcon != null) ivProfileShiftStatusIcon.setColorFilter(0xFF94A3B8);
                    Toast.makeText(this, "Shift is OFFLINE - Paused", Toast.LENGTH_SHORT).show();
                }
            });
        }

        if (btnDeliveryOpenMap != null) {
            btnDeliveryOpenMap.setOnClickListener(v -> {
                Intent intent = new Intent(DeliveryProfileActivity.this, DeliveryMapActivity.class);
                startActivity(intent);
            });
        }

        if (btnDeliveryOpenDashboard != null) {
            btnDeliveryOpenDashboard.setOnClickListener(v -> {
                Intent intent = new Intent(DeliveryProfileActivity.this, DeliveryDashboardActivity.class);
                startActivity(intent);
            });
        }

        if (btnDeliverySignOut != null) {
            btnDeliverySignOut.setOnClickListener(v -> {
                SharedPreferences prefs = getSharedPreferences("PharmaGoPrefs", MODE_PRIVATE);
                if (prefs != null) {
                    prefs.edit().clear().apply();
                }
                Intent intent = new Intent(DeliveryProfileActivity.this, LoginActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            });
        }
    }

    private void loadDeliveryData() {
        SharedPreferences prefs = getSharedPreferences("PharmaGoPrefs", MODE_PRIVATE);
        String username = prefs.getString("username", "DEL402");
        String fullName = prefs.getString("fullName", "Rajesh Kumar");
        String phone = prefs.getString("phone", "+91 98765 43214");
        String email = prefs.getString("email", "del402@pharmago.com");
        String vehicle = prefs.getString("vehicle", "Two-Wheeler (Hero Electric Optima) • KA-01-EQ-9872");
        boolean shiftActive = prefs.getBoolean("shift_active", true);

        if (tvDeliveryProfileFullName != null) tvDeliveryProfileFullName.setText(fullName);
        if (tvDeliveryProfileAgentId != null) tvDeliveryProfileAgentId.setText("Courier ID: " + username);
        if (tvDeliveryPhone != null) tvDeliveryPhone.setText(phone);
        if (tvDeliveryEmail != null) tvDeliveryEmail.setText(email);
        if (tvDeliveryVehicleInfo != null) tvDeliveryVehicleInfo.setText(vehicle);
        if (switchProfileDeliveryShift != null) switchProfileDeliveryShift.setChecked(shiftActive);
    }

    private void showEditProfileDialog() {
        SharedPreferences prefs = getSharedPreferences("PharmaGoPrefs", MODE_PRIVATE);
        String curName = prefs.getString("fullName", "Rajesh Kumar");
        String curPhone = prefs.getString("phone", "+91 98765 43214");
        String curEmail = prefs.getString("email", "del402@pharmago.com");
        String curVehicle = prefs.getString("vehicle", "Two-Wheeler (Hero Electric Optima) • KA-01-EQ-9872");

        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_edit_profile, null);
        TextInputEditText etName = dialogView.findViewById(R.id.etEditName);
        TextInputEditText etPhone = dialogView.findViewById(R.id.etEditPhone);
        TextInputEditText etEmail = dialogView.findViewById(R.id.etEditEmail);
        TextInputEditText etAddress = dialogView.findViewById(R.id.etEditAddress);

        if (etName != null) etName.setText(curName);
        if (etPhone != null) etPhone.setText(curPhone);
        if (etEmail != null) etEmail.setText(curEmail);
        if (etAddress != null) {
            etAddress.setHint("Vehicle & Registration");
            etAddress.setText(curVehicle);
        }

        AlertDialog dialog = new MaterialAlertDialogBuilder(this)
                .setTitle("Edit Courier Profile")
                .setView(dialogView)
                .setPositiveButton("Save Changes", null)
                .setNegativeButton("Cancel", null)
                .create();

        dialog.setOnShowListener(d -> {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
                String newName = etName != null && etName.getText() != null ? etName.getText().toString().trim() : "";
                String newPhone = etPhone != null && etPhone.getText() != null ? etPhone.getText().toString().trim() : "";
                String newEmail = etEmail != null && etEmail.getText() != null ? etEmail.getText().toString().trim() : "";
                String newVehicle = etAddress != null && etAddress.getText() != null ? etAddress.getText().toString().trim() : "";

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
                        .putString("vehicle", newVehicle)
                        .apply();

                loadDeliveryData();
                Toast.makeText(this, "Profile updated successfully!", Toast.LENGTH_SHORT).show();
                dialog.dismiss();
            });
        });

        dialog.show();
    }
}
