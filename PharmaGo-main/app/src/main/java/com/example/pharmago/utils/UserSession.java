package com.example.pharmago.utils;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Requirement 2.1: UserSession Singleton
 * Manages active user authentication session using SharedPreferences ("UserSessionPrefs").
 * Provides instant role-based access control and persistent profile synchronization.
 */
public class UserSession {

    private static final String PREF_NAME = "UserSessionPrefs";
    private static final String KEY_USER_ID = "user_id";
    private static final String KEY_USERNAME = "username";
    private static final String KEY_ROLE = "role";
    private static final String KEY_FULL_NAME = "full_name";
    private static final String KEY_PHONE = "phone";
    private static final String KEY_ADDRESS = "address";
    private static final String KEY_IS_LOGGED_IN = "is_logged_in";

    private static volatile UserSession instance;
    private final SharedPreferences prefs;

    protected UserSession(Context context) {
        this.prefs = context.getApplicationContext().getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public static synchronized UserSession getInstance(Context context) {
        if (instance == null) {
            instance = new UserSession(context);
        }
        return instance;
    }

    public synchronized void saveUser(int id, String username, String role, String fullName, String phone, String address) {
        saveUser((long) id, username, role, fullName, phone, address);
    }

    public synchronized void saveUser(long id, String username, String role, String fullName, String phone, String address) {
        prefs.edit()
                .putLong(KEY_USER_ID, id)
                .putString(KEY_USERNAME, username != null ? username : "")
                .putString(KEY_ROLE, role != null ? role.toUpperCase() : "PATIENT")
                .putString(KEY_FULL_NAME, fullName != null ? fullName : "")
                .putString(KEY_PHONE, phone != null ? phone : "")
                .putString(KEY_ADDRESS, address != null ? address : "")
                .putBoolean(KEY_IS_LOGGED_IN, true)
                .apply();
    }

    public int getUserId() {
        return (int) prefs.getLong(KEY_USER_ID, 0L);
    }

    public long getUserIdLong() {
        return prefs.getLong(KEY_USER_ID, 0L);
    }

    public String getUsername() {
        return prefs.getString(KEY_USERNAME, "");
    }

    public String getRole() {
        return prefs.getString(KEY_ROLE, "PATIENT");
    }

    public String getFullName() {
        return prefs.getString(KEY_FULL_NAME, "");
    }

    public String getPhone() {
        return prefs.getString(KEY_PHONE, "");
    }

    public String getAddress() {
        return prefs.getString(KEY_ADDRESS, "");
    }

    public boolean isLoggedIn() {
        return prefs.getBoolean(KEY_IS_LOGGED_IN, false) && getUserIdLong() > 0;
    }

    public synchronized void clearSession() {
        prefs.edit().clear().apply();
    }
}
