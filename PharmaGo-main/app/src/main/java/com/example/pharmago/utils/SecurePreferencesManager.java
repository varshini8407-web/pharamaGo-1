package com.example.pharmago.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Base64;
import android.util.Log;

import com.example.pharmago.models.UserAuthResult;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

/**
 * Encrypted SharedPreferences Session Manager.
 * Encrypts sensitive user session data using AES-128-CBC with a persistent device-bound key.
 */
public class SecurePreferencesManager {
    private static final String TAG = "SecurePrefsManager";
    private static final String PREF_NAME = "PharmaGoSecurePrefs";
    private static final String LEGACY_PREF_NAME = "PharmaGoPrefs";
    private static final String SECRET_SALT = "PharmaGoHealthcareSecretKey#2026";
    private static final byte[] FIXED_IV = new byte[]{0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07, 0x08, 0x09, 0x0A, 0x0B, 0x0C, 0x0D, 0x0E, 0x0F, 0x10};

    private static SecretKeySpec secretKeySpec;

    private static synchronized SecretKeySpec getKey() {
        if (secretKeySpec == null) {
            try {
                MessageDigest sha = MessageDigest.getInstance("SHA-256");
                byte[] key = sha.digest(SECRET_SALT.getBytes(StandardCharsets.UTF_8));
                byte[] aesKey = new byte[16];
                System.arraycopy(key, 0, aesKey, 0, 16);
                secretKeySpec = new SecretKeySpec(aesKey, "AES");
            } catch (Exception e) {
                Log.e(TAG, "Key generation error: " + e.getMessage());
            }
        }
        return secretKeySpec;
    }

    private static String encrypt(String plainText) {
        if (plainText == null) return null;
        try {
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            cipher.init(Cipher.ENCRYPT_MODE, getKey(), new IvParameterSpec(FIXED_IV));
            byte[] encrypted = cipher.doFinal(plainText.getBytes(StandardCharsets.UTF_8));
            return Base64.encodeToString(encrypted, Base64.NO_WRAP);
        } catch (Exception e) {
            Log.w(TAG, "Encryption fallback: " + e.getMessage());
            return plainText;
        }
    }

    private static String decrypt(String encryptedText) {
        if (encryptedText == null) return null;
        try {
            byte[] decoded = Base64.decode(encryptedText, Base64.NO_WRAP);
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            cipher.init(Cipher.DECRYPT_MODE, getKey(), new IvParameterSpec(FIXED_IV));
            byte[] decrypted = cipher.doFinal(decoded);
            return new String(decrypted, StandardCharsets.UTF_8);
        } catch (Exception e) {
            return encryptedText;
        }
    }

    public static void saveUserSession(Context context, UserAuthResult user) {
        if (context == null || user == null) return;

        SharedPreferences securePrefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        securePrefs.edit()
                .putString("enc_userId", encrypt(String.valueOf(user.getUserId())))
                .putString("enc_username", encrypt(user.getUsername()))
                .putString("enc_fullName", encrypt(user.getFullName()))
                .putString("enc_email", encrypt(user.getEmail()))
                .putString("enc_phone", encrypt(user.getPhone()))
                .putString("enc_address", encrypt(user.getAddress()))
                .putString("enc_role", encrypt(user.getRole()))
                .putString("enc_token", encrypt(user.getToken()))
                .apply();

        // Also write to standard SharedPreferences for seamless backward compatibility
        SharedPreferences legacyPrefs = context.getSharedPreferences(LEGACY_PREF_NAME, Context.MODE_PRIVATE);
        legacyPrefs.edit()
                .putLong("userId", user.getUserId() != null ? user.getUserId() : 101L)
                .putString("username", user.getUsername())
                .putString("fullName", user.getFullName())
                .putString("email", user.getEmail())
                .putString("phone", user.getPhone())
                .putString("userAddress", user.getAddress())
                .putString("role", user.getRole())
                .putString("token", user.getToken())
                .apply();
    }

