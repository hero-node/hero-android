/**
 * BSD License
 * Copyright (c) Hero software.
 * All rights reserved.
 * <p>
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:
 * <p>
 * Redistributions of source code must retain the above copyright notice, this
 * list of conditions and the following disclaimer.
 * <p>
 * Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 * <p>
 * Neither the name Facebook nor the names of its contributors may be used to
 * endorse or promote products derived from this software without specific
 * prior written permission.
 * <p>
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

import android.annotation.TargetApi;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.view.View;

import com.hero.depandency.ContextUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

import static android.content.Context.CLIPBOARD_SERVICE;

/**
 * Created by R9L7NGH on 2015/12/22.
 */
public class HeroDevice extends View implements IHero {

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
        HeroView.on(this, object);
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

        if (object.has("copy")) {
            copy(object.optString("copy"));
        }
        if (object.has("paste")) {
            paste();
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



    private Message generateMessage(JSONObject object) {
        Message message = postDataHandler.obtainMessage();
        message.obj = object;
        return message;
    }

    private void copy(String content) {
        ClipboardManager clipboardManager = (ClipboardManager) context.getSystemService(CLIPBOARD_SERVICE);
        clipboardManager.setPrimaryClip(ClipData.newPlainText(HeroDevice.class.getSimpleName(), content));
    }

    private void paste() {
        ClipboardManager clipboardManager = (ClipboardManager) context.getSystemService(CLIPBOARD_SERVICE);
        ClipData data = clipboardManager.getPrimaryClip();
        if (data != null && data.getItemAt(0) != null) {
            String text = data.getItemAt(0).getText().toString();
            JSONObject actionObject = new JSONObject();
            try {
                if (HeroView.getName(this) != null) {
                    actionObject.put("name", HeroView.getName(this));
                }
                HeroView.putValueToJson(actionObject, text);
                HeroView.sendActionToContext(context, actionObject);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

}
