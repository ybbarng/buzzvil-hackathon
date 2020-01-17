/*
 * Copyright 2018 Google LLC. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.buzzvil.examples.buzzar;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.os.Build;
import android.os.Build.VERSION_CODES;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.ar.core.HitResult;
import com.google.ar.core.Plane;
import com.google.ar.core.Pose;
import com.google.ar.sceneform.ux.ArFragment;

import java.util.Timer;
import java.util.TimerTask;

/**
 * This is an example activity that uses the Sceneform UX package to make common AR tasks easier.
 */
public class MainActivity extends AppCompatActivity {
    private static final String TAG = MainActivity.class.getSimpleName();
    private static final double MIN_OPENGL_VERSION = 3.0;
    private static final long AD_LOADING_INTERVAL_MS = 30 * 1000;

    private ArFragment arFragment;
    private Button createAdButton;

    private ArManager arManager;
    private Timer timer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (!checkIsSupportedDeviceOrFinish(this)) {
            return;
        }

        setContentView(R.layout.activity_ux);
        arFragment = (ArFragment) getSupportFragmentManager().findFragmentById(R.id.ux_fragment);
        createAdButton = findViewById(R.id.create_ad_button);

        arFragment.setOnTapArPlaneListener(
                (HitResult hitResult, Plane plane, MotionEvent motionEvent) -> {

                });

        this.arManager = new ArManager(arFragment);

        createAdButton.setOnClickListener(view -> this.createAd());

        this.timer = new Timer();
        timer.scheduleAtFixedRate(buildLoadAdTask(), AD_LOADING_INTERVAL_MS, AD_LOADING_INTERVAL_MS);
    }

    /**
     * Returns false and displays an error message if Sceneform can not run, true if Sceneform can run
     * on this device.
     *
     * <p>Sceneform requires Android N on the device as well as OpenGL 3.0 capabilities.
     *
     * <p>Finishes the activity if Sceneform can not run
     */
    public static boolean checkIsSupportedDeviceOrFinish(final Activity activity) {
        if (Build.VERSION.SDK_INT < VERSION_CODES.N) {
            Log.e(TAG, "Sceneform requires Android N or later");
            Toast.makeText(activity, "Sceneform requires Android N or later", Toast.LENGTH_LONG).show();
            activity.finish();
            return false;
        }
        String openGlVersionString =
                ((ActivityManager) activity.getSystemService(Context.ACTIVITY_SERVICE))
                        .getDeviceConfigurationInfo()
                        .getGlEsVersion();
        if (Double.parseDouble(openGlVersionString) < MIN_OPENGL_VERSION) {
            Log.e(TAG, "Sceneform requires OpenGL ES 3.0 later");
            Toast.makeText(activity, "Sceneform requires OpenGL ES 3.0 or later", Toast.LENGTH_LONG)
                    .show();
            activity.finish();
            return false;
        }
        return true;
    }

    private TimerTask buildLoadAdTask() {
        return new TimerTask() {
            @Override
            public void run() {
                createAd();
            }
        };
    }

    private void createAd() {
        final Pose newPosition = arManager.getRandomPosition();
        if (newPosition == null) {
            return;
        }

        AdManager.loadAd(this)
                .thenCompose(nativeAd -> arManager.buildViewRenderable(MainActivity.this, AdManager.populateAd(MainActivity.this, nativeAd)))
                .thenAccept(adRenderable -> arManager.createAdNode(newPosition, adRenderable))
                .exceptionally(throwable -> {
                    Log.e("MYAR", "Error", throwable);
                    return null;
                });
    }
}
