package com.hero;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.support.multidex.MultiDexApplication;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.webkit.ValueCallback;

import java.util.Map;
import java.util.Stack;

/**
 * Created by R9L7NGH on 2016/3/1.
 */
public abstract class HeroApplication extends MultiDexApplication {
    protected CookieManager cookieManager;
    private static HeroApplication instance;
    protected HeroApp heroApp;
    private Stack<Activity> activityStack;

    public static synchronized HeroApplication getInstance() {
        return HeroApplication.instance;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        HeroApplication.instance = this;
        cookieManager = CookieManager.getInstance();
        heroApp = new HeroApp(this);
        activityStack = new Stack<Activity>();
    }

    public static void clearAllCookies(Context context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            CookieSyncManager.createInstance(context);
            CookieManager.getInstance().removeAllCookie();
        } else {
            CookieManager.getInstance().flush();
            CookieManager.getInstance().removeAllCookies(new ValueCallback<Boolean>() {
                @Override
                public void onReceiveValue(Boolean value) {
                }
            });
        }
    }

    public CookieManager getCookieManager() {
        return cookieManager;
    }

    public void addCookies(String url, String cookie) {
        cookieManager.setCookie(url, cookie);
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            CookieSyncManager.getInstance().sync();
        } else {
            CookieManager.getInstance().flush();
        }
    }

    public static String getDomainAddress(String url) {
        int index = url.indexOf("://");
        if (index != -1) {
            url = url.substring(index + "://".length());
        }
        index = url.indexOf("/");
        if (index != -1) {
            url = url.substring(0, index);
        }
        return url;
    }

    public HeroApp getHeroApp() {
        return heroApp;
    }

    public Stack getActivityStack() {
        return activityStack;
    }

    public Activity getTopActivity() {
        if (activityStack == null || activityStack.size() == 0) {
            return null;
        }
        Activity activity = activityStack.lastElement();

        return activity;
    }

    public void pushActivity(Activity activity) {
        if (activityStack == null) {
            activityStack = new Stack<Activity>();
        }
        activityStack.add(activity);
    }

    public void popActivity(Activity activity) {
        if (activity != null) {
            activityStack.remove(activity);
        }
    }

    public boolean isActivityInStack(String className) {
        Class cls = null;
        try {
            cls = Class.forName(className);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            return false;
        }

        for (Activity activity : activityStack) {
            if (activity.getClass().getName().equals(className)) {
                return true;
            }
        }
        return false;
    }

    public int getBottomActivityInStack(String className) {
        Class cls = null;
        try {
            cls = Class.forName(className);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            return -1;
        }

        for (int i = 0; i < activityStack.size(); i++) {
            Activity activity = activityStack.get(i);
            if (activity.getClass().getName().equals(className)) {
                return i;
            }
        }
        return -1;
    }

    public int getTopActivityInStack(String className) {
        Class cls = null;
        try {
            cls = Class.forName(className);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            return -1;
        }

        int index = -1;
        for (int i = 0; i < activityStack.size(); i++) {
            Activity activity = activityStack.get(i);
            if (activity.getClass().getName().equals(className)) {
                index = i;
            }
        }
        return index;
    }

    public void popUpToActivity(int index, boolean including) {
        if (index > activityStack.size()) {
            index = activityStack.size();
        }
        // pop to the index+1 activity
        int top = index + 1;
        if (including) {
            // pop the activity of the index
            top--;
        }

        while (activityStack.size() > top && activityStack.size() > 0) {
            Activity activity = activityStack.lastElement();
            activity.finish();
            popActivity(activity);
        }
    }

    public Map getExtraHttpHeader() {
        return null;
    }

    public Intent getNewActivityIntent(Context context, String url, boolean isPresent) {
        return null;
    }

    public abstract String getHomeAddress();

    public abstract String getHttpReferer();
}