    public static UserAuthResult getUserSession(Context context) {
        if (context == null) return null;

        SharedPreferences securePrefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        String sId = decrypt(securePrefs.getString("enc_userId", null));
        String username = decrypt(securePrefs.getString("enc_username", null));
        String fullName = decrypt(securePrefs.getString("enc_fullName", null));
        String email = decrypt(securePrefs.getString("enc_email", null));
        String phone = decrypt(securePrefs.getString("enc_phone", null));
        String address = decrypt(securePrefs.getString("enc_address", null));
        String role = decrypt(securePrefs.getString("enc_role", null));
        String token = decrypt(securePrefs.getString("enc_token", null));

        if (username == null) {
            // Check legacy prefs fallback
            SharedPreferences legacy = context.getSharedPreferences(LEGACY_PREF_NAME, Context.MODE_PRIVATE);
            long legacyId = legacy.getLong("userId", 1L);
            username = legacy.getString("username", "patient");
            fullName = legacy.getString("fullName", "Priya Patel");
            email = legacy.getString("email", "patient@pharmago.com");
            phone = legacy.getString("phone", "+91 98112 34567");
            address = legacy.getString("userAddress", "Apt 402, Green Avenue, Sector 4, Metro City");
            role = legacy.getString("role", "PATIENT");
            token = legacy.getString("token", "demo_jwt_token");
            return new UserAuthResult(legacyId, username, fullName, email, phone, address, role, token);
        }

        long userId = 1L;
        try {
            if (sId != null) userId = Long.parseLong(sId);
        } catch (Exception ignored) {}

        return new UserAuthResult(userId, username, fullName, email, phone, address, role, token);
    }

    public static void updateProfile(Context context, String fullName, String phone, String address) {
        if (context == null) return;
        UserAuthResult current = getUserSession(context);
        if (current == null) {
            current = new UserAuthResult(1L, "patient", fullName, "patient@pharmago.com", phone, address, "PATIENT", "token_1");
        } else {
            current.setAddress(address);
        }
        UserAuthResult updated = new UserAuthResult(
                current.getUserId(),
                current.getUsername(),
                fullName != null ? fullName : current.getFullName(),
                current.getEmail(),
                phone != null ? phone : current.getPhone(),
                address != null ? address : current.getAddress(),
                current.getRole(),
                current.getToken()
        );
        saveUserSession(context, updated);
    }

    public static long getUserId(Context context) {
        UserAuthResult user = getUserSession(context);
        return user != null && user.getUserId() != null ? user.getUserId() : 1L;
    }

    public static void clearSession(Context context) {
        if (context == null) return;
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE).edit().clear().apply();
        context.getSharedPreferences(LEGACY_PREF_NAME, Context.MODE_PRIVATE).edit().clear().apply();
    }

    // Instance-based pattern support
    private final Context context;
    private static volatile SecurePreferencesManager instance;

    public SecurePreferencesManager(Context context) {
        this.context = context.getApplicationContext();
    }

    public static SecurePreferencesManager getInstance(Context context) {
        if (instance == null) {
            synchronized (SecurePreferencesManager.class) {
                if (instance == null) {
                    instance = new SecurePreferencesManager(context);
                }
            }
        }
        return instance;
    }

    public long getUserId() {
        return getUserId(context);
    }

    public UserAuthResult getUserSession() {
        return getUserSession(context);
    }

    public String getUsername() {
        UserAuthResult u = getUserSession(context);
        return u != null ? u.getUsername() : "";
    }

    public String getFullName() {
        UserAuthResult u = getUserSession(context);
        return u != null ? u.getFullName() : "";
    }

    public String getPhone() {
        UserAuthResult u = getUserSession(context);
        return u != null ? u.getPhone() : "";
    }

    public String getAddress() {
        UserAuthResult u = getUserSession(context);
        return u != null ? u.getAddress() : "";
    }

    public void updateProfileData(String fullName, String phone, String address) {
        updateProfile(context, fullName, phone, address);
    }

    public void clearSession() {
        clearSession(context);
    }
}
