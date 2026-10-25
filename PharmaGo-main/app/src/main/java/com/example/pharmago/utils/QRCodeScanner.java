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

        // Regex 1: ORDER:<id> or ORDER #<id> or ORDER-<id>
        java.util.regex.Pattern pattern = java.util.regex.Pattern.compile("(?i)order[:#\\s-]*(\\d+)", java.util.regex.Pattern.CASE_INSENSITIVE);
        java.util.regex.Matcher matcher = pattern.matcher(cleaned);
        if (matcher.find()) {
            try {
                return Long.parseLong(matcher.group(1));
            } catch (Exception ignored) {}
        }

        // Regex 2: Raw number string only (e.g., "102" or "8090")
        if (cleaned.matches("^\\d{1,10}$")) {
            try {
                return Long.parseLong(cleaned);
            } catch (Exception ignored) {}
        }

        return null;
    }
}
