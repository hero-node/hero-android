package com.hero.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;

/**
 * Created by Aron on 2018/10/9.
 */

public class FingerSharedPreference {

    /**
     * 保存在手机里面的文件名
     */
    private static final String FILE_NAME = "share_date";

    public static final String dataKeyName = "dataKey";
    public static final String IVKeyName = "IV";

    private static SharedPreferences mPreferences;
    private static FingerSharedPreference mFingerSharedPreference;

    public static FingerSharedPreference getInstance(Context context) {
        if (mFingerSharedPreference == null) {
            mFingerSharedPreference = new FingerSharedPreference(context);
        }
        return mFingerSharedPreference;
    }

    public FingerSharedPreference(Context context) {
        mPreferences = context.getSharedPreferences(FILE_NAME,Context.MODE_PRIVATE);
    }

    // 获取数据
    public String getData(String keyName) {
        return mPreferences.getString(keyName, "");
    }

    // 存储数据
    public boolean storeData(String key, String data) {
        SharedPreferences.Editor editor = mPreferences.edit();
        editor.putString(key, data);
        return editor.commit();
    }

    // 判断是否包含数据
    public boolean containsKey(String key) {
        return !TextUtils.isEmpty(getData(key));
    }

    // 移除数据
    public boolean removeData(String key) {
        SharedPreferences.Editor editor = mPreferences.edit();
        editor.remove(key);
        return editor.commit();
    }

}
