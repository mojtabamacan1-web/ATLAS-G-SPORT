package com.atlasg.sport;

import android.app.Activity;
import android.app.AlertDialog;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.content.SharedPreferences;
import java.util.HashSet;
import java.util.Set;

public class CustomizationActivity extends Activity {
    private SharedPreferences prefs;
    private LinearLayout root;
    private int accent = Color.rgb(212,175,55);
    private boolean english;

    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        prefs = getSharedPreferences("atlas_settings", MODE_PRIVATE);
        english = "en".equals(prefs.getString("language","fa"));
        accent = Color.parseColor(prefs.getString("accent","#D4AF37"));
        buildUi();
    }

    private String s(String fa, String en) { return english ? en : fa; }
    private int dp(float v) { return (int)(v * getResources().getDisplayMetrics().density + .5f); }

    private void buildUi() {
        boolean light = "light".equals(prefs.getString("theme","dark"));
        int bg = light ? Color.rgb(247,244,232) : Color.rgb(8,8,8);
        int card = light ? Color.WHITE : Color.rgb(20,20,20);
        int fg = light ? Color.rgb(35,32,25) : Color.rgb(245,240,220);
        root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(dp(18),dp(14),dp(18),dp(24));
        root.setBackgroundColor(bg);
        root.setLayoutDirection(english ? View.LAYOUT_DIRECTION_LTR : View.LAYOUT_DIRECTION_RTL);

        ScrollView scroll = new ScrollView(this);
        scroll.setFillViewport(true);
        LinearLayout content = new LinearLayout(this);
        content.setOrientation(LinearLayout.VERTICAL);
        scroll.addView(content);
        root.addView(scroll, new LinearLayout.LayoutParams(-1,-1));

        TextView title = text(s("مرکز شخصی‌سازی","PERSONALIZATION CENTER"),24,accent,true);
        content.addView(title);
        TextView subtitle = text(s("داشبورد خودت را طراحی کن","Build your own dashboard"),14,fg,false);
        content.addView(subtitle);

        section(content,s("زبان برنامه","APP LANGUAGE"),fg);
        LinearLayout languages = row(content);
        addButton(languages,"فارسی",card,fg,()->setLanguage("fa"));
        addButton(languages,"English",card,fg,()->setLanguage("en"));

        section(content,s("پوسته و ظاهر","THEME & APPEARANCE"),fg);
        LinearLayout themes = row(content);
        addButton(themes,s("مشکی و کرم","Dark · Black & Cream"),card,fg,()->setTheme("dark"));
        addButton(themes,s("سفید و کرم","Light · White & Cream"),card,fg,()->setTheme("light"));

        section(content,s("رنگ تأکیدی","ACCENT COLOR"),fg);
        LinearLayout colors = row(content);
        addColor(colors,"#D4AF37"); addColor(colors,"#C58B5A"); addColor(colors,"#55A7A0"); addColor(colors,"#8D83C7"); addColor(colors,"#D46A6A");

        section(content,s("مدیریت پنل‌های صفحه اصلی","HOME WIDGETS"),fg);
        addToggle(content,s("پنل آب‌وهوا","Weather panel"),"show_weather",true,fg);
        addToggle(content,s("نمایش خودروی اطلس G","Atlas G vehicle panel"),"show_car",true,fg);
        addToggle(content,s("پنل فشار باد تایرها","Tyre pressure panel"),"show_tyres",true,fg);
        addToggle(content,s("نوار ابزار پایین","Bottom dock"),"show_dock",true,fg);
        addToggle(content,s("نمایش سرعت‌سنج","Speedometer"),"show_speed",true,fg);
        addToggle(content,s("پنل موسیقی","Music player"),"show_music",true,fg);

        section(content,s("ویرایش چیدمان داشبورد","DASHBOARD LAYOUT"),fg);\n        Button editor = button(s("باز کردن ویرایشگر ابزارک‌ها","OPEN WIDGET EDITOR"),card,fg);\n        editor.setOnClickListener(v -> startActivity(new android.content.Intent(this, DashboardEditorActivity.class)));\n        content.addView(editor,params(-1,dp(52)));\n\n        section(content,s("پروفایل‌های داشبورد","DASHBOARD PROFILES"),fg);
        LinearLayout profiles = row(content);
        addButton(profiles,s("ذخیره پروفایل","Save profile"),card,fg,this::saveProfile);
        addButton(profiles,s("بازیابی پروفایل","Load profile"),card,fg,this::loadProfile);

        Button reset = button(s("بازگردانی تنظیمات اولیه","Restore defaults"),Color.rgb(90,45,35),Color.WHITE);
        reset.setOnClickListener(v -> new AlertDialog.Builder(this)
            .setMessage(s("همه تنظیمات ظاهری به حالت اولیه برگردد؟","Restore all appearance settings?"))
            .setNegativeButton(s("لغو","Cancel"),null)
            .setPositiveButton(s("بازگردانی","Restore"),(d,w)->{prefs.edit().clear().putString("language",english?"en":"fa").apply();buildUi();})
            .show());
        content.addView(reset,params(-1,dp(52)));
        Button back = button(s("بازگشت به داشبورد","BACK TO DASHBOARD"),accent,Color.BLACK);
        back.setOnClickListener(v->finish());
        content.addView(back,params(-1,dp(54)));
        setContentView(root);
    }

    private void setTheme(String value) { prefs.edit().putString("theme",value).apply(); buildUi(); }
    private void setLanguage(String value) {
        prefs.edit().putString("language",value).apply();
        english = "en".equals(value);
        buildUi();
    }
    private void addColor(LinearLayout parent,String hex) {
        Button b=button("●",Color.parseColor(hex),Color.WHITE);
        b.setTextSize(22); b.setOnClickListener(v->{prefs.edit().putString("accent",hex).apply();accent=Color.parseColor(hex);buildUi();});
        parent.addView(b,new LinearLayout.LayoutParams(0,dp(48),1));
    }
    private void addToggle(LinearLayout parent,String label,String key,boolean def,int fg) {
        CheckBox c=new CheckBox(this); c.setText(label); c.setTextColor(fg); c.setTextSize(16);
        c.setButtonTintList(android.content.res.ColorStateList.valueOf(accent));
        c.setChecked(prefs.getBoolean(key,def));
        c.setOnCheckedChangeListener((v,checked)->prefs.edit().putBoolean(key,checked).apply());
        parent.addView(c,params(-1,dp(48)));
    }
    private void saveProfile() {
        EditText input=new EditText(this); input.setSingleLine(true); input.setHint(s("نام پروفایل","Profile name"));
        new AlertDialog.Builder(this).setTitle(s("ذخیره چیدمان فعلی","Save current setup")).setView(input)
            .setNegativeButton(s("لغو","Cancel"),null).setPositiveButton(s("ذخیره","Save"),(d,w)->{
                String name=input.getText().toString().trim(); if(name.isEmpty()) return;
                String data=prefs.getString("theme","dark")+"|"+prefs.getString("accent","#D4AF37")+"|"+
                    prefs.getBoolean("show_weather",true)+"|"+prefs.getBoolean("show_car",true)+"|"+
                    prefs.getBoolean("show_tyres",true)+"|"+prefs.getBoolean("show_dock",true)+"|"+
                    prefs.getBoolean("show_speed",true)+"|"+prefs.getBoolean("show_music",true);
                prefs.edit().putString("profile_"+name,data).apply();
                Set<String> names=new HashSet<>(prefs.getStringSet("profile_names",new HashSet<>())); names.add(name);
                prefs.edit().putStringSet("profile_names",names).apply();
            }).show();
    }
    private void loadProfile() {
        Set<String> names=prefs.getStringSet("profile_names",new HashSet<>());
        if(names.isEmpty()){new AlertDialog.Builder(this).setMessage(s("هنوز پروفایلی ذخیره نشده است.","No saved profiles yet.")).setPositiveButton("OK",null).show();return;}
        String[] items=names.toArray(new String[0]);
        new AlertDialog.Builder(this).setTitle(s("انتخاب پروفایل","Choose profile")).setItems(items,(d,which)->{
            String[] p=prefs.getString("profile_"+items[which],"").split("\\|");
            if(p.length==8){SharedPreferences.Editor e=prefs.edit().putString("theme",p[0]).putString("accent",p[1])
                .putBoolean("show_weather",Boolean.parseBoolean(p[2])).putBoolean("show_car",Boolean.parseBoolean(p[3]))
                .putBoolean("show_tyres",Boolean.parseBoolean(p[4])).putBoolean("show_dock",Boolean.parseBoolean(p[5]))
                .putBoolean("show_speed",Boolean.parseBoolean(p[6])).putBoolean("show_music",Boolean.parseBoolean(p[7]));e.apply();accent=Color.parseColor(p[1]);buildUi();}
        }).show();
    }
    private void section(LinearLayout p,String label,int color){TextView t=text(label,13,accent,true);t.setPadding(0,dp(18),0,dp(6));p.addView(t);}
    private TextView text(String value,int size,int color,boolean bold){TextView t=new TextView(this);t.setText(value);t.setTextSize(size);t.setTextColor(color);if(bold)t.setTypeface(Typeface.DEFAULT,Typeface.BOLD);return t;}
    private LinearLayout row(LinearLayout p){LinearLayout r=new LinearLayout(this);r.setOrientation(LinearLayout.HORIZONTAL);r.setGravity(Gravity.CENTER_VERTICAL);p.addView(r,params(-1,dp(58)));return r;}
    private void addButton(LinearLayout p,String label,int bg,int fg,Runnable action){Button b=button(label,bg,fg);b.setOnClickListener(v->action.run());LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(0,dp(48),1);lp.setMargins(dp(3),0,dp(3),0);p.addView(b,lp);}
    private Button button(String label,int bg,int fg){Button b=new Button(this);b.setText(label);b.setTextColor(fg);b.setTextSize(13);b.setAllCaps(false);GradientDrawable d=new GradientDrawable();d.setColor(bg);d.setCornerRadius(dp(12));b.setBackground(d);return b;}
    private LinearLayout.LayoutParams params(int w,int h){LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(w,h);p.bottomMargin=dp(5);return p;}
}
