package com.buzzvil.examples.buzzar;

import android.content.Context;
import android.view.View;

import androidx.annotation.Nullable;

import com.google.ar.core.Anchor;
import com.google.ar.core.Plane;
import com.google.ar.core.Pose;
import com.google.ar.core.Session;
import com.google.ar.sceneform.AnchorNode;
import com.google.ar.sceneform.rendering.FixedWidthViewSizer;
import com.google.ar.sceneform.rendering.ViewRenderable;
import com.google.ar.sceneform.ux.ArFragment;

import java.util.Random;
import java.util.concurrent.CompletableFuture;

public class ArManager {
    final ArFragment arFragment;
    final Random random;

    public ArManager(final ArFragment arFragment) {
        this.arFragment = arFragment;
        this.random = new Random();
    }

    @Nullable
    public Pose getRandomPosition() {
        final Plane plane = getRandomPlane();
        if (plane == null) {
            return null;
        }
        return getRandomPositionIn(plane);
    }

    public AdNode createAdNode(final Pose position, final ViewRenderable adRenderable) {
        return createAdNode(createAnchor(position), adRenderable);
    }

    public AdNode createAdNode(final AnchorNode anchorNode, final ViewRenderable adRenderable) {
        final AdNode adNode = new AdNode();
        adNode.setParent(anchorNode);
        adNode.setRenderable(adRenderable);
        return adNode;
    }

    public CompletableFuture<ViewRenderable> buildViewRenderable(final Context activityContext, final View view) {
        final CompletableFuture<ViewRenderable> result = new CompletableFuture<>();

        // When you build a Renderable, Sceneform loads its resources in the background while returning
        // a CompletableFuture. Call thenAccept(), handle(), or check isDone() before calling get().
        ViewRenderable.builder()
                .setView(activityContext, view)
                .setSizer(new FixedWidthViewSizer(0.3f))
                .build()
                .thenAccept(result::complete)
                .exceptionally(throwable -> {
                    result.completeExceptionally(throwable);
                    return null;
                });

        return result;
    }


    private AnchorNode createAnchor(final Pose position) {
        final Anchor anchor = arFragment.getArSceneView().getSession().createAnchor(position);
        final AnchorNode anchorNode = new AnchorNode(anchor);
        anchorNode.setParent(arFragment.getArSceneView().getScene());
        return anchorNode;
    }

    private Plane getRandomPlane() {
        final Session session = arFragment.getArSceneView().getSession();
        if (session == null) {
            return null;
        }
        final Plane[] planes = session.getAllTrackables(Plane.class).toArray(new Plane[0]);
        if (planes.length == 0) {
            return null;
        }

        int selectedIndex;
        do {
            selectedIndex = random.nextInt(planes.length);
        } while (planes[selectedIndex].getType() != Plane.Type.HORIZONTAL_UPWARD_FACING);

        return planes[selectedIndex];
    }

    /**
     * find a random spot on the plane in the X
     * The width of the plan is 2*extentX in the range center.x +/- extentX
     * https://github.com/google-ar/sceneform-android-sdk/issues/490#issuecomment-452852833
     *
     * @param plane
     * @return pose
     */
    private Pose getRandomPositionIn(final Plane plane) {
        float maxX = plane.getExtentX() * 2 / 2;
        float randomX = (maxX * random.nextFloat()) - plane.getExtentX() / 2;

        float maxZ = plane.getExtentZ() * 2 / 2;
        float randomZ = (maxZ * random.nextFloat()) - plane.getExtentZ() / 2;

        Pose pose = plane.getCenterPose();
        float[] translation = pose.getTranslation();
        float[] rotation = pose.getRotationQuaternion();

        translation[0] += randomX;
        translation[2] += randomZ;
        return new Pose(translation, rotation);
    }
}
