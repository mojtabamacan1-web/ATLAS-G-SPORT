package com.atlasg.sport;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ClipData;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.DragEvent;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class DashboardEditorActivity extends Activity {
    private static final String PREFS = "atlas_settings";
    private static final String ORDER_KEY = "dock_order";
    private static final String[] IDS = {"speed", "map", "music", "apps", "settings", "about"};
    private static final String[] FA = {"سرعت‌سنج", "نقشه", "پخش‌کننده موسیقی", "برنامه‌ها", "تنظیمات", "درباره ما"};
    private static final String[] EN = {"Speedometer", "Maps", "Music player", "Apps", "Settings", "About"};
    private final List<String> order = new ArrayList<>();
    private LinearLayout list;
    private boolean english;
    private int accent, background, foreground, card;

    private int dp(float value) {
        return (int)(value * getResources().getDisplayMetrics().density + .5f);
    }
    private String label(String fa, String en) { return english ? en : fa; }
    private boolean enabled(String id) { return getPreferences(0).getBoolean("unused", false); }

    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        english = "en".equals(getSharedPreferences(PREFS, MODE_PRIVATE).getString("language", "fa"));
        boolean light = "light".equals(getSharedPreferences(PREFS, MODE_PRIVATE).getString("theme", "dark"));
        accent = Color.parseColor(getSharedPreferences(PREFS, MODE_PRIVATE).getString("accent", "#D4AF37"));
        background = light ? Color.rgb(247,244,232) : Color.rgb(8,8,8);
        foreground = light ? Color.rgb(45,40,30) : Color.rgb(245,240,220);
        card = light ? Color.WHITE : Color.rgb(20,20,20);
        String saved = getSharedPreferences(PREFS, MODE_PRIVATE).getString(ORDER_KEY, "");
        if (saved.isEmpty()) order.addAll(Arrays.asList(IDS));
        else {
            for (String id : saved.split(",")) if (containsId(id) && !order.contains(id)) order.add(id);
            for (String id : IDS) if (!order.contains(id)) order.add(id);
        }
        build();
    }

    private boolean containsId(String id) {
        for (String known : IDS) if (known.equals(id)) return true;
        return false;
    }
    private boolean isVisible(String id) {
        String key = "dock_show_" + id;
        boolean defaultValue = true;
        if ("speed".equals(id)) return getSharedPreferences(PREFS, MODE_PRIVATE).getBoolean("show_speed", true);
        if ("music".equals(id)) return getSharedPreferences(PREFS, MODE_PRIVATE).getBoolean("show_music", true);
        return getSharedPreferences(PREFS, MODE_PRIVATE).getBoolean(key, defaultValue);
    }
    private void setVisible(String id, boolean value) {
        android.content.SharedPreferences.Editor e = getSharedPreferences(PREFS, MODE_PRIVATE).edit();
        if ("speed".equals(id)) e.putBoolean("show_speed", value);
        else if ("music".equals(id)) e.putBoolean("show_music", value);
        else e.putBoolean("dock_show_" + id, value);
        e.apply();
    }
    private void saveOrder() {
        StringBuilder value = new StringBuilder();
        for (String id : order) { if (value.length() > 0) value.append(','); value.append(id); }
        getSharedPreferences(PREFS, MODE_PRIVATE).edit().putString(ORDER_KEY, value.toString()).apply();
    }
    private void build() {
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(dp(18), dp(14), dp(18), dp(18));
        root.setBackgroundColor(background);
        root.setLayoutDirection(english ? View.LAYOUT_DIRECTION_LTR : View.LAYOUT_DIRECTION_RTL);

        TextView title = text(label("ویرایشگر داشبورد", "DASHBOARD EDITOR"), 24, accent, true);
        root.addView(title);
        TextView hint = text(label("برای جابه‌جایی، یک ابزارک را نگه دار و روی جای جدید رها کن. از فلش‌ها هم می‌توانی استفاده کنی.", "Long-press a widget and drop it in a new position. Arrow buttons are also available."), 13, foreground, false);
        hint.setPadding(0, dp(6), 0, dp(12));
        root.addView(hint);

        LinearLayout actions = new LinearLayout(this);
        actions.setOrientation(LinearLayout.HORIZONTAL);
        Button add = button(label("افزودن ابزارک", "ADD WIDGET"), accent, Color.BLACK);
        add.setOnClickListener(v -> showAddDialog());
        actions.addView(add, new LinearLayout.LayoutParams(0, dp(48), 1));
        Button reset = button(label("بازنشانی ترتیب", "RESET ORDER"), card, foreground);
        reset.setOnClickListener(v -> { order.clear(); order.addAll(Arrays.asList(IDS)); saveOrder(); renderRows(); });
        LinearLayout.LayoutParams rp = new LinearLayout.LayoutParams(0, dp(48), 1);
        rp.setMargins(dp(8), 0, 0, 0);
        actions.addView(reset, rp);
        root.addView(actions);

        TextView section = text(label("ابزارک‌های نوار اصلی", "HOME DOCK WIDGETS"), 15, accent, true);
        section.setPadding(0, dp(16), 0, dp(8));
        root.addView(section);

        ScrollView scroll = new ScrollView(this);
        list = new LinearLayout(this);
        list.setOrientation(LinearLayout.VERTICAL);
        scroll.addView(list);
        root.addView(scroll, new LinearLayout.LayoutParams(-1, 0, 1));
        Button done = button(label("ذخیره و بازگشت", "SAVE & RETURN"), accent, Color.BLACK);
        done.setOnClickListener(v -> { saveOrder(); finish(); });
        LinearLayout.LayoutParams dp = new LinearLayout.LayoutParams(-1, dp(52));
        dp.topMargin = dp(10);
        root.addView(done, dp);
        setContentView(root);
        renderRows();
    }

    private void renderRows() {
        list.removeAllViews();
        for (int i = 0; i < order.size(); i++) {
            final String id = order.get(i);
            final int index = i;
            int labelIndex = Arrays.asList(IDS).indexOf(id);
            LinearLayout row = new LinearLayout(this);
            row.setGravity(Gravity.CENTER_VERTICAL);
            row.setOrientation(LinearLayout.HORIZONTAL);
            row.setPadding(dp(8), dp(4), dp(8), dp(4));
            GradientDrawable shape = new GradientDrawable();
            shape.setColor(card);
            shape.setCornerRadius(dp(13));
            shape.setStroke(dp(1), Color.argb(70, 212, 175, 55));
            row.setBackground(shape);
            LinearLayout.LayoutParams rowParams = new LinearLayout.LayoutParams(-1, dp(64));
            rowParams.bottomMargin = dp(7);
            list.addView(row, rowParams);

            TextView drag = text("☰", 22, accent, true);
            drag.setGravity(Gravity.CENTER);
            row.addView(drag, new LinearLayout.LayoutParams(dp(42), -1));
            drag.setOnLongClickListener(v -> {
                ClipData data = ClipData.newPlainText("widget", id);
                if (android.os.Build.VERSION.SDK_INT >= 24) v.startDragAndDrop(data, new View.DragShadowBuilder(v), null, 0);
                else v.startDrag(data, new View.DragShadowBuilder(v), null, 0);
                return true;
            });
            row.setOnDragListener((v, event) -> {
                if (event.getAction() == DragEvent.ACTION_DRAG_STARTED) return true;
                if (event.getAction() == DragEvent.ACTION_DROP) {
                    String moving = event.getClipData().getItemAt(0).getText().toString();
                    int from = order.indexOf(moving);
                    int to = order.indexOf(id);
                    if (from >= 0 && to >= 0) {
                        order.remove(from);
                        order.add(Math.min(to, order.size()), moving);
                        saveOrder();
                        renderRows();
                    }
                    return true;
                }
                return true;
            });

            TextView name = text(english ? EN[labelIndex] : FA[labelIndex], 15, foreground, true);
            row.addView(name, new LinearLayout.LayoutParams(0, -1, 1));
            CheckBox visible = new CheckBox(this);
            visible.setButtonTintList(android.content.res.ColorStateList.valueOf(accent));
            visible.setChecked(isVisible(id));
            visible.setOnCheckedChangeListener((b, checked) -> setVisible(id, checked));
            row.addView(visible);

            Button up = smallButton("↑");
            up.setEnabled(index > 0);
            up.setOnClickListener(v -> move(id, -1));
            row.addView(up);
            Button down = smallButton("↓");
            down.setEnabled(index < order.size() - 1);
            down.setOnClickListener(v -> move(id, 1));
            row.addView(down);
        }
    }
    private void move(String id, int direction) {
        int from = order.indexOf(id), to = from + direction;
        if (to < 0 || to >= order.size()) return;
        order.remove(from); order.add(to, id); saveOrder(); renderRows();
    }
    private void showAddDialog() {
        List<String> hidden = new ArrayList<>();
        List<String> labels = new ArrayList<>();
        for (int i = 0; i < IDS.length; i++) {
            if (!order.contains(IDS[i]) || !isVisible(IDS[i])) {
                hidden.add(IDS[i]);
                labels.add(english ? EN[i] : FA[i]);
            }
        }
        if (hidden.isEmpty()) {
            Toast.makeText(this, label("همه ابزارک‌ها در فهرست هستند.", "All widgets are already listed."), Toast.LENGTH_SHORT).show();
            return;
        }
        new AlertDialog.Builder(this).setTitle(label("انتخاب ابزارک", "Choose a widget"))
            .setItems(labels.toArray(new String[0]), (d, which) -> {
                String id = hidden.get(which);
                if (!order.contains(id)) order.add(id);
                setVisible(id, true);
                saveOrder();
                renderRows();
            }).setNegativeButton(label("لغو", "Cancel"), null).show();
    }
    private TextView text(String value, int size, int color, boolean bold) {
        TextView t = new TextView(this); t.setText(value); t.setTextSize(size); t.setTextColor(color);
        if (bold) t.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        return t;
    }
    private Button button(String value, int bg, int fg) {
        Button b = new Button(this); b.setText(value); b.setTextColor(fg); b.setTextSize(13); b.setAllCaps(false);
        GradientDrawable d = new GradientDrawable(); d.setColor(bg); d.setCornerRadius(dp(12)); b.setBackground(d); return b;
    }
    private Button smallButton(String value) {
        Button b = button(value, Color.rgb(55,50,40), foreground);
        b.setTextSize(17); b.setMinWidth(dp(34)); b.setPadding(0,0,0,0);
        return b;
    }
}
