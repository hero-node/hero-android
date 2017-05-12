package com.hero;

import android.app.ActionBar;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.StateListDrawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class HeroTabActivity extends HeroHomeActivity implements RadioGroup.OnCheckedChangeListener, View.OnClickListener {
    public static final String LOCAL_INTENT_TAB_CHANGE = "tabSelect";
    public static final String LOCAL_INTENT_FINISH_LOADING = "finishLoading";
    public static final String LOCAL_INTENT_SHOW_LOADING = "showLoading";
    protected String currentTag;
    protected RadioGroup radioGroupHost;
    protected Map<String, String> urlMap;
    protected List<String> contentTags;
    protected List<RadioButton> buttons;
    protected JSONArray contentArray;
    protected View tabHost;
    protected ViewGroup dotIndicatorLayout;
    private LocalBroadcastManager manager;
    private BroadcastReceiver tabChangedReceiver, badgeReceiver, finishLoadingReceiver, showLoadingReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initContent();
        if (getAppActionBar() != null) {
            getAppActionBar().setDisplayShowHomeEnabled(false);
            getAppActionBar().setDisplayHomeAsUpEnabled(false);
            getAppActionBar().setDisplayShowTitleEnabled(false);
            getAppActionBar().setDisplayUseLogoEnabled(false);
            getAppActionBar().setDisplayShowCustomEnabled(true);
            getAppActionBar().setCustomView(R.layout.custom_title);
        }

        manager = LocalBroadcastManager.getInstance(this);
        tabChangedReceiver = registerLocalReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String value = intent.getStringExtra("value");
                try {
                    int tabId = Integer.parseInt(value);
                    changeTab(tabId);
                } catch (NumberFormatException e) {
                    e.printStackTrace();
                }
            }
        }, new IntentFilter(LOCAL_INTENT_TAB_CHANGE));
        badgeReceiver = registerLocalReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String value = intent.getStringExtra(HeroApp.HEROAPP_EXTRA_BADGE_VALUE);
                int index = intent.getIntExtra(HeroApp.HEROAPP_EXTRA_BADGE_INDEX, -1);
                showDotIndicator(index, !TextUtils.isEmpty(value));
            }
        }, new IntentFilter(HeroApp.HEROAPP_BADGE));
        finishLoadingReceiver = registerLocalReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                onFinishLoading();
            }
        }, new IntentFilter(LOCAL_INTENT_FINISH_LOADING));
        showLoadingReceiver = registerLocalReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                startLoading();
            }
        }, new IntentFilter(LOCAL_INTENT_SHOW_LOADING));
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        String extra = intent.getStringExtra("newApp");
        if (extra != null) {
            try {
                JSONObject newApp = new JSONObject(extra);
                if (newApp.has("tabs")) {
                    if (HeroApplication.getInstance().getHeroApp() != null) {
                        HeroApplication.getInstance().getHeroApp().on(newApp);
                    }
                    initNewApp(newApp.getJSONArray("tabs"));
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterLocalReceiver(tabChangedReceiver);
        tabChangedReceiver = null;
        unregisterLocalReceiver(badgeReceiver);
        badgeReceiver = null;
        unregisterLocalReceiver(finishLoadingReceiver);
        finishLoadingReceiver = null;
        unregisterLocalReceiver(showLoadingReceiver);
        showLoadingReceiver = null;
    }

    protected void initContent() {
        setContentView(R.layout.tab_activity_main);
        radioGroupHost = (RadioGroup) findViewById(R.id.tab_bar_controller);
        tabHost = findViewById(R.id.tab_host);
        dotIndicatorLayout = (ViewGroup) findViewById(R.id.tab_dot_layout);
        ActionBar actionBar = getAppActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeButtonEnabled(true);
        }
        if (HeroApplication.getInstance().getHeroApp() != null) {
            Object content = HeroApplication.getInstance().getHeroApp().getContent();
            if (content != null && content instanceof JSONObject && ((JSONObject) content).has("tabs")) {
                try {
                    contentArray = ((JSONObject) content).getJSONArray("tabs");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
        if (contentArray == null) {
            finish();
            return;
        }
        initMap();
        initTabItems(radioGroupHost);
        radioGroupHost.setOnCheckedChangeListener(this);
        if (contentTags.size() > 0) setContentFragment(contentTags.get(0));
    }

    private void initMap() {
        if (contentArray == null || contentArray.length() == 0) return;
        if (urlMap == null) {
            urlMap = new HashMap<String, String>();
        }
        if (contentTags == null) {
            contentTags = new ArrayList<String>();
        }
        if (buttons == null) {
            buttons = new ArrayList<RadioButton>();
        }
        try {
            for (int j = 0; j < contentArray.length(); j++) {
                String tag = "TAG_" + j;
                contentTags.add(tag);
                urlMap.put(tag, contentArray.getJSONObject(j).getString("url"));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    protected void initTabItems(RadioGroup rg) {
        if (contentArray == null || contentArray.length() == 0) return;
        try {
            for (int j = 0; j < contentArray.length(); j++) {
                String title = contentArray.getJSONObject(j).optString("title");
                RadioButton button = (RadioButton) LayoutInflater.from(this).inflate(R.layout.tab_items, rg, false);
                if (!TextUtils.isEmpty(title)) {
                    button.setText(title);
                }
                if (contentArray.getJSONObject(j).has("imageId") && contentArray.getJSONObject(j).has("imageIdSeleted")) {
                    int id = contentArray.getJSONObject(j).getInt("imageId");
                    int idSeleted = contentArray.getJSONObject(j).getInt("imageIdSeleted");
                    StateListDrawable tabIcon = generateTabIcon(id, idSeleted);
                    button.setCompoundDrawables(null, tabIcon, null, null);
                } else if (contentArray.getJSONObject(j).has("image")) {
                    String imageName = contentArray.getJSONObject(j).getString("image");
                    int id = HeroView.getResIdByName(this, imageName, "drawable");
                    if (id != 0) {
                        StateListDrawable tabIcon = generateTabIcon(id, id);
                        button.setCompoundDrawables(null, tabIcon, null, null);
                    }
                }
                button.setTag(contentTags.get(j));
                rg.addView(button);
                buttons.add(button);
                if (dotIndicatorLayout != null) {
                    LayoutInflater.from(this).inflate(R.layout.tab_dot_indicator, dotIndicatorLayout, true);
                }
                if (j == 0) {
                    button.setChecked(true);
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    protected void showDotIndicator(int index, boolean isShown) {
        if (dotIndicatorLayout != null && index >= 0) {
            View view = dotIndicatorLayout.getChildAt(index);
            if (view != null) {
                view.setVisibility(isShown ? View.VISIBLE : View.INVISIBLE);
            }
        }
    }

    // override this method to show actionbar
    public boolean isActionBarShown() {
        return false;
    }

    public boolean isBackIconShown() {
        return false;
    }

    @Override
    public HeroFragment getCurrentFragment() {
        return (HeroFragment) getSupportFragmentManager().findFragmentByTag(currentTag);
    }

    public void setContentFragment(String page) {

        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        FragmentManager fragmentManager = getSupportFragmentManager();

        if (currentTag != null) {
            Fragment lastFragment = fragmentManager.findFragmentByTag(currentTag);
            if (lastFragment != null) {
                ft.hide(lastFragment);
            }
        }

        Fragment fragment = getFragment(page);
        if (!fragment.isAdded()) {
            ft.add(R.id.content_frame, fragment, page);
        } else {
            ft.show(fragment);
        }

        ft.commitAllowingStateLoss();
        currentTag = page;
    }

    private void initNewApp(JSONArray tabs) {
        if (tabs == null || tabs.length() == 0) {
            return;
        }
        contentArray = tabs;
        contentTags.clear();
        urlMap.clear();
        initMap();
        reInitButtons(tabs);
        reInitFragments();
        if (urlMap.size() <= 1) {
            tabHost.setVisibility(View.GONE);
        } else {
            tabHost.setVisibility(View.VISIBLE);
        }
    }

    private void reInitButtons(final JSONArray tabs) {
        try {
            if (buttons != null) {
                for (int i = 0; i < buttons.size(); i++) {
                    Button button = buttons.get(i);
                    if (i >= tabs.length()) {
                        break;
                    }
                    JSONObject object = tabs.getJSONObject(i);
                    button.setText(object.getString("title"));
                    String imageName = object.getString("image");
                    int id = HeroView.getResIdByName(this, imageName, "drawable");
                    if (id != 0) {
                        StateListDrawable tabIcon = generateTabIcon(id, id);
                        button.setCompoundDrawables(null, tabIcon, null, null);
                    }
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    protected void reInitFragments() {
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        FragmentManager fragmentManager = getSupportFragmentManager();
        for (String tag : contentTags) {
            Fragment oldFragment = fragmentManager.findFragmentByTag(tag);
            if (oldFragment != null) {
                if (isHomePage(tag)) {
                    Fragment fragment = null;
                    if (urlMap != null && urlMap.size() > 0) {
                        String url = urlMap.get(tag);
                        if (url != null) {
                            fragment = generateFragment(isHomePage(tag));
                            Bundle bundle = new Bundle();
                            bundle.putString("url", url);
                            fragment.setArguments(bundle);
                        }
                    }
                    fragment.setRetainInstance(false);
                    ft.replace(R.id.content_frame, fragment, tag);
                } else {
                    ft.remove(oldFragment);
                }
            }
        }
        ft.commitAllowingStateLoss();
        setContentFragment(contentTags.get(0));
    }

    @Override
    public void onCheckedChanged(RadioGroup group, int checkedId) {
        Object tag = group.findViewById(checkedId).getTag();
        String page = tag.toString();
        if (page != null) {
            setContentFragment(page);
        }
    }

    protected void changeTab(int index) {
        if (index < buttons.size()) {
            try {
                buttons.get(index).performClick();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onClick(View v) {
        if (v != null) {
            Object tag = v.getTag();

            String url = urlMap.get(tag.toString());
            // if it's a radio button, just change the fragment, else start an activity
            if (!(v instanceof RadioButton)) {
                if (url != null) {
                    Intent intent = new Intent(this, HeroActivity.class);
                    intent.putExtra("url", url);
                    intent.putExtra("headBarVisible", true);
                    startActivity(intent);
                }
            } else {
                setContentFragment(tag.toString());
            }
        }
    }

    public Fragment getFragment(String page) {
        Fragment fragment = getSupportFragmentManager().findFragmentByTag(page);
        if (fragment != null) {
            return fragment;
        }

        if (urlMap != null && urlMap.size() > 0) {
            String url = urlMap.get(page);
            if (url != null) {
                fragment = generateFragment(isHomePage(page));
                Bundle bundle = new Bundle();
                bundle.putString("url", url);
                fragment.setArguments(bundle);
            }
        }
        fragment.setRetainInstance(false);
        return fragment;
    }

    protected Fragment generateFragment(boolean isHomePage) {
        return new HeroFragment();
    }

    @Override
    public void on(Object object) throws JSONException {
        if (object instanceof JSONObject) {
            JSONObject json = (JSONObject) object;
            HeroFragment fragment = getFragmentWithTag(json);
            if (fragment != null) {
                fragment.on(object);
            }
        } else if (object instanceof JSONArray) {
            JSONObject taggedObject = null;
            for (int i = 0; i < ((JSONArray) object).length(); i ++) {
                JSONObject item = ((JSONArray) object).optJSONObject(i);
                if (item.has("fragment_tag")) {
                    taggedObject = item;
                    break;
                }
            }
            HeroFragment fragment = getFragmentWithTag(taggedObject);
            fragment.on(object);
        }
    }

    private HeroFragment getFragmentWithTag(JSONObject json) {
        HeroFragment fragment = null;
        if (json != null && json.has("fragment_tag")) {
            String tag = json.optString("fragment_tag");
            fragment = (HeroFragment) (getSupportFragmentManager().findFragmentByTag(tag));
        }
        if (fragment == null) {
            fragment = getCurrentFragment();
        }
        return fragment;
    }

    private StateListDrawable generateTabIcon(int id, int idSeleted) {
        StateListDrawable drawable = new StateListDrawable();
        Drawable icons[] = new Drawable[2];
        icons[0] = getResources().getDrawable(idSeleted);
        icons[1] = getResources().getDrawable(id);

        drawable.addState(new int[] {android.R.attr.state_checked}, icons[0]);
        drawable.addState(new int[] {}, icons[1]);
        drawable.setBounds(0, 0, getResources().getDimensionPixelSize(R.dimen.tab_icon_width), getResources().getDimensionPixelSize(R.dimen.tab_icon_height));
        return drawable;
    }

    private boolean isHomePage(String tag) {
        return "TAG_0".equals(tag);
    }

    protected BroadcastReceiver registerLocalReceiver(BroadcastReceiver receiver, IntentFilter filter) {
        if (receiver != null && manager != null) {
            manager.registerReceiver(receiver, filter);
        }
        return receiver;
    }

    protected void unregisterLocalReceiver(BroadcastReceiver receiver) {
        if (receiver != null && manager != null) {
            manager.unregisterReceiver(receiver);
        }
    }
}
