package com.hero.sample;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import com.hero.HeroApplication;
import com.hero.signature.HeroSignatureActivity;
import com.tiger.cash.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Aron on 2018/7/25.
 */
public class SplashActivity extends Activity {

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        if (HeroApplication.getInstance() != null) {
            HeroApplication.getInstance().pushActivity(this);
        }

        //设置透明的状态栏和控制面板
        getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS
                    | WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
            window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(Color.TRANSPARENT);
            window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        }
        setContentView(R.layout.splash_activity_layout);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {

                gotoMain();

            }
        }, 2000);

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    private void gotoMain() {
        try {
            LocalBroadcastManager manager = LocalBroadcastManager.getInstance(this);
            Intent intent = new Intent("newApp");
            JSONObject jsonObject = new JSONObject();
            JSONArray jsonArray = new JSONArray();
            JSONObject jsonObjectInArray = new JSONObject();
            jsonObjectInArray.put("title","home");
            jsonObjectInArray.put("url","http://192.168.32.156:3000/projects/hero-home/home.html");
            jsonArray.put(jsonObjectInArray);
            jsonObject.put("tabs",jsonArray);
            jsonObject.put("key","newApp");

            Log.i("Hero",jsonObject.toString());
            intent.putExtra("jsonObject", jsonObject.toString());
            manager.sendBroadcast(intent);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        SplashActivity.this.finish();
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }
}