package com.hero;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by liuguoping on 15/9/23.
 */
public interface IHero {
    void on(JSONObject jsonObject) throws JSONException;
}
