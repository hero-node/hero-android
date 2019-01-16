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
import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Looper;
import android.support.annotation.ColorInt;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.support.v4.view.ViewCompat;
import android.text.TextUtils;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.webkit.WebView;
import android.widget.AbsListView;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.hero.depandency.ACache;
import com.hero.depandency.ContextUtils;
import com.hero.depandency.ImageLoadUtils;
import com.hero.depandency.LoadingTextView;
import com.hero.depandency.ReminderDelegate;
import com.hero.depandency.StringUtil;
import com.hero.depandency.UploadUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;

public class HeroFragment extends Fragment implements IHeroContext {
    public static final String ARGUMENTS_URL = "url";
    public static final String ARGUMENTS_MAGIC_VIEW = "magicViewName";
    public static final String MAGIC_VIEW_PREFIX = "magic";
    private static final boolean USE_CACHE = true;//!BuildConfig.DEBUG;
    public final static float PROGESS_DIALOG_DIM_AMOUNT = 0.2f;
    public final static float LONG_PROGESS_DIM_AMOUNT = 0.6f;
    protected HeroFragment self = this;
    protected HeroWebView mWebview;
    protected HeroWebView mWebview2;
    protected FrameLayout mLayout;
    protected String mUrl;
    protected JSONObject mLeftItem;
    protected JSONArray mRightItems;
    protected JSONObject mActionDatas;
    protected Activity activity;
    protected Dialog customDialog;
    private ImageView closeImageView;
    protected String title;
    private boolean shouldSendViewWillAppear;
    private boolean isHidden = false;
    private boolean isNavigationBarHidden = false;
    private boolean showLoading;
    private boolean injectJS;

    private ArrayList<Integer> requestCodes = new ArrayList<Integer>();
    private ACache mCache;
    protected ProgressDialog progressDialog;
    protected int titleBackgroundColor;
    protected View toolbar;
    private TextView toolbarTitleView;
    private ImageView toolbarImageView;
    private ImageView backImageView;
    private ViewGroup rootLayout;
    private ViewGroup toolbarContainer;
    private RelativeLayout popLayout = null;
    private ViewGroup leftItemsLayout;
    private ViewGroup rightItemsLayout;
    private View mainContentView;
    protected HeroWebView customWebView = null;
    private int viewIndex = 0;
    private Map<Integer, View> contextMenuHandler;
    private ReminderDelegate reminderDelegate;
    private Queue<JSONObject> pendingDialogQueue;
    private Window window;

