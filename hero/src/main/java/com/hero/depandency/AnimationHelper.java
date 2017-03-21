/**
 * BSD License
 * Copyright (c) Hero software.
 * All rights reserved.

 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:

 * Redistributions of source code must retain the above copyright notice, this
 * list of conditions and the following disclaimer.

 * Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.

 * Neither the name Facebook nor the names of its contributors may be used to
 * endorse or promote products derived from this software without specific
 * prior written permission.

 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package com.hero.depandency;

import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.AnimationUtils;
import android.view.animation.CycleInterpolator;
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
    public static final String ANIMATION_FRAME = "frame";
    public static final String ANIMATION_FLIP = "flip";

    public static void startAnimation(View view, String animType, float time, JSONObject params, final AnimationEndListener listener) {
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
            } else if (ANIMATION_FRAME.equals(animType)) {
                if (params != null) {
                    TranslateAnimation animation1 = new TranslateAnimation(params.optInt("x"), params.optInt("toX"), params.optInt("y"), params.optInt("toY"));
                    animation1.setDuration((long) (time * 1000));
                    animation1.setRepeatCount(0);

                    animation = new AnimationSet(false);
                    animation.setFillAfter(false);
                    ((AnimationSet) animation).addAnimation(animation1);
                    if (params.has("scaleX") || params.has("scaleY")) {
                        ScaleAnimation animation2 = new ScaleAnimation(1.0f, (float) params.optDouble("scaleX"), 1.0f, (float) params.optDouble("scaleY"), Animation.ABSOLUTE, 0.0f, Animation.ABSOLUTE, 0.0f);
                        animation2.setDuration((long) (time * 1000));
                        animation2.setRepeatCount(0);
                        ((AnimationSet) animation).addAnimation(animation2);
                    }
                }
            }

            if (animation != null) {
                if (listener != null) {
                    animation.setAnimationListener(new Animation.AnimationListener() {
                        @Override
                        public void onAnimationStart(Animation animation) {

                        }

                        @Override
                        public void onAnimationEnd(Animation animation) {
                            listener.onAnimationEnd(animation);
                        }

                        @Override
                        public void onAnimationRepeat(Animation animation) {

                        }
                    });
                }
                view.startAnimation(animation);
            }
        }
    }

    public interface AnimationEndListener {
        public void onAnimationEnd(Animation animation);
    }
}
