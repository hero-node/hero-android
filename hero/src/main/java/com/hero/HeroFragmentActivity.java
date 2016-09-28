package com.hero;

import android.app.ActionBar;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;

import com.hero.depandency.IImagePickHandler;

import org.json.JSONArray;
import org.json.JSONException;

public abstract class HeroFragmentActivity extends AppCompatActivity implements IHeroContext {
    private boolean hasPresentActivity;
    JSONArray mRightItems;

    @Override
    public void on(Object object) throws JSONException {
        HeroFragment fragment = getCurrentFragment();
        if (fragment != null) {
            fragment.on(object);
        }
    }

    @Override
    public void setImagePickHandler(IImagePickHandler handler) {
        // do nothing
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
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (HeroApplication.getInstance() != null) {
            HeroApplication.getInstance().popActivity(this);
        }
    }

    // override this method to show actionbar
    public boolean isActionBarShown() {
        return false;
    }

    public boolean isBackIconShown() {
        return true;
    }

    //    @Override
    //    public boolean onCreateOptionsMenu(Menu menu) {
    //        getCurrentFragment().onCreateOptionsMenu(menu, getMenuInflater());
    //        return true;
    //    }
    //
    //    @Override
    //    public boolean onOptionsItemSelected(MenuItem item) {
    //        return getCurrentFragment().onOptionsItemSelected(item);
    //    }

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
