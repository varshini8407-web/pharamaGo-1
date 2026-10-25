package com.example.pharmago.views;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.LinearInterpolator;

import androidx.annotation.Nullable;

public class DeliveryMapView extends View {

    private Paint backgroundPaint;
    private Paint blockPaint;
    private Paint waterPaint;
    private Paint roadBasePaint;
    private Paint routeCasingPaint;
    private Paint routePaint;
    private Paint markerPaint;
    private Paint homeCirclePaint;
    private Paint textPaint;

    private Path routePath;
    private float scooterProgress = 0.65f; // 65% of the way to home
    private ValueAnimator animator;

    public DeliveryMapView(Context context) {
        super(context);
        init();
    }

    public DeliveryMapView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public DeliveryMapView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        backgroundPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        backgroundPaint.setColor(Color.parseColor("#F1F5F9"));

        blockPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        blockPaint.setColor(Color.parseColor("#E2E8F0"));

        waterPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        waterPaint.setColor(Color.parseColor("#E0F2FE"));

        roadBasePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        roadBasePaint.setColor(Color.WHITE);
        roadBasePaint.setStyle(Paint.Style.STROKE);
        roadBasePaint.setStrokeWidth(18f);

        routeCasingPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        routeCasingPaint.setColor(Color.WHITE);
        routeCasingPaint.setStyle(Paint.Style.STROKE);
        routeCasingPaint.setStrokeWidth(16f);
        routeCasingPaint.setStrokeCap(Paint.Cap.ROUND);
        routeCasingPaint.setStrokeJoin(Paint.Join.ROUND);

        routePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        routePaint.setColor(Color.parseColor("#2563EB")); // Vibrant Google Maps blue
        routePaint.setStyle(Paint.Style.STROKE);
        routePaint.setStrokeWidth(10f);
        routePaint.setStrokeCap(Paint.Cap.ROUND);
        routePaint.setStrokeJoin(Paint.Join.ROUND);

        markerPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

        homeCirclePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        homeCirclePaint.setColor(Color.parseColor("#34D399"));
        homeCirclePaint.setStyle(Paint.Style.FILL);
        homeCirclePaint.setAlpha(60);

        textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        textPaint.setTextSize(26f);
        textPaint.setColor(Color.parseColor("#64748B"));
        textPaint.setFakeBoldText(true);
    }

    public void animateScooterProgress(float targetProgress) {
        if (animator != null && animator.isRunning()) {
            animator.cancel();
        }
        animator = ValueAnimator.ofFloat(scooterProgress, targetProgress);
        animator.setDuration(1200);
        animator.setInterpolator(new LinearInterpolator());
        animator.addUpdateListener(animation -> {
            scooterProgress = (float) animation.getAnimatedValue();
            invalidate();
        });
        animator.start();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        int w = getWidth();
        int h = getHeight();
        if (w == 0 || h == 0) return;

        // 1. Draw Map Base
        canvas.drawRect(0, 0, w, h, backgroundPaint);

        // 2. Draw Decorative City Blocks / Parks
        canvas.drawRoundRect(new RectF(w * 0.05f, h * 0.08f, w * 0.35f, h * 0.28f), 16, 16, blockPaint);
        canvas.drawRoundRect(new RectF(w * 0.42f, h * 0.05f, w * 0.88f, h * 0.22f), 16, 16, blockPaint);
        canvas.drawRoundRect(new RectF(w * 0.65f, h * 0.28f, w * 0.95f, h * 0.55f), 16, 16, waterPaint); // Water body
        canvas.drawRoundRect(new RectF(w * 0.05f, h * 0.68f, w * 0.40f, h * 0.92f), 16, 16, blockPaint);
        canvas.drawRoundRect(new RectF(w * 0.48f, h * 0.65f, w * 0.92f, h * 0.88f), 16, 16, blockPaint);

        // 3. Draw Background Secondary Roads
        canvas.drawLine(0, h * 0.32f, w, h * 0.32f, roadBasePaint);
        canvas.drawLine(0, h * 0.62f, w, h * 0.62f, roadBasePaint);
        canvas.drawLine(w * 0.38f, 0, w * 0.38f, h, roadBasePaint);
        canvas.drawLine(w * 0.72f, 0, w * 0.72f, h, roadBasePaint);

        // Road Names
        canvas.drawText("Whitefield Main Rd", w * 0.12f, h * 0.30f, textPaint);
        canvas.drawText("Apollo Health Ave", w * 0.48f, h * 0.60f, textPaint);

        // 4. Construct Delivery Route Path (Matches layout in Reference Image 1)
        // From Pharmacy (Top Right) to Home (Bottom Left)
        float startX = w * 0.85f;
        float startY = h * 0.48f;

        float p1X = w * 0.68f;
        float p1Y = h * 0.46f;

        float p2X = w * 0.52f;
        float p2Y = h * 0.42f;

        float p3X = w * 0.44f;
        float p3Y = h * 0.52f;

        float p4X = w * 0.32f;
        float p4Y = h * 0.62f;

        float homeX = w * 0.20f;
        float homeY = h * 0.56f;

        routePath = new Path();
        routePath.moveTo(startX, startY);
        routePath.lineTo(p1X, p1Y);
        routePath.lineTo(p2X, p2Y);
        routePath.lineTo(p3X, p3Y);
        routePath.lineTo(p4X, p4Y);
        routePath.lineTo(homeX, homeY);

        // Draw Route (Casing + Blue Polyline)
        canvas.drawPath(routePath, routeCasingPaint);
        canvas.drawPath(routePath, routePaint);

        // 5. Draw Home Destination (Green Radius + House Pin)
        canvas.drawCircle(homeX, homeY, 32f, homeCirclePaint);

        markerPaint.setColor(Color.parseColor("#0F172A"));
        canvas.drawCircle(homeX, homeY, 18f, markerPaint);

        // Home Icon inside pin
        markerPaint.setColor(Color.WHITE);
        Path homeIcon = new Path();
        homeIcon.moveTo(homeX - 10f, homeY);
        homeIcon.lineTo(homeX, homeY - 10f);
        homeIcon.lineTo(homeX + 10f, homeY);
        homeIcon.lineTo(homeX + 7f, homeY);
        homeIcon.lineTo(homeX + 7f, homeY + 8f);
        homeIcon.lineTo(homeX - 7f, homeY + 8f);
        homeIcon.lineTo(homeX - 7f, homeY);
        homeIcon.close();
        canvas.drawPath(homeIcon, markerPaint);

        // 6. Draw Pharmacy Origin Pin (Top Right)
        markerPaint.setColor(Color.parseColor("#0F56B3"));
        canvas.drawCircle(startX, startY, 14f, markerPaint);
        markerPaint.setColor(Color.WHITE);
        canvas.drawCircle(startX, startY, 6f, markerPaint);

        // 7. Calculate Scooter Position based on scooterProgress (0.0 to 1.0)
        // Interpolate along the points
        float curX, curY;
        if (scooterProgress < 0.3f) {
            float t = scooterProgress / 0.3f;
            curX = startX + t * (p1X - startX);
            curY = startY + t * (p1Y - startY);
        } else if (scooterProgress < 0.6f) {
            float t = (scooterProgress - 0.3f) / 0.3f;
            curX = p1X + t * (p2X - p1X);
            curY = p1Y + t * (p2Y - p1Y);
        } else if (scooterProgress < 0.85f) {
            float t = (scooterProgress - 0.6f) / 0.25f;
            curX = p2X + t * (p3X - p2X);
            curY = p2Y + t * (p3Y - p2Y);
        } else {
            float t = (scooterProgress - 0.85f) / 0.15f;
            curX = p3X + t * (homeX - p3X);
            curY = p3Y + t * (homeY - p3Y);
        }

        // Draw Scooter Marker (Red Delivery Box & Scooter Badge matching Image 1)
        markerPaint.setColor(Color.parseColor("#DC2626")); // Red courier box
        canvas.drawRoundRect(new RectF(curX - 18f, curY - 12f, curX + 18f, curY + 12f), 8f, 8f, markerPaint);

        markerPaint.setColor(Color.WHITE);
        canvas.drawCircle(curX - 6f, curY, 5f, markerPaint);
        canvas.drawCircle(curX + 6f, curY, 5f, markerPaint);

        // Pulsing radar around the scooter
        markerPaint.setStyle(Paint.Style.STROKE);
        markerPaint.setColor(Color.parseColor("#DC2626"));
        markerPaint.setStrokeWidth(3f);
        canvas.drawCircle(curX, curY, 26f, markerPaint);
        markerPaint.setStyle(Paint.Style.FILL);

        // 8. Draw Google Watermark (Bottom Left)
        Paint googlePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        googlePaint.setColor(Color.parseColor("#94A3B8"));
        googlePaint.setTextSize(30f);
        googlePaint.setFakeBoldText(true);
        canvas.drawText("Google", 28f, h - 28f, googlePaint);
    }
}
