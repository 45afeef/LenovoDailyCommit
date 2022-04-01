package com.automate.lenovo.Activity;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.automate.lenovo.R;
import com.automate.lenovo.Service.LenovoAutomator;

import java.util.List;

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




    }
    private String querySettingPkgName() {
        Intent intent = new Intent(android.provider.Settings.ACTION_SETTINGS);
        List<ResolveInfo> resolveInfos = getPackageManager().queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);
        if (resolveInfos == null || resolveInfos.size() == 0) {
            return "";
        }

        return resolveInfos.get(0).activityInfo.packageName;
    }
}