package com.hero.utils;

import android.content.Context;
import android.content.SharedPreferences;

import org.json.JSONArray;

import java.util.List;

/**
 * Created by Aron on 2018/12/17.
 */

public class ShareUtils {
    private String path = "hero_share_data";

    private static ShareUtils prefUtils;
    private final SharedPreferences sp;

    public ShareUtils(Context context) {
        sp = context.getSharedPreferences(path, Context.MODE_PRIVATE);
    }

    public static ShareUtils getInstance(Context context) {
        if (prefUtils == null) {
            synchronized (ShareUtils.class) {
                if (prefUtils == null) {
                    prefUtils = new ShareUtils(context);
                }
            }
        }
        return prefUtils;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public void putBoolean(String key, boolean value) {
        sp.edit().putBoolean(key, value).apply();
    }

    public boolean getBoolean(String key, boolean defValue) {
        return sp.getBoolean(key, defValue);
    }

    public void putString(String key, String value) {
        sp.edit().putString(key, value).apply();
    }

    public String getString(String key, String defValue) {
        return sp.getString(key, defValue);
    }

    public void putInt(String key, int value) {
        sp.edit().putInt(key, value).apply();
    }

    public int getInt(String key, int defValue) {
        return sp.getInt(key, defValue);
    }

    public void remove(String key) {
        sp.edit().remove(key).apply();
    }

    public boolean contains(String key) {
        return sp.contains(key);
    }

    public void clear() {
        if (sp != null)
            sp.edit().clear().apply();
    }

}
