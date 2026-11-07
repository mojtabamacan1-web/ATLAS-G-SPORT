
package com.atlasg.sport;

import android.Manifest;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends Activity {

    private TextView tvClock;
    private TextView tvDate;
    private TextView tvSpeed;
    private TextView tvSongName;
    private TextView tvArtist;

    private Button btnPlay;
    private Button btnPrev;
    private Button btnNext;

    private LocationManager locationManager;

    private final Handler handler =
            new Handler(Looper.getMainLooper());

    private final Runnable clockRunnable = new Runnable() {
        @Override
        public void run() {
            updateClock();
            handler.postDelayed(this, 1000);
        }
    };

    private final LocationListener locationListener =
            new LocationListener() {

        @Override
        public void onLocationChanged(Location location) {
            double speed = location.getSpeed() * 3.6;

            tvSpeed.setText(
                    String.format(Locale.US, "%.0f\nkm/h", speed)
            );
        }

        @Override
        public void onProviderEnabled(String provider) {
        }

        @Override
        public void onProviderDisabled(String provider) {
        }
    };

    private final MediaAccessService.MediaUpdateListener
            mediaUpdateListener = (title, artist, packageName, isPlaying) -> {

        tvSongName.setText(title);

        tvArtist.setText(
                artist == null || artist.isEmpty()
                        ? "Media player"
                        : artist
        );

        btnPlay.setText(isPlaying ? "Ⅱ" : "▶");
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        initializeViews();
        setupButtons();

        locationManager = (LocationManager)
                getSystemService(LOCATION_SERVICE);

        updateClock();
        startLocationUpdates();
    }

    private void initializeViews() {

        tvClock = findViewById(R.id.tvClock);
        tvDate = findViewById(R.id.tvDate);
        tvSpeed = findViewById(R.id.tvSpeed);

        tvSongName = findViewById(R.id.tvSongName);
        tvArtist = findViewById(R.id.tvArtist);

        btnPlay = findViewById(R.id.btnPlay);
        btnPrev = findViewById(R.id.btnPrev);
        btnNext = findViewById(R.id.btnNext);
    }

    private void setupButtons() {

        Button btnMap = findViewById(R.id.btnMap);
        Button btnApps = findViewById(R.id.btnApps);
        Button btnSettings = findViewById(R.id.btnSettings);
        Button btnAbout = findViewById(R.id.btnAbout);

        // نقشه
        btnMap.setOnClickListener(v -> {
            try {
                Intent intent = new Intent(
                        Intent.ACTION_VIEW,
                        Uri.parse("geo:0,0?q=Yazd")
                );

                startActivity(intent);

            } catch (Exception e) {
                showToast("برنامه نقشه پیدا نشد");
            }
        });

        // فهرست برنامه‌های نصب‌شده
        btnApps.setOnClickListener(v -> {
            try {
                Intent intent = new Intent(
                        MainActivity.this,
                        AppsActivity.class
                );

                startActivity(intent);

            } catch (Exception e) {
                showToast("خطا در باز کردن فهرست برنامه‌ها");
            }
        });

        // تنظیمات اندروید
        btnSettings.setOnClickListener(v -> {
            try {
                startActivity(
                        new Intent(Settings.ACTION_SETTINGS)
                );
            } catch (Exception e) {
                showToast("تنظیمات در دسترس نیست");
            }
        });

        // درباره ما
        btnAbout.setOnClickListener(v -> {
            showToast("ATLAS G SPORT - Version 1.0");
        });

        // پخش و توقف موسیقی
        btnPlay.setOnClickListener(v -> {
            if (!hasNotificationAccess()) {
                openNotificationSettings();
                return;
            }

            MediaAccessService.playPause();
        });

        // آهنگ قبلی
        btnPrev.setOnClickListener(v -> {
            if (!hasNotificationAccess()) {
                openNotificationSettings();
                return;
            }

            MediaAccessService.previous();
        });

        // آهنگ بعدی
        btnNext.setOnClickListener(v -> {
            if (!hasNotificationAccess()) {
                openNotificationSettings();
                return;
            }

            MediaAccessService.next();
        });
    }

    private boolean hasNotificationAccess() {

        String enabledListeners = Settings.Secure.getString(
                getContentResolver(),
                "enabled_notification_listeners"
        );

        if (enabledListeners == null) {
            return false;
        }

        ComponentName component = new ComponentName(
                this,
                MediaAccessService.class
        );

        return enabledListeners.contains(
                component.flattenToString()
        );
    }

    private void openNotificationSettings() {

        showToast(
                "لطفاً دسترسی اعلان‌ها را برای ATLAS G SPORT فعال کن"
        );

        try {
            startActivity(
                    new Intent(
                            Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS
                    )
            );
        } catch (Exception e) {
            showToast(
                    "تنظیمات دسترسی اعلان‌ها در دسترس نیست"
            );
        }
    }

    private void updateClock() {

        Date now = new Date();

        SimpleDateFormat clockFormat =
                new SimpleDateFormat(
                        "HH:mm",
                        Locale.getDefault()
                );

        SimpleDateFormat dateFormat =
                new SimpleDateFormat(
                        "yyyy/MM/dd",
                        Locale.getDefault()
                );

        tvClock.setText(clockFormat.format(now));
        tvDate.setText(dateFormat.format(now));
    }

    private void startLocationUpdates() {

        if (checkSelfPermission(
                Manifest.permission.ACCESS_FINE_LOCATION
        ) != PackageManager.PERMISSION_GRANTED) {

            requestPermissions(
                    new String[]{
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION
                    },
                    100
            );

            return;
        }

        try {
            locationManager.requestLocationUpdates(
                    LocationManager.GPS_PROVIDER,
                    1000,
                    1,
                    locationListener
            );

        } catch (SecurityException ignored) {
        }
    }

    @Override
    public void onRequestPermissionsResult(
            int requestCode,
            String[] permissions,
            int[] grantResults
    ) {
        super.onRequestPermissionsResult(
                requestCode,
                permissions,
                grantResults
        );

        if (requestCode == 100 &&
                grantResults.length > 0 &&
                grantResults[0] == PackageManager.PERMISSION_GRANTED) {

            startLocationUpdates();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        handler.removeCallbacks(clockRunnable);
        handler.post(clockRunnable);

        MediaAccessService.setUiListener(
                mediaUpdateListener
        );

        startLocationUpdates();
    }

    @Override
    protected void onPause() {
        super.onPause();

        handler.removeCallbacks(clockRunnable);

        MediaAccessService.setUiListener(null);

        if (locationManager != null &&
                checkSelfPermission(
                        Manifest.permission.ACCESS_FINE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED) {

            locationManager.removeUpdates(locationListener);
        }
    }

    private void showToast(String message) {

        Toast.makeText(
                this,
                message,
                Toast.LENGTH_LONG
        ).show();
    }
}
