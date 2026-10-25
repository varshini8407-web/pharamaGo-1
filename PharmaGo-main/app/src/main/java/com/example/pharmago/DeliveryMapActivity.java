package com.example.pharmago;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.pharmago.models.Delivery;
import com.example.pharmago.models.DeliveryOrderModel;
import com.example.pharmago.models.OrderStatus;
import com.example.pharmago.services.DeliveryService;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.Locale;

/**
 * Member 3: Turn-by-Turn Routing & Maps Integration (DeliveryMapActivity)
 * Displays Google Maps (SupportMapFragment) with Pharmacy & Patient markers,
 * route polyline bounds, turn-by-turn navigation via explicit Google Maps intent,
 * customer direct dial, and doorstep arrival OTP verification.
 */
public class DeliveryMapActivity extends AppCompatActivity implements OnMapReadyCallback {

    private static final String TAG = "DeliveryMapActivity";
    public static final String EXTRA_DELIVERY_ORDER = "EXTRA_DELIVERY_ORDER";

    private MaterialCardView btnBackFromMap;
    private TextView tvMapOrderIdTitle;
    private TextView tvMapEtaSubtitle;
    private MaterialCardView btnOpenExternalGoogleMaps;
    private FloatingActionButton fabCallCustomerFromMap;

    private TextView tvMapCustomerName;
    private TextView tvMapCustomerPhone;
    private TextView tvMapPaymentBadge;
    private TextView tvMapDeliveryAddress;
    private MaterialButton btnArrivedAtCustomerDoorstep;
    private MaterialCardView cardCourierPositionMarker;

