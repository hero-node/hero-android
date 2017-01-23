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

import android.Manifest;
import android.annotation.TargetApi;
import android.content.ContentResolver;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.view.View;

import com.hero.depandency.ContextUtils;
import com.hero.depandency.MPermissionUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

import rx.functions.Action1;

/**
 * Created by R9L7NGH on 2015/12/22.
 */
public class HeroDevice extends View implements IHero {
    public static final int SMS_POST_COUNT = 50;

    private Context context;

    private Handler postDataHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (msg.obj != null && msg.obj instanceof JSONObject) {
                HeroView.sendActionToContext(getContext(), (JSONObject) msg.obj);
            }
        }
    };

    public HeroDevice(Context c) {
        super(c);
        context = c;
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    @Override
    public void on(final JSONObject object) throws JSONException {
        if (object.has("getInfo")) {
            JSONObject jsonObject = object.getJSONObject("getInfo");
            if (jsonObject.has("appInfo")) {
                HeroView.putValueToJson(jsonObject.getJSONObject("appInfo"), ContextUtils.getSimpleVersionName(context));
            }

            if (jsonObject.has("sysInfo")) {
                HeroView.putValueToJson(jsonObject.getJSONObject("sysInfo"), ContextUtils.getSystemVersion());
            }

            if (jsonObject.has("deviceInfo")) {
                HeroView.putValueToJson(jsonObject.getJSONObject("deviceInfo"), ContextUtils.getDeviceName());
            }
            if (jsonObject.has("channel")) {
                HeroView.putValueToJson(jsonObject.getJSONObject("channel"), ContextUtils.getChannel(jsonObject.optJSONObject("channel").optString("type")));
            }
            if (jsonObject.has("UMDeviceToken")) {
                HeroView.putValueToJson(jsonObject.getJSONObject("UMDeviceToken"), ContextUtils.getDeviceToken(getContext()));
            }
            if (jsonObject.has("deviceId")) {
                JSONObject value = new JSONObject();
                value.put("imei", ContextUtils.getIMEI(context));
                value.put("androidId", ContextUtils.getAndroidId(context));
                value.put("uuid", ContextUtils.getUUID(context));
                HeroView.putValueToJson(jsonObject.getJSONObject("deviceId"), value);
            }
            ((IHeroContext) getContext()).on(jsonObject);
        }
        if (object.has("getAppList")) {
            postInstalledApp(object.getJSONObject("getAppList"));
        }
        if (object.has("getSms")) {
            final JSONObject smsObject = object.getJSONObject("getSms");
            MPermissionUtils.requestPermissionAndCall(getContext(), Manifest.permission.READ_SMS, new Action1<Boolean>() {
                @Override
                public void call(Boolean aBoolean) {
                    if (aBoolean) {
                        postSms(smsObject);
                    } else {
                        postSmsDataOnce(smsObject, new JSONArray());
                    }
                }
            });
        }
    }

    private void postInstalledApp(final JSONObject jsonObject) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    JSONArray jsonArray = getAppList();
                    JSONObject value = new JSONObject();
                    value.put("appList", jsonArray);
                    value.put("system", "ANDROID");
                    HeroView.putValueToJson(jsonObject, value);
                    postDataHandler.sendMessage(generateMessage(jsonObject));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    private JSONArray getAppList() {
        PackageManager pm = context.getPackageManager();
        List<ApplicationInfo> apps = pm.getInstalledApplications(PackageManager.GET_META_DATA);
        JSONArray jsonArray = new JSONArray();
        for (ApplicationInfo app : apps) {
            if (pm.getLaunchIntentForPackage(app.packageName) != null) {
                if ((app.flags & ApplicationInfo.FLAG_UPDATED_SYSTEM_APP) == 1) {
                    // updated system apps

                } else if ((app.flags & ApplicationInfo.FLAG_SYSTEM) == 1) {
                    // system apps

                } else {
                    // user installed apps
                    jsonArray.put(app.packageName);
                }
            }
        }
        return jsonArray;
    }

    private void postSms(final JSONObject jsonObject) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    JSONArray jsonArray = getAllSms();
                    if (SMS_POST_COUNT < jsonArray.length()) {
                        // if sms count is too much, send them every SMS_POST_COUNT items
                        JSONArray array = new JSONArray();
                        for (int count = 0; count < jsonArray.length(); count++) {
                            if (array.length() == SMS_POST_COUNT) {
                                array = new JSONArray();
                            }
                            array.put(jsonArray.get(count));
                            if (array.length() == SMS_POST_COUNT || count == jsonArray.length() - 1) {
                                JSONObject copyOfObject = new JSONObject(jsonObject.toString());
                                postSmsDataOnce(copyOfObject, array);
                                Thread.sleep(500);
                            }
                        }
                    } else {
                        postSmsDataOnce(jsonObject, jsonArray);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }


            }
        }).start();
    }

    private void postSmsDataOnce(JSONObject jsonObject, JSONArray array) {
        JSONObject value = new JSONObject();
        try {
            value.put("smsList", array);
            value.put("system", "ANDROID");
            HeroView.putValueToJson(jsonObject, value);
            postDataHandler.sendMessage(generateMessage(jsonObject));
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private Message generateMessage(JSONObject object) {
        Message message = postDataHandler.obtainMessage();
        message.obj = object;
        return message;
    }

    private boolean requestSmsPermission() {
        return MPermissionUtils.checkAndRequestPermission(getContext(), Manifest.permission.READ_SMS, MPermissionUtils.HERO_PERMISSION_SMS);
    }

    private JSONArray getAllSms() {
        final String SORT_ORDER = "date DESC";
        final String SMS_URI = "content://sms";
        final String SMS_COL_BODY = "body";
        final String SMS_COL_ADDRESS = "address";
        final String SMS_COL_PERSON = "person";
        final String SMS_COL_DATE = "date";
        final String SMS_COL_TYPE = "type";

        JSONArray jsonArray = new JSONArray();
        if (!requestSmsPermission()) {
            return jsonArray;
        }

        final ContentResolver resolver = getContext().getContentResolver();
        Uri uri = Uri.parse(SMS_URI);

        String[] projection = new String[] {SMS_COL_ADDRESS, SMS_COL_DATE, SMS_COL_BODY, SMS_COL_TYPE, SMS_COL_PERSON};
        String selection = "type < 3";
        String sortOrder = SORT_ORDER;

        Cursor cursor = resolver.query(uri, projection, selection, null, sortOrder);
        if (cursor != null && cursor.moveToFirst()) {
            do {
                JSONObject item = new JSONObject();
                try {
                    String address = cursor.getString(0);
                    long date = cursor.getLong(1);
                    String body = cursor.getString(2);
                    int type = cursor.getInt(3);
                    item.put("address", address);
                    item.put("date", date);
                    item.put("body", body);
                    // 1 : inbox; 2 : outbox
                    item.put("type", type == 1 ? "INBOX" : "OUTBOX");
                    jsonArray.put(item);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            } while (cursor.moveToNext());
        }
        cursor.close();
        return jsonArray;
    }
}
