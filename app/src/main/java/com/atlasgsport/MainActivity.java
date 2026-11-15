
package com.atlasg.sport;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
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
        applySavedLanguage();
        applySavedAppearance();

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

        // مرکز شخصی‌سازی کامل لانچر
        btnSettings.setOnClickListener(v -> {
            try {
                startActivity(new Intent(MainActivity.this, CustomizationActivity.class));
            } catch (Exception e) {
                showToast("مرکز شخصی‌سازی در دسترس نیست");
            }
        });

        // درباره ما: فقط نام و شناسه تلگرام طراح
        btnAbout.setOnClickListener(v -> {
            new AlertDialog.Builder(MainActivity.this)
                    .setTitle("درباره ما")
                    .setMessage("مهندس مجتبی زارع\\n@Mangentzare")
                    .setPositiveButton("تأیید", null)
                    .show();
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

    private void applySavedLanguage() {
        boolean en = "en".equals(getSharedPreferences("atlas_settings", MODE_PRIVATE).getString("language", "fa"));
        translateTree(findViewById(android.R.id.content), en);
        ((Button) findViewById(R.id.btnMap)).setText(en ? "⌖ Maps" : "⌖ نقشه");
        ((Button) findViewById(R.id.btnApps)).setText(en ? "▦ Apps" : "▦ برنامه‌ها");
        ((Button) findViewById(R.id.btnSettings)).setText(en ? "⚙ Settings" : "⚙ تنظیمات");
        ((Button) findViewById(R.id.btnAbout)).setText(en ? "ⓘ About" : "ⓘ درباره ما");
    }

    private void translateTree(android.view.View view, boolean en) {
        if (view instanceof TextView) {
            TextView t = (TextView) view;
            String s = t.getText().toString();
            String[][] pairs = {
                {"آب‌وهوا", "Weather"}, {"یزد", "Yazd"}, {"نیمه ابری", "Partly cloudy"},
                {"امروز   فردا   پس‌فردا", "Today   Tomorrow   Next day"},
                {"فشار باد تایرها", "Tire pressure"}, {"جلو\\n--  |  --", "Front\\n--  |  --"},
                {"عقب\\n--  |  --", "Rear\\n--  |  --"}, {"● وضعیت نامشخص", "● Status unavailable"},
                {"موسیقی در حال پخش", "Now playing"}, {"نقشه", "Maps"}, {"برنامه‌ها", "Apps"},
                {"تنظیمات", "Settings"}, {"درباره ما", "About"}, {"موسیقی", "Music"}
            };
            for (String[] p : pairs) {
                if (en && s.equals(p[0])) { t.setText(p[1]); break; }
                if (!en && s.equals(p[1])) { t.setText(p[0]); break; }
            }
        }
        if (view instanceof android.view.ViewGroup) {
            android.view.ViewGroup group = (android.view.ViewGroup) view;
            for (int i = 0; i < group.getChildCount(); i++) translateTree(group.getChildAt(i), en);
        }
    }

    private void applySavedAppearance() {
        android.content.SharedPreferences p = getSharedPreferences("atlas_settings", MODE_PRIVATE);
        boolean light = "light".equals(p.getString("theme", "dark"));
        int bg = light ? android.graphics.Color.rgb(247,244,232) : android.graphics.Color.rgb(8,8,8);
        int card = light ? android.graphics.Color.WHITE : android.graphics.Color.rgb(20,20,20);
        int accent = android.graphics.Color.parseColor(p.getString("accent", "#D4AF37"));
        android.view.View root = ((android.view.ViewGroup)findViewById(android.R.id.content)).getChildAt(0);
        root.setBackgroundColor(bg);
        int[] cards = {R.id.weatherPanel, R.id.centerPanel, R.id.tirePanel, R.id.bottomBar};
        for (int id : cards) {
            android.view.View v = findViewById(id);
            if (v != null) {
                v.setBackgroundTintList(android.content.res.ColorStateList.valueOf(card));
                v.setVisibility(id == R.id.weatherPanel ? (p.getBoolean("show_weather", true) ? android.view.View.VISIBLE : android.view.View.GONE)
                    : id == R.id.centerPanel ? (p.getBoolean("show_car", true) ? android.view.View.VISIBLE : android.view.View.GONE)
                    : id == R.id.tirePanel ? (p.getBoolean("show_tyres", true) ? android.view.View.VISIBLE : android.view.View.GONE)
                    : (p.getBoolean("show_dock", true) ? android.view.View.VISIBLE : android.view.View.GONE));
            }
        }
        int text = light ? android.graphics.Color.rgb(48,43,32) : android.graphics.Color.rgb(245,240,220);
        TextView[] labels = {tvClock, tvDate, tvSongName, tvArtist};
        for (TextView label : labels) if (label != null) label.setTextColor(text);
        tvSpeed.setVisibility(p.getBoolean("show_speed", true) ? android.view.View.VISIBLE : android.view.View.GONE);
        int musicVisibility = p.getBoolean("show_music", true) ? android.view.View.VISIBLE : android.view.View.GONE;
        tvSongName.setVisibility(musicVisibility);
        tvArtist.setVisibility(musicVisibility);
        btnPlay.setVisibility(musicVisibility);
        btnPrev.setVisibility(musicVisibility);
        btnNext.setVisibility(musicVisibility);
    }

    private void showToast(String message) {

        Toast.makeText(
                this,
                message,
                Toast.LENGTH_LONG
        ).show();
    }
}
