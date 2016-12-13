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
public class HeroDevice extends View implements IHero {

    private Context context;

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
                HeroView.putValueToJson(jsonObject.getJSONObject("deviceId"), value);
            }
            ((IHeroContext) getContext()).on(jsonObject);
        }
    }
}
