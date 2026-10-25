package com.example.pharmago.utils;

import android.graphics.Bitmap;
import android.graphics.Color;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;

import java.io.ByteArrayOutputStream;
import java.util.EnumMap;
import java.util.Map;

/**
 * Member 3 Smart Features: QR Code Generator
 * Generates standards-compliant QR Code bitmaps and payloads for patient takeaway verification.
 */
public class QRCodeGenerator {

    /**
     * Formats standardized takeaway pass payload
     */
    public static String formatTakeawayPayload(long orderId, long invoiceId) {
        return "ORDER:" + orderId + ":INV:" + invoiceId;
    }

    /**
     * Generates a QR Code as an Android Bitmap with UTF-8 encoding hints and clinical styling
     *
     * @param payload The text/payload to encode into the QR code
     * @param width The width in pixels
     * @param height The height in pixels
     * @return Bitmap of the generated QR code or null on error
     */
    public static Bitmap generateQRCode(String payload, int width, int height) {
        if (payload == null || payload.trim().isEmpty()) {
            payload = "ORDER:0:INV:0";
        }

        try {
            Map<EncodeHintType, Object> hints = new EnumMap<>(EncodeHintType.class);
            hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");
            hints.put(EncodeHintType.MARGIN, 1);

            BitMatrix bitMatrix = new MultiFormatWriter().encode(
                    payload,
                    BarcodeFormat.QR_CODE,
                    width,
                    height,
                    hints
            );

            Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
            int darkColor = Color.parseColor("#0F172A"); // Primary Clinical Text
            int lightColor = Color.WHITE;

            for (int x = 0; x < width; x++) {
                for (int y = 0; y < height; y++) {
                    bitmap.setPixel(x, y, bitMatrix.get(x, y) ? darkColor : lightColor);
                }
            }
            return bitmap;
        } catch (WriterException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Backward-compatible square bitmap generator
     */
    public static Bitmap generateQRCodeBitmap(String content, int size) {
        return generateQRCode(content, size, size);
    }

    /**
     * Generates a QR Code compressed as PNG byte array
     */
    public static byte[] generateQRCodeBytes(String content, int size) {
        Bitmap bitmap = generateQRCode(content, size, size);
        if (bitmap == null) return new byte[0];

        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
        return stream.toByteArray();
    }
}
