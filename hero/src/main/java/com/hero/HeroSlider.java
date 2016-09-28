package com.hero;

import android.content.Context;
import android.widget.FrameLayout;
import android.widget.SeekBar;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by liuguoping on 15/10/24.
 */
public class HeroSlider extends SeekBar implements IHero {
    public HeroSlider(Context context) {
        super(context);
        this.setMax(100);
        this.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (HeroView.getJson(seekBar).has("change")) {
                    try {
                        JSONObject change = HeroView.getJson(seekBar).getJSONObject("change");
                        change.put("value", progress);
                        ((IHeroContext) seekBar.getContext()).on(change);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
    }

    @Override
    public void on(JSONObject jsonObject) throws JSONException {
        HeroView.on(this, jsonObject);
        FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) this.getLayoutParams();
        if (params.height > 0) {
            params.height = FrameLayout.LayoutParams.WRAP_CONTENT;
            this.setLayoutParams(params);
        }
        if (jsonObject.has("value")) {
            this.setProgress(jsonObject.getInt("value") * 100);
        }

    }
}
