package com.example.pharmago;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.pharmago.api.ApiService;
import com.example.pharmago.api.RetrofitClient;
import com.example.pharmago.models.RegisterRequest;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RegisterActivity extends AppCompatActivity {

    EditText etFullName, etUsername, etEmail, etRegisterPhone, etRegisterPassword, etConfirmPassword;
    Button btnRegister;
    TextView tvLogin, tvRoleHint;
    com.google.android.material.button.MaterialButtonToggleGroup toggleRoleGroup;
    ApiService apiService;
    private String selectedRole = "CUSTOMER";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Requirement 2: setContentView strictly BEFORE any findViewById
        setContentView(R.layout.activity_register);

        // Cross-referenced IDs against activity_register.xml
        etFullName = findViewById(R.id.etFullName);
        etUsername = findViewById(R.id.etUsername);
        etEmail = findViewById(R.id.etEmail);
        etRegisterPhone = findViewById(R.id.etRegisterPhone);
        etRegisterPassword = findViewById(R.id.etRegisterPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);
        btnRegister = findViewById(R.id.btnRegister);
        tvLogin = findViewById(R.id.tvLogin);
        tvRoleHint = findViewById(R.id.tvRoleHint);
        toggleRoleGroup = findViewById(R.id.toggleRoleGroup);

        if (toggleRoleGroup != null) {
            toggleRoleGroup.check(R.id.btnRolePatient);
            toggleRoleGroup.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
                if (!isChecked) return;
                if (checkedId == R.id.btnRolePatient) {
                    selectedRole = "CUSTOMER";
                    if (tvRoleHint != null) tvRoleHint.setText("Standard patient account for medicines and pickup passes.");
                    if (etUsername != null) etUsername.setHint("Username");
                    if (btnRegister != null) btnRegister.setText("Create Patient Account");
                } else if (checkedId == R.id.btnRolePharmacist) {
                    selectedRole = "PHARMACIST";
                    if (tvRoleHint != null) tvRoleHint.setText("Hospital staff ID must start with 'ITA' (e.g. ITA1024).");
                    if (etUsername != null) etUsername.setHint("Staff ID (e.g. ITA1024)");
                    if (btnRegister != null) btnRegister.setText("Register Pharmacist (ITA)");
                } else if (checkedId == R.id.btnRoleDelivery) {
                    selectedRole = "DELIVERY";
                    if (tvRoleHint != null) tvRoleHint.setText("Delivery courier ID must start with 'DEL' (e.g. DEL402).");
                    if (etUsername != null) etUsername.setHint("Courier ID (e.g. DEL402)");
                    if (btnRegister != null) btnRegister.setText("Register Delivery Courier (DEL)");
                }
            });
        }

        apiService = RetrofitClient.getClient().create(ApiService.class);

        if (btnRegister != null) {
            btnRegister.setOnClickListener(v -> {
                String fullName = etFullName != null && etFullName.getText() != null
                        ? etFullName.getText().toString().trim() : "";
                String username = etUsername != null && etUsername.getText() != null
                        ? etUsername.getText().toString().trim() : "";
                String email = etEmail != null && etEmail.getText() != null
                        ? etEmail.getText().toString().trim() : "";
                String phone = etRegisterPhone != null && etRegisterPhone.getText() != null
                        ? etRegisterPhone.getText().toString().trim() : "";
                String password = etRegisterPassword != null && etRegisterPassword.getText() != null
                        ? etRegisterPassword.getText().toString().trim() : "";
                String confirmPassword = etConfirmPassword != null && etConfirmPassword.getText() != null
                        ? etConfirmPassword.getText().toString().trim() : "";

                // Client-side validations to eliminate NPE and empty submissions
                if (fullName.isEmpty()) {
                    if (etFullName != null) {
                        etFullName.setError("Full name is required");
                        etFullName.requestFocus();
                    }
                    return;
                }

                if (username.isEmpty()) {
                    if (etUsername != null) {
                        etUsername.setError("Username is required");
                        etUsername.requestFocus();
                    }
                    return;
                }

                if (email.isEmpty()) {
                    if (etEmail != null) {
                        etEmail.setError("Email is required");
                        etEmail.requestFocus();
                    }
                    return;
                }

                if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                    if (etEmail != null) {
                        etEmail.setError("Please enter a valid email address");
                        etEmail.requestFocus();
                    }
                    return;
                }

                if (phone.isEmpty()) {
                    if (etRegisterPhone != null) {
                        etRegisterPhone.setError("Phone number is required");
                        etRegisterPhone.requestFocus();
                    }
                    return;
                }

                if (password.isEmpty()) {
                    if (etRegisterPassword != null) {
                        etRegisterPassword.setError("Password is required");
                        etRegisterPassword.requestFocus();
                    }
                    return;
                }

                if (password.length() < 4) {
                    if (etRegisterPassword != null) {
                        etRegisterPassword.setError("Password must be at least 4 characters");
                        etRegisterPassword.requestFocus();
                    }
                    return;
                }

                if (confirmPassword.isEmpty()) {
                    if (etConfirmPassword != null) {
                        etConfirmPassword.setError("Please confirm your password");
                        etConfirmPassword.requestFocus();
                    }
                    return;
                }

                if (!password.equals(confirmPassword)) {
                    if (etConfirmPassword != null) {
                        etConfirmPassword.setError("Passwords do not match");
                        etConfirmPassword.requestFocus();
                    }
                    return;
                }

                performRegistration(username, email, password, fullName, phone);
            });
        }

        if (tvLogin != null) {
            tvLogin.setOnClickListener(v -> {
                // Requirement 4: Explicit intent RegisterActivity -> LoginActivity
                Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                finish();
            });
        }
    }

    private void performRegistration(String username, String email, String password, String fullName, String phone) {
        if (btnRegister != null) {
            btnRegister.setEnabled(false);
            btnRegister.setText("Registering via JDBC...");
        }

        // Enforce role-based ID prefixes
        boolean isPharmacistIntent = "PHARMACIST".equalsIgnoreCase(selectedRole) ||
                username.toUpperCase().startsWith("ITA");
        boolean isDeliveryIntent = "DELIVERY".equalsIgnoreCase(selectedRole) ||
                username.toUpperCase().startsWith("DEL");

        if (isPharmacistIntent && !username.toUpperCase().startsWith("ITA")) {
            if (btnRegister != null) {
                btnRegister.setEnabled(true);
                btnRegister.setText("Register Pharmacist (ITA)");
            }
            if (etUsername != null) {
                etUsername.setError("Pharmacist staff ID must begin with prefix 'ITA' (e.g. ITA1024)");
                etUsername.requestFocus();
            }
            Toast.makeText(this, "Pharmacist registration requires 'ITA' prefix (e.g. ITA1024)", Toast.LENGTH_LONG).show();
            return;
        }

        if (isDeliveryIntent && !username.toUpperCase().startsWith("DEL")) {
            if (btnRegister != null) {
                btnRegister.setEnabled(true);
                btnRegister.setText("Register Delivery Courier (DEL)");
            }
            if (etUsername != null) {
                etUsername.setError("Delivery username must begin with prefix 'DEL' (e.g. DEL402)");
                etUsername.requestFocus();
            }
            Toast.makeText(this, "Delivery agent username must start with prefix 'DEL' (e.g. DEL402)", Toast.LENGTH_LONG).show();
            return;
        }

        String role = isPharmacistIntent ? "PHARMACIST" : (isDeliveryIntent ? "DELIVERY" : "CUSTOMER");

        com.example.pharmago.services.BackendService.getInstance(this).registerUser(
                username,
                email,
                password,
                fullName,
                phone,
                "PharmaGo Healthcare Campus",
                role,
                new com.example.pharmago.database.DbCallback<Boolean>() {
                    @Override
                    public void onSuccess(Boolean result) {
                        if (btnRegister != null) {
                            btnRegister.setEnabled(true);
                            btnRegister.setText("REGISTER");
                        }

                        Toast.makeText(RegisterActivity.this, "Registration successful!", Toast.LENGTH_SHORT).show();

                        // Requirement 4: Explicit Intent RegisterActivity -> LoginActivity
                        Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(intent);
                        finish();
                    }

                    @Override
                    public void onFailure(Exception e) {
                        if (btnRegister != null) {
                            btnRegister.setEnabled(true);
                            btnRegister.setText("REGISTER");
                        }
                        Toast.makeText(RegisterActivity.this, "Registration error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    }
                }
        );
    }
}