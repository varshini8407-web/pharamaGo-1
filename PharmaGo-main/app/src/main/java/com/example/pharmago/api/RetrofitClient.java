package com.example.pharmago.api;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
public class RetrofitClient {

    public static boolean isEmulator() {
        return (android.os.Build.BRAND != null && android.os.Build.BRAND.startsWith("generic") && android.os.Build.DEVICE != null && android.os.Build.DEVICE.startsWith("generic"))
                || (android.os.Build.FINGERPRINT != null && (android.os.Build.FINGERPRINT.startsWith("generic") || android.os.Build.FINGERPRINT.startsWith("unknown")))
                || (android.os.Build.HARDWARE != null && (android.os.Build.HARDWARE.contains("goldfish") || android.os.Build.HARDWARE.contains("ranchu")))
                || (android.os.Build.MODEL != null && (android.os.Build.MODEL.contains("google_sdk") || android.os.Build.MODEL.contains("Emulator") || android.os.Build.MODEL.contains("Android SDK built for x86")))
                || (android.os.Build.PRODUCT != null && (android.os.Build.PRODUCT.contains("sdk_google") || android.os.Build.PRODUCT.contains("google_sdk") || android.os.Build.PRODUCT.contains("sdk")));
    }

    public static String getBaseUrl() {
        if (isEmulator()) {
            return "http://10.0.2.2:8081/";
        }
        return "http://10.143.137.47:8081/";
    }

    private static Retrofit retrofit = null;

    public static Retrofit getClient() {
        if (retrofit == null) {
            HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
            interceptor.setLevel(HttpLoggingInterceptor.Level.BODY);

            OkHttpClient client = new OkHttpClient.Builder()
                    .addInterceptor(interceptor)
                    .connectTimeout(30, TimeUnit.SECONDS)
                    .readTimeout(30, TimeUnit.SECONDS)
                    .writeTimeout(30, TimeUnit.SECONDS)
                    .build();

            retrofit = new Retrofit.Builder()
                    .baseUrl(getBaseUrl())
                    .addConverterFactory(GsonConverterFactory.create())
                    .client(client)
                    .build();
        }
        return retrofit;
    }

    public static Retrofit getClientWithAuth(Context context) {
        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
        logging.setLevel(HttpLoggingInterceptor.Level.BODY);

        OkHttpClient client = new OkHttpClient.Builder()
                .addInterceptor(logging)
                .addInterceptor(chain -> {
                    SharedPreferences prefs = context.getSharedPreferences("PharmaGoPrefs", Context.MODE_PRIVATE);
                    String token = prefs.getString("token", null);
                    Request.Builder builder = chain.request().newBuilder();
                    if (token != null) {
                        builder.addHeader("Authorization", "Bearer " + token);
                    }
                    return chain.proceed(builder.build());
                })
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .build();

        return new Retrofit.Builder()
                .baseUrl(getBaseUrl())
                .addConverterFactory(GsonConverterFactory.create())
                .client(client)
                .build();
    }
}