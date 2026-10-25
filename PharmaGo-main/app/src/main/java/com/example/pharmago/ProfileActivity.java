package com.example.pharmago;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;

public class ProfileActivity extends AppCompatActivity {

    private TextView tvProfileUsername, tvProfileEmail, tvProfilePhone, tvProfileId;
    private TextView tvProfileBloodGroup, tvProfileEmergency, tvProfileAddress, tvProfileInsurance;
    private Button btnLogout, btnEditProfile;
    private View btnBackProfile, layoutAvatarClick, btnAvatarEditBadge, btnEditRecords, btnEditAddressSection;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        // Bind all UI components
        tvProfileUsername = findViewById(R.id.tvProfileUsername);
        tvProfileEmail = findViewById(R.id.tvProfileEmail);
        tvProfilePhone = findViewById(R.id.tvProfilePhone);
        tvProfileId = findViewById(R.id.tvProfileId);
        tvProfileBloodGroup = findViewById(R.id.tvProfileBloodGroup);
        tvProfileEmergency = findViewById(R.id.tvProfileEmergency);
        tvProfileInsurance = findViewById(R.id.tvProfileInsurance);
        tvProfileAddress = findViewById(R.id.tvProfileAddress);

        btnLogout = findViewById(R.id.btnLogout);
        btnEditProfile = findViewById(R.id.btnEditProfile);
        btnBackProfile = findViewById(R.id.btnBackProfile);
        layoutAvatarClick = findViewById(R.id.layoutAvatarClick);
        btnAvatarEditBadge = findViewById(R.id.btnAvatarEditBadge);
        btnEditRecords = findViewById(R.id.btnEditRecords);
        btnEditAddressSection = findViewById(R.id.btnEditAddressSection);

        if (btnBackProfile != null) {
            btnBackProfile.setOnClickListener(v -> finish());
        }

        loadProfileData();

        // Multiple intuitive entry points to Edit Profile
        View.OnClickListener editClickListener = v -> showEditProfileDialog();

        if (btnEditProfile != null) btnEditProfile.setOnClickListener(editClickListener);
        if (layoutAvatarClick != null) layoutAvatarClick.setOnClickListener(editClickListener);
        if (btnAvatarEditBadge != null) btnAvatarEditBadge.setOnClickListener(editClickListener);
        if (btnEditRecords != null) btnEditRecords.setOnClickListener(editClickListener);
        if (btnEditAddressSection != null) btnEditAddressSection.setOnClickListener(editClickListener);

        if (btnLogout != null) {
            btnLogout.setOnClickListener(v -> {
                SharedPreferences prefs = getSharedPreferences("PharmaGoPrefs", MODE_PRIVATE);
                if (prefs != null) {
                    SharedPreferences.Editor editor = prefs.edit();
                    editor.clear();
                    editor.apply();
                }

                Intent intent = new Intent(ProfileActivity.this, LoginActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            });
        }
    }

    private void loadProfileData() {
        SharedPreferences prefs = getSharedPreferences("PharmaGoPrefs", MODE_PRIVATE);
        if (prefs == null) return;

        String username = prefs.getString("username", "Admin User");
        String email = prefs.getString("email", "admin@pharmago.com");
        String phone = prefs.getString("phone", "+91 98765 43210");
        String bloodGroup = prefs.getString("bloodGroup", "O+ (Universal)");
        String emergency = prefs.getString("emergencyContact", "+91 98765 43210");
        String address = prefs.getString("userAddress", "Home • Sector 4, Metro City");
        long userId = prefs.getLong("userId", 101L);

        if (tvProfileUsername != null) tvProfileUsername.setText(username);
        if (tvProfileEmail != null) tvProfileEmail.setText(email);
        if (tvProfilePhone != null) tvProfilePhone.setText(phone);
        if (tvProfileBloodGroup != null) tvProfileBloodGroup.setText(bloodGroup);
        if (tvProfileEmergency != null) tvProfileEmergency.setText(emergency);
        if (tvProfileAddress != null) tvProfileAddress.setText("🏡 " + address);
        if (tvProfileId != null) tvProfileId.setText(userId == -1L ? "101" : String.valueOf(userId));
    }

