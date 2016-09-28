package com.hero;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.widget.Toast;

/**
 * Created by R9L7NGH on 2015/11/12.
 */
public abstract class HeroHomeActivity extends HeroFragmentActivity {

    public static final String LAUNCH_FROM_HOME = "launchFromHome";
    public static final int RESULT_CODE_EXIT = -1001;
    public static Activity theHomeActivity = null;
    private boolean backPressed = false;

    public static Activity getTheHomeActivity() {
        return theHomeActivity;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (theHomeActivity == null) {
            theHomeActivity = this;
        }
        startLoading();
    }

    protected abstract void startLoading();

    protected abstract void finishLoading();

    public void onFinishLoading() {
        finishLoading();
    }

    @Override
    protected void onDestroy() {
        HeroApplication.clearAllCookies(HeroHomeActivity.this);
        //        CookieSyncManager.getInstance().sync();

        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        if (this.backPressed) {
            this.finish();
            return;
        }
        backPressed = true;
        Toast.makeText(this, getString(R.string.pressMoreToExit), Toast.LENGTH_SHORT).show();
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                backPressed = false;
            }
        }, 2000);
    }
}
