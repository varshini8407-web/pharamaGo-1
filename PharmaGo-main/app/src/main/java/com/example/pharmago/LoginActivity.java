package com.example.pharmago;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.pharmago.api.ApiService;
import com.example.pharmago.api.RetrofitClient;
import com.example.pharmago.models.AuthResponse;
import com.example.pharmago.models.LoginRequest;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends AppCompatActivity {

    EditText etUsername, etPassword;
    Button btnLogin, btnPharmacistPortal, btnDeliveryAgentPortal;
    TextView tvRegister, tvServerStatus;
    ApiService apiService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Requirement 2: setContentView strictly BEFORE any findViewById
        setContentView(R.layout.activity_login);

        // Cross-referenced IDs against activity_login.xml
        etUsername = findViewById(R.id.etUsername);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        btnPharmacistPortal = findViewById(R.id.btnPharmacistPortal);
        btnDeliveryAgentPortal = findViewById(R.id.btnDeliveryAgentPortal);
        tvRegister = findViewById(R.id.tvRegister);
        tvServerStatus = findViewById(R.id.tvServerStatus);

        if (btnPharmacistPortal != null) {
            btnPharmacistPortal.setOnClickListener(v -> performLogin("ITA1024", "admin"));
        }

        if (btnDeliveryAgentPortal != null) {
            btnDeliveryAgentPortal.setOnClickListener(v -> performLogin("AGENT-402", "admin"));
        }

        // Provide convenient demo default credentials for Phase 1 UI testing
        if (etUsername != null && (etUsername.getText() == null || etUsername.getText().toString().isEmpty())) {
            etUsername.setText("admin");
        }
        if (etPassword != null && (etPassword.getText() == null || etPassword.getText().toString().isEmpty())) {
            etPassword.setText("admin");
        }

        apiService = RetrofitClient.getClient().create(ApiService.class);

        checkBackendHealth();

        if (btnLogin != null) {
            btnLogin.setOnClickListener(v -> {
                String username = etUsername != null && etUsername.getText() != null
                        ? etUsername.getText().toString().trim() : "";
                String password = etPassword != null && etPassword.getText() != null
                        ? etPassword.getText().toString().trim() : "";

                // Requirement 2: Client-side input validations
                if (username.isEmpty()) {
                    if (etUsername != null) {
                        etUsername.setError("Please enter your username");
                        etUsername.requestFocus();
                    }
                    return;
                }

                if (password.isEmpty()) {
                    if (etPassword != null) {
                        etPassword.setError("Please enter your password");
                        etPassword.requestFocus();
                    }
                    return;
                }

                performLogin(username, password);
            });
        }

        if (tvRegister != null) {
            tvRegister.setOnClickListener(v -> {
                // Requirement 4: Explicit intent to RegisterActivity
                Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
                startActivity(intent);
            });
        }
    }

    private void checkBackendHealth() {
        if (apiService == null || tvServerStatus == null) return;

        apiService.checkHealth().enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (tvServerStatus == null) return;
                if (response.isSuccessful()) {
                    try {
                        String message = response.body() != null ? response.body().string() : "Connected";
                        tvServerStatus.setText(getString(R.string.server_status_format, message));
                        tvServerStatus.setTextColor(ContextCompat.getColor(LoginActivity.this, android.R.color.holo_green_dark));
                    } catch (Exception e) {
                        tvServerStatus.setText(getString(R.string.server_status_connected));
                    }
                } else {
                    tvServerStatus.setText(getString(R.string.server_status_error, response.code()));
                    tvServerStatus.setTextColor(ContextCompat.getColor(LoginActivity.this, android.R.color.holo_red_dark));
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                if (tvServerStatus == null) return;
                tvServerStatus.setText("Offline Mode (Phase 1 UI Testing Active)");
                tvServerStatus.setTextColor(ContextCompat.getColor(LoginActivity.this, android.R.color.holo_blue_dark));
            }
        });
    }

    private void performLogin(String username, String password) {
        if (btnLogin != null) {
            btnLogin.setEnabled(false);
            btnLogin.setText("Authenticating live...");
        }

        if (apiService != null) {
            apiService.login(new LoginRequest(username, password)).enqueue(new Callback<AuthResponse>() {
                @Override
                public void onResponse(Call<AuthResponse> call, Response<AuthResponse> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        AuthResponse auth = response.body();
                        String role = "CUSTOMER";
                        if (auth.getRoles() != null) {
                            for (String r : auth.getRoles()) {
                                if (r.contains("PHARMACIST")) role = "PHARMACIST";
                                else if (r.contains("DELIVERY")) role = "DELIVERY";
                                else if (r.contains("ADMIN")) role = "ADMIN";
                            }
                        }
                        if (username.toUpperCase().startsWith("ITA")) role = "PHARMACIST";
                        if (username.toUpperCase().startsWith("AGENT") || username.toUpperCase().startsWith("DEL")) role = "DELIVERY";

                        com.example.pharmago.models.UserAuthResult result = new com.example.pharmago.models.UserAuthResult(
                                auth.getId() != null ? auth.getId() : 101L,
                                auth.getUsername() != null ? auth.getUsername() : username,
                                auth.getUsername() != null ? auth.getUsername() : username,
                                auth.getEmail() != null ? auth.getEmail() : username + "@pharmago.com",
                                "+91 98765 43210",
                                role,
                                auth.getToken()
                        );
                        if (btnLogin != null) {
                            btnLogin.setEnabled(true);
                            btnLogin.setText("LOGIN");
                        }
                        routeAuthenticatedUser(result);
                    } else {
                        fallbackAuthenticateViaBackend(username, password);
                    }
                }

                @Override
                public void onFailure(Call<AuthResponse> call, Throwable t) {
                    fallbackAuthenticateViaBackend(username, password);
                }
            });
        } else {
            fallbackAuthenticateViaBackend(username, password);
        }
    }

    private void fallbackAuthenticateViaBackend(String username, String password) {
        com.example.pharmago.services.BackendService.getInstance(this).authenticateUser(
                username,
                password,
                new com.example.pharmago.database.DbCallback<com.example.pharmago.models.UserAuthResult>() {
                    @Override
                    public void onSuccess(com.example.pharmago.models.UserAuthResult auth) {
                        if (btnLogin != null) {
                            btnLogin.setEnabled(true);
                            btnLogin.setText("LOGIN");
                        }
                        routeAuthenticatedUser(auth);
                    }

                    @Override
                    public void onFailure(Exception e) {
                        if (btnLogin != null) {
                            btnLogin.setEnabled(true);
                            btnLogin.setText("LOGIN");
                        }
                        Toast.makeText(LoginActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
        );
    }

    private void routeAuthenticatedUser(com.example.pharmago.models.UserAuthResult auth) {
        if (auth == null) return;

        SharedPreferences prefs = getSharedPreferences("PharmaGoPrefs", MODE_PRIVATE);
        if (prefs != null) {
            SharedPreferences.Editor editor = prefs.edit();
            editor.putString("token", auth.getToken() != null ? auth.getToken() : "jwt_token");
            editor.putLong("userId", auth.getUserId() != null ? auth.getUserId() : 101L);
            editor.putString("username", auth.getUsername() != null ? auth.getUsername() : "User");
            editor.putString("email", auth.getEmail() != null ? auth.getEmail() : "user@pharmago.com");
            editor.putString("role", auth.getRole());
            editor.apply();
        }

        Toast.makeText(LoginActivity.this, "Welcome, " + auth.getUsername() + "!", Toast.LENGTH_SHORT).show();

        // Requirement 3: Check returned user role and route to HomeActivity (PATIENT), PharmacistHomeActivity (PHARMACIST), or DeliveryDashboardActivity (DELIVERY)
        if (auth.isDelivery()) {
            Intent intent = new Intent(LoginActivity.this, DeliveryDashboardActivity.class);
            startActivity(intent);
            finish();
            return;
        }

        if (auth.isPharmacist()) {
            Intent intent = new Intent(LoginActivity.this, PharmacistHomeActivity.class);
            startActivity(intent);
            finish();
            return;
        }

        // Default Patient / Customer
        Intent intent = new Intent(LoginActivity.this, HomeActivity.class);
        startActivity(intent);
        finish();
    }

    private void saveSessionAndNavigate(String username, String email, Long userId, String token) {
        String role = "CUSTOMER";
        if (username != null && username.toUpperCase().startsWith("ITA")) {
            role = "PHARMACIST";
        } else if (username != null && (username.toUpperCase().startsWith("AGENT") || username.toUpperCase().startsWith("DEL"))) {
            role = "DELIVERY";
        }
        routeAuthenticatedUser(new com.example.pharmago.models.UserAuthResult(userId, username, username, email, "", role, token));
    }
}
