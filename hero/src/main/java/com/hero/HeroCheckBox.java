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
