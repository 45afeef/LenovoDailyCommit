package com.automate.lenovo.Activity;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.media.AudioManager;
import android.os.Bundle;
import android.provider.Settings;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.automate.lenovo.R;
import com.automate.lenovo.Service.LenovoAutomator;

import java.util.List;
import java.util.Set;

public class MainActivity extends AppCompatActivity {

    final String TAG = "Lenovo_Main_Activity";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button openSettingBtn = findViewById(R.id.btn_open_settings);
        openSettingBtn.setOnClickListener(view -> {
            startActivity(new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS));
        });

        Button startBtn = findViewById(R.id.btn_start_automation);
        startBtn.setOnClickListener(view -> {
            startService(new Intent(this, LenovoAutomator.class));
        });

        Button allowFromChrome = findViewById(R.id.allow_from_chrome_btn);
        allowFromChrome.setOnClickListener(view -> {
            startActivity(new Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES));
        });

        Button disableBtn = findViewById(R.id.btn_disable_apps);
        disableBtn.setOnClickListener(view -> {
//            No Activity found to handle the Intent
//            startActivity(new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS));
            startActivity(new Intent(Settings.ACTION_MANAGE_APPLICATIONS_SETTINGS));

        });

        findViewById(R.id.button2).setOnClickListener(view -> {
           startActivity(new Intent(Settings.ACTION_MANAGE_ALL_APPLICATIONS_SETTINGS));
        });
        findViewById(R.id.button3).setOnClickListener(view -> {

            // Set Audio
            AudioManager audioManager = (AudioManager) getSystemService(AUDIO_SERVICE);
            audioManager.setStreamVolume(AudioManager.STREAM_ALARM,audioManager.getStreamMinVolume(AudioManager.STREAM_ALARM),0);
            audioManager.setStreamVolume(AudioManager.STREAM_MUSIC,audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)/2,0);
            audioManager.setStreamVolume(AudioManager.STREAM_NOTIFICATION,audioManager.getStreamMinVolume(AudioManager.STREAM_NOTIFICATION),0);
            // Audio set successfully
            startActivity(new Intent(Settings.ACTION_SETTINGS));
//            Settings.ACTION_APPLICATION_SETTINGS

        });
    }

    private String querySettingPkgName() {
        Intent intent = new Intent(Settings.ACTION_HOME_SETTINGS);
        List<ResolveInfo> resolveInfos = getPackageManager().queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);
        if (resolveInfos == null || resolveInfos.size() == 0) {
            return "";
        }

        return resolveInfos.get(0).activityInfo.packageName;
    }
}