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

package com.hero.depandency;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Base64;

import com.hero.HeroApplication;

import java.util.UUID;

public class ContextUtils {

    // 以此channel标记GooglePlay应用
    private static final String CHANNEL_GOOGLE = "play.google.com";

    public static final String PREFERENCES_NAME = "CONTEXT";

    public static String getVersionName(Context context) {
        return getPackageInfo(context).versionName;
    }

    public static String getSimpleVersionName(Context context) {
        String versionName = getPackageInfo(context).versionName;
        if (!TextUtils.isEmpty(versionName)) {
            String[] sections = versionName.split("\\.");
            if (sections.length > 3) {
                int lastIndex = versionName.lastIndexOf('.');
                if (lastIndex > 0) {
                    versionName = versionName.substring(0, lastIndex);
                }
            }
        }
        return versionName;
    }

    public static int getVersionCode(Context context) {
        return getPackageInfo(context).versionCode;
    }

    public static PackageInfo getPackageInfo(Context context) {
        try {
            return context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
        } catch (NameNotFoundException e) {
        }
        return null;
    }

    public static Bundle getMetaData() {
        try {
            return HeroApplication.getInstance().getPackageManager().getApplicationInfo(HeroApplication.getInstance().getPackageName(), PackageManager.GET_META_DATA).metaData;
        } catch (NameNotFoundException e) {
        }
        return null;
    }

    public static String getChannel(String channelType) {
        String channel = getMetaData().getString(channelType);
        if (channel == null) {
            channel = "UNKNOWN";
        }
        return channel;
    }

    public static String getUmengChannel() {
        return getChannel("UMENG_CHANNEL");
    }

    public static String getDeviceName() {
        return Build.MODEL;
    }

    public static String getDeviceBrand() {
        return Build.BRAND;
    }

    public static String getSystemVersion() {
        return "" + Build.VERSION.SDK_INT;
    }

    public static String getMetaData(Context context, String key) {
        String data = null;
        try {
            PackageManager packageManager = context.getPackageManager();
            if (packageManager != null) {
                ApplicationInfo applicationInfo = packageManager.getApplicationInfo(context.getPackageName(), PackageManager.GET_META_DATA);
                if (applicationInfo != null) {
                    if (applicationInfo.metaData != null) {
                        data = applicationInfo.metaData.getString(key);
                    }
                }
            }
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return data;
    }

    public static String getLocalPath(Context context) {
        return context.getFilesDir().getAbsolutePath();
    }

    public static String getExternalPath() {
        return Environment.getExternalStorageDirectory().getAbsolutePath();
    }

    public static void saveDeviceToken(Context context, String token) {
        SharedPreferences sp = context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE);
        sp.edit().putString("deviceToken", token).commit();
    }

    public static String getDeviceToken(Context context) {
        SharedPreferences sp = context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE);
        return sp.getString("deviceToken", "");
    }

    public static void saveDeviceID(Context context, String id) {
        SharedPreferences sp = context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE);
        sp.edit().putString("deviceID", id).commit();
    }

    public static String getDeviceID(Context context) {
        SharedPreferences sp = context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE);
        return sp.getString("deviceID", "");
    }

    public static String getPageName(String url) {
        if (url == null) {
            return null;
        }
        int start = url.lastIndexOf("/");
        int end = url.lastIndexOf(".");
        if (start >= 0) {
            if (start  + 1 < end) {
                return url.substring(start + 1, end);
            } else {
                return url.substring(start + 1, url.length());
            }
        }
        return url;
    }

    public static String getIMEI(Context context) {
        String deviceId = getDeviceID(context);

        if (!TextUtils.isEmpty(deviceId)) {
            return deviceId;
        }

        try {
            deviceId = ((TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE)).getDeviceId();
        } catch (SecurityException e) {
            // no permission
        } catch (Exception e1) {
            e1.printStackTrace();
        }

        if (TextUtils.isEmpty(deviceId)) {
            return "";
        }

        saveDeviceID(context, deviceId);
        return deviceId;
    }

    public static String getAdbSN() {
        try {
            String sn = Build.SERIAL;
            return sn;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    public static String getAndroidId(Context context) {
        try {
            return Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    public static void saveUUID(Context context, String id) {
        SharedPreferences sp = context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE);
        sp.edit().putString("heroUUID", id).commit();
    }

    public static String getUUID(Context context) {
        SharedPreferences sp = context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE);
        String uuid = sp.getString("heroUUID", "");
        if (TextUtils.isEmpty(uuid)) {
            uuid = generateUUID();
            saveUUID(context, uuid);
        }
        return uuid;
    }

    private static String generateUUID() {
        String uuid = UUID.randomUUID().toString();
        return Base64.encodeToString(uuid.getBytes(), Base64.DEFAULT);
    }
}
