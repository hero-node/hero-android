package com.hero;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.MenuItem;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by liuguoping on 15/10/15.
 * 用于弹出一个唯一界面。和iOS的presentViewController一样
 */
public class HeroOneActivity extends HeroActivity {
    private boolean backPressed = false;
    private boolean isLaunchFromHome = false;
    //    private boolean hasLeftItem = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        isLaunchFromHome = getIntent().getBooleanExtra(HeroHomeActivity.LAUNCH_FROM_HOME, false);
    }

    @Override
    public void on(Object object) throws JSONException {
        super.on(object);
        //        if (object != null && object instanceof JSONObject) {
        //            final JSONObject json = (JSONObject) object;
        //            if (json.has("ui")) {
        //                JSONObject ui = (JSONObject) json.get("ui");
        //                if (ui.has("nav")) {
        //                    JSONObject nav = ui.getJSONObject("nav");
        //                    if (nav.has("leftItems")) {
        //                        hasLeftItem = true;
        //                    } else {
        //                        hasLeftItem = false;
        //                    }
        //                }
        //            }
        //        }
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        // only no left item and launched form home leads to exit
//        if (isLaunchFromHome) {
//            if (getCurrentFragment().getLeftItem() == null) {
//                if (this.backPressed) {
//                    setResult(HeroHomeActivity.RESULT_CODE_EXIT);
//                    this.finish();
//                    return;
//                }
//                backPressed = true;
//                Toast.makeText(this, getString(R.string.pressMoreToExit), Toast.LENGTH_SHORT).show();
//                new Handler().postDelayed(new Runnable() {
//                    @Override
//                    public void run() {
//                        backPressed = false;
//                    }
//                }, 2000);
//                return;
//            } //else {
//                if (getCurrentFragment().getLeftItem().has("click")) {
//                    try {
//                        JSONObject click = getCurrentFragment().getLeftItem().getJSONObject("click");
//                        self.on(click);
//                        return;
//                    } catch (JSONException e) {
//                        e.printStackTrace();
//                    }
//                }
//            }
//        }
        super.onBackPressed();
    }

    public boolean isBackIconShown() {
        return false;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (resultCode == HeroActivity.RESULT_CODE_DISMISS) {
            self.finish();
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void finish() {
        super.finish();
        HeroActivity.activitySwitchAnimation(this, R.anim.activity_still, R.anim.activity_pop_down);
    }
}
