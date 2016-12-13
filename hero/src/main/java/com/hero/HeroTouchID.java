package com.hero;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.view.View;

import com.hero.depandency.ContextUtils;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by R9L7NGH on 2015/12/22.
 */
public class HeroTouchID extends View implements IHero {

    public HeroTouchID(Context c) {
        super(c);
    }

    @Override
    public void on(final JSONObject object) throws JSONException {
        if (object.has("checkEnable")) {
            JSONObject jsonObject = object.getJSONObject("checkEnable");
            HeroView.putValueToJson(jsonObject, false);
            ((IHeroContext) getContext()).on(jsonObject);
        }
    }
}
