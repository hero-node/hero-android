package com.hero.depandency;

import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.CycleInterpolator;
import android.view.animation.Interpolator;

import com.hero.R;

/**
 * Created by xincai on 16-5-13.
 */
public class AnimationHelper {
    public static final boolean SHOW_ANIMATION = true;

    public static final String ANIMATION_SHAKE = "shake";

    public static void startAnimation(View view, String animType, float time, Animation.AnimationListener listener) {
        if (view != null && SHOW_ANIMATION) {
            Animation animation = null;
            if (ANIMATION_SHAKE.equals(animType)) {
                animation = AnimationUtils.loadAnimation(view.getContext(), R.anim.shake_x);
                // time in "shake" means cycle times but not duration
                if (time > 1) {
                    CycleInterpolator interpolator = new CycleInterpolator(time);
                    animation.setInterpolator(interpolator);
                }
            }
            if (animation != null) {
                if (listener != null) {
                    animation.setAnimationListener(listener);
                }
                view.startAnimation(animation);
            }
        }
    }
}
