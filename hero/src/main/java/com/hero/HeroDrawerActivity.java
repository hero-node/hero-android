package com.hero;

import android.content.Intent;
import android.os.Bundle;
import android.os.Looper;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.DrawerLayout;
import android.view.Gravity;
import android.view.ViewGroup;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


/**
 * Created by xincai on 17-3-7.
 */

public abstract class HeroDrawerActivity extends HeroHomeActivity {

    private HeroFragment mainFragment;
    private HeroFragment drawerFragment;
    private DrawerLayout drawerLayout;
    private ViewGroup drawerContentLayout;
    private int drawerGravity = Gravity.LEFT;
    protected String mainUrl;

    public static final String COMMAND_SHOW_MENU = "showMenu";
    public static final String COMMAND_ENABLE_MENU = "enableMenu";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initContent();
    }

    protected void initContent() {
        setContentView(R.layout.activity_drawer);
        drawerLayout = (DrawerLayout) findViewById(R.id.drawer);
        drawerGravity = Gravity.LEFT;
        drawerContentLayout = (ViewGroup) findViewById(R.id.drawerContent);
        int screenWidth = HeroView.getScreenWidth(this);
        if (screenWidth > 0) {
            drawerContentLayout.getLayoutParams().width = screenWidth *2 / 3;
        }
        mainUrl = initFragment();
        initDrawerFragment();
        enableDrawer(false); // default disabled
    }

    public boolean isActionBarShown() {
        return false;
    }

    public boolean isBackIconShown() {
        return false;
    }

    public boolean isDrawerOpen() {
        return drawerLayout != null && (drawerLayout.isDrawerOpen(Gravity.LEFT) || drawerLayout.isDrawerOpen(Gravity.RIGHT));
    }

    public int getHomeTabHeight() {
        return 0;
    }

    public void openDrawer(boolean isOpen) {
        if (drawerGravity != Gravity.NO_GRAVITY) {
            if (isOpen) {
                drawerLayout.openDrawer(drawerGravity);
            } else {
                drawerLayout.closeDrawer(drawerGravity);
            }
        }
    }

    public void enableDrawer(boolean isEnable) {
        if (isEnable) {
            drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED, drawerGravity);
        } else {
            drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED, drawerGravity);
        }
    }

    private void setDrawerGravity(final int gravity) {
        DrawerLayout.LayoutParams params = (DrawerLayout.LayoutParams) drawerContentLayout.getLayoutParams();
        params.gravity = gravity;
        drawerContentLayout.setLayoutParams(params);
    }

    private String initFragment() {
        String url = null;
        if (HeroApplication.getInstance().getHeroApp() != null) {
            Object content = HeroApplication.getInstance().getHeroApp().getContent();
            if (content != null && content instanceof JSONObject && ((JSONObject) content).has("tabs")) {
                try {
                    JSONArray contentArray = ((JSONObject) content).getJSONArray("tabs");
                    mainFragment = new HeroFragment();
                    Bundle bundle = new Bundle();
                    url = contentArray.getJSONObject(0).getString("url");
                    bundle.putString("url", url);
                    mainFragment.setArguments(bundle);
                    mainFragment.setRetainInstance(false);
                    if (mainFragment != null) {
                        FragmentManager fm = getSupportFragmentManager();
                        fm.beginTransaction().replace(R.id.mainContent, mainFragment).commit();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
        return url;
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
                    reInitFragment();
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    private void reInitFragment() {
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.remove(mainFragment); // commit() will be executed in next step
        initFragment();
        if (drawerFragment != null) {
            drawerFragment.getView().removeAllViews();
        }
    }

    private void initDrawerFragment() {
        if (drawerFragment == null) {
            drawerFragment = new HeroFragment();
        }
        drawerFragment.setFullHeight(true);
        if (drawerFragment != null) {
            FragmentManager fm = getSupportFragmentManager();
            fm.beginTransaction().replace(R.id.drawerContent, drawerFragment, "drawer").commit();
        }
    }

    @Override
    public HeroFragment getCurrentFragment() {
        return mainFragment;
    }

    @Override
    public void on(final Object object) throws JSONException {
        if (object instanceof JSONObject) {
            final JSONObject json = (JSONObject) object;
            if (Looper.myLooper() != Looper.getMainLooper()) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            on(json);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });
                return;
            }
            try {
                if (json.has("ui")) {
                    handleUIObject(json.getJSONObject("ui"));
                } else if (json.has("datas")) {
                    handleData(json.get("datas"));
                } else if (json.has("command")) {
                    Object cmdObj = json.get("command");
                    if (cmdObj instanceof JSONObject) {
                        handleCommand(((JSONObject) cmdObj));
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            HeroFragment fragment = getCurrentFragment();
            if (fragment != null) {
                fragment.on(json);
            }
        } else if (object instanceof JSONArray) {
            for (int i = 0; i < ((JSONArray) object).length(); i++) {
                this.on(((JSONArray) object).get(i));
            }
            return;
        }
    }

    private void handleCommand(JSONObject data) throws JSONException {
        if (data.has(COMMAND_SHOW_MENU)) {
            openDrawer(data.getBoolean(COMMAND_SHOW_MENU));
        } else if (data.has(COMMAND_ENABLE_MENU)) {
            enableDrawer(data.optBoolean(COMMAND_ENABLE_MENU));
        }
    }

    private void handleData(Object content) throws JSONException {
        if (drawerFragment != null) {
            JSONObject datas = new JSONObject();
            datas.put("datas", content);
            drawerFragment.on(datas);
        }
    }

    private void handleUIObject(JSONObject content) {
        try {
            JSONObject menuObject = null;
            if (content.has("leftMenu")) {
                menuObject = content.getJSONObject("leftMenu");
                drawerGravity = Gravity.LEFT;
            } else if (content.has("rightMenu")) {
                menuObject = content.getJSONObject("rightMenu");
                drawerGravity = Gravity.RIGHT;
            }
            if (menuObject != null) {
                setDrawerGravity(drawerGravity);
                if (menuObject.has("views")) {
                    JSONObject ui = new JSONObject();
                    ui.put("ui", menuObject);
                    if (drawerFragment != null) {
                        drawerFragment.on(ui);
                    }
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}