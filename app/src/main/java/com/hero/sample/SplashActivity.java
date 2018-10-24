package com.hero.sample;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import com.hero.signature.HeroSignatureActivity;
import com.tiger.cash.R;

/**
 * Created by Aron on 2018/7/25.
 */
public class SplashActivity extends Activity {

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
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
//            window.setNavigationBarColor(Color.TRANSPARENT);
            window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

//            //隐藏底部导航栏
//            window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
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
        Intent mainIntent = new Intent(SplashActivity.this, HeroSignatureActivity.class);
        SplashActivity.this.startActivity(mainIntent);
        overridePendingTransition(R.anim.fragment_slide_left_in, R.anim.fragment_slide_right_out);
        SplashActivity.this.finish();
    }
}