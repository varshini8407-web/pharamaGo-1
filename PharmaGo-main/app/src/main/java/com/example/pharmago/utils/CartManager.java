package com.example.pharmago.utils;

import android.content.Context;
import com.example.pharmago.models.Medicine;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Singleton Cart Manager with Single-Pharmacy Enforcement and Rx Requirement Tracking.
 */
public class CartManager {
    private static CartManager instance;

    private Integer currentPharmacyId = null;
    private String currentPharmacyName = "Apollo Central Pharmacy";
    private final List<Medicine> cartItems = new ArrayList<>();
    private final Map<Long, Integer> quantities = new HashMap<>();
    private boolean requiresPrescription = false;

    public interface OnCartActionCallback {
        void onCartUpdated(int totalItems, double totalAmount);
    }

    public interface CartCallback {
        void onSuccess();
        void onFailure(Exception e);
    }

    public static synchronized CartManager getInstance() {
        if (instance == null) {
            instance = new CartManager();
        }
        return instance;
    }

    private CartManager() {}

    /**
     * Requirement Part 1.2: Single-Pharmacy Cart Enforcement
     * In addToCart(Medicine item, int pharmacyId):
     * - If currentPharmacyId == null || currentPharmacyId == pharmacyId, add the item and set currentPharmacyId = pharmacyId.
     * - If currentPharmacyId != pharmacyId, fire an alert dialog to confirm replacement.
     */
    public boolean addToCart(Medicine item, int pharmacyId) {
        if (item == null) return false;

        if (currentPharmacyId == null || currentPharmacyId == pharmacyId) {
            addItemInternal(item, pharmacyId, null);
            return true;
        }
        return false;
    }

    /**
     * Context-aware overload that prompts the user if there is a pharmacy conflict.
     */
    public void addToCart(Context context, Medicine item, int pharmacyId, String pharmacyName, OnCartActionCallback callback) {
        if (item == null) return;

        if (currentPharmacyId == null || currentPharmacyId == pharmacyId) {
            addItemInternal(item, pharmacyId, pharmacyName);
            persistCartItemAsync(context, item, pharmacyId, pharmacyName);
            if (callback != null) {
                callback.onCartUpdated(getItemCount(), getTotalAmount());
            }
        } else {
            // Swiggy / Zomato Multi-Store Cart Conflict Dialog
            if (context != null) {
                String existingStore = (currentPharmacyName != null && !currentPharmacyName.isEmpty()) ? currentPharmacyName : "Pharmacy #" + currentPharmacyId;
                String incomingStore = (pharmacyName != null && !pharmacyName.isEmpty()) ? pharmacyName : "Pharmacy #" + pharmacyId;

                new MaterialAlertDialogBuilder(context)
                        .setTitle("Replace cart items?")
                        .setMessage("Replace cart items? Your cart contains medicines from " + existingStore + ". Would you like to discard it and add items from " + incomingStore + "?")
                        .setPositiveButton("Discard & Add", (dialog, which) -> {
                            flushAndReplaceCartAsync(context, pharmacyId, pharmacyName, item);
                            clearCart();
                            addItemInternal(item, pharmacyId, pharmacyName);
                            if (callback != null) {
                                callback.onCartUpdated(getItemCount(), getTotalAmount());
                            }
                        })
                        .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                        .show();
            }
        }
    }

    /**
     * Requirement Phase 4: Single-store constraint with CartCallback
     */
    public void addItem(Context context, Medicine item, int pharmacyId, CartCallback callback) {
        if (item == null) {
            if (callback != null) callback.onFailure(new IllegalArgumentException("Medicine item cannot be null"));
            return;
        }

        if (currentPharmacyId == null || currentPharmacyId.equals(pharmacyId)) {
            addItemInternal(item, pharmacyId, currentPharmacyName);
            persistCartItemAsync(context, item, pharmacyId, currentPharmacyName);
            if (callback != null) callback.onSuccess();
        } else {
            if (context != null) {
                new MaterialAlertDialogBuilder(context)
                        .setTitle("Replace cart items?")
                        .setMessage("Replace cart items? Your cart contains medicines from another store. Clear cart to continue?")
                        .setPositiveButton("Clear Cart", (dialog, which) -> {
                            flushAndReplaceCartAsync(context, pharmacyId, "Pharmacy #" + pharmacyId, item);
                            clearCart();
                            addItemInternal(item, pharmacyId, "Pharmacy #" + pharmacyId);
                            if (callback != null) callback.onSuccess();
                        })
                        .setNegativeButton("Cancel", (dialog, which) -> {
                            dialog.dismiss();
                            if (callback != null) callback.onFailure(new Exception("Cart replacement cancelled by user"));
                        })
                        .show();
            } else {
                if (callback != null) callback.onFailure(new IllegalStateException("Context is null"));
            }
        }
    }

    public void addItem(Context context, com.example.pharmago.models.MedicineItem item, int pharmacyId, CartCallback callback) {
        addItem(context, (Medicine) item, pharmacyId, callback);
    }

