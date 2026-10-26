package com.example.pharmago;

import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.pharmago.database.DbCallback;
import com.example.pharmago.models.UserAuthResult;
import com.example.pharmago.services.BackendService;
import com.example.pharmago.utils.SecurePreferencesManager;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

/**
 * Requirement Part 1.6: Profile Data Persistence & Edit Profile
 * - Binds inputs to existing user values
 * - Executes UPDATE users SET full_name = ?, phone = ?, address = ? WHERE id = ? via BackendService
 * - Updates encrypted session cache in SecurePreferencesManager
 * - Returns RESULT_OK to immediately refresh ProfileActivity
 */
public class EditProfileActivity extends AppCompatActivity {

    private TextInputEditText etEditFullName, etEditPhone, etEditAddress;
    private MaterialButton btnSaveProfile;
    private ProgressBar pbEditProfile;
    private long userId = 101L;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        MaterialToolbar toolbar = findViewById(R.id.toolbarEditProfile);
        if (toolbar != null) {
            toolbar.setNavigationOnClickListener(v -> finish());
        }

        etEditFullName = findViewById(R.id.etEditFullName);
        etEditPhone = findViewById(R.id.etEditPhone);
        etEditAddress = findViewById(R.id.etEditAddress);
        btnSaveProfile = findViewById(R.id.btnSaveProfile);
        pbEditProfile = findViewById(R.id.pbEditProfile);

        // Load existing session values
        SecurePreferencesManager securePrefs = SecurePreferencesManager.getInstance(this);
        userId = securePrefs.getUserId();
        String currentName = securePrefs.getFullName();
        String currentPhone = securePrefs.getPhone();
        String currentAddress = securePrefs.getAddress();

        if (etEditFullName != null) etEditFullName.setText(currentName);
        if (etEditPhone != null) etEditPhone.setText(currentPhone);
        if (etEditAddress != null) etEditAddress.setText(currentAddress);

        // Fetch fresh from MySQL/Database
        BackendService.getInstance(this).getUserProfile(userId, new DbCallback<UserAuthResult>() {
            @Override
            public void onSuccess(UserAuthResult user) {
                if (user != null) {
                    if (etEditFullName != null && (user.getFullName() != null && !user.getFullName().isEmpty())) {
                        etEditFullName.setText(user.getFullName());
                    }
                    if (etEditPhone != null && (user.getPhone() != null && !user.getPhone().isEmpty())) {
                        etEditPhone.setText(user.getPhone());
                    }
                    if (etEditAddress != null && (user.getAddress() != null && !user.getAddress().isEmpty())) {
                        etEditAddress.setText(user.getAddress());
                    }
                }
            }

            @Override
            public void onFailure(Exception e) {
                // Keep loaded cached values safely
            }
        });

        if (btnSaveProfile != null) {
            btnSaveProfile.setOnClickListener(v -> saveProfileChanges());
        }
    }

    private void saveProfileChanges() {
        String newFullName = etEditFullName != null && etEditFullName.getText() != null
                ? etEditFullName.getText().toString().trim() : "";
        String newPhone = etEditPhone != null && etEditPhone.getText() != null
                ? etEditPhone.getText().toString().trim() : "";
        String newAddress = etEditAddress != null && etEditAddress.getText() != null
                ? etEditAddress.getText().toString().trim() : "";

        if (newFullName.isEmpty()) {
            if (etEditFullName != null) etEditFullName.setError("Full name is required");
            return;
        }

        if (pbEditProfile != null) pbEditProfile.setVisibility(View.VISIBLE);
        if (btnSaveProfile != null) btnSaveProfile.setEnabled(false);

        // Requirement Part 1.6: Execute UPDATE users SET full_name = ?, phone = ?, address = ? WHERE id = ?
        BackendService.getInstance(this).updateUserProfile(userId, newFullName, newPhone, newAddress, new DbCallback<Boolean>() {
            @Override
            public void onSuccess(Boolean result) {
                if (pbEditProfile != null) pbEditProfile.setVisibility(View.GONE);
                if (btnSaveProfile != null) btnSaveProfile.setEnabled(true);

                // Update session cache in SecurePreferencesManager & UserSession
                SecurePreferencesManager.getInstance(EditProfileActivity.this)
                        .updateProfileData(newFullName, newPhone, newAddress);
                com.example.pharmago.utils.UserSession session = com.example.pharmago.utils.UserSession.getInstance(EditProfileActivity.this);
                session.saveUser(session.getUserId(), session.getUsername(), session.getRole(), newFullName, newPhone, newAddress);

                // Also update legacy preferences for compatibility
                getSharedPreferences("PharmaGoPrefs", MODE_PRIVATE).edit()
                        .putString("username", newFullName)
                        .putString("phone", newPhone)
                        .putString("userAddress", newAddress)
                        .apply();

                Toast.makeText(EditProfileActivity.this, "Profile updated successfully! ✓", Toast.LENGTH_SHORT).show();
                setResult(RESULT_OK);
                finish();
            }

            @Override
            public void onFailure(Exception e) {
                if (pbEditProfile != null) pbEditProfile.setVisibility(View.GONE);
                if (btnSaveProfile != null) btnSaveProfile.setEnabled(true);
                Toast.makeText(EditProfileActivity.this, "Failed to save to server: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
