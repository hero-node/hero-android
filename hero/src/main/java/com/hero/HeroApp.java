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

package com.hero;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by R9L7NGH on 2015/12/22.
 */
public class HeroApp implements IHero {
    public static final String ACTION_HOME = "hero.intent.action.HOME";
    private Object content;
    private Context context;
    private LocalBroadcastManager broadcastManager;

    public static final String HEROAPP_BADGE = "badgeValue";
    public static final String HEROAPP_TAB_CHANGED = "tabSelect";
    public static final String HEROAPP_NEW_APP = "newApp";

    public static final String HEROAPP_EXTRA_BADGE_VALUE = "badgeValue";
    public static final String HEROAPP_EXTRA_BADGE_INDEX = "index";

    public HeroApp(Context c) {
        context = c;
        broadcastManager = LocalBroadcastManager.getInstance(c);
    }

    @Override
    public void on(final JSONObject object) throws JSONException {
        if (object.has("tabs")) {
            content = object;
        } else {
            // send a local broadcast
            if ("HeroApp".equals(object.optString("key"))) {
                Intent intent = new Intent();
                if (object.has(HEROAPP_BADGE)) {
                    JSONObject data = object.getJSONObject(HEROAPP_BADGE);
                    intent.setAction(HEROAPP_BADGE);
                    intent.putExtra(HEROAPP_EXTRA_BADGE_INDEX, data.optInt(HEROAPP_EXTRA_BADGE_INDEX));
                    intent.putExtra(HEROAPP_EXTRA_BADGE_VALUE, data.optString(HEROAPP_EXTRA_BADGE_VALUE));
                } else if (object.has(HEROAPP_NEW_APP)) {
                    intent = getHomeIntent(context);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                    Bundle bundle = new Bundle();
                    bundle.putString(HEROAPP_NEW_APP, object.optJSONObject(HEROAPP_NEW_APP).toString());
                    intent.putExtras(bundle);
                    context.startActivity(intent);

                    return;
                } else if (object.has(HEROAPP_TAB_CHANGED)) {
                    intent.setAction(HEROAPP_TAB_CHANGED);
                    if (object.has("value")) {
                        intent.putExtra("value", object.getString("value"));
                    }
                }
                broadcastManager.sendBroadcast(intent);
            }
        }
    }

    public Object getContent() {
        return content;
    }

    public static Intent getHomeIntent(Context c) {
        Activity home = HeroHomeActivity.getTheHomeActivity();
        if (home != null) {
            return new Intent(c, home.getClass());
        } else {
            return new Intent(ACTION_HOME);
        }
    }
}
