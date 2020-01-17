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
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.ar.core.Anchor;
import com.google.ar.core.HitResult;
import com.google.ar.core.Plane;
import com.google.ar.sceneform.AnchorNode;
import com.google.ar.sceneform.rendering.FixedWidthViewSizer;
import com.google.ar.sceneform.rendering.ViewRenderable;
import com.google.ar.sceneform.ux.ArFragment;

import java.util.concurrent.CompletableFuture;

/**
 * This is an example activity that uses the Sceneform UX package to make common AR tasks easier.
 */
public class MainActivity extends AppCompatActivity {
    private static final String TAG = MainActivity.class.getSimpleName();
    private static final double MIN_OPENGL_VERSION = 3.0;

    private ArFragment arFragment;

    @Override
    @SuppressWarnings({"AndroidApiChecker", "FutureReturnValueIgnored"})
    // CompletableFuture requires api level 24
    // FutureReturnValueIgnored is not valid
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (!checkIsSupportedDeviceOrFinish(this)) {
            return;
        }

        setContentView(R.layout.activity_ux);
        arFragment = (ArFragment) getSupportFragmentManager().findFragmentById(R.id.ux_fragment);

        arFragment.setOnTapArPlaneListener(
                (HitResult hitResult, Plane plane, MotionEvent motionEvent) -> {
                    if (plane.getType() != Plane.Type.HORIZONTAL_UPWARD_FACING) {
                        return;
                    }

                    AdManager.loadAd(this)
                            .thenCompose(nativeAd -> buildViewRenderable(AdManager.populateAd(this, nativeAd)))
                            .thenAccept(adRenderable -> createAdNode(hitResult, adRenderable))
                            .exceptionally(throwable -> {
                                Log.e("MYAR", "Error", throwable);
                                return null;
                            });
                });
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

    private CompletableFuture<ViewRenderable> buildViewRenderable(final View view) {
        final CompletableFuture<ViewRenderable> result = new CompletableFuture<>();

        // When you build a Renderable, Sceneform loads its resources in the background while returning
        // a CompletableFuture. Call thenAccept(), handle(), or check isDone() before calling get().
        ViewRenderable.builder()
                .setView(this, view)
                .build()
                .thenAccept(result::complete)
                .exceptionally(
                        throwable -> {
                            this.onError("Unable to load ad view renderable");
                            result.completeExceptionally(throwable);
                            return null;
                        });

        return result;
    }

    private void onError(final String message) {
        Toast toast =
                Toast.makeText(this, message, Toast.LENGTH_LONG);
        toast.setGravity(Gravity.CENTER, 0, 0);
        toast.show();
    }

    private void createAdNode(final HitResult hitResult, final ViewRenderable adRenderable) {
        Log.d("MYAR", "createAdNode");

        // Create the Anchor.
        Anchor anchor = hitResult.createAnchor();
        AnchorNode anchorNode = new AnchorNode(anchor);
        anchorNode.setParent(arFragment.getArSceneView().getScene());

        // Create the AdView and add it to the anchor.
        AdNode adNode = new AdNode();
        adNode.setParent(anchorNode);
        adNode.setRenderable(adRenderable);
    }
}
