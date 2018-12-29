package com.hero.sample;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.View;

import com.hero.HeroApplication;
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
        getWindow().setStatusBarColor(Color.TRANSPARENT);
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
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
            jsonObjectInArray.put("url","http://10.122.16.189:3000/projects/hero-home/home.html");
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