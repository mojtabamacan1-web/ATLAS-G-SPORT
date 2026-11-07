
package com.atlasg.sport;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private TextView tvClock;
    private TextView tvDate;
    private TextView tvSpeed;

    private Button btnMap;
    private Button btnPlay;
    private Button btnApps;
    private Button btnSettings;
    private Button btnAbout;

    private LocationManager locationManager;
    private LocationListener locationListener;

    private final Handler handler = new Handler();

    private boolean isPlaying = false;

    private final Runnable clockRunnable = new Runnable() {
        @Override
        public void run() {
            updateClock();
            handler.postDelayed(this, 1000);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        getWindow().getDecorView().setSystemUiVisibility(5894);

        initializeViews();
        setupButtons();
        updateClock();
        setupGPS();
    }

    private void initializeViews() {

        tvClock = findViewById(R.id.tvClock);
        tvDate = findViewById(R.id.tvDate);
        tvSpeed = findViewById(R.id.tvSpeed);

        btnMap = findViewById(R.id.btnMap);
        btnPlay = findViewById(R.id.btnPlay);
        btnApps = findViewById(R.id.btnApps);
        btnSettings = findViewById(R.id.btnSettings);
        btnAbout = findViewById(R.id.btnAbout);
    }

    private void updateClock() {

        Date now = new Date();

        SimpleDateFormat timeFormat =
                new SimpleDateFormat("HH:mm", Locale.getDefault());

        SimpleDateFormat dateFormat =
                new SimpleDateFormat("EEEE, dd MMMM", Locale.getDefault());

        tvClock.setText(timeFormat.format(now));
        tvDate.setText(dateFormat.format(now));
    }

    private void setupButtons() {

        btnPlay.setOnClickListener(v -> {

            isPlaying = !isPlaying;

            btnPlay.setText(
                    isPlaying ? "Ⅱ\nموسیقی" : "♫\nموسیقی"
            );

            Toast.makeText(
                    this,
                    isPlaying ? "Music selected" : "Music paused",
                    Toast.LENGTH_SHORT
            ).show();
        });

        btnMap.setOnClickListener(v -> {

            try {
                Intent intent = new Intent(
                        Intent.ACTION_VIEW,
                        Uri.parse("geo:0,0?q=Iran")
                );

                startActivity(intent);

            } catch (Exception e) {
                Toast.makeText(
                        this,
                        "No map application found",
                        Toast.LENGTH_SHORT
                ).show();
            }
        });

        btnSettings.setOnClickListener(v -> {

            startActivity(
                    new Intent(Settings.ACTION_SETTINGS)
            );
        });

        btnApps.setOnClickListener(v -> {

            Intent intent = new Intent(Intent.ACTION_MAIN);
            intent.addCategory(Intent.CATEGORY_LAUNCHER);

            try {
                startActivity(
                        new Intent(Intent.ACTION_MAIN)
                                .addCategory(Intent.CATEGORY_HOME)
                );
            } catch (Exception e) {
                Toast.makeText(
                        this,
                        "Apps unavailable",
                        Toast.LENGTH_SHORT
                ).show();
            }
        });

        btnAbout.setOnClickListener(v -> {

            Toast.makeText(
                    this,
                    "ATLAS G SPORT - Luxury Sport Edition",
                    Toast.LENGTH_LONG
            ).show();
        });
    }

    private void setupGPS() {

        locationManager =
                (LocationManager) getSystemService(LOCATION_SERVICE);

        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
        ) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(
                    this,
                    new String[]{
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION
                    },
                    100
            );

            return;
        }

        startGPSUpdates();
    }

    private void startGPSUpdates() {

        try {

            locationListener = location -> {

                float speed = location.hasSpeed()
                        ? location.getSpeed() * 3.6f
                        : 0;

                tvSpeed.setText(
                        Math.round(speed) + " km/h"
                );
            };

            locationManager.requestLocationUpdates(
                    LocationManager.GPS_PROVIDER,
                    1000,
                    1,
                    locationListener
            );

        } catch (SecurityException e) {

            Toast.makeText(
                    this,
                    "GPS permission required",
                    Toast.LENGTH_SHORT
            ).show();
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

        if (requestCode == 100
                && grantResults.length > 0
                && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

            startGPSUpdates();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        handler.post(clockRunnable);
    }

    @Override
    protected void onPause() {
        super.onPause();

        handler.removeCallbacks(clockRunnable);

        if (locationManager != null && locationListener != null) {
            locationManager.removeUpdates(locationListener);
        }
    }
}
