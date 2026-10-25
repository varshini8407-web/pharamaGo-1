package com.example.pharmago.utils;

import android.app.Activity;
import android.content.Intent;

import com.example.pharmago.ClinicMedicineActivity;
import com.example.pharmago.PharmacistHomeActivity;
import com.example.pharmago.PharmacistOrdersActivity;
import com.example.pharmago.PharmacistReportsActivity;
import com.example.pharmago.QRScanCounterActivity;
import com.example.pharmago.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class PharmacistNavigationHelper {

    public static void setupBottomNavigation(Activity activity, BottomNavigationView bnv, int currentItemId) {
        if (bnv == null || activity == null) return;

        bnv.setSelectedItemId(currentItemId);

        bnv.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == currentItemId) {
                return true;
            }

            Intent intent = null;
            if (itemId == R.id.nav_pharm_dashboard) {
                intent = new Intent(activity, PharmacistHomeActivity.class);
            } else if (itemId == R.id.nav_pharm_orders) {
                intent = new Intent(activity, PharmacistOrdersActivity.class);
            } else if (itemId == R.id.nav_pharm_scan) {
                intent = new Intent(activity, QRScanCounterActivity.class);
            } else if (itemId == R.id.nav_pharm_inventory) {
                intent = new Intent(activity, ClinicMedicineActivity.class);
            } else if (itemId == R.id.nav_pharm_reports) {
                intent = new Intent(activity, PharmacistReportsActivity.class);
            }

            if (intent != null) {
                intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                activity.startActivity(intent);
                activity.overridePendingTransition(0, 0);
                return true;
            }
            return false;
        });
    }
}
