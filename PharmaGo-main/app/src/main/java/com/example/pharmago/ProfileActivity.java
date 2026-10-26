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
        View.OnClickListener editClickListener = v -> {
            Intent intent = new Intent(ProfileActivity.this, EditProfileActivity.class);
            startActivity(intent);
        };

        if (btnEditProfile != null) btnEditProfile.setOnClickListener(editClickListener);
        if (layoutAvatarClick != null) layoutAvatarClick.setOnClickListener(editClickListener);
        if (btnAvatarEditBadge != null) btnAvatarEditBadge.setOnClickListener(editClickListener);
        if (btnEditRecords != null) btnEditRecords.setOnClickListener(editClickListener);
        if (btnEditAddressSection != null) btnEditAddressSection.setOnClickListener(editClickListener);

        if (btnLogout != null) {
            btnLogout.setOnClickListener(v -> {
                com.example.pharmago.utils.SecurePreferencesManager.getInstance(this).clearSession();
                com.example.pharmago.utils.UserSession.getInstance(this).clearSession();
                SharedPreferences prefs = getSharedPreferences("PharmaGoPrefs", MODE_PRIVATE);
                if (prefs != null) {
                    prefs.edit().clear().apply();
                }

                Intent intent = new Intent(ProfileActivity.this, LoginActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            });
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadProfileData();
    }

    private void loadProfileData() {
        com.example.pharmago.utils.SecurePreferencesManager securePrefs =
                com.example.pharmago.utils.SecurePreferencesManager.getInstance(this);

        long userId = securePrefs.getUserId();
        String cachedName = securePrefs.getFullName();
        String cachedPhone = securePrefs.getPhone();
        String cachedAddress = securePrefs.getAddress();

        if (tvProfileUsername != null) tvProfileUsername.setText(cachedName);
        if (tvProfileEmail != null) tvProfileEmail.setText(securePrefs.getUsername());
        if (tvProfilePhone != null) tvProfilePhone.setText(cachedPhone);
        if (tvProfileAddress != null) tvProfileAddress.setText("🏡 " + cachedAddress);
        if (tvProfileId != null) tvProfileId.setText(String.valueOf(userId));

        // Requirement Part 1.6: On ProfileActivity.onCreate(), fetch fresh user data via BackendService.getUserProfile(userId)
        com.example.pharmago.services.BackendService.getInstance(this).getUserProfile(userId, new com.example.pharmago.database.DbCallback<com.example.pharmago.models.UserAuthResult>() {
            @Override
            public void onSuccess(com.example.pharmago.models.UserAuthResult user) {
                if (user != null) {
                    if (user.getFullName() != null && !user.getFullName().isEmpty()) {
                        if (tvProfileUsername != null) tvProfileUsername.setText(user.getFullName());
                    }
                    if (user.getUsername() != null && !user.getUsername().isEmpty()) {
                        if (tvProfileEmail != null) tvProfileEmail.setText(user.getUsername());
                    }
                    if (user.getPhone() != null && !user.getPhone().isEmpty()) {
                        if (tvProfilePhone != null) tvProfilePhone.setText(user.getPhone());
                    }
                    if (user.getAddress() != null && !user.getAddress().isEmpty()) {
                        if (tvProfileAddress != null) tvProfileAddress.setText("🏡 " + user.getAddress());
                    }
                    // Update local secure cache
                    securePrefs.updateProfileData(user.getFullName(), user.getPhone(), user.getAddress());
                }
            }

            @Override
            public void onFailure(Exception e) {
                // Keep encrypted local cached values
            }
        });
    }
}