package com.example.pharmago.utils;

import android.content.Context;
import androidx.activity.result.ActivityResultLauncher;
import com.journeyapps.barcodescanner.ScanOptions;

/**
 * Member 3 Smart Features: QR Code Scanner helper
 * Configures ZXing ScanOptions for camera counter scanning.
 */
public class QRCodeScanner {

    /**
     * Creates standard ScanOptions for scanning takeaway QR codes at counter
     */
    public static ScanOptions createCounterScanOptions() {
        ScanOptions options = new ScanOptions();
        options.setPrompt("Align patient takeaway QR pass within the viewfinder");
        options.setBeepEnabled(true);
        options.setOrientationLocked(true);
        options.setCameraId(0); // Rear camera
        options.setBarcodeImageEnabled(false);
        options.setDesiredBarcodeFormats(ScanOptions.QR_CODE);
        return options;
    }

    /**
     * Parses payload string to extract order ID
     * Supports formats:
     * - "ORDER:<orderId>:INV:<invoiceId>"
     * - "PHARMAGO-PICKUP-ORDER-<orderId>"
     * - Raw order number (e.g., "101")
     */
    public static Long parseOrderIdFromPayload(String payload) {
        if (payload == null || payload.trim().isEmpty()) {
            return null;
        }

        String cleaned = payload.trim();

        // Standard requirement: "ORDER:<orderId>:INV:<invoiceId>"
        if (cleaned.toUpperCase().startsWith("ORDER:")) {
            try {
                String[] parts = cleaned.split(":");
                if (parts.length >= 2) {
                    return Long.parseLong(parts[1]);
                }
            } catch (Exception ignored) {}
        }

        // Format: "PHARMAGO-PICKUP-ORDER-<orderId>"
        if (cleaned.toUpperCase().contains("ORDER-")) {
            try {
                String idPart = cleaned.substring(cleaned.lastIndexOf("ORDER-") + 6);
                return Long.parseLong(idPart.replaceAll("[^0-9]", ""));
            } catch (Exception ignored) {}
        }

        // Raw numbers
        try {
            return Long.parseLong(cleaned.replaceAll("[^0-9]", ""));
        } catch (Exception ignored) {}

        return null;
    }
}
