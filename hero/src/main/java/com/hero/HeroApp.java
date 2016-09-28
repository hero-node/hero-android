package com.hero;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.view.View;

import com.hero.depandency.ContextUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Objects;

/**
 * Created by R9L7NGH on 2015/12/22.
 */
public class HeroApp implements IHero {
    private Object content;

    public HeroApp(Context c) {
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    @Override
    public void on(final JSONObject object) throws JSONException {
        content = object;
    }

    public Object getContent() {
        return content;
    }
}
