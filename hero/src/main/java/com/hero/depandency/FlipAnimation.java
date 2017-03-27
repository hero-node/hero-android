package com.hero.depandency;

import android.graphics.Camera;
import android.graphics.Matrix;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.Transformation;

/**
 * Created by xincai on 17-3-21.
 */

public class FlipAnimation extends Animation {
    private Camera camera;
    private View mainView;

    private float centerX;
    private float centerY;

    private OnFlipListener listener;
    private boolean isFlipped;

    public FlipAnimation(View view, OnFlipListener flipListener, long time) {
        this.mainView = view;
        listener = flipListener;
        setDuration(time);
        setFillAfter(false);
        setInterpolator(new LinearInterpolator());
    }

    @Override
    public void initialize(int width, int height, int parentWidth, int parentHeight) {
        super.initialize(width, height, parentWidth, parentHeight);
        centerX = width / 2;
        centerY = height / 2;
        camera = new Camera();
        isFlipped = false;
    }

    @Override
    protected void applyTransformation(float interpolatedTime, Transformation t) {
        final double radians = Math.PI * interpolatedTime;
        float degrees = (float) (180.0 * radians / Math.PI);

//        if (interpolatedTime <= 0.5f) {
//            mainView.setScaleX((1 - interpolatedTime));
//            mainView.setScaleY((1 - interpolatedTime));
//        }

        if (interpolatedTime >= 0.5f) {
            degrees -= 180.f;
            if (listener != null && !isFlipped) {
                listener.onFlippedToOpposite(mainView);
                isFlipped = true;
            }
        }
//
//        if (interpolatedTime >= 0.5f) {
//            mainView.setScaleX(interpolatedTime);
//            mainView.setScaleY(interpolatedTime);
//        }

        final Matrix matrix = t.getMatrix();
        camera.save();
        camera.translate(0, 0, Math.abs(degrees) * 5);
        camera.getMatrix(matrix);
        camera.rotateY(degrees);
        camera.getMatrix(matrix);
        camera.restore();
        matrix.preTranslate(-centerX, -centerY);
        matrix.postTranslate(centerX, centerY);
    }

    public static Animation createFlipAnimation(View rootView, OnFlipListener flipListener, long timeMillis) {
        FlipAnimation flipAnimation = new FlipAnimation(rootView, flipListener, timeMillis);
        return flipAnimation;
    }

    public interface OnFlipListener {
        public void onFlippedToOpposite(View root);
    }
}