    private void showEditProfileDialog() {
        SharedPreferences prefs = getSharedPreferences("PharmaGoPrefs", MODE_PRIVATE);
        if (prefs == null) return;

        LayoutInflater inflater = LayoutInflater.from(this);
        View dialogView = inflater.inflate(R.layout.dialog_edit_profile, null);

        TextInputEditText etEditName = dialogView.findViewById(R.id.etEditName);
        TextInputEditText etEditEmail = dialogView.findViewById(R.id.etEditEmail);
        TextInputEditText etEditPhone = dialogView.findViewById(R.id.etEditPhone);
        TextInputEditText etEditAddress = dialogView.findViewById(R.id.etEditAddress);
        TextInputEditText etEditBloodGroup = dialogView.findViewById(R.id.etEditBloodGroup);
        TextInputEditText etEditEmergency = dialogView.findViewById(R.id.etEditEmergency);
        Button btnSaveProfileDialog = dialogView.findViewById(R.id.btnSaveProfileDialog);
        Button btnCancelProfileDialog = dialogView.findViewById(R.id.btnCancelProfileDialog);

        // Pre-fill with current profile values
        if (etEditName != null) etEditName.setText(prefs.getString("username", "Admin User"));
        if (etEditEmail != null) etEditEmail.setText(prefs.getString("email", "admin@pharmago.com"));
        if (etEditPhone != null) etEditPhone.setText(prefs.getString("phone", "+91 98765 43210"));
        if (etEditAddress != null) etEditAddress.setText(prefs.getString("userAddress", "Home • Sector 4, Metro City"));
        if (etEditBloodGroup != null) etEditBloodGroup.setText(prefs.getString("bloodGroup", "O+ (Universal)"));
        if (etEditEmergency != null) etEditEmergency.setText(prefs.getString("emergencyContact", "+91 98765 43210"));

        AlertDialog dialog = new MaterialAlertDialogBuilder(this)
                .setView(dialogView)
                .setCancelable(true)
                .create();

        if (btnCancelProfileDialog != null) {
            btnCancelProfileDialog.setOnClickListener(v -> dialog.dismiss());
        }

        if (btnSaveProfileDialog != null) {
            btnSaveProfileDialog.setOnClickListener(v -> {
                String newName = etEditName != null && etEditName.getText() != null ? etEditName.getText().toString().trim() : "Admin User";
                String newEmail = etEditEmail != null && etEditEmail.getText() != null ? etEditEmail.getText().toString().trim() : "admin@pharmago.com";
                String newPhone = etEditPhone != null && etEditPhone.getText() != null ? etEditPhone.getText().toString().trim() : "+91 98765 43210";
                String newAddress = etEditAddress != null && etEditAddress.getText() != null ? etEditAddress.getText().toString().trim() : "Home • Sector 4, Metro City";
                String newBlood = etEditBloodGroup != null && etEditBloodGroup.getText() != null ? etEditBloodGroup.getText().toString().trim() : "O+ (Universal)";
                String newEmergency = etEditEmergency != null && etEditEmergency.getText() != null ? etEditEmergency.getText().toString().trim() : "+91 98765 43210";

                if (newName.isEmpty()) newName = "Admin User";
                if (newAddress.isEmpty()) newAddress = "Home • Sector 4, Metro City";

                // Persist to SharedPreferences
                prefs.edit()
                        .putString("username", newName)
                        .putString("email", newEmail)
                        .putString("phone", newPhone)
                        .putString("userAddress", newAddress)
                        .putString("bloodGroup", newBlood)
                        .putString("emergencyContact", newEmergency)
                        .apply();

                // Update UI immediately
                loadProfileData();
                dialog.dismiss();
                Toast.makeText(ProfileActivity.this, "Profile updated successfully! ✓", Toast.LENGTH_SHORT).show();
            });
        }

        dialog.show();
    }
}