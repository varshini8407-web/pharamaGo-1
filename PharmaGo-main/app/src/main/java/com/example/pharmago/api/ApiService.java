package com.example.pharmago.api;

import com.example.pharmago.models.AuthResponse;
import com.example.pharmago.models.DeliveryResponse;
import com.example.pharmago.models.LoginRequest;
import com.example.pharmago.models.Medicine;
import com.example.pharmago.models.OrderRequest;
import com.example.pharmago.models.OrderResponse;
import com.example.pharmago.models.Pharmacy;
import com.example.pharmago.models.PrescriptionResponse;
import com.example.pharmago.models.RegisterRequest;

import java.util.List;
import java.util.Map;

import okhttp3.MultipartBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface ApiService {
    // Health Check
    @GET("api/health")
    Call<ResponseBody> checkHealth();

    // Authentication
    @POST("api/auth/login")
    Call<AuthResponse> login(@Body LoginRequest request);

    @POST("api/auth/register")
    Call<ResponseBody> register(@Body RegisterRequest request);

    // Pharmacies
    @GET("api/pharmacies")
    Call<List<Pharmacy>> getAllPharmacies();

    @GET("api/pharmacies/{id}")
    Call<Pharmacy> getPharmacyById(@Path("id") Long id);

    // Medicines
    @GET("api/medicines")
    Call<List<Medicine>> getAllMedicines();

    @GET("api/medicines/search")
    Call<List<Medicine>> searchMedicines(@Query("query") String query);

    @GET("api/medicines/{id}")
    Call<Medicine> getMedicineById(@Path("id") Long id);

    // Orders
    @POST("api/orders")
    Call<OrderResponse> placeOrder(@Body OrderRequest request);

    @GET("api/orders/my-orders")
    Call<List<OrderResponse>> getMyOrders();

    @GET("api/orders/{id}")
    Call<OrderResponse> getOrderDetails(@Path("id") Long id);

    // Delivery
    @GET("api/deliveries/order/{orderId}")
    Call<DeliveryResponse> getDeliveryTracking(@Path("orderId") Long orderId);

    // Pickup QR
    @GET("api/pickup/qr/{id}")
    Call<Map<String, String>> getPickupQrCode(@Path("id") Long orderId);

    @POST("api/pickup/validate")
    Call<ResponseBody> validatePickup(@Body Map<String, String> request);

    // Prescription Upload
    @Multipart
    @POST("api/prescriptions/upload")
    Call<PrescriptionResponse> uploadPrescription(@Part MultipartBody.Part file);
}