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

package com.hero;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.view.View;
import android.widget.Checkable;
import android.widget.ImageView;

import com.hero.depandency.ImageLoadUtils;

import org.json.JSONException;
import org.json.JSONObject;

public class HeroCheckBox extends ImageView implements IHero, Checkable {
    private boolean isChecked;
    private String selectedIconUrl;
    private String unselectedIconUrl;
    private Drawable selectedDrawable;
    private Drawable unselectedDrawable;

    public HeroCheckBox(Context context) {
        super(context);
        this.setScaleType(ScaleType.CENTER_INSIDE);
        selectedDrawable = getResources().getDrawable(android.R.drawable.checkbox_on_background);
        unselectedDrawable = getResources().getDrawable(android.R.drawable.checkbox_off_background);
    }

    @Override
    public void on(JSONObject jsonObject) throws JSONException {
        HeroView.on(this, jsonObject);
        if (jsonObject.has("click")) {
            this.setTag(R.id.kAction, jsonObject.get("click"));
        }

        if (jsonObject.has("checked")) {
            this.setChecked(jsonObject.getBoolean("checked"));
        }

        // has self-defined button
        Resources res = getContext().getResources();
        if (jsonObject.has("selectedImage") && jsonObject.has("unselectedImage")) {
            selectedIconUrl = jsonObject.getString("selectedImage");
            unselectedIconUrl = jsonObject.getString("unselectedImage");
            if (!TextUtils.isEmpty(selectedIconUrl) && !TextUtils.isEmpty(unselectedIconUrl)) {
                //                this.setButtonDrawable(android.R.color.transparent);
                if (!selectedIconUrl.startsWith("http")) {
                    int resId = ImageLoadUtils.getLocalImageIdByName(getContext(), selectedIconUrl);
                    if (resId != 0) {
                        selectedDrawable = res.getDrawable(resId);
                    }
                }
                if (!unselectedIconUrl.startsWith("http")) {
                    int resId = ImageLoadUtils.getLocalImageIdByName(getContext(), unselectedIconUrl);
                    if (resId != 0) {
                        unselectedDrawable = res.getDrawable(resId);
                    }
                }
            }
        }
        setCheckIcon();

        this.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                HeroCheckBox.this.toggle();
                Object click = view.getTag(R.id.kAction);
                if (click != null) {
                    try {
                        JSONObject action = (JSONObject) click;
                        HeroView.putValueToJson(action, isChecked);
                        ((IHeroContext) getContext()).on(action);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    private void setCheckIcon() {
        if (selectedIconUrl != null && unselectedIconUrl != null) {
            if (selectedIconUrl.startsWith("http") && isChecked()) {
                ImageLoadUtils.LoadImage(this, selectedIconUrl);
                return;
            } else if (unselectedIconUrl.startsWith("http") && !isChecked()) {
                ImageLoadUtils.LoadImage(this, unselectedIconUrl);
                return;
            }
        }
        if (selectedDrawable == null || unselectedDrawable == null) return;
        if (this.isChecked()) {
            this.setImageDrawable(selectedDrawable);
        } else {
            this.setImageDrawable(unselectedDrawable);
        }
    }

    @Override
    public boolean isChecked() {
        return isChecked;
    }

    @Override
    public void setChecked(boolean b) {
        isChecked = b;
        setCheckIcon();
    }

    @Override
    public void toggle() {
        isChecked = !isChecked;
        setCheckIcon();
    }
}
