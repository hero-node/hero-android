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

package com.hero;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.StateListDrawable;
import android.os.Build;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.hero.depandency.ImageLoadUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by liuguoping on 15/9/23.
 */
public class HeroButton extends Button implements IHero, IHeroBackground {
    private int normalColor, borderColor, pressedColor, borderWidth, disableBackgroundColor;
    private int normalTextColor, disableTextColor;
    private float cornerRadius;
    private float[] cornerRadii;
    private boolean isBackgroundValid;
    private boolean isOnDialog = false;
    protected boolean hasSystemBackground = false;

    private ClickListener clickListener = null;

    public HeroButton(Context context) {
        super(context);
    }

    public HeroButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        hasSystemBackground = true;
    }

    public HeroButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        hasSystemBackground = true;
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public HeroButton(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        hasSystemBackground = true;
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    @Override
    public void on(final JSONObject jsonObject) throws JSONException {
        HeroView.on(this, jsonObject);

        boolean hasBackground = false;
        if (jsonObject.has("title")) {
            this.setText(jsonObject.getString("title"));
        } else if (jsonObject.has("text")) {
            this.setText(jsonObject.getString("text"));
        }
        if (jsonObject.has("titleColor")) {
            normalTextColor = HeroView.parseColor("#" + jsonObject.getString("titleColor"));
            this.setTextColor(normalTextColor);
        }// else if (jsonObject.has("tinyColor")) {
        //            this.setTextColor(Color.parseColor("#" + jsonObject.getString("tinyColor")));
        //        }

        if (jsonObject.has("titleDisabledColor")) {
            disableTextColor = HeroView.parseColor("#" + jsonObject.getString("titleDisabledColor"));
            if (jsonObject.has("enable")) {
                try {
                    boolean enabled = jsonObject.getBoolean("enable");
                    if (!enabled) {
                        this.setTextColor(disableTextColor);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
        if (jsonObject.has("backgroundDisabledColor")) {
            disableBackgroundColor = HeroView.parseColor("#" + jsonObject.getString("backgroundDisabledColor"));
        }

        if (jsonObject.has("titleH")) {
            Log.w("not implements", "titleH");
        }
        if (jsonObject.has("titleColorH")) {
            Log.w("not implements", "titleColorH");
        }

        if (jsonObject.has("size")) {
            this.setTextSize(jsonObject.getInt("size"));
        }
        if (jsonObject.has("imageN")) {
            isBackgroundValid = true;
            int resId = ImageLoadUtils.getLocalImageIdByName(getContext(), jsonObject.getString("imageN"));
            if (resId != 0) {
                if (android.os.Build.VERSION.SDK_INT > 15) {
                    this.setBackground(getResources().getDrawable(resId));
                } else {
                    this.setBackgroundDrawable(getResources().getDrawable(resId));
                }
            }
        }
        if (jsonObject.has("imageH")) {
            this.setBackground(new ColorDrawable(Color.RED));
            Log.w("not implements", "imageH");
        }
        if (jsonObject.has("borderWidth")) {
            hasBackground = true;
            this.setBorderWidth(jsonObject.getInt("borderWidth"));
        }
        if (jsonObject.has("borderColor")) {
            hasBackground = true;
            this.setBorderColor(HeroView.parseColor("#" + jsonObject.getString("borderColor")));
        }

        if (jsonObject.has("cornerRadius")) {
            hasBackground = true;
            this.setCornerRadius(jsonObject.getDouble("cornerRadius"));
        }
        if (jsonObject.has("androidCornerRadii")) {
            hasBackground = true;
            this.setCornerRadiiArray(jsonObject.optJSONArray("androidCornerRadii"));
        }
        if (jsonObject.has("backgroundColor")) {
            hasBackground = true;
            this.setBackground(HeroView.parseColor("#" + jsonObject.getString("backgroundColor")));
        }

        if (jsonObject.has("click")) {
            this.setTag(R.id.kAction, jsonObject.get("click"));
        }

        if(jsonObject.has("frame")) {
            float textH = getTextSize();
            int height = getLayoutParams().height;
            float gap = getContext().getResources().getDimension(R.dimen.button_padding_gap);
            if (height < (textH + getPaddingBottom() + getPaddingTop() + gap)) {
                setPadding(0,0,0,0);
            }
        }
        this.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isOnDialog) {
                    try {
                        ((IHeroContext) v.getContext()).on(new JSONObject("{command:'closePopup'}"));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                try {
                    if (clickListener != null) {
                        clickListener.onClick();
                    }
                    Object click = v.getTag(R.id.kAction);
                    if (click != null) {
                        ((IHeroContext) v.getContext()).on(click);
                        HeroFragment.hideSoftKeyboard(v.getContext());
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        if (jsonObject.has("enable")) {
            try {
                boolean enabled;
                Object objEnabled = jsonObject.get("enable");
                if (objEnabled instanceof Integer) {
                    int enable = (int) objEnabled;
                    if (enable == 1) {
                        enabled = true;
                    } else {
                        enabled = false;
                    }
                } else {
                    enabled = jsonObject.getBoolean("enable");
                }
                if (enabled && normalTextColor != 0) {
                    this.setTextColor(normalTextColor);
                } else if (!enabled && disableTextColor != 0) {
                    this.setTextColor(disableTextColor);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        if (hasSystemBackground) {
            isBackgroundValid = true;
        } else if (hasBackground) {
            isBackgroundValid = true;
            this.invalidateBackground();
        } else {
            // if the background has been set before, do not set it to transparent
            if (!isBackgroundValid) {
                this.setBackgroundColor(Color.TRANSPARENT);
            }
        }

        if (jsonObject.has("raised")) {
            HeroView.setRaised(this, -1);
        }
        if (jsonObject.has("ripple")) {
            HeroView.setRipple(this);
        }
    }

    public void setBackground(int color) {
        normalColor = color;
        pressedColor = HeroView.getPressedColor(color, false);
    }

    public void setBorderWidth(int width) {
        borderWidth = width;
    }

    public void setBorderColor(int color) {
        borderColor = color;
    }

    public void setCornerRadius(double radius) {
        cornerRadius = HeroView.dip2px(this.getContext(), (float) radius);
    }

    public void setCornerRadiiArray(JSONArray array) {
        cornerRadii = HeroView.generateCornerRadiiArray(getContext(), array);
    }

    public void setOnDialog(boolean isOn) {
        isOnDialog = isOn;
    }

    @Override
    public void invalidateBackground() {
        if (android.os.Build.VERSION.SDK_INT > 15) {
            this.setBackground(createBackground());
        } else {
            this.setBackgroundDrawable(createBackground());
        }
    }

    private StateListDrawable createBackground() {
        int count = 2;
        StateListDrawable bg = new StateListDrawable();
        GradientDrawable drawables[] = new GradientDrawable[count];
        // background color not set
        if (normalColor == 0 && pressedColor == 0) {
            setBackground(Color.TRANSPARENT);
        }
        if (borderColor == 0) {
            borderColor = Color.TRANSPARENT;
        } else {
            // if border color is set but border width not set, default border width = 1
            if (borderWidth == 0) {
                this.setBorderWidth(1);
            }
        }
        if (cornerRadii != null) {
            drawables[0] = HeroView.createBackgroundDrawable(normalColor, borderWidth, borderColor, cornerRadii);
            drawables[1] = HeroView.createBackgroundDrawable(pressedColor, borderWidth, borderColor, cornerRadii);
        } else {
            drawables[0] = HeroView.createBackgroundDrawable(normalColor, borderWidth, borderColor, cornerRadius);
            drawables[1] = HeroView.createBackgroundDrawable(pressedColor, borderWidth, borderColor, cornerRadius);
        }

        bg.addState(new int[] {android.R.attr.state_pressed, android.R.attr.state_enabled}, drawables[1]);
        bg.addState(new int[] {android.R.attr.state_enabled}, drawables[0]);
        if (disableBackgroundColor != 0) {
            GradientDrawable disableDrawable = HeroView.createBackgroundDrawable(disableBackgroundColor, borderWidth, disableBackgroundColor, cornerRadius);
            bg.addState(new int[] {}, disableDrawable);
        } else {
            bg.addState(new int[] {}, drawables[0]);
        }
        return bg;
    }

    private StateListDrawable createTransparentBackground() {
        int count = 2;
        StateListDrawable bg = new StateListDrawable();
        ColorDrawable drawables[] = new ColorDrawable[count];
        drawables[0] = new ColorDrawable(Color.TRANSPARENT);
        drawables[1] = new ColorDrawable(HeroView.parseColor("#22222222"));

        bg.addState(new int[] {android.R.attr.state_pressed, android.R.attr.state_enabled}, drawables[1]);
        bg.addState(new int[] {android.R.attr.state_enabled}, drawables[0]);
        bg.addState(new int[] {}, drawables[0]);
        return bg;
    }

    public void setClickListener(ClickListener f) {
        clickListener = f;
    }

    public interface ClickListener {
        void onClick();
    }
}
