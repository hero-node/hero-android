package com.hero.depandency;

import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.CycleInterpolator;
import android.view.animation.Interpolator;
import android.view.animation.ScaleAnimation;
import android.view.animation.TranslateAnimation;

import com.hero.R;

import org.json.JSONObject;

/**
 * Created by xincai on 16-5-13.
 */
public class AnimationHelper {
    public static final boolean SHOW_ANIMATION = true;

    public static final String ANIMATION_SHAKE = "shake";
    public static final String ANIMATION_SCALE = "scale";
    public static final String ANIMATION_TRANSLATION = "translation";

    public static void startAnimation(View view, String animType, float time, JSONObject params, Animation.AnimationListener listener) {
        if (view != null && SHOW_ANIMATION) {
            Animation animation = null;
            if (ANIMATION_SHAKE.equals(animType)) {
                animation = AnimationUtils.loadAnimation(view.getContext(), R.anim.shake_x);
                // time in "shake" means cycle times but not duration
                if (time > 1) {
                    CycleInterpolator interpolator = new CycleInterpolator(time);
                    animation.setInterpolator(interpolator);
                }
            } else if (ANIMATION_SCALE.equals(animType)) {
                if (params != null) {
                    animation = new ScaleAnimation(1.0f, params.optInt("scaleX"), 1.0f, params.optInt("scaleY"), Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
                    animation.setDuration((long) (time * 1000));
                    animation.setRepeatCount(0);
                    animation.setFillAfter(true);
                }
            } else if (ANIMATION_TRANSLATION.equals(animType)) {
                if (params != null) {
                    animation = new TranslateAnimation(params.optInt("x"), params.optInt("toX"), params.optInt("y"), params.optInt("toY"));
                    animation.setDuration((long) (time * 1000));
                    animation.setRepeatCount(0);
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