    public static final String VIEW_WILL_APPEAR_EXPRESSION = "document.readyState === 'complete' && window.Hero && Hero.viewWillAppear()";
    public static final String VIEW_WILL_DISAPPEAR_EXPRESSION = "window.Hero && Hero.viewWillDisappear()";
    public static final int VIEW_WILL_APPEAR_DELAY = 400;
    public static int backgroundColor = R.color.heroWhite;
    public static int tintColor = android.R.color.background_dark;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        contextMenuHandler = new HashMap<>();
        progressDialog = createProgressDialog(getContext());
        Bundle bundle = getArguments();
        if (bundle != null) {
            mUrl = bundle.getString(ARGUMENTS_URL);
            showLoading = bundle.getBoolean("showLoading");
            if (showLoading){
                showProgressDialog(progressDialog,true);
            }
            injectJS = bundle.getBoolean("injectJS");
        }
        shouldSendViewWillAppear = false;
        if (((HeroFragmentActivity) getActivity()).isActionBarShown()) {
            setHasOptionsMenu(true);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        activity = this.getActivity();
        int layoutId = getLayoutId();
        View v = inflater.inflate(layoutId, container, false);
        initViews((ViewGroup) v);
        if (mUrl != null) {
            this.loadUrl(mUrl);
        }
        mLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hideSoftKeyboard(activity);
                View focusedView = v.findFocus();
                if (focusedView != null) {
                    focusedView.clearFocus();
                }
            }
        });
        return v;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    public void onDestroy() {
        if (mWebview != null) {
            mWebview.destroy();
        }
        if (mWebview2 != null) {
            mWebview2.destroy();
        }
        closePopWindow();
        shouldSendViewWillAppear = true;
        unregisterContextMenuHandler();
        if (reminderDelegate != null) {
            reminderDelegate.destroy();
        }
        super.onDestroy();
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onStop() {
        super.onStop();
        this.getWebView().pause();
    }

    @Override
    public void onPause() {
        shouldSendViewWillAppear = true;
        super.onPause();
        this.getWebView().pause();
    }

    @Override
    public void onResume() {
        super.onResume();
        this.getWebView().resume();
        if (!isHidden) {
            if (mActionDatas != null && shouldSendViewWillAppear && mActionDatas.has("viewWillAppear")) {
                try {
                    self.on(mActionDatas.get("viewWillAppear"));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            evaluateJavaScript(VIEW_WILL_APPEAR_EXPRESSION);
        }
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        isHidden = hidden;
        if (this.isResumed() && !hidden) {
            if (getActivity() instanceof HeroHomeActivity && !TextUtils.isEmpty(title)) {
                setActivityTitle(title);
            }
            if (mActionDatas != null && mActionDatas.has("viewWillAppear")) {
                try {
                    self.on(mActionDatas.get("viewWillAppear"));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            evaluateJavaScript(VIEW_WILL_APPEAR_EXPRESSION);
        } else if (hidden) {
            if (mActionDatas != null && mActionDatas.has("viewWillDisappear")) {
                try {
                    self.on(mActionDatas.get("viewWillDisappear"));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            evaluateJavaScript(VIEW_WILL_DISAPPEAR_EXPRESSION);
        }

        super.onHiddenChanged(hidden);
    }


    public FrameLayout getView() {
        return mLayout;
    }
    public View getRootContentView() {
        return rootLayout;
    }

    public HeroWebView getWebView() {
        return mWebview;
    }

    @SuppressLint("ResourceAsColor")
    protected void initViews(ViewGroup viewGroup) {
        mLayout = (FrameLayout) viewGroup.findViewById(R.id.mainLayout);
        toolbar = viewGroup.findViewById(R.id.layoutToolbar);
        toolbarTitleView = (TextView) viewGroup.findViewById(R.id.txtTitle);
        toolbarImageView = (ImageView) viewGroup.findViewById(R.id.centerImage);
        leftItemsLayout = (ViewGroup) viewGroup.findViewById(R.id.layoutLeftItem);
        rightItemsLayout = (ViewGroup) viewGroup.findViewById(R.id.layoutRightItem);
        rootLayout = (ViewGroup) viewGroup.findViewById(R.id.rootLayout);
        toolbarContainer = (ViewGroup) viewGroup.findViewById(R.id.toolbarContainer);
        mainContentView = viewGroup.findViewById(R.id.mainScrollView);
        backImageView = (ImageView) toolbar.findViewById(R.id.leftImage);
        if (((HeroFragmentActivity) getActivity()).isBackIconShown()) {
            backImageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mLeftItem != null && mLeftItem.has("click")) {
                        try {
                            JSONObject click = mLeftItem.getJSONObject("click");
                            HeroView.sendActionToContext(getContext(), click);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    } else {
                        if (goBackWebView()) {
                            return;
                        }
                        activity.finish();
                    }
                }
            });
        } else {
            backImageView.setVisibility(View.GONE);
        }
        Drawable originDrawable = ContextCompat.getDrawable(getActivity(),
                R.drawable.ic_back);
        backImageView.setImageDrawable(tintDrawable(originDrawable, tintColor));
        toolbarTitleView.setTextColor(tintColor);
//
//
        mLayout.setBackgroundColor(backgroundColor);
        if (window == null) {
            window = getActivity().getWindow();
        }
        // 浅色
        if (!isColorDark(backgroundColor)) {
            window.setStatusBarColor(Color.TRANSPARENT);
            window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
        } else {
            int flag = ((window.getDecorView().getSystemUiVisibility()) & (~View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR));
            window.getDecorView().setSystemUiVisibility(flag);
        }
    }

    public void loadUrl(String url) {
        if (mWebview == null) {
            this.initWebView();
        }
        if (mCache == null) {
            mCache = ACache.get(this.getContext());
        }
        if (mCache != null) {
            JSONObject ui_cache = mCache.getAsJSONObject(url);
            try {
                if (USE_CACHE) {
                    this.on(ui_cache);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        mWebview.loadUrl(url);

    }

    public void refresh() {
        if (mWebview == null) {
            this.initWebView();
        }
        mWebview.loadUrl(mUrl);
    }

    private void initWebView() {
        mWebview = new HeroWebView(getActivity(), getResources().getColor(R.color.heroWhite),injectJS);
        mLayout.addView(mWebview);
        mWebview.setFragment(this);
        FrameLayout.LayoutParams webViewParams = new FrameLayout.LayoutParams(android.view.ViewGroup.LayoutParams.MATCH_PARENT, android.view.ViewGroup.LayoutParams.MATCH_PARENT);
        mWebview.setLayoutParams(webViewParams);
        customWebView = mWebview;
    }

    private HeroWebView newWebView() {
        HeroWebView webView = new HeroWebView(getActivity(), getResources().getColor(R.color.heroWhite),injectJS);
        webView.setVisibility(View.GONE);
        mLayout.addView(webView);
        webView.setFragment(this);
        return webView;
    }

    public IHero createNewIHeroView(JSONObject jsonObject) throws NoSuchMethodException, java.lang.InstantiationException, JSONException, IllegalAccessException, InvocationTargetException, ClassNotFoundException {
        IHero view = HeroView.fromJson(getContext(), jsonObject);
        HeroView.setFragmentTag((View) view, HeroFragment.this.getTag());
        return view;
    }

    public static boolean isColorDark(@ColorInt int color) {
        double darkness = 1 - (0.299 * Color.red(color) + 0.587 * Color.green(color) + 0.114 * Color.blue(color)) / 255;
        return darkness >= 0.5;
    }

    @SuppressLint("ResourceAsColor")
    public void on(final Object object) throws JSONException {
        if (object == null) return;
        if (object instanceof JSONArray) {
            for (int i = 0; i < ((JSONArray) object).length(); i++) {
                this.on(((JSONArray) object).get(i));
            }
            return;
        }
        final JSONObject json = (JSONObject) object;
        if (getActivity() == null) return;
        if (Looper.myLooper() != Looper.getMainLooper()) {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    try {
                        self.on(json);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            });
        } else {
            try {
                if (json.has("globle") || json.has("global")) {
                    JSONObject globalEvent;
                    if (json.has("globle") ) {
                        globalEvent = json.getJSONObject("globle");
                    } else {
                        globalEvent = json.optJSONObject("global");
                    }
                    String key = globalEvent.getString("key");
                    LocalBroadcastManager manager = LocalBroadcastManager.getInstance(self.getContext());
                    Intent intent = new Intent(key);
                    if (globalEvent.has("value")) {
                        intent.putExtra("value", globalEvent.getString("value"));
                    }
                    intent.putExtra("jsonObject", globalEvent.toString());
                    manager.sendBroadcast(intent);
                }
                if (json.has("ui")) {
                    boolean isCache = false;
                    int cacheVersion = 0;
                    JSONObject ui = (JSONObject) json.get("ui");
//                    if (json.has("ui")) {
//                        Object object1 = json.get("ui");
//                        if (object1.equals("blank")) {
//                            return;
//                        }
//                        ui = (JSONObject) json.get("ui");
//                        JSONObject ui_cache = (mCache == null) ? null : mCache.getAsJSONObject(mUrl);
//                        if (ui_cache != null && ui_cache.has("ui_cache")) {
//                            ui_cache = ui_cache.getJSONObject("ui_cache");
//                            if (ui.has("version")) {
//                                int version = ui.getInt("version");
//                                int version_old = -1000;
//                                if (ui_cache.has("version")) {
//                                    version_old = ui_cache.getInt("version");
//                                }
//
//                                String uiData = ui.toString();
//                                if (isAlwaysNeedReload(uiData, version)) {
//                                    //                                    JSONObject ui_new = new JSONObject();
//                                    //                                    ui_cache.put("ui_cache", ui);
//                                    //                                    mCache.put(mUrl, ui_new);
//                                } else {
//                                    if (version != version_old) {
//                                        JSONObject ui_new = new JSONObject();
//                                        ui_new.put("ui_cache", ui);
//                                        mCache.put(mUrl, ui_new);
//                                    } else {
//                                        // get the original cache string for comparison
//                                        if (USE_CACHE) {
//                                            String cacheStr = mCache.getAsString(mUrl);
//                                            JSONObject uiNow = new JSONObject();
//                                            uiNow.put("ui_cache", ui);
//                                            String uiStr = uiNow.toString();
//                                            if (uiStr.length() == cacheStr.length() && uiStr.equals(cacheStr)) {
//                                                return;
//                                            }
//                                        }
//                                    }
//                                }
//                            }
//                        } else {
//                            int version = 0;
//                            if (ui.has("version")) {
//                                version = ui.getInt("version");
//                            }
//                            String uiData = ui.toString();
//                            if (!isAlwaysNeedReload(uiData, version)) {
//                                JSONObject ui_new = new JSONObject();
//                                ui_new.put("ui_cache", ui);
//                                mCache.put(mUrl, ui_new);
//                            }
//                        }
//                    } else {
//                        ui = (JSONObject) json.get("ui_cache");
//                        isCache = true;
//                        if (ui.has("version")) {
//                            cacheVersion = ui.getInt("version");
//                        }
//                    }
                    if (ui.has("backgroundColor")) {
                        backgroundColor = HeroView.parseColor("#" + ui.getString("backgroundColor"));
                        mLayout.setBackgroundColor(backgroundColor);
                        rootLayout.setBackgroundColor(backgroundColor);
                        mainContentView.setBackgroundColor(backgroundColor);
                        setTitleBackgroundColor(backgroundColor);
//                        // 浅色
                        if (!isColorDark(backgroundColor)) {
                            // 黑色
                            window.setStatusBarColor(Color.TRANSPARENT);
                            window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
                        } else {
                            int flag = ((window.getDecorView().getSystemUiVisibility()) & (~View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR));
                            window.getDecorView().setSystemUiVisibility(flag);
                        }
                    }

                    // TODO
                    if (ui.has("tintColor")) {
                        titleBackgroundColor = HeroView.parseColor("#" + ui.getString("tintColor"));
                        tintColor = HeroView.parseColor("#" + ui.getString("tintColor"));
                        Drawable originDrawable = ContextCompat.getDrawable(getActivity(),
                                R.drawable.ic_back);
                        backImageView.setImageDrawable(tintDrawable(originDrawable, tintColor));
                        toolbarTitleView.setTextColor(tintColor);
                    }
                    mLayout.removeAllViews();
                    customWebView = null;
                    if (ui.has("nav")) {
                        JSONObject nav = ui.getJSONObject("nav");
                        JSONObject appearance = new JSONObject();
                        appearance.put("appearance", nav);
                        this.on(appearance);
                    }
                    if (ui.has("views")) {
                        JSONArray views = ui.getJSONArray("views");
                        for (int i = 0; i < views.length(); i++) {
                            JSONObject view = views.getJSONObject(i);
                            IHero v = createNewIHeroView(view);
                            addDescriptionToView(v);
                            if (v instanceof HeroWebView) {
                                customWebView = (HeroWebView) v;
                            }
                            if (v != null) {
                                if (view.has("parent")) {
                                    String parent = view.getString("parent");
                                    ViewGroup p = (ViewGroup) HeroView.findViewByName(mLayout, parent);
                                    if (p != null) {
                                        p.addView((View) v);
                                    }
                                } else {
                                    mLayout.addView((View) v);
                                }
                                v.on(view);
                                if (v instanceof HeroTableView) {
                                    AbsListView.OnScrollListener scrollListener = getListScrollListener();
                                    if (scrollListener != null) {
                                        ((HeroTableView) v).setOnScrollListener(scrollListener);
                                    }
                                }
                            }
                        }
                    }

                    Bundle bundle = getArguments();
                    if (bundle != null &&bundle.containsKey(ARGUMENTS_MAGIC_VIEW)) {
                        // cache != 0 means ui should be cached
                        if (!isCache || cacheVersion != 0) {
                            transitionMagicView(bundle.getString(ARGUMENTS_MAGIC_VIEW));
                        }
                    }
                } else if (json.has("ui_fragment")) {
                    Object ui_fragment;
                    ui_fragment = json.get("ui_fragment");

                    if (ui_fragment instanceof JSONArray) {
                        JSONArray views = (JSONArray) ui_fragment;
                        for (int i = 0; i < views.length(); i++) {
                            JSONObject view = views.getJSONObject(i);
                            IHero v = createNewIHeroView(view);
                            addDescriptionToView(v);
                            if (v != null) {
                                if (view.has("parent")) {
                                    String parent = view.getString("parent");
                                    ViewGroup p = (ViewGroup) HeroView.findViewByName(mLayout, parent);
                                    if (p != null) {
                                        p.addView((View) v);
                                    }
                                } else {
                                    mLayout.addView((View) v);
                                }
                                v.on(view);
                            }
                        }
                    }
                    // ui_fragment end
                } else if (json.has("datas")) {
                    Object datas = json.get("datas");
                    if (datas instanceof JSONObject) {
                        String name = ((JSONObject) datas).getString("name");
                        IHero view = HeroView.findViewByName(mLayout, name);
                        if (view == null) {
                            view = findViewOnPopWindow(name);
                        }
                        if (view != null) {
                            view.on(((JSONObject) datas));
                        }
                    } else if (datas instanceof JSONArray) {
                        JSONArray arr = (JSONArray) datas;
                        for (int i = 0; i < arr.length(); i++) {
                            String name = arr.getJSONObject(i).getString("name");
                            IHero view = HeroView.findViewByName(mLayout, name);
                            if (view == null) {
                                view = findViewOnPopWindow(name);
                            }
                            if (view != null) {
                                view.on((arr.getJSONObject(i)));
                            }
                        }
                    }
                } else if (json.has("appearance")) {
                    Object appearance = json.get("appearance");
                    if (appearance instanceof JSONObject) {
                        JSONObject jsonAppearance = (JSONObject) appearance;
                        if (jsonAppearance.has("navigationBarHidden")) {
                            isNavigationBarHidden = jsonAppearance.getBoolean("navigationBarHidden");
                            if (isNavigationBarHidden) {
                                setNavigationBarHidden(true);
                            } else {
                                setNavigationBarHidden(false);
                            }
                        }
                        if (jsonAppearance.has("title")) {
                            String titleStr = jsonAppearance.getString("title");
                            if (!TextUtils.isEmpty(titleStr)) {
                                title = titleStr;
                                setActivityTitle(titleStr);
                            }
                        }
                        if (jsonAppearance.has("image")) {
                            String url = jsonAppearance.getString("image");
                            if (!TextUtils.isEmpty(url)) {
                                setActivityImageTitle(url);
                            }
                        }

                        if (jsonAppearance.has("titleView")) {
                            boolean showActionBar = false;
                            boolean showHome = false;
                            if (getActivity() instanceof HeroFragmentActivity && ((HeroFragmentActivity) getActivity()).isActionBarShown()) {
                                showActionBar = true;
                            }
                            JSONObject titleView = jsonAppearance.getJSONObject("titleView");
                            if (showActionBar) {
                                IHero tView = createNewIHeroView(titleView);
                                if (getActivity() instanceof HeroActivity) {
                                    showHome = true;
                                }
                                addCustomActionBar(tView, titleView, showHome);
                            } else {
                                if (titleView.has("text")) {
                                    setActivityTitle(titleView.getString("text"));
                                }
                            }
                        }
                        if (jsonAppearance.has("rightItems")) {
                            Object rightItems = jsonAppearance.get("rightItems");
                            if (rightItems instanceof JSONArray) {
                                mRightItems = jsonAppearance.getJSONArray("rightItems");
                            } else {
                                mRightItems = null;
                            }
                            if (getActivity() instanceof HeroFragmentActivity) {
                                ((HeroFragmentActivity) getActivity()).setRightItems(mRightItems);
                            }
                            if (getActionBar() == null) {
                                setupToolbarRightItems(jsonAppearance);
                            }
                            getActivity().invalidateOptionsMenu();
                        } else {
                            mRightItems = null;
                        }

                        if (jsonAppearance.has("leftItems")) {
                            Object objLeftItems = jsonAppearance.get("leftItems");
                            if (objLeftItems instanceof JSONArray) {
                                JSONArray leftItems = jsonAppearance.getJSONArray("leftItems");
                                mLeftItem = leftItems.optJSONObject(0);
                            } else {
                                mLeftItem = (JSONObject) objLeftItems;
                            }
                            if (getActivity() != null) {
                                if (getActivity() instanceof HeroActivity) {
                                    setActionbarDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM | ActionBar.DISPLAY_SHOW_TITLE | ActionBar.DISPLAY_HOME_AS_UP);
                                } else {
                                    if (((HeroFragmentActivity) getActivity()).isActionBarShown()) {
                                        setActionbarDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM | ActionBar.DISPLAY_SHOW_TITLE);
                                    }
                                }
                            }
                            if (mLeftItem!=null && mLeftItem.has("title")) {
                                String titleStr = mLeftItem.getString("title");
                                //                                    title = titleStr;
                                setLeftItemTitle(titleStr);
                            }
                            if (getActionBar() == null) {
                                setupToolbarLeftItems(mLeftItem);
                            }
                        } else {
                            if (getActivity() != null && self.getActionBar() != null) {
                                if (getActivity() instanceof HeroOneActivity) {
                                    // only show title
                                    setActionbarDisplayOptions(ActionBar.DISPLAY_SHOW_TITLE);
                                } else if (getActionBar().getCustomView() != null) {
                                    setActionbarTitleEnabled(false);
                                }
                            }
                        }
                    }
                } else if (json.has("command")) {
                    Object cmdObj = json.get("command");
                    if (cmdObj instanceof String) {
                        String command = (String) cmdObj;
                        if (command.startsWith("refresh")) {
                            self.refresh();
                        } else if (command.startsWith("goto:")) {
                            String url = command.replace("goto:", "");
                            if (url.startsWith("tel:")) {
                                Intent callIntent = new Intent(Intent.ACTION_DIAL);
                                callIntent.setData(Uri.parse(url.replaceAll("/", "")));
                            } else {
                                if (getContext() instanceof HeroFragmentActivity) {
                                    Intent intent = ((HeroFragmentActivity) getContext()).getGotoIntent();//new Intent(getContext(), HeroActivity.class);
                                    intent.putExtra("url", url);
                                    intent.putExtra("headBarVisible", true);
                                    if (getActivity() instanceof HeroOneActivity) {
                                        int requestCode = HeroActivity.getAutoGenerateRequestCode();
                                        requestCodes.add(requestCode);
                                        startActivityForResult(intent, requestCode);
                                    } else {
                                        startActivity(intent);
                                    }
                                    HeroActivity.activitySwitchAnimation(getActivity(), R.anim.activity_slide_in, R.anim.activity_still);
                                }
                            }
                        } else if (command.startsWith("magicGoto:")){
                            String url = command.replace("magicGoto:", "");
                            if (getContext() instanceof HeroFragmentActivity) {
                                Intent intent = ((HeroFragmentActivity) getContext()).getGotoIntent();
                                intent.putExtra("url", url);
                                View magicView = null;
                                if (mCache != null) {
                                    JSONObject nextCache = mCache.getAsJSONObject(url);
                                    // only find magic view when next page has a cache
                                    if (nextCache != null) {
                                        magicView = findMagicView();
                                    }
                                }
                                if (getActivity() instanceof HeroOneActivity) {
                                    int requestCode = HeroActivity.getAutoGenerateRequestCode();
                                    requestCodes.add(requestCode);
                                    magicGoto(getActivity(), intent, magicView, requestCode);
                                } else {
                                    magicGoto(getActivity(), intent, magicView, -1);
                                }
                            }
                        } else if (command.startsWith("gotoWithLoading:")){
                            String url = command.replace("gotoWithLoading:", "");
                            if (getContext() instanceof HeroFragmentActivity) {
                                Intent intent = ((HeroFragmentActivity) getContext()).getGotoIntent();
                                intent.putExtra("url", url);
                                intent.putExtra("showLoading", true);
                                intent.putExtra("injectJS", true);
                                if (getActivity() instanceof HeroOneActivity) {
                                    int requestCode = HeroActivity.getAutoGenerateRequestCode();
                                    requestCodes.add(requestCode);
                                    startActivityForResult(intent, requestCode);
                                } else {
                                    startActivity(intent);
                                }
                                HeroActivity.activitySwitchAnimation(getActivity(), R.anim.activity_slide_in, R.anim.activity_still);
                            }
                        } else if (command.startsWith("load:")) {
                            shouldSendViewWillAppear = true;
                            String url = command.replace("load:", "");
                            mUrl = url;
                            self.loadUrl(url);
                        } else if (command.startsWith("parallel:")) {
                            // add a new webview
                            String url = command.replace("parallel:", "");
                            mWebview2 = newWebView();
                            mWebview2.loadUrl(url);
                        } else if (command.startsWith("showLoading")) {
                            if (command.startsWith("showLoading:")) {
                                String content = command.replace("showLoading:", "");
                                if (!TextUtils.isEmpty(content)) {
                                    showClosableProgressDialog(progressDialog, true, content);
                                } else {
                                    showProgressDialog(progressDialog, true);
                                }
                            } else {
                                showProgressDialog(progressDialog, true);
                            }
                        } else if (command.startsWith("stopLoading")) {
                            hideProgressDialog(progressDialog);
                        }  else if (command.startsWith("webViewDidFinishLoad")) {
                            hideProgressDialog(progressDialog);
                        } else if (command.startsWith("present:")) {
                            String url = command.replace("present:", "");
                            if (getContext() instanceof HeroFragmentActivity) {

                                Intent intent = ((HeroFragmentActivity) getContext()).getPresentIntent();
                                intent.putExtra("url", url);
                                intent.putExtra("headBarVisible", true);
                                if (getActivity() instanceof HeroHomeActivity) {
                                    intent.putExtra(HeroHomeActivity.LAUNCH_FROM_HOME, true);
                                }
                                if (getActivity() instanceof HeroFragmentActivity) {
                                    if (((HeroFragmentActivity) getActivity()).isHasPresentActivity()) {
                                        return;
                                    }
                                    ((HeroFragmentActivity) getActivity()).setHasPresentActivity(true);
                                }
                                startActivity(intent);//, requestCode);
                                HeroActivity.activitySwitchAnimation(getActivity(), R.anim.activity_pop_up, R.anim.activity_still);
                            }
                        } else if (command.startsWith("back")) {
                            activity.finish();
                        } else if (command.startsWith("rootBack")) {
                            // go home or go to the bottom present activity
                            Intent intent1 = ((HeroFragmentActivity) getContext()).getPresentIntent();
                            boolean isPresentActivityInStack = HeroApplication.getInstance().isActivityInStack(intent1.getComponent().getClassName());
                            if (isPresentActivityInStack) {
                                try {
                                    Intent intent = ((HeroFragmentActivity) getContext()).getPresentIntent();
                                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                    intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                                    startActivity(intent);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            } else {
                                returnToHome();
                            }
                        } else if (command.startsWith("dismiss")) {
                            // dismiss will close parent activity also
                            Intent intent1 = ((HeroFragmentActivity) getContext()).getPresentIntent();
                            int topActivityIndex = HeroApplication.getInstance().getTopActivityInStack(intent1.getComponent().getClassName());
                            if (topActivityIndex > 0) {
                                try {
                                    HeroApplication.getInstance().popUpToActivity(topActivityIndex, true);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                    activity.finish();
                                }
                            } else {
                                activity.setResult(HeroActivity.RESULT_CODE_DISMISS);
                                activity.finish();
                            }
                        } else if (command.startsWith("closePopup") || command.startsWith("stopShowAlertLoading")) {
                            closePopWindow();
                        } else if (command.startsWith("uploadFiles")) {
                            final JSONObject content = json.getJSONObject("content");
                            final String url = json.getString("url");
                            final JSONArray files = json.getJSONArray("files");
                            final JSONObject apiReturn = json.getJSONObject("apiReturn");
                            new Thread(new Runnable() {
                                @Override
                                public void run() {
                                    UploadUtils.getInstance().uploadFiles(getContext(), url, content, files, new UploadUtils.UploadListener() {
                                        @Override
                                        public void onUploadSuccess(String message, Object obj) {
                                            JSONObject jsonObject = (JSONObject) obj;
                                            try {
                                                HeroView.putValueToJson(apiReturn, jsonObject);
                                                self.on(apiReturn);
                                            } catch (JSONException e) {
                                                e.printStackTrace();
                                            }
                                        }

                                        @Override
                                        public void onUploadProcess(String message, Object apiReturn) {

                                        }

                                        @Override
                                        public void onUploadFailed(String message, Object object) {
                                            JSONObject jsonObject = new JSONObject();
                                            try {
                                                jsonObject.put("error", "fail");
                                                HeroView.putValueToJson(apiReturn, jsonObject);
                                                self.on(apiReturn);
                                            } catch (JSONException e) {
                                                e.printStackTrace();
                                            }
                                        }
                                    });
                                }
                            }).start();

                        } else if (command.startsWith("submit")) {
                            JSONObject submitData = new JSONObject();
                            for (int i = 0; i < mLayout.getChildCount(); i++) {
                                View child = mLayout.getChildAt(i);
                                if (child instanceof HeroTextField) {
                                    if (HeroView.getName(child) != null) {
                                        if (HeroView.getJson(child).has("secure")) {
                                            String resultString = StringUtil.MD5Encode(((HeroTextField) child).getText().toString());
                                            submitData.put(HeroView.getName(child), resultString);
                                        } else {
                                            submitData.put(HeroView.getName(child), ((HeroTextField) child).getText());
                                        }
                                    }
                                } else if (child instanceof HeroSwitch) {
                                    if (HeroView.getName(child) != null) {
                                        submitData.put(HeroView.getName(child), ((HeroSwitch) child).isChecked());
                                    }
                                }
                            }
                            JSONObject data = new JSONObject();
                            data.put("her", submitData);
                            self.on(data);
                        }
                    } else if (cmdObj instanceof JSONObject) {
                        JSONObject cmdJson = (JSONObject) cmdObj;
                        if (cmdJson.has("show")) {
                            final JSONObject showObj = cmdJson.optJSONObject("show");
                            if (showObj != null) {
                                if (isPopWindowShown()) {
                                    // if a dialog is shown, put the object to pending queue
                                    addPendingDialogContent(showObj);
                                    return;
                                }
                                if (showObj.has("class")) {
                                    String closeImage = null;
                                    if (showObj.has("closeImage")) {
                                        closeImage = showObj.getString("closeImage");
                                    }
                                    closePopWindow();
                                    customDialog = addContentToPopWindow(showObj, closeImage);
                                    if (showObj.has("cancelable")) {
                                        customDialog.setCanceledOnTouchOutside(showObj.getBoolean("cancelable"));
                                    }
                                    if (closeImageView != null) {
                                        closeImageView.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View view) {
                                                closePopWindow();
                                            }
                                        });
                                    }
                                    showPopWindow();
                                } else {
                                    AlertDialog.Builder builder;
                                    String title = showObj.has("title") ? showObj.getString("title") : "";
                                    String content = showObj.has("content") ? showObj.getString("content") : "";
                                    builder = createCommonAlertDialog(title, content);
                                    builder.setNegativeButton(showObj.has("cancel") ? showObj.getString("cancel") : "cancel", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            if (which == Dialog.BUTTON_NEGATIVE) {
                                                // cancel
                                                if (showObj.has("cancelAction")) {
                                                    try {
                                                        final JSONObject action = showObj.getJSONObject("cancelAction");
                                                        HeroView.sendActionToContext(getContext(), action);
                                                    } catch (JSONException e) {
                                                        e.printStackTrace();
                                                    }
                                                }
                                            }
                                        }
                                    });
                                    if (showObj.has("others")) {
                                        String otherButton = null;
                                        try {
                                            Object others = showObj.get("others");
                                            if (others instanceof JSONArray) {
                                                otherButton = ((JSONArray) others).getString(0);
                                            } else {
                                                otherButton = showObj.getString("others");
                                            }
                                        } catch (JSONException e1) {
                                            e1.printStackTrace();
                                        }
                                        if (otherButton != null) {
                                            builder.setNeutralButton(otherButton, new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialog, int which) {
                                                    if (showObj.has("action")) {
                                                        try {
                                                            final JSONObject action = showObj.getJSONObject("action");
                                                            HeroView.sendActionToContext(getContext(), action);
                                                        } catch (JSONException e) {
                                                            e.printStackTrace();
                                                        }
                                                    }
                                                }
                                            });
                                        }
                                    }
                                    closePopWindow();
                                    AlertDialog dialog = builder.create();
                                    if (showObj.has("cancelable")) {
                                        dialog.setCanceledOnTouchOutside(showObj.getBoolean("cancelable"));
                                    } else {
                                        dialog.setCanceledOnTouchOutside(false);
                                    }
                                    dialog.show();
                                    customDialog = dialog;
                                }
                                if (customDialog != null) {
                                    customDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                                        @Override
                                        public void onDismiss(DialogInterface dialog) {
                                            Object nextView = getPendingDialogContent();
                                            // show the content in queue
                                            if (nextView != null && nextView instanceof JSONObject) {
                                                showPendingDialog((JSONObject) nextView);
                                            }
                                        }
                                    });
                                }
                            }
                        } else if (cmdJson.has("sendSms")) {
                            final JSONObject showObj = cmdJson.getJSONObject("sendSms");
                            if (showObj != null) {
                                String phone = showObj.has("phone") ? showObj.getString("phone") : "";
                                String content = showObj.has("content") ? showObj.getString("content") : "";
                                Intent sendIntent = new Intent(Intent.ACTION_SENDTO);
                                sendIntent.setData(Uri.parse("smsto:" + phone));
                                sendIntent.putExtra("sms_body", content);
                                startActivity(sendIntent);
                            }
                        } else if ((cmdJson.has("delay"))) {
                            final Object delayObj = cmdJson.get("delay");
                            double delayTime = cmdJson.getDouble("delayTime");
                            mWebview.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    try {
                                        self.on(delayObj);
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                }
                            }, (long) (delayTime * 1000));
                        }
                    }
                } else if (json.has("remind")) {
                    addReminder(json);
                } else {
                    evaluateJavaScript("window.Hero && Hero.in(" + json.toString() + ")");
                }
            } catch (JSONException e) {
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            } catch (java.lang.InstantiationException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (NoSuchMethodException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            } catch (NoSuchMethodError e) {
                e.printStackTrace();
            }
        }
    }

    private void evaluateJavaScript(String expression) {
        if (mWebview != null) {
            evaluateJavaScript(mWebview, expression);
        }
    }

    public static void evaluateJavaScript(WebView webView, String expression) {
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            try {
                webView.evaluateJavascript(expression, null);
            } catch (IllegalStateException e) {
                webView.loadUrl("javascript:" + expression);
            }
        } else {
            webView.loadUrl("javascript:" + expression);
        }
    }

    /**
     * 对目标Drawable 进行着色
     *
     * @param drawable 目标Drawable
     * @param color    着色的颜色值
     * @return 着色处理后的Drawable
     */
    public static Drawable tintDrawable(@NonNull Drawable drawable, int color) {
        Drawable wrappedDrawable = DrawableCompat.wrap(drawable);
        DrawableCompat.setTint(wrappedDrawable, color);
        return wrappedDrawable;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        if (mRightItems != null) {
            super.onCreateOptionsMenu(menu, inflater);
            try {
                for (int i = 0; i < mRightItems.length(); i++) {
                    String title;
                    try {
                        title = mRightItems.getJSONObject(i).getString("title");
                    } catch (JSONException e) {
                        title = "";
                        e.printStackTrace();
                    }
                    MenuItem item = menu.add(title);
                    item.setOnMenuItemClickListener(new HeroMenuItemClickListener(getContext(), mRightItems.getJSONObject(i).getJSONObject("click")));
                    item.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }
            //resetActionBarOptions();
            if (getTitle() != null) {
                setActivityTitle(getTitle());
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            if (mLeftItem != null && mLeftItem.has("click")) {
                try {
                    JSONObject click = mLeftItem.getJSONObject("click");
                    HeroView.sendActionToContext(getContext(), click);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            } else {
                activity.finish();
            }
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        if (contextMenuHandler != null && contextMenuHandler.size() > 0) {
            View view = contextMenuHandler.get(item.getItemId());
            if (view != null && view instanceof ContextMenuItemListener) {
                ((ContextMenuItemListener) view).onMenuSelected(item);
                return true;
            }
        }
        return super.onContextItemSelected(item);
    }

    public void registerContextMenuHandler(final View view, final int[] itemId, final String[] itemTitle) {
        view.setOnCreateContextMenuListener(new View.OnCreateContextMenuListener() {
            public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
                int i = 0;
                for (int id : itemId) {
                    if (i >= itemTitle.length) break;
                    menu.add(Menu.NONE, id, Menu.NONE, itemTitle[i]);
                    if (contextMenuHandler != null) {
                        contextMenuHandler.put(id, view);
                    }
                    i++;
                }
            }
        });
    }

    public void registerContextMenuHandler(final View view, final int[] itemId, final String[] itemTitle, final ContextMenuItemListener contextListener) {
        view.setOnCreateContextMenuListener(new View.OnCreateContextMenuListener() {
            public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
                int i = 0;
                for (int id : itemId) {
                    if (i >= itemTitle.length) break;
                    menu.add(Menu.NONE, id, Menu.NONE, itemTitle[i]);
                    if (contextMenuHandler != null) {
                        contextMenuHandler.put(id, (View) contextListener);
                    }
                    i++;
                }
            }
        });
    }

    public void unregisterContextMenuHandler() {
        if (contextMenuHandler != null && contextMenuHandler.size() > 0) {
            for (int id : contextMenuHandler.keySet()) {
                try {
                    View view = contextMenuHandler.get(id);
                    if (view != null) {
                        unregisterForContextMenu(view);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == HeroHomeActivity.RESULT_CODE_EXIT && requestCodes.contains(requestCode)) {
            getActivity().finish();
            return;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private View findMagicView() {
        IHero view = HeroView.findViewByPrefix(mLayout, MAGIC_VIEW_PREFIX);
        if (view != null) {
            return (View) view;
        }
        return null;
    }

    public static void magicGoto(Activity originActivity, Intent intent, View view, int requestCode) {
        String viewName = "";
        if (view != null) {
            viewName = HeroView.getName(view);
            intent.putExtra(ARGUMENTS_MAGIC_VIEW, viewName);
            ActivityOptionsCompat options =
                    ActivityOptionsCompat.makeSceneTransitionAnimation(
                            originActivity, view, viewName);
            if (requestCode == -1) {
                ActivityCompat.startActivity(originActivity, intent, options.toBundle());
            } else {
                ActivityCompat.startActivityForResult(originActivity, intent, requestCode, options.toBundle());
            }
        } else {
            if (requestCode == -1) {
                originActivity.startActivity(intent);
            } else {
                originActivity.startActivityForResult(intent, requestCode);
            }
            HeroActivity.activitySwitchAnimation(originActivity, R.anim.activity_slide_in, R.anim.activity_still);
        }
    }

    public void transitionMagicView(final String transitionName) {
        final IHero view = HeroView.findViewByName(mLayout, transitionName);
        if (view != null) {
            ViewCompat.setTransitionName((View) view, transitionName);
        }
    }

    protected void resetActionBarOptions() {
        if (getActionBar() != null) {
            getActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_TITLE | ActionBar.DISPLAY_USE_LOGO | ActionBar.DISPLAY_SHOW_HOME);
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    protected ActionBar getActionBar() {
        if (getActivity() != null) {
            return getActivity().getActionBar();
        }
        return null;
    }

    protected void setActivityTitle(int titleId) {
        if (getActionBar() != null) {
            getActionBar().setTitle(titleId);
        } else {
            if (toolbar != null && toolbar.getVisibility() == View.VISIBLE) {
                toolbarTitleView.setText(titleId);
            } else {
                getActivity().setTitle(title);
            }
        }
    }

    protected void setActivityTitle(CharSequence title) {
        if (getActionBar() != null) {
            getActivity().setTitle(title);
        } else {
            if (toolbar != null) {// && toolbar.getVisibility() == View.VISIBLE) {
                toolbarTitleView.setText(title);
            }
        }
    }

    protected void setActivityImageTitle(String url) {
        if (toolbar != null && toolbar.getVisibility() == View.VISIBLE) {
            ImageLoadUtils.loadLocalImage(toolbarImageView, url);
        }

    }
    protected void setTitleBackgroundColor(int color) {
        ActionBar actionBar = getActionBar();
        if (actionBar != null) {
            if (android.os.Build.VERSION.SDK_INT >= 19) {
                actionBar.setStackedBackgroundDrawable(new ColorDrawable(color));
            }
        } else {
            if (toolbar != null) {
                toolbar.setBackgroundColor(color);
            }
        }
    }

    protected void setLeftItemTitle(CharSequence title) {
        if (getActionBar() != null) {
            ((HeroFragmentActivity) getActivity()).setLeftItemTitle(title);
        } else {
            if (leftItemsLayout != null) {
                View leftItem = getFirstChild(leftItemsLayout);
                if (leftItem instanceof TextView) {
                    ((TextView) leftItem).setText(title);
                }
            }
        }
    }

    protected final String getTitle() {
        if (title != null) {
            return title;
        }
        return null;
    }

    public void setTitle(int title) {
        this.title = getString(title);
    }

    public void setTitle(String title) {
        this.title = title;
        setActivityTitle(title);
    }

    public JSONObject getLeftItem() {
        return mLeftItem;
    }

    public JSONArray getRightItem() {
        return mRightItems;
    }

    public static ProgressDialog createProgressDialog(Context context) {
        ProgressDialog dialog;
        dialog = new ProgressDialog(context);
        dialog.setIndeterminate(true);
        dialog.setCancelable(true);
        dialog.setCanceledOnTouchOutside(false);
        return dialog;
    }

    public static void showProgressDialog(ProgressDialog dialog, boolean cancelable) {
        if (dialog != null) {
            dialog.show();
            dialog.setCancelable(cancelable);
            dialog.getWindow().setDimAmount(PROGESS_DIALOG_DIM_AMOUNT);
            dialog.setContentView(R.layout.layout_progress_dialog);
            dialog.getWindow().setBackgroundDrawableResource(R.color.heroTransparent);
        }
    }

    public void showLongProgressDialog(ProgressDialog dialog, boolean cancelable, Object contents) {
        if (dialog != null) {
            dialog.show();
            dialog.setCancelable(cancelable);
            dialog.getWindow().setDimAmount(LONG_PROGESS_DIM_AMOUNT);
            View progressView = LayoutInflater.from(getContext()).inflate(R.layout.layout_long_progress_dialog, null);
            final LoadingTextView loadingTextView = (LoadingTextView) progressView.findViewById(R.id.loadingText);
            loadingTextView.setTextArray(contents);
            dialog.setContentView(progressView);
        }
    }

    public void showClosableProgressDialog(final ProgressDialog dialog, boolean cancelable, String content) {
        if (dialog != null) {
            dialog.show();
            dialog.setCancelable(cancelable);
            dialog.getWindow().setDimAmount(PROGESS_DIALOG_DIM_AMOUNT);
            View progressView = LayoutInflater.from(getContext()).inflate(R.layout.layout_closable_progress_dialog, null);
            final TextView textView = (TextView) progressView.findViewById(R.id.loadingText);
            final View closeView = progressView.findViewById(R.id.closeView);
            textView.setText(content);

            closeView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    hideProgressDialog(dialog);
                }
            });
            dialog.setContentView(progressView);
        }
    }

    public static void hideProgressDialog(ProgressDialog dialog) {
        try {
            if (dialog != null && dialog.isShowing()) {
                dialog.dismiss();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private AlertDialog.Builder createCommonAlertDialog(String title, String content) {
        return createCommonAlertDialog(getContext(), title, content);
    }

    public static AlertDialog.Builder createCommonAlertDialog(Context context, String title, String content) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        View customView = LayoutInflater.from(context).inflate(R.layout.hero_common_dialog, null);
        TextView txtTitle = (TextView) customView.findViewById(R.id.txtTitle);
        TextView txtContent = (TextView) customView.findViewById(R.id.txtContent);
        if (TextUtils.isEmpty(title)) {
            txtTitle.setVisibility(View.GONE);
        } else {
            txtTitle.setVisibility(View.VISIBLE);
            txtTitle.setText(title);
        }
        if (!TextUtils.isEmpty(content)) {
            txtContent.setText(content);
        }
        builder.setView(customView);
        return builder;
    }

    private Dialog addContentToPopWindow(Object object, String closeImage) {
        View customView = LayoutInflater.from(getContext()).inflate(R.layout.custom_dialog, null);
        ViewGroup root = (ViewGroup) customView.findViewById(R.id.content);
        if (closeImage != null) {
            ImageView close = (ImageView) customView.findViewById(R.id.closeImage);
            close.setImageResource(ImageLoadUtils.getLocalImageIdByName(getContext(), closeImage));
            closeImageView = close;
        }
        Dialog dialog = new Dialog(getContext(), R.style.CustomDialog);

        dialog.setContentView(customView);
        customView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hideSoftKeyboard(activity);
                View focusedView = v.findFocus();
                if (focusedView != null) {
                    focusedView.clearFocus();
                }
            }
        });
        if (object instanceof JSONObject) {
            try {
                JSONObject json = (JSONObject) object;
                IHero view = createNewIHeroView(json);
                addDescriptionToView(view);
                if (view != null) {
                    root.addView((View) view);
                    // views on dialog can extend to navigation bar
                    HeroView.setExtendToNavigationBar((View) view);
                    view.on(json);
                    // as defined, a view added to dialog is always center, its left and top should be ignored
                    ViewGroup.LayoutParams params = ((View) view).getLayoutParams();
                    if (params != null && params instanceof FrameLayout.LayoutParams) {
                        ((FrameLayout.LayoutParams) params).leftMargin = 0;
                        ((FrameLayout.LayoutParams) params).topMargin = 0;
                        ((FrameLayout.LayoutParams) params).gravity = Gravity.CENTER_HORIZONTAL;
                    }

                    if (view instanceof ViewGroup) {
                        final int count = ((ViewGroup) view).getChildCount();
                        for (int i = 0; i < count; i++) {
                            View child = ((ViewGroup) view).getChildAt(i);
                            if (child instanceof HeroButton) {
                                ((HeroButton) child).setOnDialog(true);
                            }
                        }
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            } catch (NoSuchMethodException e) {
                e.printStackTrace();
            } catch (java.lang.InstantiationException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            }
        }
        return dialog;
    }

    private boolean isPopWindowShown() {
        return (customDialog != null && customDialog.isShowing());
    }

    private JSONObject getPendingDialogContent() {
        if (pendingDialogQueue == null || pendingDialogQueue.size() == 0) {
            return null;
        }
        JSONObject view = pendingDialogQueue.poll();
        return view;
    }

    private void addPendingDialogContent(JSONObject view) {
        if (pendingDialogQueue == null) {
            pendingDialogQueue = new LinkedList<>();
        }
        pendingDialogQueue.offer(view);
    }

    private void showPendingDialog(JSONObject content) {
        JSONObject show = new JSONObject();
        JSONObject command = new JSONObject();
        try {
            show.put("show", content);
            command.put("command", show);
            this.on(command);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void showPopWindow() {
        if (customDialog != null && !customDialog.isShowing()) {
            customDialog.show();
        }
    }

    private void closePopWindow() {
        if (customDialog != null && customDialog.isShowing()) {
            customDialog.dismiss();
            customDialog = null;
        }
    }

    private IHero findViewOnPopWindow(String name) {
        IHero view = null;
        if (customDialog != null) {
            View dialogView = customDialog.findViewById(R.id.layoutRoot);
            if (dialogView != null) {
                view = HeroView.findViewByName(dialogView, name);
            }
        }
        return view;
    }

    private void createPopupLayer(View content) {
        if (popLayout != null) {
            popLayout.removeAllViews();
        }
        if (popLayout == null) {
            popLayout = new RelativeLayout(getContext());
            popLayout.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
            popLayout.setBackgroundColor(getResources().getColor(R.color.popupWindowBackground));
            popLayout.setClickable(true);
            RelativeLayout.LayoutParams param = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            param.addRule(RelativeLayout.CENTER_IN_PARENT);
            popLayout.addView(content, param);
        }
    }

    private void addPopupLayer(View content) {
        if (popLayout != null && popLayout.getParent() == null) {
            final ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
            if (rootLayout != null) {
                try {
                    rootLayout.addView(popLayout, params);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                hideSoftKeyboard(activity);
            }
        }
    }

    private void removePopupLayer() {
        if (popLayout != null) {
            try {
                popLayout.removeAllViews();
                rootLayout.removeView(popLayout);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public ArrayList<Integer> getRequestCodes() {
        return requestCodes;
    }

    private boolean isAlwaysNeedReload(String data, int version) {
        if (TextUtils.isEmpty(data)) {
            return false;
        }
        // as defined, version 0 means the page always need reloading, such as which contains captcha
        if (version == 0) {
            return true;
        }
        return false;
    }

    protected class HeroMenuItemClickListener implements MenuItem.OnMenuItemClickListener {
        Activity mContext;
        JSONObject mAction;

        public HeroMenuItemClickListener(Context context, JSONObject action) {
            super();
            mContext = (Activity) context;
            mAction = action;
        }

        @Override
        public boolean onMenuItemClick(MenuItem item) {
            if (mContext != null && mAction != null) {
                try {
                    ((IHeroContext) mContext).on(mAction);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                return true;
            }
            return false;
        }
    }

    private void setActionbarTitleEnabled(boolean enabled) {
        if (getActionBar() != null) {
            getActionBar().setDisplayShowTitleEnabled(enabled);
        }
    }

    private void setActionbarDisplayOptions(int options) {
        if (getActionBar() != null) {
            getActionBar().setDisplayOptions(options);
        }
    }

    public static void hideSoftKeyboard(Context context) {
        if (context != null) {
            try {
                InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
                View focusView = ((Activity) context).getCurrentFocus();
                if (focusView != null) {
                    imm.hideSoftInputFromWindow(focusView.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void setupToolbarLeftItems(JSONObject data) {
        if (leftItemsLayout != null) {
            if (mLeftItem == null || mLeftItem.length() == 0) {
                if (!((HeroFragmentActivity) getActivity()).isBackIconShown()) {
                    leftItemsLayout.removeAllViews();
                }
            } else {
                if (mLeftItem.has("title")) {
                    leftItemsLayout.removeAllViews();
                    try {
                        String title = mLeftItem.getString("title");
                        if (!TextUtils.isEmpty(title)) {
                            createTextButton(mLeftItem, leftItemsLayout, mLeftItem.optJSONObject("click"));
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                } else if (mLeftItem.has("image")) {
                    leftItemsLayout.removeAllViews();
                    createImageButton(leftItemsLayout, mLeftItem);
                }
            }
        }
    }

    public void setupToolbarRightItems(JSONObject data) {

        if (rightItemsLayout != null) {
            rightItemsLayout.removeAllViews();
            if (mRightItems != null) {
                for (int i = 0; i < mRightItems.length(); i++) {
                    JSONObject item = null;
                    String title;
                    JSONObject action = null;
                    try {
                        item = mRightItems.getJSONObject(i);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    if (item.has("title")) {
                        try {
                            title = item.getString("title");
                            action = item.optJSONObject("click");
                        } catch (JSONException e) {
                            title = "";
                            e.printStackTrace();
                        }
                        if (title != null) {
                            createTextButton(item, rightItemsLayout, action);
                        }
                    } else if (item.has("image")) {
                        createImageButton(rightItemsLayout, item);
                    }
                }
            }
        }
    }

    private View getFirstChild(ViewGroup viewGroup) {
        if (viewGroup.getChildCount() == 0) {
            return null;
        }
        return viewGroup.getChildAt(0);
    }

    private void returnToHome() {
        Intent intent = HeroApp.getHomeIntent(getContext());
        if ((getActivity() instanceof HeroActivity) && intent != null) {
            try {
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @SuppressLint("ResourceAsColor")
    private TextView createTextButton(JSONObject object, ViewGroup parent, final JSONObject clickEvent) {
        String text = object.optString("title");
        TextView textButton = (TextView) LayoutInflater.from(getContext()).inflate(R.layout.action_button, parent, false);
        if (text != null) {
            textButton.setText(text);
        }
        if (object.has("tintColor")) {
            try {
                textButton.setTextColor(HeroView.parseColor(object.getString("tintColor")));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        } else {
            textButton.setTextColor(tintColor);
        }
        parent.addView(textButton);

        if (clickEvent != null) {
            (textButton).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    HeroView.sendActionToContext(getContext(), clickEvent);
                    hideSoftKeyboard(getContext());
                }
            });
        }
        return textButton;
    }

    private ImageView createImageButton(ViewGroup parent, JSONObject data) {
        View view = LayoutInflater.from(getContext()).inflate(R.layout.action_image, parent, false);

        parent.addView(view);
        ImageView imageView = (ImageView) view.findViewById(R.id.actionImage);
        String image = data.optString("image");
        if (image != null) {
            Drawable originDrawable = ContextCompat.getDrawable(getActivity(),
                    ImageLoadUtils.getLocalImageIdByName(getContext(), image));
            imageView.setImageDrawable(tintDrawable(originDrawable, tintColor));

            final JSONObject clickEvent = data.optJSONObject("click");
            if (clickEvent != null) {
                imageView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        HeroView.sendActionToContext(getContext(), clickEvent);
                        hideSoftKeyboard(getContext());
                    }
                });
            }
        }
        return imageView;
    }

    public View getToolbar() {
        return toolbar;
    }

    public int getLayoutId() {
        return R.layout.base_fragment;
    }

    public AbsListView.OnScrollListener getListScrollListener() {
        return null;
    }

    public boolean getNavigationBarHidden() {
        return isNavigationBarHidden;
    }

    public void setNavigationBarHidden(boolean hidden) {
        isNavigationBarHidden = hidden;
        if (toolbarContainer != null){
            toolbarContainer.setVisibility(hidden ? View.GONE : View.VISIBLE);
        }
    }
    public boolean onBackPressed() {
        if (goBackWebView()) {
            return true;
        }
        return false;
    }

    private void addDescriptionToView(IHero view) {
        if (BuildConfig.DEBUG && view instanceof View) {
            String pageName = ContextUtils.getPageName(mUrl);
            if (pageName != null) {
                ((View) view).setContentDescription(pageName + viewIndex);
                viewIndex++;
            }
        }
    }

    private void startActivitySafely(Intent i) {
        try {
            startActivity(i);
        } catch (ActivityNotFoundException e) {
            e.printStackTrace();
        }
    }

//    private void removeRootScrollView() {
//        if (rootLayout != null && mainContentView != null && mainContentView instanceof ScrollView) {
//            if (mLayout != null) {
//                ((ViewGroup) mainContentView).removeView(mLayout);
//                rootLayout.removeView(mainContentView);
//                rootLayout.addView(mLayout, 0);
//                // restore the top offset of main content
//                mainContentView = mLayout;
//                setNavigationBarOverlayed(isNavigationBarHidden);
//            }
//        }
//    }

    public void addReminder(JSONObject jsonObject) {
        if (jsonObject.has("remind")) {
            try {
                JSONObject remindObj = jsonObject.getJSONObject("remind");
                String date = remindObj.optString("date");
                if (!TextUtils.isEmpty(date) && remindObj.has("title")) {
                    ReminderDelegate.ReminderInfo info = ReminderDelegate.generateReminderInfo(getContext(), date, remindObj.optString("title"));
                    if (info != null) {
                        getReminderDelegate().setReminder(info);
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    private ReminderDelegate getReminderDelegate() {
        if (reminderDelegate == null) {
            reminderDelegate = new ReminderDelegate(getContext());
        }
        return reminderDelegate;
    }

    private void addCustomActionBar(IHero view, JSONObject titleView, boolean showHome) throws JSONException {
        addDescriptionToView(view);
        if (view != null) {
            ((View) view).setBackgroundColor(Color.TRANSPARENT);
            view.on(titleView);
            if (getActivity() != null) {
                ActionBar actionBar = self.getActionBar();
                if (actionBar != null) {
                    ActionBar.LayoutParams p = new ActionBar.LayoutParams(((View) view).getLayoutParams());
                    p.gravity = p.gravity & ~Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL;
                    if (!showHome) {
                        setActionbarDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM | ActionBar.DISPLAY_SHOW_TITLE);
                    } else {
                        setActionbarDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM | ActionBar.DISPLAY_SHOW_TITLE | ActionBar.DISPLAY_HOME_AS_UP);
                    }
                    getActionBar().setCustomView((View) view, p);
                    setActivityTitle("");
                }
            }
        }
    }

    private void setStatusBarColor(int color) {
        if(Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP) {
            if (getActivity() != null) {
                getActivity().getWindow().setStatusBarColor(color);
            }
        }
    }

    public boolean goBackWebView() {
        if (customWebView != null && customWebView.getParent() != null) {
            if (customWebView.canGoBack()) {
                customWebView.goBack();
                return true;
            }
        }
        return false;
    }

    public interface ContextMenuItemListener {
        public void onMenuSelected(MenuItem item);
    }
}
