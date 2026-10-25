package com.example.pharmago;

import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.pharmago.models.DeliveryOrderModel;
import com.example.pharmago.services.DeliveryService;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Member 3: Secure 6-Digit OTP Delivery Verification BottomSheet
 * Interactively prompts delivery agent to enter the 6-digit OTP from patient,
 * validates against DatabaseConnection/DeliveryService asynchronously,
 * triggers notifications, haptics, and status transitions to DELIVERED.
 */
public class OTPVerificationBottomSheet extends BottomSheetDialogFragment {

    public interface OnOtpVerificationListener {
        void onVerificationSuccess(DeliveryOrderModel order);
    }

    private static final String ARG_ORDER = "arg_order";
    private DeliveryOrderModel order;
    private OnOtpVerificationListener listener;

    private TextView tvOtpSheetOrderId;
    private MaterialCardView cardCodAlertBanner;
    private TextView tvCodAmountText;
    private TextInputLayout tilDeliveryOtp;
    private TextInputEditText etDeliveryOtpInput;
    private TextView tvTestOtpHint;
    private MaterialButton btnCancel;
    private MaterialButton btnConfirm;

    private final ExecutorService executorService = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    public static OTPVerificationBottomSheet newInstance(DeliveryOrderModel order) {
        OTPVerificationBottomSheet fragment = new OTPVerificationBottomSheet();
        Bundle args = new Bundle();
        args.putSerializable(ARG_ORDER, order);
        fragment.setArguments(args);
        return fragment;
    }

    public void setOnOtpVerificationListener(OnOtpVerificationListener listener) {
        this.listener = listener;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            order = (DeliveryOrderModel) getArguments().getSerializable(ARG_ORDER);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.dialog_otp_verification, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        tvOtpSheetOrderId = view.findViewById(R.id.tvOtpSheetOrderId);
        cardCodAlertBanner = view.findViewById(R.id.cardCodAlertBanner);
        tvCodAmountText = view.findViewById(R.id.tvCodAmountText);
        tilDeliveryOtp = view.findViewById(R.id.tilDeliveryOtp);
        etDeliveryOtpInput = view.findViewById(R.id.etDeliveryOtpInput);
        tvTestOtpHint = view.findViewById(R.id.tvTestOtpHint);
        btnCancel = view.findViewById(R.id.btnCancelOtpVerification);
        btnConfirm = view.findViewById(R.id.btnConfirmDeliveryOtp);

        if (order != null) {
            tvOtpSheetOrderId.setText(String.format(Locale.getDefault(), "Order #%d • Patient: %s",
                    order.getOrderId(), order.getPatientName() != null ? order.getPatientName() : "Customer"));

            if (order.isCashOnDelivery()) {
                cardCodAlertBanner.setVisibility(View.VISIBLE);
                tvCodAmountText.setText(String.format(Locale.getDefault(),
                        "Cash on Delivery: Please collect ₹%.2f from customer before confirming.", order.getTotalAmount()));
            } else {
                cardCodAlertBanner.setVisibility(View.GONE);
            }

            if (order.getOtpCode() != null && !order.getOtpCode().isEmpty()) {
                tvTestOtpHint.setText("Customer OTP: " + order.getOtpCode());
            } else {
                tvTestOtpHint.setText("Customer OTP: 549123");
            }
        }

        btnCancel.setOnClickListener(v -> dismiss());

        etDeliveryOtpInput.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                tilDeliveryOtp.setError(null);
            }
            @Override public void afterTextChanged(Editable s) {}
        });

        btnConfirm.setOnClickListener(v -> handleOtpSubmission());
    }

    private void handleOtpSubmission() {
        if (order == null) {
            dismiss();
            return;
        }

        String inputOtp = etDeliveryOtpInput.getText() != null ? etDeliveryOtpInput.getText().toString().trim() : "";
        if (inputOtp.length() != 6) {
            tilDeliveryOtp.setError("Please enter the complete 6-digit OTP");
            return;
        }

        btnConfirm.setEnabled(false);
        btnConfirm.setText("Verifying...");

        Context context = getContext();
        if (context == null) return;

        com.example.pharmago.services.BackendService.getInstance(context).verifyAndCompleteDelivery(
                order.getOrderId(),
                inputOtp,
                new com.example.pharmago.database.DbCallback<Boolean>() {
                    @Override
                    public void onSuccess(Boolean success) {
                        if (!isAdded()) return;
                        btnConfirm.setEnabled(true);
                        btnConfirm.setText("Confirm Delivery");

                        order.setOrderStatus(com.example.pharmago.models.OrderStatus.DELIVERED);
                        if (order.isCashOnDelivery()) {
                            order.setPaymentStatus("PAID (Cash Collected)");
                        }

                        triggerHapticSuccess();
                        Toast.makeText(requireContext(), "🎉 Order #" + order.getOrderId() + " delivered successfully via MySQL!", Toast.LENGTH_LONG).show();
                        if (listener != null) {
                            listener.onVerificationSuccess(order);
                        }
                        dismiss();
                    }

                    @Override
                    public void onFailure(Exception e) {
                        if (!isAdded()) return;
                        btnConfirm.setEnabled(true);
                        btnConfirm.setText("Confirm Delivery");
                        tilDeliveryOtp.setError("Invalid OTP. Please check the 6-digit code received by the customer.");
                    }
                }
        );
    }

    private void triggerHapticSuccess() {
        try {
            Context ctx = getContext();
            if (ctx == null) return;
            Vibrator vibrator = (Vibrator) ctx.getSystemService(Context.VIBRATOR_SERVICE);
            if (vibrator != null && vibrator.hasVibrator()) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    vibrator.vibrate(VibrationEffect.createOneShot(150, VibrationEffect.DEFAULT_AMPLITUDE));
                } else {
                    vibrator.vibrate(150);
                }
            }
        } catch (Exception ignored) {}
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        executorService.shutdown();
    }
}
