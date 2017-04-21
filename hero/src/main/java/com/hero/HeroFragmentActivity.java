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

import android.app.ActionBar;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.util.SparseArrayCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;

import org.json.JSONArray;
import org.json.JSONException;

public abstract class HeroFragmentActivity extends AppCompatActivity implements IHeroContext {
    private boolean hasPresentActivity;
    JSONArray mRightItems;

    private static final String TAG = "HeroFragmentActivity";

    public interface IRequestView {
        void onActivityResult(int requestCode, int resultCode,Intent data);
    }

    int mNextCandidateRequestIndex;
    SparseArrayCompat<IRequestView> requestViews;
    static final int MAX_NUM_PENDING_RESULTS = 0xfff - 1;

    @Override
    public void on(Object object) throws JSONException {
        HeroFragment fragment = getCurrentFragment();
        if (fragment != null) {
            fragment.on(object);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        hasPresentActivity = false;
    }

    public abstract HeroFragment getCurrentFragment();

    public HeroFragment getParentFragment(View view) {
        String tag = HeroView.getFragmentTag(view);
        if (!TextUtils.isEmpty(tag)) {
            Fragment fragment = (HeroFragment) (getSupportFragmentManager().findFragmentByTag(tag));
            if (fragment != null && fragment instanceof HeroFragment) {
                return (HeroFragment) fragment;
            }
        }
        return getCurrentFragment();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (HeroApplication.getInstance() != null) {
            HeroApplication.getInstance().pushActivity(this);
        }
        hasPresentActivity = false;
        if (requestViews == null) {
            requestViews = new SparseArrayCompat<>();
            mNextCandidateRequestIndex = 0;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (HeroApplication.getInstance() != null) {
            HeroApplication.getInstance().popActivity(this);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        int requestIndex = requestCode>>12;
        if (requestIndex != 0) {
            requestIndex--;

            IRequestView who = requestViews.get(requestIndex);
            requestViews.remove(requestIndex);
            if (who == null) {
                Log.w(TAG, "Activity result delivered for unknown Views.");
                return;
            }
            who.onActivityResult(requestCode & 0x0fff, resultCode, data);
            return;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    public void startActivityForResult(IRequestView requestView, Intent intent,int requestCode){
        this.startActivityForResult(requestView,intent,requestCode,null);
    }

    /**
     *
     * Calls from Views, request the activity result and get the result from the callback
     *
     * @Param requestView, views have to implement this interface to get the result
     * @Param intent, same with called from Activity/Fragment
     * @Param requestCode, same with called from Activity/Fragment, Please note it should no
     * more than 0x0FFF
     * @Param options, same with called from Activity/Fragment
     *
     * */
    public void startActivityForResult(IRequestView requestView, Intent intent,
                                       int requestCode, @Nullable Bundle options){
        if (requestCode == -1) {
            ActivityCompat.startActivityForResult(this, intent, -1, options);
            return;
        }
        if (!checkForValidRequestCode(requestCode)){
            Log.e(TAG,"request Code is invalid");
            return;
        }
        int requestIndex = allocateRequestIndex(requestView);
        ActivityCompat.startActivityForResult(
                this, intent, ((requestIndex + 1) << 12) + (requestCode & 0x0fff), options);
    }

    boolean checkForValidRequestCode(int requestCode) {
        if ((requestCode & 0xfffff000) != 0) {
            return false;
        }
        return true;
    }

    // Allocates the next available startActivityForResult request index.
    private int allocateRequestIndex(IRequestView view) {
        // Sanity check that we havn't exhaused the request index space.
        if (requestViews.size() >= MAX_NUM_PENDING_RESULTS) {
            throw new IllegalStateException("Too many pending Fragment activity results.");
        }

        // Find an unallocated request index in the mPendingFragmentActivityResults map.
        while (requestViews.indexOfKey(mNextCandidateRequestIndex) >= 0) {
            mNextCandidateRequestIndex =
                    (mNextCandidateRequestIndex + 1) % MAX_NUM_PENDING_RESULTS;
        }

        int requestIndex = mNextCandidateRequestIndex;
        requestViews.put(requestIndex, view);
        mNextCandidateRequestIndex =
                (mNextCandidateRequestIndex + 1) % MAX_NUM_PENDING_RESULTS;

        return requestIndex;
    }
    // override this method to show actionbar
    public boolean isActionBarShown() {
        return false;
    }

    public boolean isBackIconShown() {
        return true;
    }

    public void setRightItems(JSONArray array) {
        mRightItems = array;
    }

    @Override
    public void setTitle(CharSequence title) {
        if (getAppActionBar() != null) {
            getAppActionBar().setTitle(title);
        }
    }

    // in normal activity, set left item title is same as set title
    public void setLeftItemTitle(CharSequence title) {
        if (getAppActionBar() != null) {
            getAppActionBar().setTitle(title);
        }
    }

    public ActionBar getAppActionBar() {
        return getActionBar();
    }

    public Intent getGotoIntent() {
        return new Intent(this, HeroActivity.class);
    }

    public Intent getPresentIntent() {
        return new Intent(this, HeroOneActivity.class);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            HeroFragment fragment = getCurrentFragment();
            if (fragment != null) {
                if (fragment.onBackPressed()) {
                    return true;
                }
            }
        }
        return super.onKeyDown(keyCode, event);
    }

    public boolean isHasPresentActivity() {
        return hasPresentActivity;
    }

    public void setHasPresentActivity(boolean value) {
        hasPresentActivity = value;
    }
}
