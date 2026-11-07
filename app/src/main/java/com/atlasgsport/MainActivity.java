
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
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private TextView tvClock, tvDate, tvSpeed;
    private TextView tvGpsStatus, tvSongName;

    private Button btnPlay, btnPrev, btnNext;
    private Button btnMap, btnSettings;

    private final Handler handler = new Handler();
    private LocationManager locationManager;

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

        getWindow().getDecorView().setSystemUiVisibility(
                5894
        );

        initializeViews();
        updateClock();
        setupButtons();
        setupGPS();
    }

    private void initializeViews() {
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

    private void updateClock() {
        if (tvClock == null || tvDate == null) return;

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

            btnPlay.setText(isPlaying ? "Ⅱ" : "▶");

            tvSongName.setText(
                    isPlaying ? "Demo playback" : "Playback paused"
            );
        });

        btnPrev.setOnClickListener(v ->
                tvSongName.setText("Previous track")
        );

        btnNext.setOnClickListener(v ->
                tvSongName.setText("Next track")
        );

        btnMap.setOnClickListener(v -> {
            Intent intent = new Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse("geo:0,0?q=Iran")
            );

            startActivity(intent);
        });

        btnSettings.setOnClickListener(v -> {
            startActivity(new Intent(
                    android.provider.Settings.ACTION_SETTINGS
            ));
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

            tvGpsStatus.setText("GPS • PERMISSION");
            return;
        }

        startGPSUpdates();
    }

    private void startGPSUpdates() {
        try {
            LocationListener listener = location -> {
                float speed = location.hasSpeed()
                        ? location.getSpeed() * 3.6f
                        : 0;

                tvSpeed.setText(
                        String.valueOf(Math.round(speed))
                );

                tvGpsStatus.setText("GPS • CONNECTED");
            };

            locationManager.requestLocationUpdates(
                    LocationManager.GPS_PROVIDER,
                    1000,
                    1,
                    listener
            );

        } catch (SecurityException e) {
            tvGpsStatus.setText("GPS • ERROR");
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
    }
}
