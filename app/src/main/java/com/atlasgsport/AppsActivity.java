
package com.atlasg.sport;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public class AppsActivity extends AppCompatActivity {

    private static class AppItem {
        String label;
        String packageName;
        android.graphics.drawable.Drawable icon;
    }

    private final List<AppItem> apps = new ArrayList<>();

    private LinearLayout pagesContainer;
    private TextView pageIndicator;
    private TextView pageTitle;

    private int currentPage = 0;
    private static final int APPS_PER_PAGE = 10;
    private static final int COLUMNS = 5;

    private GestureDetector gestureDetector;

    private final int GOLD = Color.rgb(224, 190, 125);
    private final int WHITE = Color.WHITE;
    private final int BACKGROUND = Color.rgb(13, 8, 28);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().setStatusBarColor(BACKGROUND);
        getWindow().setNavigationBarColor(BACKGROUND);

        loadInstalledApps();
        buildScreen();

        gestureDetector = new GestureDetector(
                this,
                new GestureDetector.SimpleOnGestureListener() {

                    @Override
                    public boolean onFling(
                            MotionEvent e1,
                            MotionEvent e2,
                            float velocityX,
                            float velocityY) {

                        if (e1 == null || e2 == null) return false;

                        float difference = e2.getX() - e1.getX();

                        if (Math.abs(difference) > 80 &&
                                Math.abs(velocityX) > 100) {

                            if (difference < 0) {
                                showPage(currentPage + 1);
                            } else {
                                showPage(currentPage - 1);
                            }

                            return true;
                        }

                        return false;
                    }
                }
        );
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        if (gestureDetector != null) {
            gestureDetector.onTouchEvent(event);
        }
        return super.dispatchTouchEvent(event);
    }

    private void loadInstalledApps() {

        apps.clear();

        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_LAUNCHER);

        PackageManager pm = getPackageManager();

        List<ResolveInfo> results =
                pm.queryIntentActivities(intent, 0);

        for (ResolveInfo info : results) {

            String packageName =
                    info.activityInfo.packageName;

            // خود لانچر در فهرست برنامه‌ها نمایش داده نمی‌شود.
            if (packageName.equals(getPackageName())) {
                continue;
            }

            AppItem item = new AppItem();

            item.packageName = packageName;
            item.label = info.loadLabel(pm).toString();
            item.icon = info.loadIcon(pm);

            apps.add(item);
        }

        Collections.sort(apps, (a, b) ->
                Collator.getInstance(Locale.getDefault())
                        .compare(a.label, b.label));
    }

    private void buildScreen() {

        FrameLayout root = new FrameLayout(this);
        root.setBackground(createBackground());

        LinearLayout main = new LinearLayout(this);
        main.setOrientation(LinearLayout.VERTICAL);
        main.setPadding(dp(24), dp(14), dp(24), dp(8));

        FrameLayout.LayoutParams mainParams =
                new FrameLayout.LayoutParams(
                        -1, -1
                );

        root.addView(main, mainParams);

        // Header
        LinearLayout header = new LinearLayout(this);
        header.setGravity(Gravity.CENTER_VERTICAL);
        header.setOrientation(LinearLayout.HORIZONTAL);
        header.setPadding(dp(18), 0, dp(18), 0);
        header.setBackground(createRounded(
                Color.rgb(25, 23, 24),
                GOLD,
                18,
                1
        ));

        TextView back = new TextView(this);
        back.setText("‹");
        back.setTextSize(36);
        back.setTextColor(GOLD);
        back.setGravity(Gravity.CENTER);
        back.setPadding(dp(8), 0, dp(16), 0);
        back.setOnClickListener(v -> finish());

        header.addView(back,
                new LinearLayout.LayoutParams(dp(45), -1));

        pageTitle = new TextView(this);
        pageTitle.setText("ATLAS G SPORT  |  برنامه‌های من");
        pageTitle.setTextColor(WHITE);
        pageTitle.setTextSize(18);
        pageTitle.setTypeface(null, Typeface.BOLD);

        header.addView(pageTitle,
                new LinearLayout.LayoutParams(
                        0, -1, 1
                ));

        TextView refresh = new TextView(this);
        refresh.setText("⟳");
        refresh.setTextSize(30);
        refresh.setTextColor(GOLD);
        refresh.setGravity(Gravity.CENTER);
        refresh.setOnClickListener(v -> {
            loadInstalledApps();
            currentPage = 0;
            renderPages();
        });

        header.addView(refresh,
                new LinearLayout.LayoutParams(dp(45), -1));

        main.addView(header,
                new LinearLayout.LayoutParams(-1, dp(58)));

        // App pages
        pagesContainer = new LinearLayout(this);
        pagesContainer.setOrientation(LinearLayout.VERTICAL);
        pagesContainer.setGravity(Gravity.CENTER);

        LinearLayout.LayoutParams pagesParams =
                new LinearLayout.LayoutParams(
                        -1, 0, 1
                );

        pagesParams.topMargin = dp(14);
        pagesParams.bottomMargin = dp(5);

        main.addView(pagesContainer, pagesParams);

        // Page indicators
        pageIndicator = new TextView(this);
        pageIndicator.setTextColor(GOLD);
        pageIndicator.setTextSize(20);
        pageIndicator.setGravity(Gravity.CENTER);

        main.addView(pageIndicator,
                new LinearLayout.LayoutParams(-1, dp(30)));

        renderPages();

        setContentView(root);
    }

    private void renderPages() {

        pagesContainer.removeAllViews();

        int totalPages = Math.max(
                1,
                (int) Math.ceil(apps.size() / (double) APPS_PER_PAGE)
        );

        if (currentPage >= totalPages) {
            currentPage = totalPages - 1;
        }

        for (int page = 0; page < totalPages; page++) {

            GridLayout grid = new GridLayout(this);
            grid.setColumnCount(COLUMNS);
            grid.setRowCount(2);
            grid.setUseDefaultMargins(false);
            grid.setAlignmentMode(GridLayout.ALIGN_BOUNDS);

            int start = page * APPS_PER_PAGE;
            int end = Math.min(
                    start + APPS_PER_PAGE,
                    apps.size()
            );

            for (int i = start; i < end; i++) {

                AppItem app = apps.get(i);

                LinearLayout tile = createAppTile(app);

                int position = i - start;
                int column = position % COLUMNS;
                int row = position / COLUMNS;

                GridLayout.LayoutParams params =
                        new GridLayout.LayoutParams(
                                GridLayout.spec(row, 1, 1f),
                                GridLayout.spec(column, 1, 1f)
                        );

                params.width = 0;
                params.height = 0;
                params.setMargins(dp(5), dp(8), dp(5), dp(8));

                grid.addView(tile, params);
            }

            pagesContainer.addView(
                    grid,
                    new LinearLayout.LayoutParams(-1, -1)
            );

            grid.setVisibility(
                    page == currentPage ? View.VISIBLE : View.GONE
            );
        }

        updateIndicator(totalPages);
    }

    private LinearLayout createAppTile(AppItem app) {

        LinearLayout tile = new LinearLayout(this);
        tile.setOrientation(LinearLayout.VERTICAL);
        tile.setGravity(Gravity.CENTER);
        tile.setPadding(dp(4), dp(5), dp(4), dp(5));

        ImageView icon = new ImageView(this);
        icon.setImageDrawable(app.icon);
        icon.setScaleType(ImageView.ScaleType.FIT_CENTER);

        tile.addView(icon,
                new LinearLayout.LayoutParams(dp(82), dp(82)));

        TextView label = new TextView(this);
        label.setText(app.label);
        label.setTextColor(WHITE);
        label.setTextSize(15);
        label.setGravity(Gravity.CENTER);
        label.setMaxLines(2);
        label.setTypeface(null, Typeface.NORMAL);

        LinearLayout.LayoutParams labelParams =
                new LinearLayout.LayoutParams(
                        -1, dp(48)
                );

        labelParams.topMargin = dp(7);
        tile.addView(label, labelParams);

        tile.setOnClickListener(v -> openApp(app.packageName));

        return tile;
    }

    private void openApp(String packageName) {

        try {

            Intent launchIntent =
                    getPackageManager()
                            .getLaunchIntentForPackage(packageName);

            if (launchIntent != null) {
                startActivity(launchIntent);
            } else {
                Toast.makeText(
                        this,
                        "امکان اجرای این برنامه وجود ندارد",
                        Toast.LENGTH_SHORT
                ).show();
            }

        } catch (Exception e) {

            Toast.makeText(
                    this,
                    "خطا در باز کردن برنامه",
                    Toast.LENGTH_SHORT
            ).show();
        }
    }

    private void showPage(int page) {

        int totalPages = Math.max(
                1,
                (int) Math.ceil(apps.size() / (double) APPS_PER_PAGE)
        );

        if (page < 0 || page >= totalPages) {
            return;
        }

        currentPage = page;
        renderPages();
    }

    private void updateIndicator(int totalPages) {

        StringBuilder dots = new StringBuilder();

        for (int i = 0; i < totalPages; i++) {
            dots.append(i == currentPage ? "●" : "○");
            dots.append("   ");
        }

        pageIndicator.setText(dots.toString());
    }

    private GradientDrawable createBackground() {

        GradientDrawable bg = new GradientDrawable(
                GradientDrawable.Orientation.TL_BR,
                new int[]{
                        Color.rgb(23, 8, 39),
                        BACKGROUND,
                        Color.rgb(7, 14, 48)
                }
        );

        bg.setCornerRadius(dp(12));
        return bg;
    }

    private GradientDrawable createRounded(
            int fill,
            int stroke,
            int radius,
            int strokeWidth) {

        GradientDrawable drawable = new GradientDrawable();
        drawable.setColor(fill);
        drawable.setCornerRadius(dp(radius));
        drawable.setStroke(dp(strokeWidth), stroke);

        return drawable;
    }

    private int dp(float value) {
        return (int) (value *
                getResources().getDisplayMetrics().density + 0.5f);
    }

    @Override
    protected void onResume() {
        super.onResume();

        // هنگام برگشت از یک برنامه، فهرست دوباره به‌روز می‌شود.
        if (pagesContainer != null) {
            loadInstalledApps();
            renderPages();
        }
    }
}
