package com.automate.lenovo.Activity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.media.AudioManager;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.automate.lenovo.R;
import com.automate.lenovo.Service.LenovoAutomator;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

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

        Button disableBtn = findViewById(R.id.btn_disable_apps);
        disableBtn.setOnClickListener(view -> {
//            No Activity found to handle the Intent
//            startActivity(new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS));
            startActivity(new Intent(Settings.ACTION_MANAGE_APPLICATIONS_SETTINGS));

        });

        findViewById(R.id.button2).setOnClickListener(view -> {
           startActivity(new Intent(Settings.ACTION_MANAGE_ALL_APPLICATIONS_SETTINGS));
        });


        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference countsRef = db.collection("data").document("counts");



        countsRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        Log.d(TAG, "DocumentSnapshot data: " + document.getData());
                        Long buttonClicks = document.getLong("ButtonClicks");
                        if(buttonClicks != null && buttonClicks < 50){
                            Button startAutomationBtn = findViewById(R.id.start_automation_btn);
                            startAutomationBtn.setEnabled(true);
                            startAutomationBtn.setOnClickListener(view -> {

                                // Atomically increment the population of the city by 1.
                                countsRef.update("ButtonClicks", FieldValue.increment(1));

                                SharedPreferences sharedpreferences = getSharedPreferences("LenovoAutomator", Context.MODE_PRIVATE);
                                SharedPreferences.Editor editor = sharedpreferences.edit();
                                editor.putBoolean("CANIRUN", true);
                                editor.commit();

                                // Set Audio
                                AudioManager audioManager = (AudioManager) getSystemService(AUDIO_SERVICE);
                                audioManager.setStreamVolume(AudioManager.STREAM_ALARM,audioManager.getStreamMinVolume(AudioManager.STREAM_ALARM),0);
                                audioManager.setStreamVolume(AudioManager.STREAM_MUSIC,audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)/2,0);
                                audioManager.setStreamVolume(AudioManager.STREAM_NOTIFICATION,audioManager.getStreamMinVolume(AudioManager.STREAM_NOTIFICATION),0);
                                // Audio set successfully
                                startActivity(new Intent(Settings.ACTION_SETTINGS));
                            });
                        }
                    } else {
                        Log.d(TAG, "No such document");
                    }
                } else {
                    Log.d(TAG, "get failed with ", task.getException());
                }
            }
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