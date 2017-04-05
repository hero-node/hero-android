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

 * Neither the name Hero nor the names of its contributors may be used to
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

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.os.Handler;
import android.util.AttributeSet;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;

/**
 * Created by caixin on 15/9/23.
 */
public class LoadingTextView extends TextView {

    private String[] textArray;
    private boolean isLoading = false;
    private final int STILL_DURATION = 2700;
    private final int ANIM_DURATION = 150;
    private int index;
    public static final int FADE_OUT = 0;
    public static final int FADE_IN = 1;

    protected final Handler mHandler = new Handler();

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public LoadingTextView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    public LoadingTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public LoadingTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public LoadingTextView(Context context) {
        super(context);
    }

    public void setTextArray(Object content) {
        if (content instanceof JSONArray) {
            textArray = jsonToArray((JSONArray) content);
        } else if (content instanceof String[]) {
            textArray = ((String[]) content).clone();
        } else {
            return;
        }

        index = 0;
        if (textArray.length > index) {
            setText(textArray[index]);
        }
        if (textArray.length > 1) {
            startLoading();
        }
    }

    public void startLoading() {
        isLoading = true;
        if (getVisibility() == VISIBLE) {
            mHandler.postDelayed(loadingRunnable, STILL_DURATION);
        }
    }

    public void stopLoading() {
        try {
            mHandler.removeCallbacks(loadingRunnable);
        } catch (Exception e) {
            e.printStackTrace();
        }
        isLoading = false;
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        stopLoading();
    }

    private Runnable loadingRunnable = new Runnable() {
        @Override
        public void run() {
            index++;
            if (index >= textArray.length) {
                index = 0;
            }
            startFadeAnim(textArray[index], FADE_OUT);
        }
    };

    private void startFadeAnim(final String nextText, final int type) { // 0 : fade out, 1: fade in
        float startAlpha = (type == FADE_OUT ? 1f : 0f);
        float endAlpha = (type == FADE_OUT ? 0f : 1f);
        ObjectAnimator animator = new ObjectAnimator().ofFloat(this, "alpha", startAlpha, endAlpha);
        animator.setDuration(ANIM_DURATION);
        animator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animator) {
            }

            @Override
            public void onAnimationEnd(Animator animator) {
                if (type == 0) {
                    setText(nextText);
                    startFadeAnim(nextText, FADE_IN);
                } else {
                    if (getVisibility() == VISIBLE && isLoading) {
                        mHandler.postDelayed(loadingRunnable, STILL_DURATION);
                    }
                }
            }

            @Override
            public void onAnimationCancel(Animator animator) {
            }

            @Override
            public void onAnimationRepeat(Animator animator) {
            }
        });
        animator.start();
    }

    public String[] jsonToArray(JSONArray array) {
        String strings[] = new String[array.length()];
        for (int i = 0; i < array.length(); i++) {
            try {
                strings[i] = array.getString(i);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return strings;
    }
}
