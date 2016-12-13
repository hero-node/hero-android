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