    private DeliveryOrderModel currentOrder;
    private ValueAnimator markerPulseAnimator;
    private GoogleMap googleMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_delivery_map);

        initViews();
        extractOrderData();
        populateOrderData();
        setupListeners();
        startCourierPositionAnimation();
        initGoogleMapFragment();
    }

    private void initViews() {
        btnBackFromMap = findViewById(R.id.btnBackFromMap);
        tvMapOrderIdTitle = findViewById(R.id.tvMapOrderIdTitle);
        tvMapEtaSubtitle = findViewById(R.id.tvMapEtaSubtitle);
        btnOpenExternalGoogleMaps = findViewById(R.id.btnOpenExternalGoogleMaps);
        fabCallCustomerFromMap = findViewById(R.id.fabCallCustomerFromMap);

        tvMapCustomerName = findViewById(R.id.tvMapCustomerName);
        tvMapCustomerPhone = findViewById(R.id.tvMapCustomerPhone);
        tvMapPaymentBadge = findViewById(R.id.tvMapPaymentBadge);
        tvMapDeliveryAddress = findViewById(R.id.tvMapDeliveryAddress);
        btnArrivedAtCustomerDoorstep = findViewById(R.id.btnArrivedAtCustomerDoorstep);
        cardCourierPositionMarker = findViewById(R.id.cardCourierPositionMarker);
    }

    private void initGoogleMapFragment() {
        try {
            SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.mapContainer);
            if (mapFragment == null) {
                mapFragment = SupportMapFragment.newInstance();
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.mapContainer, mapFragment)
                        .commitAllowingStateLoss();
            }
            mapFragment.getMapAsync(this);
        } catch (Exception e) {
            Log.w(TAG, "Google Maps fragment initialization note: " + e.getMessage());
        }
    }

    @Override
    public void onMapReady(@NonNull GoogleMap map) {
        this.googleMap = map;
        try {
            googleMap.getUiSettings().setZoomControlsEnabled(true);
            googleMap.getUiSettings().setCompassEnabled(true);
            googleMap.getUiSettings().setMyLocationButtonEnabled(true);

            double pharmLat = (currentOrder instanceof Delivery) ? ((Delivery) currentOrder).getPharmacyLat() : 12.9716;
            double pharmLng = (currentOrder instanceof Delivery) ? ((Delivery) currentOrder).getPharmacyLng() : 77.5946;
            double patLat = (currentOrder.getLatitude() != null && currentOrder.getLatitude() != 0.0) ? currentOrder.getLatitude() : 12.9815;
            double patLng = (currentOrder.getLongitude() != null && currentOrder.getLongitude() != 0.0) ? currentOrder.getLongitude() : 77.6050;

            LatLng pharmacyPos = new LatLng(pharmLat, pharmLng);
            LatLng patientPos = new LatLng(patLat, patLng);

            // Pickup Marker
            googleMap.addMarker(new MarkerOptions()
                    .position(pharmacyPos)
                    .title("Pickup: " + (currentOrder.getPharmacyAddress() != null ? currentOrder.getPharmacyAddress() : "Hospital Pharmacy"))
                    .snippet("Dispatch Counter"));

            // Dropoff Marker
            googleMap.addMarker(new MarkerOptions()
                    .position(patientPos)
                    .title("Dropoff: " + currentOrder.getPatientName())
                    .snippet(currentOrder.getDeliveryAddress()));

            // Route Polyline
            googleMap.addPolyline(new PolylineOptions()
                    .add(pharmacyPos, patientPos)
                    .width(10f)
                    .color(0xFF0F56B3)
                    .geodesic(true));

            LatLngBounds bounds = new LatLngBounds.Builder()
                    .include(pharmacyPos)
                    .include(patientPos)
                    .build();

            googleMap.setOnMapLoadedCallback(() -> {
                try {
                    googleMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, 180));
                } catch (Exception e) {
                    googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(patientPos, 14f));
                }
            });
        } catch (Exception e) {
            Log.w(TAG, "onMapReady setup note: " + e.getMessage());
        }
    }

    private void extractOrderData() {
        if (getIntent() != null && getIntent().hasExtra(EXTRA_DELIVERY_ORDER)) {
            currentOrder = (DeliveryOrderModel) getIntent().getSerializableExtra(EXTRA_DELIVERY_ORDER);
        }

        // Fallback default if launched directly for testing
        if (currentOrder == null) {
            currentOrder = new Delivery(
                    8093L,
                    "Priya Patel",
                    "+91 98112 34567",
                    "Flat 402, Lotus Orchid, Sector 4, Metro City",
                    "Apollo Central Pharmacy, Block B",
                    110.0,
                    "CASH_ON_DELIVERY",
                    OrderStatus.OUT_FOR_DELIVERY,
                    "549123",
                    12.9815,
                    77.6050,
                    "Today, 02:45 PM",
                    "Paracetamol 500mg, Amoxicillin 250mg"
            );
        }
    }

    private void populateOrderData() {
        if (currentOrder == null) return;

        tvMapOrderIdTitle.setText(String.format(Locale.getDefault(), "Navigating: Order #%d", currentOrder.getOrderId()));

        double distance = 2.4;
        if (currentOrder instanceof Delivery) {
            distance = ((Delivery) currentOrder).getDistanceKm();
        } else if (currentOrder.getLatitude() != null && currentOrder.getLongitude() != null) {
            distance = Delivery.calculateDistanceKm(12.9716, 77.5946, currentOrder.getLatitude(), currentOrder.getLongitude());
        }
        tvMapEtaSubtitle.setText(String.format(Locale.getDefault(), "Estimated Delivery: ~10 mins • %.1f km", distance));

        tvMapCustomerName.setText(currentOrder.getPatientName());
        tvMapCustomerPhone.setText(currentOrder.getPatientPhone());
        tvMapDeliveryAddress.setText(currentOrder.getDeliveryAddress());

        if (currentOrder.isCashOnDelivery()) {
            tvMapPaymentBadge.setText(String.format(Locale.getDefault(), "💵 CASH ON DELIVERY: ₹%.2f", currentOrder.getTotalAmount()));
            tvMapPaymentBadge.setBackgroundTintList(ContextCompat.getColorStateList(this, R.color.warning_container));
            tvMapPaymentBadge.setTextColor(ContextCompat.getColor(this, R.color.on_warning_container));
        } else {
            tvMapPaymentBadge.setText("✓ PAID ONLINE (UPI)");
            tvMapPaymentBadge.setBackgroundTintList(ContextCompat.getColorStateList(this, R.color.secondary_container));
            tvMapPaymentBadge.setTextColor(ContextCompat.getColor(this, R.color.on_secondary_container));
        }
    }

    private void setupListeners() {
        btnBackFromMap.setOnClickListener(v -> finish());

        // Quick Direct Dial Customer Phone
        fabCallCustomerFromMap.setOnClickListener(v -> {
            if (currentOrder != null && currentOrder.getPatientPhone() != null) {
                Intent dialIntent = new Intent(Intent.ACTION_DIAL);
                dialIntent.setData(Uri.parse("tel:" + currentOrder.getPatientPhone().trim()));
                startActivity(dialIntent);
            } else {
                Toast.makeText(this, "Customer contact number unavailable", Toast.LENGTH_SHORT).show();
            }
        });

        // Launch External Google Maps Turn-by-Turn Navigation
        btnOpenExternalGoogleMaps.setOnClickListener(v -> launchGoogleMapsNavigation());

        // Arrived at Doorstep -> Open 6-digit OTP verification BottomSheet
        btnArrivedAtCustomerDoorstep.setOnClickListener(v -> openOtpVerificationDialog());
    }

    private void launchGoogleMapsNavigation() {
        if (currentOrder == null) return;

        try {
            double lat = currentOrder.getLatitude() != null ? currentOrder.getLatitude() : 12.9815;
            double lng = currentOrder.getLongitude() != null ? currentOrder.getLongitude() : 77.6050;

            // Explicit turn-by-turn navigation uri: google.navigation:q=lat,lng
            Uri gmmIntentUri = Uri.parse(String.format(Locale.US, "google.navigation:q=%f,%f", lat, lng));
            Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
            mapIntent.setPackage("com.google.android.apps.maps");

            if (mapIntent.resolveActivity(getPackageManager()) != null) {
                startActivity(mapIntent);
            } else {
                // Fallback to geo URI or web maps
                Uri fallbackUri = Uri.parse(String.format(Locale.US, "geo:%f,%f?q=%s",
                        lat, lng, Uri.encode(currentOrder.getDeliveryAddress())));
                Intent fallbackIntent = new Intent(Intent.ACTION_VIEW, fallbackUri);
                startActivity(fallbackIntent);
            }
        } catch (Exception e) {
            Toast.makeText(this, "Opening navigation for: " + currentOrder.getDeliveryAddress(), Toast.LENGTH_SHORT).show();
        }
    }

    private void openOtpVerificationDialog() {
        if (currentOrder == null) return;

        OTPVerificationBottomSheet bottomSheet = OTPVerificationBottomSheet.newInstance(currentOrder);
        bottomSheet.setOnOtpVerificationListener(verifiedOrder -> {
            // Order successfully delivered and updated in DB
            setResult(RESULT_OK);
            finish();
        });
        bottomSheet.show(getSupportFragmentManager(), "OTPVerificationBottomSheet");
    }

    private void startCourierPositionAnimation() {
        if (cardCourierPositionMarker == null) return;

        markerPulseAnimator = ObjectAnimator.ofFloat(cardCourierPositionMarker, "scaleX", 1.0f, 1.15f, 1.0f);
        markerPulseAnimator.setDuration(1600);
        markerPulseAnimator.setRepeatCount(ValueAnimator.INFINITE);
        markerPulseAnimator.setRepeatMode(ValueAnimator.REVERSE);

        ObjectAnimator scaleYAnimator = ObjectAnimator.ofFloat(cardCourierPositionMarker, "scaleY", 1.0f, 1.15f, 1.0f);
        scaleYAnimator.setDuration(1600);
        scaleYAnimator.setRepeatCount(ValueAnimator.INFINITE);
        scaleYAnimator.setRepeatMode(ValueAnimator.REVERSE);

        markerPulseAnimator.start();
        scaleYAnimator.start();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (markerPulseAnimator != null && markerPulseAnimator.isRunning()) {
            markerPulseAnimator.cancel();
        }
    }
}
