package com.hero;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.FrameLayout;
import android.widget.Switch;

import org.json.JSONException;
import org.json.JSONObject;

public class HeroSwitch extends FrameLayout implements IHero {
    private Switch theSwitch;

    public HeroSwitch(Context context) {
        super(context);
        View switchView = LayoutInflater.from(context).inflate(R.layout.switch_button, this, false);
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT);
        this.addView(switchView, params);
        theSwitch = (Switch) switchView;
    }

    @Override
    public void on(JSONObject jsonObject) throws JSONException {
        HeroView.on(this, jsonObject);
        FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) this.getLayoutParams();
        if (params.height == -1 || params.height == 0) {
            params.height = FrameLayout.LayoutParams.WRAP_CONTENT;
        }
        params.width = FrameLayout.LayoutParams.WRAP_CONTENT;
        this.setLayoutParams(params);
        if (jsonObject.has("value")) {
            theSwitch.setChecked(jsonObject.getBoolean("value"));
        }
        theSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                JSONObject json = HeroView.getJson(HeroSwitch.this);
                if (json.has("click")) {
                    try {
                        JSONObject action = (JSONObject) json.getJSONObject("click");
                        action.put("value", isChecked);
                        ((IHeroContext) buttonView.getContext()).on(action);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }

            }
        });
    }

    public boolean isChecked() {
        return theSwitch.isChecked();
    }
}
