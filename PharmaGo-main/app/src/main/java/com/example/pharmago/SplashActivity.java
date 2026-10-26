package com.example.pharmago;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import androidx.appcompat.app.AppCompatActivity;

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            com.example.pharmago.utils.UserSession session = com.example.pharmago.utils.UserSession.getInstance(SplashActivity.this);
            if (session.isLoggedIn()) {
                String role = session.getRole();
                Intent intent;
                if ("PHARMACIST".equalsIgnoreCase(role)) {
                    intent = new Intent(SplashActivity.this, PharmacistHomeActivity.class);
                } else if ("DELIVERY".equalsIgnoreCase(role)) {
                    intent = new Intent(SplashActivity.this, DeliveryDashboardActivity.class);
                } else {
                    intent = new Intent(SplashActivity.this, HomeActivity.class);
                }
                startActivity(intent);
                finish();
            } else {
                Intent intent = new Intent(SplashActivity.this, LoginActivity.class);
                startActivity(intent);
                finish();
            }
        }, 1200);
    }
}