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

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

public class HeroActivity extends HeroFragmentActivity {
    public static final int RESULT_CODE_DISMISS = -1009;
    private static int autoGenerateRequestCode = 1000;

    public static final int LOCAL_CROP = 13;// 本地图库

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

//            if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
//                    != PackageManager.PERMISSION_GRANTED) {
//                //申请WRITE_EXTERNAL_STORAGE权限
//                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA},
//                        100);
//            } else {
//
//            }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case 100:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this,"相机权限已打开", Toast.LENGTH_SHORT).show();
                    return;
                } else {
                    Toast.makeText(this,"请手动打开相机权限", Toast.LENGTH_SHORT).show();
                }
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case LOCAL_CROP:// 系统图库
                if (resultCode == RESULT_OK) {
                    // 创建intent用于裁剪图片
                    Intent intent1 = new Intent("com.android.camera.action.CROP");
                    // 获取图库所选图片的uri
                    Uri uri = data.getData();


                }
                break;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
}