    private void persistCartItemAsync(Context context, Medicine item, int pharmacyId, String pharmacyName) {
        if (context == null || item == null) return;
        new Thread(() -> {
            try {
                long userId = SecurePreferencesManager.getUserId(context);
                long mId = item.getId() != null ? item.getId() : (long) item.getName().hashCode();
                double price = 45.0;
                try {
                    if (item.getPrice() != null) price = Double.parseDouble(item.getPrice().replaceAll("[^0-9.]", ""));
                } catch (Exception ignored) {}
                com.example.pharmago.database.DatabaseConnection.getInstance(context)
                        .syncActiveCartItem(userId, pharmacyId, pharmacyName, mId, item.getName(), 1, price);
            } catch (Exception ignored) {}
        }).start();
    }

    private void flushAndReplaceCartAsync(Context context, int newPharmacyId, String newPharmacyName, Medicine newItem) {
        if (context == null) return;
        new Thread(() -> {
            try {
                long userId = SecurePreferencesManager.getUserId(context);
                com.example.pharmago.database.DatabaseConnection.getInstance(context)
                        .flushActiveCart(userId, newPharmacyId, newPharmacyName);
                if (newItem != null) {
                    long mId = newItem.getId() != null ? newItem.getId() : (long) newItem.getName().hashCode();
                    double price = 45.0;
                    try {
                        if (newItem.getPrice() != null) price = Double.parseDouble(newItem.getPrice().replaceAll("[^0-9.]", ""));
                    } catch (Exception ignored) {}
                    com.example.pharmago.database.DatabaseConnection.getInstance(context)
                            .syncActiveCartItem(userId, newPharmacyId, newPharmacyName, mId, newItem.getName(), 1, price);
                }
            } catch (Exception ignored) {}
        }).start();
    }

    private synchronized void addItemInternal(Medicine item, int pharmacyId, String pharmacyName) {
        this.currentPharmacyId = pharmacyId;
        if (pharmacyName != null && !pharmacyName.isEmpty()) {
            this.currentPharmacyName = pharmacyName;
        }

        long mId = item.getId() != null ? item.getId() : (long) item.getName().hashCode();
        if (quantities.containsKey(mId)) {
            quantities.put(mId, quantities.get(mId) + 1);
        } else {
            quantities.put(mId, 1);
            cartItems.add(item);
        }

        // Requirement Part 1.3: If an "Rx Required" medicine is added to cart, flag cart as requires_prescription = true
        if (item.isRequiresPrescription()) {
            this.requiresPrescription = true;
        }
    }

    public synchronized void removeItem(Medicine item) {
        if (item == null) return;
        long mId = item.getId() != null ? item.getId() : (long) item.getName().hashCode();
        if (quantities.containsKey(mId)) {
            int q = quantities.get(mId);
            if (q > 1) {
                quantities.put(mId, q - 1);
            } else {
                quantities.remove(mId);
                cartItems.remove(item);
            }
        }
        recalculateRxRequirement();
        if (cartItems.isEmpty()) {
            currentPharmacyId = null;
        }
    }

    public synchronized void clearCart() {
        cartItems.clear();
        quantities.clear();
        currentPharmacyId = null;
        requiresPrescription = false;
    }

    private void recalculateRxRequirement() {
        boolean rx = false;
        for (Medicine m : cartItems) {
            if (m.isRequiresPrescription()) {
                rx = true;
                break;
            }
        }
        this.requiresPrescription = rx;
    }

    public synchronized List<Medicine> getItems() {
        return new ArrayList<>(cartItems);
    }

    public synchronized int getQuantity(Medicine item) {
        if (item == null) return 0;
        long mId = item.getId() != null ? item.getId() : (long) item.getName().hashCode();
        return quantities.containsKey(mId) ? quantities.get(mId) : 0;
    }

    public synchronized int getItemCount() {
        int count = 0;
        for (int q : quantities.values()) {
            count += q;
        }
        return count;
    }

    public synchronized double getTotalAmount() {
        double total = 0.0;
        for (Medicine m : cartItems) {
            long mId = m.getId() != null ? m.getId() : (long) m.getName().hashCode();
            int qty = quantities.containsKey(mId) ? quantities.get(mId) : 1;
            double p = 45.0;
            try {
                if (m.getPrice() != null) {
                    p = Double.parseDouble(m.getPrice().replaceAll("[^0-9.]", ""));
                }
            } catch (Exception ignored) {}
            total += (p * qty);
        }
        return total;
    }

    public Integer getCurrentPharmacyId() {
        return currentPharmacyId;
    }

    public String getCurrentPharmacyName() {
        return currentPharmacyName != null ? currentPharmacyName : "Apollo Metro Pharmacy";
    }

    public boolean isRequiresPrescription() {
        return requiresPrescription;
    }

    public void setRequiresPrescription(boolean requiresPrescription) {
        this.requiresPrescription = requiresPrescription;
    }
}
