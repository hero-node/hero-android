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

import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.FragmentManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class HeroActivity extends HeroFragmentActivity {
    public static final int RESULT_CODE_DISMISS = -1009;
    private static int autoGenerateRequestCode = 1000;

    public static final boolean SHOW_ACTIVITY_ANIM = true;

    HeroActivity self = this;
    JSONArray mRightItems;
    JSONObject mActionDatas;
    boolean shouldSendViewWillAppear;
    private HeroFragment mainFragment;
    private ProgressDialog progressDialog;

    public static int getAutoGenerateRequestCode() {
        return autoGenerateRequestCode++;
    }

    public static boolean isNetworkAvailable(Context c) {
        ConnectivityManager connManager = (ConnectivityManager) c.getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo.State state = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState();
        if (state == NetworkInfo.State.CONNECTED || state == NetworkInfo.State.CONNECTING) {
            return true;
        }

        state = connManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState();
        if (state == NetworkInfo.State.CONNECTED || state == NetworkInfo.State.CONNECTING) {
            return true;
        }
        return false;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initContent();
    }

    @SuppressLint("ResourceAsColor")
    protected void initContent() {
        mainFragment = new HeroFragment();
        mainFragment.setArguments(getIntent().getExtras());
        setContentView(R.layout.hero_base_activity);
        if (mainFragment != null) {
            FragmentManager fm = getSupportFragmentManager();
            fm.beginTransaction().replace(R.id.layoutRoot, mainFragment).commit();
        }
    }

    @Override
    protected void onDestroy() {
        shouldSendViewWillAppear = true;
        super.onDestroy();
    }

    @Override
    protected void onStop() {
        super.onStop();
        shouldSendViewWillAppear = true;
    }

    @Override
    protected void onPause() {
        shouldSendViewWillAppear = true;
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    public void onBackPressed() {
        JSONObject leftItem = getCurrentFragment().getLeftItem();
        if (leftItem != null && leftItem.has("click")) {
            try {
                JSONObject click = leftItem.getJSONObject("click");
                self.on(click);
                return;
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        super.onBackPressed();
    }

    public static String getPathFromURI(Context context, Uri uri) {
        String result;
        Cursor cursor = context.getContentResolver().query(uri, null, null, null, null);
        if (cursor == null) {
            result = uri.getPath();
        } else {
            cursor.moveToFirst();
            int idx = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA);
            result = cursor.getString(idx);
            cursor.close();
        }
        return result;
    }

    @Override
    public HeroFragment getCurrentFragment() {
        return mainFragment;
    }

    @Override
    public boolean isActionBarShown() {
        return false;
    }

    @Override
    public void setRightItems(JSONArray array) {
        mRightItems = array;
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }


    public static void activitySwitchAnimation(Activity activity, int startAnim, int exitAnim) {
        if (SHOW_ACTIVITY_ANIM) {
            activity.overridePendingTransition(startAnim, exitAnim);
        }
    }

    @Override
    public void finish() {
        super.finish();
        activitySwitchAnimation(this, R.anim.activity_still, R.anim.activity_slide_out);
    }

    public static void startHeroActivity(Context context, String url) {
        Intent intent = new Intent(context, HeroActivity.class);
        intent.putExtra("url", url);
        try {
            context.startActivity(intent);
        } catch (ActivityNotFoundException e) {
            e.printStackTrace();
        }
    }
}
