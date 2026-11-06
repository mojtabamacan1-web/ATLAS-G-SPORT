package com.atlasgsport;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends AppCompatActivity implements LocationListener {

    private TextView tvClock, tvDate, tvSpeed, tvGpsStatus, tvSongName;
    private Button btnPlay, btnPrev, btnNext, btnMap, btnSettings;
    private Handler handler = new Handler(Looper.getMainLooper());
    private Runnable clockRunnable;
    private LocationManager locationManager;
    private boolean isPlaying = false;

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1001;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Fullscreen & Keep Screen On for Automotive Monitor
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        setContentView(R.layout.activity_main);

        initViews();
        setupClock();
        setupListeners();
        setupLocation();
    }

    private void initViews() {
        tvClock = findViewById(R.id.tvClock);
        tvDate = findViewById(R.id.tvDate);
        tvSpeed = findViewById(R.id.tvSpeed);
        tvGpsStatus = findViewById(R.id.tvGpsStatus);
        tvSongName = findViewById(R.id.tvSongName);

        btnPlay = findViewById(R.id.btnPlay);
        btnPrev = findViewById(R.id.btnPrev);
        btnNext = findViewById(R.id.btnNext);
        btnMap = findViewById(R.id.btnMap);
        btnSettings = findViewById(R.id.btnSettings);
    }

    private void setupClock() {
        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
        SimpleDateFormat dateFormat = new SimpleDateFormat("EEEE، d MMMM", new Locale("fa", "IR"));

        clockRunnable = new Runnable() {
            @Override
            public void run() {
                long currentTime = System.currentTimeMillis();
                tvClock.setText(timeFormat.format(new Date(currentTime)));
                tvDate.setText(dateFormat.format(new Date(currentTime)));
                handler.postDelayed(this, 1000);
            }
        };
        handler.post(clockRunnable);
    }

    private void setupListeners() {
        btnPlay.setOnClickListener(v -> {
            isPlaying = !isPlaying;
            if (isPlaying) {
                btnPlay.setText("⏸");
                tvSongName.setText("در حال پخش (Demo)");
                Toast.makeText(this, "پخش موزیک", Toast.LENGTH_SHORT).show();
            } else {
                btnPlay.setText("▶");
                tvSongName.setText("آماده پخش");
                Toast.makeText(this, "توقف موزیک", Toast.LENGTH_SHORT).show();
            }
        });

        btnPrev.setOnClickListener(v -> Toast.makeText(this, "موزیک قبلی", Toast.LENGTH_SHORT).show());
        btnNext.setOnClickListener(v -> Toast.makeText(this, "موزیک بعدی", Toast.LENGTH_SHORT).show());

        btnMap.setOnClickListener(v -> {
            try {
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setPackage("com.google.android.apps.maps");
                startActivity(intent);
            } catch (Exception e) {
                Toast.makeText(this, "برنامه نقشه یافت نشد", Toast.LENGTH_SHORT).show();
            }
        });

        btnSettings.setOnClickListener(v -> {
            try {
                Intent intent = new Intent(Settings.ACTION_SETTINGS);
                startActivity(intent);
            } catch (Exception e) {
                Toast.makeText(this, "خطا در باز کردن تنظیمات", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupLocation() {
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
        } else {
            startLocationUpdates();
        }
    }

    private void startLocationUpdates() {
        try {
            if (locationManager != null) {
                if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                    tvGpsStatus.setText("GPS: متصل");
                    locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 1, this);
                } else {
                    tvGpsStatus.setText("GPS: غیرفعال");
                }
            }
        } catch (SecurityException e) {
            tvGpsStatus.setText("GPS: خطای دسترسی");
        }
    }

    @Override
    public void onLocationChanged(@NonNull Location location) {
        if (location.hasSpeed()) {
            int speedKmH = (int) (location.getSpeed() * 3.6);
            tvSpeed.setText(String.valueOf(speedKmH));
        } else {
            tvSpeed.setText("0");
        }
    }

    @Override
    public void onProviderEnabled(@NonNull String provider) {
        if (LocationManager.GPS_PROVIDER.equals(provider)) {
            tvGpsStatus.setText("GPS: متصل");
        }
    }

    @Override
    public void onProviderDisabled(@NonNull String provider) {
        if (LocationManager.GPS_PROVIDER.equals(provider)) {
            tvGpsStatus.setText("GPS: غیرفعال");
            tvSpeed.setText("0");
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startLocationUpdates();
            } else {
                tvGpsStatus.setText("GPS: دسترسی رد شد");
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (handler != null && clockRunnable != null) {
            handler.removeCallbacks(clockRunnable);
        }
        if (locationManager != null) {
            locationManager.removeUpdates(this);
        }
    }
}
