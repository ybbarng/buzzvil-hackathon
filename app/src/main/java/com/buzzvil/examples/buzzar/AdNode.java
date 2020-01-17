package com.buzzvil.examples.buzzar;

import android.view.MotionEvent;

import com.google.ar.sceneform.HitTestResult;
import com.google.ar.sceneform.Node;

public class AdNode extends Node implements Node.OnTapListener {
    @Override
    public void onTap(HitTestResult hitTestResult, MotionEvent motionEvent) {
        select();
    }

    private void select() {

    }
}
