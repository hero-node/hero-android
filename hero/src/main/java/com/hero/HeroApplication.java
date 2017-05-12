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

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.multidex.MultiDexApplication;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.webkit.ValueCallback;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;
import java.util.Stack;

/**
 * Created by R9L7NGH on 2016/3/1.
 */
public abstract class HeroApplication extends MultiDexApplication {
    protected CookieManager cookieManager;
    private static HeroApplication instance;
    private static Context applicationContext;
    protected HeroApp heroApp;
    private Stack<Activity> activityStack;

    public HeroApplication() {
        super();
    }

    public HeroApplication(Context c, String homeAddress) {
        applicationContext = c;
        cookieManager = CookieManager.getInstance();
        activityStack = new Stack<Activity>();
        heroApp = new HeroApp(c);
    }

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

    public static void createInstance(Application c, final String homeAddress, final String referer) {
        if (instance == null) {
            instance = new HeroApplication(c, homeAddress) {
                @Override
                public String getHomeAddress() {
                    return homeAddress;
                }

                @Override
                public String getHttpReferer() {
                    return referer;
                }
            };
        }
    }

    public String getPackageName() {
        if (applicationContext != null) {
            return applicationContext.getPackageName();
        }
        return getBaseContext().getPackageName();
    }

    public PackageManager getPackageManager() {
        if (applicationContext != null) {
            return applicationContext.getPackageManager();
        }
        return getBaseContext().getPackageManager();
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
        try {
            URL netUrl = new URL(url);
            return netUrl.getHost();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

        return null;
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

    public String getExtraUserAgent() {
        return "";
    }

    public Intent getNewActivityIntent(Context context, String url, boolean isPresent) {
        return null;
    }

    public abstract String getHomeAddress();

    public abstract String getHttpReferer();
}
