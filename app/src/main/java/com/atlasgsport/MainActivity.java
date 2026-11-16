
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
    private long lastWeatherFetchAt = 0;

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
            if (System.currentTimeMillis() - lastWeatherFetchAt > 30L * 60L * 1000L) {
                lastWeatherFetchAt = System.currentTimeMillis();
                fetchWeather(location);
            }
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
        boolean persian = !"en".equals(getSharedPreferences("atlas_settings", MODE_PRIVATE).getString("language", "fa"));
        if (persian) {
            java.util.Calendar calendar = java.util.Calendar.getInstance();
            calendar.setTime(now);
            int[] jalali = toJalali(calendar.get(java.util.Calendar.YEAR),
                    calendar.get(java.util.Calendar.MONTH) + 1,
                    calendar.get(java.util.Calendar.DAY_OF_MONTH));
            tvDate.setText(String.format(Locale.US, "%04d/%02d/%02d", jalali[0], jalali[1], jalali[2]));
        } else {
            tvDate.setText(dateFormat.format(now));
        }
    }

    private int[] toJalali(int gy, int gm, int gd) {
        int[] monthDays = {0,31,59,90,120,151,181,212,243,273,304,334};
        int jy;
        if (gy > 1600) { jy = 979; gy -= 1600; }
        else { jy = 0; gy -= 621; }
        int gy2 = (gm > 2) ? gy + 1 : gy;
        int days = 365 * gy + (gy2 + 3) / 4 - (gy2 + 99) / 100
                + (gy2 + 399) / 400 - 80 + gd + monthDays[gm - 1];
        jy += 33 * (days / 12053);
        days %= 12053;
        jy += 4 * (days / 1461);
        days %= 1461;
        if (days > 365) {
            jy += (days - 1) / 365;
            days = (days - 1) % 365;
        }
        int jm = (days < 186) ? 1 + days / 31 : 7 + (days - 186) / 30;
        int jd = 1 + ((days < 186) ? days % 31 : (days - 186) % 30);
        return new int[]{jy, jm, jd};
    }

    private void fetchWeather(Location location) {
        final double latitude = location.getLatitude();
        final double longitude = location.getLongitude();
        new Thread(() -> {
            java.net.HttpURLConnection connection = null;
            try {
                String endpoint = "https://api.open-meteo.com/v1/forecast?latitude="
                        + latitude + "&longitude=" + longitude
                        + "&current=temperature_2m,weather_code&timezone=auto";
                connection = (java.net.HttpURLConnection) new java.net.URL(endpoint).openConnection();
                connection.setConnectTimeout(8000);
                connection.setReadTimeout(8000);
                connection.setRequestMethod("GET");
                java.io.BufferedReader reader = new java.io.BufferedReader(
                        new java.io.InputStreamReader(connection.getInputStream(), "UTF-8"));
                StringBuilder body = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) body.append(line);
                reader.close();
                org.json.JSONObject current = new org.json.JSONObject(body.toString()).getJSONObject("current");
                final double temperature = current.getDouble("temperature_2m");
                final int code = current.getInt("weather_code");
                final String condition = weatherDescription(code);
                handler.post(() -> {
                    android.widget.TextView temp = findViewById(R.id.tvWeatherTemp);
                    android.widget.TextView city = findViewById(R.id.tvWeatherCity);
                    android.widget.TextView status = findViewById(R.id.tvWeatherCondition);
                    if (temp != null) temp.setText(String.format(Locale.US, "%.0f°C", temperature));
                    if (city != null) city.setText("موقعیت فعلی");
                    if (status != null) status.setText(condition);
                });
            } catch (Exception ignored) {
                handler.post(() -> {
                    android.widget.TextView status = findViewById(R.id.tvWeatherCondition);
                    if (status != null) status.setText("دریافت وضعیت هوا ناموفق بود");
                });
            } finally {
                if (connection != null) connection.disconnect();
            }
        }).start();
    }

    private String weatherDescription(int code) {
        boolean en = "en".equals(getSharedPreferences("atlas_settings", MODE_PRIVATE).getString("language", "fa"));
        if (code == 0) return en ? "Clear sky" : "آسمان صاف";
        if (code <= 3) return en ? "Partly cloudy" : "کمی تا نیمه‌ابری";
        if (code == 45 || code == 48) return en ? "Fog" : "مه‌آلود";
        if (code >= 51 && code <= 57) return en ? "Drizzle" : "نم‌نم باران";
        if (code >= 61 && code <= 67) return en ? "Rain" : "بارانی";
        if (code >= 71 && code <= 77) return en ? "Snow" : "برفی";
        if (code >= 80 && code <= 82) return en ? "Rain showers" : "رگبار";
        if (code >= 95) return en ? "Thunderstorm" : "رعدوبرق";
        return en ? "Current conditions" : "وضعیت فعلی هوا";
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

    private android.graphics.drawable.GradientDrawable makeSurface(
            int top, int bottom, int stroke, float radius) {
        android.graphics.drawable.GradientDrawable shape =
                new android.graphics.drawable.GradientDrawable(
                        android.graphics.drawable.GradientDrawable.Orientation.TOP_BOTTOM,
                        new int[]{top, bottom});
        shape.setCornerRadius(radius * getResources().getDisplayMetrics().density);
        shape.setStroke((int)(getResources().getDisplayMetrics().density), stroke);
        return shape;
    }

    private void applySavedAppearance() {
        android.content.SharedPreferences p = getSharedPreferences("atlas_settings", MODE_PRIVATE);
        boolean light = "light".equals(p.getString("theme", "dark"));
        int accent = android.graphics.Color.parseColor(p.getString("accent", "#D4AF37"));
        int text = light ? android.graphics.Color.rgb(52, 46, 34) : android.graphics.Color.rgb(245, 240, 220);
        int muted = light ? android.graphics.Color.rgb(112, 105, 88) : android.graphics.Color.rgb(164, 167, 175);
        int panelTop = light ? android.graphics.Color.rgb(255, 254, 248) : android.graphics.Color.rgb(38, 42, 51);
        int panelBottom = light ? android.graphics.Color.rgb(237, 232, 216) : android.graphics.Color.rgb(15, 18, 24);
        int stroke = light ? android.graphics.Color.rgb(205, 190, 145) : android.graphics.Color.rgb(102, 91, 59);
        int rootTop = light ? android.graphics.Color.rgb(250, 247, 236) : android.graphics.Color.rgb(13, 16, 22);
        int rootBottom = light ? android.graphics.Color.rgb(230, 224, 207) : android.graphics.Color.rgb(5, 7, 10);

        android.view.View root = ((android.view.ViewGroup)findViewById(android.R.id.content)).getChildAt(0);
        root.setBackground(makeSurface(rootTop, rootBottom, rootBottom, 0));

        int[] cards = {R.id.header, R.id.weatherPanel, R.id.tirePanel, R.id.bottomBar, R.id.musicWidget};
        for (int id : cards) {
            android.view.View v = findViewById(id);
            if (v != null) {
                v.setBackground(makeSurface(panelTop, panelBottom, stroke, 22));
                v.setElevation(7f * getResources().getDisplayMetrics().density);
            }
        }
        android.view.View hero = findViewById(R.id.centerPanel);
        if (hero != null) {
            int heroTop = light ? android.graphics.Color.rgb(246, 242, 228) : android.graphics.Color.rgb(34, 40, 50);
            int heroBottom = light ? android.graphics.Color.rgb(225, 218, 198) : android.graphics.Color.rgb(8, 11, 17);
            hero.setBackground(makeSurface(heroTop, heroBottom, accent, 26));
            hero.setElevation(12f * getResources().getDisplayMetrics().density);
        }

        TextView[] primaryText = {tvClock, tvSongName, findViewById(R.id.tvFrontTires), findViewById(R.id.tvRearTires), findViewById(R.id.tvWeatherTemp), findViewById(R.id.tvWeatherCity)};
        for (TextView label : primaryText) if (label != null) {
            label.setTextColor(text);
            label.setTypeface(android.graphics.Typeface.create("sans-serif-medium", android.graphics.Typeface.NORMAL));
            label.setLetterSpacing(0.015f);
        }
        TextView[] mutedText = {tvDate, tvArtist, findViewById(R.id.tvWeatherCondition)};
        for (TextView label : mutedText) if (label != null) {
            label.setTextColor(muted);
            label.setTypeface(android.graphics.Typeface.create("sans-serif", android.graphics.Typeface.NORMAL));
        }

        int gold = accent;
        int[] accentLabels = {R.id.tvSpeed, R.id.tvTpmsStatus};
        for (int id : accentLabels) {
            TextView label = findViewById(id);
            if (label != null) label.setTextColor(id == R.id.tvSpeed ? android.graphics.Color.BLACK : gold);
        }
        android.view.View speed = findViewById(R.id.tvSpeed);
        if (speed != null) {
            android.graphics.drawable.GradientDrawable speedDial =
                    new android.graphics.drawable.GradientDrawable(
                            android.graphics.drawable.GradientDrawable.Orientation.TL_BR,
                            new int[] {
                                    light ? android.graphics.Color.rgb(255, 250, 225) : android.graphics.Color.rgb(48, 39, 19),
                                    light ? android.graphics.Color.rgb(232, 211, 151) : android.graphics.Color.rgb(17, 19, 23)
                            });
            speedDial.setShape(android.graphics.drawable.GradientDrawable.OVAL);
            speedDial.setStroke(
                    Math.max(2, Math.round(2 * getResources().getDisplayMetrics().density)),
                    accent);
            speed.setBackground(speedDial);
            speed.setTextColor(text);
            speed.setElevation(8f * getResources().getDisplayMetrics().density);
        }

        int[] buttonIds = {R.id.btnMap, R.id.btnApps, R.id.btnSettings, R.id.btnAbout, R.id.btnPrev, R.id.btnNext};
        for (int id : buttonIds) {
            Button button = findViewById(id);
            if (button != null) {
                button.setBackground(makeSurface(
                        light ? android.graphics.Color.WHITE : android.graphics.Color.rgb(49, 53, 62),
                        light ? android.graphics.Color.rgb(232, 226, 209) : android.graphics.Color.rgb(23, 26, 32),
                        stroke, 14));
                button.setTextColor(text);
            }
        }
        if (btnPlay != null) {
            btnPlay.setBackground(makeSurface(
                    light ? accent : android.graphics.Color.rgb(242, 216, 135),
                    light ? accent : android.graphics.Color.rgb(164, 125, 36),
                    accent, 14));
            btnPlay.setTextColor(android.graphics.Color.BLACK);
            btnPlay.setElevation(6f * getResources().getDisplayMetrics().density);
        }

        findViewById(R.id.weatherPanel).setVisibility(p.getBoolean("show_weather", true) ? android.view.View.VISIBLE : android.view.View.GONE);
        findViewById(R.id.centerPanel).setVisibility(p.getBoolean("show_car", true) ? android.view.View.VISIBLE : android.view.View.GONE);
        findViewById(R.id.tirePanel).setVisibility(p.getBoolean("show_tyres", true) ? android.view.View.VISIBLE : android.view.View.GONE);
        findViewById(R.id.bottomBar).setVisibility(p.getBoolean("show_dock", true) ? android.view.View.VISIBLE : android.view.View.GONE);
        tvSpeed.setVisibility(p.getBoolean("show_speed", true) ? android.view.View.VISIBLE : android.view.View.GONE);
        int musicVisibility = p.getBoolean("show_music", true) ? android.view.View.VISIBLE : android.view.View.GONE;
        tvSongName.setVisibility(musicVisibility);
        tvArtist.setVisibility(musicVisibility);
        btnPlay.setVisibility(musicVisibility);
        btnPrev.setVisibility(musicVisibility);
        btnNext.setVisibility(musicVisibility);
        android.view.View musicWidget = findViewById(R.id.musicWidget);
        if (musicWidget != null) musicWidget.setVisibility(musicVisibility);
        findViewById(R.id.btnMap).setVisibility(p.getBoolean("dock_show_map", true) ? android.view.View.VISIBLE : android.view.View.GONE);
        findViewById(R.id.btnApps).setVisibility(p.getBoolean("dock_show_apps", true) ? android.view.View.VISIBLE : android.view.View.GONE);
        findViewById(R.id.btnSettings).setVisibility(p.getBoolean("dock_show_settings", true) ? android.view.View.VISIBLE : android.view.View.GONE);
        findViewById(R.id.btnAbout).setVisibility(p.getBoolean("dock_show_about", true) ? android.view.View.VISIBLE : android.view.View.GONE);
        applyDockOrder();
    }

    private void applyDockOrder() {
        android.widget.LinearLayout dock = findViewById(R.id.bottomBar);
        if (dock == null) return;
        java.util.LinkedHashMap<String, android.view.View> widgets = new java.util.LinkedHashMap<>();
        widgets.put("speed", findViewById(R.id.tvSpeed));
        widgets.put("map", findViewById(R.id.btnMap));
        widgets.put("music", findViewById(R.id.musicWidget));
        widgets.put("apps", findViewById(R.id.btnApps));
        widgets.put("settings", findViewById(R.id.btnSettings));
        widgets.put("about", findViewById(R.id.btnAbout));
        String saved = getSharedPreferences("atlas_settings", MODE_PRIVATE).getString("dock_order", "speed,map,music,apps,settings,about");
        java.util.ArrayList<String> order = new java.util.ArrayList<>();
        for (String id : saved.split(",")) if (widgets.containsKey(id) && !order.contains(id)) order.add(id);
        for (String id : widgets.keySet()) if (!order.contains(id)) order.add(id);
        java.util.HashMap<String, android.view.ViewGroup.LayoutParams> params = new java.util.HashMap<>();
        for (java.util.Map.Entry<String, android.view.View> entry : widgets.entrySet()) params.put(entry.getKey(), entry.getValue().getLayoutParams());
        dock.removeAllViews();
        for (String id : order) {
            android.view.View child = widgets.get(id);
            if (child != null) dock.addView(child, params.get(id));
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
