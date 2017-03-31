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

import android.app.Activity;
import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.LayerDrawable;
import android.graphics.drawable.RippleDrawable;
import android.graphics.drawable.StateListDrawable;
import android.os.Build;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.view.animation.Animation;
import android.widget.FrameLayout;

import com.hero.depandency.AnimationHelper;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by liuguoping on 15/9/23.
 * 为了和iOS兼容，暂时只支持Frame绝对布局方式
 */

public class HeroView extends FrameLayout implements IHero {

    public HeroView(Context context) {
        super(context);
    }

    public static IHero findViewByName(View view, String name) {
        if (name == null || view == null) return null;
        if (name.equalsIgnoreCase(HeroView.getName(view))) {
            return (IHero) view;
        }
        if (view instanceof ViewGroup) {
            for (int i = 0; i < ((ViewGroup) view).getChildCount(); i++) {
                IHero v = findViewByName(((ViewGroup) view).getChildAt(i), name);
                if (v != null) {
                    return v;
                }
            }
        }
        return null;
    }

    public static IHero findViewByPrefix(View view, String prefix) {
        if (prefix == null || view == null) return null;
        String name = HeroView.getName(view);
        if (!TextUtils.isEmpty(name) && name.startsWith(prefix)) {
            return (IHero) view;
        }
        if (view instanceof ViewGroup) {
            for (int i = 0; i < ((ViewGroup) view).getChildCount(); i++) {
                IHero v = findViewByPrefix(((ViewGroup) view).getChildAt(i), prefix);
                if (v != null) {
                    return v;
                }
            }
        }
        return null;
    }

    public static int px2dip(Context context, float pxValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (pxValue / scale + 0.5f);
    }

    public static int dip2px(Context context, float dip) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dip * scale);
    }

    public static String getName(View view) {
        return (String) view.getTag(R.id.kName);
    }

    public static String getParent(View view) {
        return (String) view.getTag(R.id.kParent);
    }

    public static JSONObject getJson(View view) {
        return (JSONObject) view.getTag(R.id.kHeroJson);
    }

    public static String getFragmentTag(View view) {
        return (String) view.getTag(R.id.kParentFragmentTag);
    }

    public static void setFragmentTag(View view, String tag) {
        if (!TextUtils.isEmpty(tag)) {
            ((View) view).setTag(R.id.kParentFragmentTag, tag);
        }
    }

    public static List<View> getLayoutListener(View view, boolean createIfNull) {
        List<View> layoutListener = (List) view.getTag(R.id.kLayoutListenner);
        if (layoutListener == null) {
            if (createIfNull) {
                layoutListener = new ArrayList<View>();
                view.setTag(R.id.kLayoutListenner, layoutListener);
            }
        }
        return layoutListener;
    }

    public static void setExtendToNavigationBar(View view) {
        view.setTag(R.id.kIsExtendToNavigation, "true");
    }

    public static boolean isExtendToNavigationBar(View view) {
        String value = (String) view.getTag(R.id.kIsExtendToNavigation);
        return "true".equals(value);
    }

    public static int getParentWidth(View view) {
        int width = 0;
        int screen_width = getScreenWidth(view.getContext());
        if (view.getParent() != null) {
            if (((View) view.getParent()).getLayoutParams() != null) {
                width = ((View) view.getParent()).getLayoutParams().width;
            } else {
                width = ((View) view.getParent()).getWidth();
            }
            if (width <= 0) {
                width = ((View) view.getParent()).getWidth();;
            }
        }
        if (width <= 0) {
            width = screen_width;
        }
        return Math.min(width, screen_width);
    }

    public static int getFixedActionBarHeight(Context context) {
        int actionBarHeight = 0;
        try {
            final TypedArray typedArray = context.getTheme().obtainStyledAttributes(new int[] {android.R.attr.actionBarSize});
            actionBarHeight = (int) typedArray.getDimension(0, 0);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return actionBarHeight;
    }

    private static int getFixedStatusBarHeight(Context context) {
        int result = 0;
        try {
            int resourceId = context.getResources().getIdentifier("status_bar_height", "dimen", "android");
            if (resourceId > 0) {
                result = context.getResources().getDimensionPixelSize(resourceId);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    public static int getParentHeight(View view) {
        int height = 0;
        int screen_height = getScreenHeight(view.getContext());
        int screen_top = getScreenTop(view.getContext());
        if (screen_top == 0) {
            screen_top = getFixedStatusBarHeight(view.getContext());
        }
        screen_height -= screen_top;
//        if (HeroHomeActivity.getAvailableScreenHeight() > 0) {
//           screen_height = Math.min(screen_height, HeroHomeActivity.getAvailableScreenHeight());
//        }
        if (view.getContext() instanceof AppCompatActivity && ((AppCompatActivity) view.getContext()).getSupportActionBar() != null) {
            int actionBarHeight = ((AppCompatActivity) view.getContext()).getSupportActionBar().getHeight();
            if (actionBarHeight == 0 && view.getContext() instanceof HeroActivity) {
                actionBarHeight = getFixedActionBarHeight(view.getContext());
            }
            screen_height -= actionBarHeight;
        } else if (((Activity) view.getContext()).getActionBar() != null) {
            int actionBarHeight = ((Activity) view.getContext()).getActionBar().getHeight();
            if (actionBarHeight == 0 && view.getContext() instanceof HeroActivity) {
                actionBarHeight = getFixedActionBarHeight(view.getContext());
            }
            screen_height -= actionBarHeight;
        } else {
            // if no actionbar, subtract toolbar height
            boolean isFullHeight = false;
            HeroFragment fragment = null;
            if (view.getContext() instanceof HeroFragmentActivity) {
                fragment = ((HeroFragmentActivity) view.getContext()).getParentFragment(view);//.getCurrentFragment();
                if (fragment != null) {
                    isFullHeight = fragment.isFullHeight();
                }
            }
            if (!isFullHeight && !isExtendToNavigationBar(view)) {
                int toolBarHeight;
                if (fragment != null) {
                    toolBarHeight = fragment.getToolBarHeight();
                } else {
                    toolBarHeight = getFixedActionBarHeight(view.getContext());
                }
                screen_height -= toolBarHeight;
            }
        }
        if (view.getContext() instanceof HeroHomeActivity) {
            screen_height -= view.getContext().getResources().getDimensionPixelSize(R.dimen.tab_bar_height);
        }
        if (view.getParent() != null) {
            if (((View) view.getParent()).getLayoutParams() != null) {
                height = ((View) view.getParent()).getLayoutParams().height;
            } else {
                height = ((View) view.getParent()).getHeight();
            }

            if (height <= 0) {
                height = ((View) view.getParent()).getHeight();
            }
        }

        if (height <= 0) {
            height = screen_height;
        }
        return Math.min(height, screen_height);
    }

    public static int getScreenWidth(Context context) {
        DisplayMetrics dm = context.getResources().getDisplayMetrics();
        return dm.widthPixels;
    }

    public static int getScreenHeight(Context context) {
        DisplayMetrics dm = context.getResources().getDisplayMetrics();
        return dm.heightPixels;
    }

    public static int getScreenTop(Context context) {
        Rect frame = new Rect();
        ((Activity) context).getWindow().getDecorView().getWindowVisibleDisplayFrame(frame);
        return frame.top;
    }

    public static float getAnimTime(JSONObject jsonObject) throws JSONException {
        String animation = jsonObject.getString("animation");
        float animTime = 0.0f;
        try {
            animTime = Float.parseFloat(animation);
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }
        return animTime;
    }

    public static IHero fromJson(Context context, JSONObject jsonObject) throws JSONException, ClassNotFoundException, NoSuchMethodException, InstantiationException, IllegalAccessException, InvocationTargetException {
        IHero view = null;
        if (jsonObject.has("res")) {
            int layoutId = getResIdIgnoreCase(context, jsonObject.getString("res"), "layout");
            if (layoutId != 0) {
                IHero v = (IHero) LayoutInflater.from(context).inflate(layoutId, null);
                return v;
            }
        }
        String type = jsonObject.getString("class");
        if (type != null) {
            String namespace = jsonObject.has("package") ? jsonObject.getString("package") : null;
            Class c;
            if (namespace != null) {
                c = Class.forName(namespace + "." + type);
            } else {
                try {
                    c = Class.forName("com.hero." + type);
                } catch (ClassNotFoundException e) {
                    c = Class.forName("mafia." + type);
                }
            }
            if (c == null) {
                c = Class.forName("com.hero.HeroView");
            }
            Constructor c1 = c.getDeclaredConstructor(Context.class);
            view = (IHero) c1.newInstance(context);
        }
        return view;
    }

    public static void on(final View view, JSONObject jsonObject) {
        final Context context = view.getContext();
        try {
            if (jsonObject.has("class")) {
                view.setTag(R.id.kHeroJson, jsonObject);
            }
            if (jsonObject.has("name")) {
                view.setTag(R.id.kName, jsonObject.get("name"));
            }
            if (jsonObject.has("parent")) {
                view.setTag(R.id.kParent, jsonObject.get("parent"));
            }
            if (jsonObject.has("backgroundColor")) {
                // IHeroBackground will handle background itself
                if (!(view instanceof IHeroBackground)) {
                    view.setBackgroundColor(parseColor(jsonObject.getString("backgroundColor")));
                }
            }
            if (jsonObject.has("hidden")) {
                if (jsonObject.getBoolean("hidden")) {
                    view.setVisibility(INVISIBLE);
                } else {
                    view.setVisibility(VISIBLE);
                }
            }
            if (jsonObject.has("android_hidden")) {
                view.setVisibility(INVISIBLE);
            }

            if (jsonObject.has("frame")) {
                JSONObject frame = jsonObject.getJSONObject("frame");
                String x = frame.has("x") ? frame.getString("x") : null;
                String y = frame.has("y") ? frame.getString("y") : null;
                String l = frame.has("l") ? frame.getString("l") : null;
                String t = frame.has("t") ? frame.getString("t") : null;
                String w = frame.has("w") ? frame.getString("w") : null;
                String h = frame.has("h") ? frame.getString("h") : null;
                String r = frame.has("r") ? frame.getString("r") : null;
                String b = frame.has("b") ? frame.getString("b") : null;
                final FrameLayout.LayoutParams p = new FrameLayout.LayoutParams(0, 0);
                int w_screen = HeroView.getParentWidth(view);
                int h_screen = HeroView.getParentHeight(view);
                if (x != null) {
                    p.leftMargin = x.endsWith("x") ? (int) (Float.parseFloat(x.substring(0, x.length() - 1)) * w_screen) : dip2px(view.getContext(), Float.parseFloat(x));
                }
                if (y != null) {
                    p.topMargin = y.endsWith("x") ? (int) (Float.parseFloat(y.substring(0, y.length() - 1)) * h_screen) : dip2px(view.getContext(), Float.parseFloat(y));
                }
                if (l != null) {
                    p.leftMargin = l.endsWith("x") ? (int) (Float.parseFloat(l.substring(0, l.length() - 1)) * w_screen) : dip2px(view.getContext(), Float.parseFloat(l));
                }
                if (t != null) {
                    p.topMargin = t.endsWith("x") ? (int) (Float.parseFloat(t.substring(0, t.length() - 1)) * h_screen) : dip2px(view.getContext(), Float.parseFloat(t));
                }
                if (w != null) {
                    p.width = w.endsWith("x") ? (int) (Float.parseFloat(w.substring(0, w.length() - 1)) * w_screen) : dip2px(view.getContext(), Float.parseFloat(w));
                }
                if (h != null) {
                    p.height = h.endsWith("x") ? (int) (Float.parseFloat(h.substring(0, h.length() - 1)) * h_screen) : dip2px(view.getContext(), Float.parseFloat(h));
                }
                if (r != null) {
                    if (l == null && x == null) {
                        p.leftMargin = w_screen - p.width - (r.endsWith("x") ? (int) (Float.parseFloat(r.substring(0, r.length() - 1)) * w_screen) : dip2px(view.getContext(), Float.parseFloat(r)));
                    } else {
                        p.width = w_screen - p.leftMargin - (r.endsWith("x") ? (int) (Float.parseFloat(r.substring(0, r.length() - 1)) * w_screen) : dip2px(view.getContext(), Float.parseFloat(r)));
                    }
                }
                if (b != null) {
                    if (t == null && y == null) {
                        p.topMargin = h_screen - p.height - (b.endsWith("x") ? (int) (Float.parseFloat(b.substring(0, b.length() - 1)) * h_screen) : dip2px(view.getContext(), Float.parseFloat(b)));
                    } else {
                        p.height = h_screen - p.topMargin - (b.endsWith("x") ? (int) (Float.parseFloat(b.substring(0, b.length() - 1)) * h_screen) : dip2px(view.getContext(), Float.parseFloat(b)));
                    }
                }

                if (jsonObject.has("center")) {
                    JSONObject center = jsonObject.getJSONObject("center");
                    x = center.has("x") ? center.getString("x") : null;
                    y = center.has("y") ? center.getString("y") : null;
                    //                     w_screen = HeroView.getParentWidth(view);
                    //                     h_screen = HeroView.getParentHeight(view);
                    //                     FrameLayout.LayoutParams p = new FrameLayout.LayoutParams(view.getLayoutParams());

                    if (x != null) {
                        p.leftMargin = (x.endsWith("x") ? (int) (Float.parseFloat(x.substring(0, x.length() - 1)) * w_screen) : dip2px(view.getContext(), Float.parseFloat(x))) - p.width / 2;
                    }
                    if (y != null) {
                        p.topMargin = (y.endsWith("x") ? (int) (Float.parseFloat(y.substring(0, y.length() - 1)) * h_screen) : dip2px(view.getContext(), Float.parseFloat(y))) - p.height / 2;
                    }
                    //                    view.setLayoutParams(p);
                }

                if (frame.length() > 0) {
                    // frame animation
                    JSONObject animParam = null;
                    if (jsonObject.has("animation") && !jsonObject.has("animationType")) {
                        FrameLayout.LayoutParams oldP = (LayoutParams) view.getLayoutParams();
                        animParam = new JSONObject();
                        if (oldP.width != 0 && oldP.height != 0 && (p.width != oldP.width || p.height != oldP.height)) {
                            animParam.put("scaleX", (float) p.width / oldP.width);
                            animParam.put("scaleY", (float) p.height / oldP.height);
                        }
                        if (p.leftMargin != oldP.leftMargin || p.topMargin != oldP.topMargin) {
                            animParam.put("toX", (p.leftMargin - oldP.leftMargin));
                            animParam.put("toY", (p.topMargin - oldP.topMargin));
                        }
                    }
                    if (animParam != null && animParam.length() > 0) {
                        float animTime = getAnimTime(jsonObject);
                        AnimationHelper.startAnimation(view, AnimationHelper.ANIMATION_FRAME, animTime, animParam, new AnimationHelper.AnimationEndListener() {
                            @Override
                            public void onAnimationEnd(Animation animation) {
                                view.clearAnimation();
                                view.setLayoutParams(p);
                            }
                        });
                    } else {
                        view.setLayoutParams(p);
                    }
                    JSONObject json = HeroView.getJson(view);
                    if (json != null && !frame.equals(json.optJSONObject("frame"))) {
                        json.put("frame", frame);
                    }
                }

                if (jsonObject.has("yOffset")) {
                    String yOffset = jsonObject.getString("yOffset");
                    String[] content = yOffset.split("\\+");
                    if (content.length > 1) {
                        String name = content[0];
                        float offset = Float.parseFloat(content[1]);
                        View top = null;
                        if (view.getParent() != null) {
                            top = (View) HeroView.findViewByName((View) view.getParent(), name);
                        }
                        if (top == null) {
                            HeroFragment fragment = ((HeroFragmentActivity) view.getContext()).getParentFragment(view);
                            if (fragment != null) {
                                top = (View) HeroView.findViewByName(fragment.getView(), name);
                            }
                        }
                        FrameLayout.LayoutParams viewLayout = (FrameLayout.LayoutParams) view.getLayoutParams();
                        if (top != null) {
                            FrameLayout.LayoutParams topLayout = (FrameLayout.LayoutParams) top.getLayoutParams();
                            viewLayout.topMargin = topLayout.topMargin + topLayout.height + dip2px(view.getContext(), offset);
                            view.setLayoutParams(viewLayout);
                            List<View> topLayoutListeners = HeroView.getLayoutListener(top, true);
                            if (topLayoutListeners != null) {
                                if (!topLayoutListeners.contains(view)) {
                                    topLayoutListeners.add(view);
                                }
                            }
                        } else {
                            viewLayout.topMargin = dip2px(view.getContext(), offset);
                        }
                        if (HeroView.getJson(view) != null) {
                            JSONObject originalJson = (JSONObject) HeroView.getJson(view);
                            JSONObject newFrame = new JSONObject();
                            newFrame.put("x", px2dip(context, viewLayout.leftMargin));
                            newFrame.put("y", px2dip(context, viewLayout.topMargin));
                            newFrame.put("w", px2dip(context, viewLayout.width));
                            newFrame.put("h", px2dip(context, viewLayout.height));
                            if (viewLayout.height == 1 && px2dip(context, viewLayout.height) == 0) {
                                newFrame.put("h", 0.5);
                            }
                            originalJson.put("frame", newFrame);
                            originalJson.put("yOffset", yOffset);

                            if (jsonObject.has("center") && jsonObject.optJSONObject("center") != null) {
                                originalJson.put("center", jsonObject.getJSONObject("center"));
                            }
                        }
                    }
                }

                if (jsonObject.has("xOffset")) {
                    String xOffset = jsonObject.getString("xOffset");
                    String[] content = xOffset.split("\\+");
                    if (content.length > 1) {
                        String name = content[0];
                        float offset = Float.parseFloat(content[1]);
                        // find the left view
                        View leftView = null;
                        if (view.getParent() != null) {
                            leftView = (View) HeroView.findViewByName((View) view.getParent(), name);
                        }
                        if (leftView == null) {
                            HeroFragment fragment = ((HeroFragmentActivity) view.getContext()).getParentFragment(view);
                            if (fragment != null) {
                                leftView = (View) HeroView.findViewByName(fragment.getView(), name);
                            }
                        }
                        FrameLayout.LayoutParams viewLayout = (FrameLayout.LayoutParams) view.getLayoutParams();
                        if (leftView != null) {
                            // update layout parameters according to the left view
                            FrameLayout.LayoutParams leftLayout = (FrameLayout.LayoutParams) leftView.getLayoutParams();
                            viewLayout.leftMargin = leftLayout.leftMargin + leftLayout.width + dip2px(view.getContext(), offset);
                            view.setLayoutParams(viewLayout);
                            // add the view to the listener list of left view
                            List<View> leftLayoutListeners = HeroView.getLayoutListener(leftView, true);
                            if (leftLayoutListeners != null) {
                                if (!leftLayoutListeners.contains(view)) {
                                    leftLayoutListeners.add(view);
                                }
                            }
                        } else {
                            viewLayout.leftMargin = dip2px(view.getContext(), offset);
                        }

                        // update frame info of this view
                        if (HeroView.getJson(view) != null) {
                            JSONObject originalJson = (JSONObject) HeroView.getJson(view);
                            JSONObject newFrame = new JSONObject();
                            newFrame.put("x", px2dip(context, viewLayout.leftMargin));
                            newFrame.put("y", px2dip(context, viewLayout.topMargin));
                            newFrame.put("w", px2dip(context, viewLayout.width));
                            newFrame.put("h", px2dip(context, viewLayout.height));
                            if (viewLayout.width == 1 && px2dip(context, viewLayout.width) == 0) {
                                newFrame.put("w", 0.5);
                            }
                            originalJson.put("frame", newFrame);
                            originalJson.put("xOffset", xOffset);

                            if (jsonObject.has("center") && jsonObject.optJSONObject("center") != null) {
                                originalJson.put("center", jsonObject.getJSONObject("center"));
                            }
                        }
                    }
                }
                // update the listeners of this view after its frame changed
                List<View> layoutListeners = HeroView.getLayoutListener(view, false);
                if (layoutListeners != null && layoutListeners.size() > 0) {
                    for (View layoutListener : layoutListeners) {
                        if (layoutListener != null) {
                            JSONObject layoutEvent = new JSONObject();
                            layoutEvent.put("frame", HeroView.getJson(layoutListener).get("frame"));
                            if (HeroView.getJson(layoutListener).has("yOffset")) {
                                layoutEvent.put("yOffset", HeroView.getJson(layoutListener).getString("yOffset"));
                            }
                            if (HeroView.getJson(layoutListener).has("xOffset")) {
                                layoutEvent.put("xOffset", HeroView.getJson(layoutListener).getString("xOffset"));
                            }

                            if (HeroView.getJson(layoutListener).has("center") && HeroView.getJson(layoutListener).optJSONObject("center") != null) {
                                layoutEvent.put("center", HeroView.getJson(layoutListener).getJSONObject("center"));
                            }

                            ((IHero) layoutListener).on(layoutEvent);
                        }
                    }
                }

                // parent view frame also changes
                if (HeroView.getJson(view) != null && (HeroView.getJson(view).has("contentSizeElement") || HeroView.getJson(view).has("contentSizeElementY"))) {
                    try {
                        final boolean isContentSizeElement = HeroView.getJson(view).has("contentSizeElement") ? HeroView.getJson(view).getBoolean("contentSizeElement") : false;
                        final boolean isContentSizeElementY = HeroView.getJson(view).has("contentSizeElementY") ? HeroView.getJson(view).getBoolean("contentSizeElementY") : false;
                        if ((isContentSizeElement || isContentSizeElementY) && view.getVisibility() == VISIBLE) {
                            final FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) view.getLayoutParams();
                            final int right = params.leftMargin + params.width;
                            final int bottom = params.topMargin + params.height;
                            final View parent = (View) view.getParent();
                            if (parent != null && parent instanceof UIView) {
                                new Handler().post(new Runnable() {
                                    @Override
                                    public void run() {
                                        FrameLayout.LayoutParams parentParam = (FrameLayout.LayoutParams) parent.getLayoutParams();
                                        try {
                                            JSONObject parentJson = HeroView.getJson(parent);
                                            if (parentJson != null && parentJson.has("frame")) {
                                                JSONObject parentFrame = parentJson.getJSONObject("frame");
                                                if (isContentSizeElementY) {
                                                    //                                                    parentFrame.put("w", px2dip(context, parentParam.width));
                                                } else {
                                                    parentFrame.put("w", px2dip(context, right));
                                                }
                                                parentFrame.put("h", px2dip(context, bottom));
                                                JSONObject object = new JSONObject();
                                                object.put("frame", parentFrame);

                                                if (parentJson.has("center") && parentJson.optJSONObject("center") != null) {
                                                    object.put("center", parentJson.getJSONObject("center"));

                                                }
                                                ((IHero) parent).on(object);
                                            }
                                        } catch (JSONException e) {
                                            e.printStackTrace();
                                        }
                                    }
                                });

                            }
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }

            if (jsonObject.has("alpha")) {
                view.setAlpha((float) jsonObject.getDouble("alpha"));
            }
            if (jsonObject.has("enable")) {
                Object enabled = jsonObject.get("enable");
                if (enabled instanceof Integer) {
                    int enable = (int) enabled;
                    if (enable == 1) {
                        view.setEnabled(true);
                    } else {
                        view.setEnabled(false);
                    }
                } else {
                    view.setEnabled(jsonObject.getBoolean("enable"));
                }
            }
            if (jsonObject.has("cornerRadius") || jsonObject.has("borderWidth") || jsonObject.has("borderColor") || jsonObject.has("androidCornerRadii")) {
                // IHeroBackground will handle background itself
                if (!(view instanceof IHeroBackground)) {
                    int color, borderColor = Color.TRANSPARENT, borderWidth = 0;
                    float cornerRadius = 0;
                    float[] cornerRadii = null;
                    if (jsonObject.has("backgroundColor")) {
                        color = HeroView.parseColor("#" + jsonObject.getString("backgroundColor"));
                    } else {
                        color = Color.TRANSPARENT;
                    }
                    if (jsonObject.has("cornerRadius")) {
                        cornerRadius = dip2px(view.getContext(), (float) jsonObject.getDouble("cornerRadius"));
                    }
                    if (jsonObject.has("androidCornerRadii")) {
                        JSONArray array = jsonObject.optJSONArray("androidCornerRadii");
                        cornerRadii = generateCornerRadiiArray(view.getContext(), array);
                    }
                    if (jsonObject.has("borderWidth")) {
                        borderWidth = dip2px(view.getContext(), jsonObject.getInt("borderWidth"));
                    }
                    if (jsonObject.has("borderColor")) {
                        borderColor = HeroView.parseColor("#" + jsonObject.getString("borderColor"));
                    }
                    if (borderColor != Color.TRANSPARENT && borderWidth == 0) {
                        borderWidth = 1;
                    }
                    GradientDrawable drawable;
                    if (cornerRadii != null) {
                        drawable = createBackgroundDrawable(color, borderWidth, borderColor, cornerRadii);
                    } else {
                        drawable = createBackgroundDrawable(color, borderWidth, borderColor, cornerRadius);
                    }

                    if (drawable != null) {
                        if (android.os.Build.VERSION.SDK_INT > 15) {
                            view.setBackground(drawable);
                        } else {
                            view.setBackgroundDrawable(drawable);
                        }
                    }
                }
            }
            if (jsonObject.has("dashBorder")) {
                JSONObject dashBorder = jsonObject.getJSONObject("dashBorder");
                boolean left, right, top, bottom;
                left = dashBorder.optBoolean("left");
                right = dashBorder.optBoolean("right");
                top = dashBorder.optBoolean("top");
                bottom = dashBorder.optBoolean("bottom");
                int dashWidth = context.getResources().getDimensionPixelSize(R.dimen.dash_default_width);
                int dashGap = context.getResources().getDimensionPixelSize(R.dimen.dash_default_gap);
                int hideOffset = -2;
                if (left && right && bottom && top) {
                    view.setBackgroundResource(R.drawable.selector_dash_rect);
                    GradientDrawable drawable = (GradientDrawable) view.getBackground();
                    if (dashBorder.has("color")) {
                        drawable.setStroke(1, parseColor(dashBorder.optString("color")), dashWidth, dashGap);
                    }
                    if (jsonObject.has("backgroundColor")) {
                        drawable.setColor(parseColor(jsonObject.getString("backgroundColor")));
                    }
                } else {
                    view.setBackgroundResource(R.drawable.selector_dash_line);
                    LayerDrawable drawable = (LayerDrawable) view.getBackground();
                    drawable.setLayerInset(0, (left ? 0 : hideOffset), (top ? 0 : hideOffset), (right ? 0 : hideOffset), (bottom ? 0 : hideOffset));
                    Drawable layer0 = drawable.getDrawable(0);
                    if (layer0 instanceof GradientDrawable) {
                        if (dashBorder.has("pattern")) {
                            JSONArray pattern = dashBorder.getJSONArray("pattern");
                            dashWidth = dip2px(context, pattern.getInt(0));
                            dashGap = dip2px(context, pattern.getInt(1));
                        }
                        if (dashBorder.has("color")) {
                            ((GradientDrawable) layer0).setStroke(1, parseColor(dashBorder.optString("color")), dashWidth, dashGap);
                        }
                        if (jsonObject.has("backgroundColor")) {
                            ((GradientDrawable) layer0).setColor(parseColor(jsonObject.getString("backgroundColor")));
                        }
                    }
                }
            }
            if (jsonObject.has("autolayout")) {
                Log.w("not implement", "autolayout");
            }
            if (!(view instanceof IHeroBackground)) {
                // IHeroBackground will handle the attributes after its background created
                if (jsonObject.has("raised")) {
                    HeroView.setRaised(view, -1);
                }
                if (jsonObject.has("ripple")) {
                    HeroView.setRipple(view);
                }
            }
            if (jsonObject.has("subViews")) {
                JSONArray views = jsonObject.getJSONArray("subViews");
                // subview has not flip animation
                if (!(jsonObject.has("animation") && AnimationHelper.ANIMATION_FLIP.equals(jsonObject.optString("animationType")))) {
                    if (view instanceof ViewGroup) {
                        ((ViewGroup) view).removeAllViews();
                    }
                    createSubViews(view, views);
                }
            }
            if (jsonObject.has("animation")) {
                if (jsonObject.has("animationType")) {
                    String animType = jsonObject.getString("animationType");
                    float animTime = getAnimTime(jsonObject);
                    JSONObject animParam = null;
                    if (jsonObject.has("subViews")) {
                        animParam = jsonObject;
                    }
                    AnimationHelper.startAnimation(view, animType, animTime, animParam, null);
                }
            }
            if (view instanceof ViewGroup) {
                ViewGroup viewgroup = (ViewGroup) view;
                if (jsonObject.has("tinyColor")) {
                    Log.w("not implement", "tinyColor");
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();

        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

    }

    public static void createSubViews(View rootView, JSONArray viewData) throws NoSuchMethodException, InstantiationException, JSONException, IllegalAccessException, InvocationTargetException, ClassNotFoundException {
        if (viewData != null && rootView instanceof ViewGroup) {
            for (int i = 0; i < viewData.length(); i++) {
                JSONObject viewsJSONObject = viewData.getJSONObject(i);
                IHero v = HeroView.fromJson(rootView.getContext(), viewsJSONObject);
                ((ViewGroup) rootView).addView((View) v);
                String fragmentTag = getFragmentTag((rootView));
                if (fragmentTag != null) {
                    HeroView.setFragmentTag((View)v, fragmentTag);
                }
                if (HeroView.isExtendToNavigationBar(rootView)) {
                    HeroView.setExtendToNavigationBar((View) v);
                }
                v.on(viewsJSONObject);
            }
        }
    }

    public static StateListDrawable createSelector(int colorNormal, int colorPressed) {
        StateListDrawable bg = new StateListDrawable();
        Drawable normal = new ColorDrawable(colorNormal);
        Drawable pressed = new ColorDrawable(colorPressed);
        bg.addState(new int[] {android.R.attr.state_pressed, android.R.attr.state_enabled}, pressed);
        // View.ENABLED_STATE_SET
        bg.addState(new int[] {android.R.attr.state_enabled}, normal);
        bg.addState(new int[] {}, normal);
        return bg;
    }

    public static GradientDrawable createBackgroundDrawable(int color, int borderW, int borderColor, float cornerRadius) {
        GradientDrawable drawable = new GradientDrawable();
        drawable.setColor(color);

        if (borderW != 0 && borderColor != Color.TRANSPARENT) {
            drawable.setStroke(borderW, borderColor);
        }
        if (cornerRadius != 0) {
            drawable.setCornerRadius(cornerRadius);
        }
        return drawable;
    }

    private static ColorStateList createColorStateList(int normal, int pressed) {
        int[] colors = new int[] { pressed, normal};
        int[][] states = new int[2][];
        states[0] = new int[] { android.R.attr.state_pressed };
        states[1] = new int[] {};
        ColorStateList colorList = new ColorStateList(states, colors);
        return colorList;
    }

    public static Drawable createRippleDrawable(ColorStateList colors, View view) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            RippleDrawable rippleDrawable;
            Drawable background = view.getBackground();
            if (background != null && background instanceof RippleDrawable) {
                // avoid adding ripple background many times
                return background;
            }
            rippleDrawable = new RippleDrawable(colors, view.getBackground(), null);
            return rippleDrawable;
        }
        return null;
    }

    public static void setRipple(View view) {
        Resources res = view.getResources();
        int normalColor = res.getColor(R.color.defaultRippleColor);
        int pressedColor = res.getColor(R.color.defaultRipplePressedColor);

        Drawable rippleDrawable = createRippleDrawable(createColorStateList(normalColor, pressedColor), view);
        if (rippleDrawable != null) {
            view.setBackground(rippleDrawable);
        }
    }

    public static void setRaised(View view, float size) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            if (size == -1) {
                size = view.getContext().getResources().getDimension(R.dimen.button_elevation_size);
            }
            view.setElevation(size);
        }
    }

    public static GradientDrawable createBackgroundDrawable(int color, int borderW, int borderColor, float[] cornerRadiusArray) {
        GradientDrawable drawable = new GradientDrawable();
        drawable.setColor(color);

        if (borderW != 0 && borderColor != Color.TRANSPARENT) {
            drawable.setStroke(borderW, borderColor);
        }
        if (cornerRadiusArray != null && cornerRadiusArray.length >= 8) {
            drawable.setCornerRadii(cornerRadiusArray);
        }
        return drawable;
    }

    public static float[] generateCornerRadiiArray(Context context, JSONArray array) {
        float[] values = null;
        if (array != null) {
            values = new float[array.length() * 2];
            for (int i = 0; i < array.length(); i++) {
                try {
                    values[i * 2] = HeroView.dip2px(context, (float) array.getDouble(i));
                    values[i * 2 + 1] = values[i * 2];
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
        return values;
    }

    public static int getPressedColor(int color, boolean ignoreTransparent) {
        int a, r, g, b;

        a = (color & 0xff000000) >>> 24;
        if (a == 0 && ignoreTransparent) {
            return color;
        }
        // special case for a white background button
        if (color == Color.WHITE) {
            return color;
        }

        if (a == 0) a = 30;
        r = (color & 0xff0000) >>> 16;
        g = (color & 0xff00) >>> 8;
        b = color & 0xff;
        r = changeSingleColor(r);
        g = changeSingleColor(g);
        b = changeSingleColor(b);
        return Color.argb(a, r, g, b);
    }

    public static int changeSingleColor(int color) {
        int color1 = color;
        int threshold = 96;
        int step = color >>> 3;
        color1 = (color1 < threshold) ? color1 + step : color1 - step;
        return Math.max(0, Math.min(color1, 255));
    }

    public static int getResIdByName(Context context, String name, String type) {
        String packageName = context.getApplicationInfo().packageName;
        if (name.lastIndexOf(".") > 0) {
            name = name.substring(0, name.lastIndexOf("."));
        }
        String lowerCaseName = name.toLowerCase();
        return context.getResources().getIdentifier(lowerCaseName, type, packageName);
    }

    public static int getResId(Context c, String name, String defType) {
        String packageName = c.getApplicationInfo().packageName;
        return c.getResources().getIdentifier(name, defType, packageName);
    }

    public static int getResIdIgnoreCase(Context context, String name, String defType) {
        String packageName = context.getApplicationInfo().packageName;
        String lowerCaseName = name.toLowerCase();
        return context.getResources().getIdentifier(lowerCaseName, defType, packageName);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
    }

    public void on(JSONObject jsonObject) {
        HeroView.on(this, jsonObject);
    }

    public static void putValueToJson(JSONObject jsonObject, Object value) throws JSONException {
        jsonObject.put("value", value);
    }

    public static int parseColor(String colorString) {
        colorString = colorString.replaceAll("#", "");
        int color = Color.RED;
        try {
            if (colorString.length() == 8) {
                String newString = colorString.substring(0, 6);
                color = Color.parseColor("#" + colorString.substring(6, 8) + newString);
            } else {
                color = Color.parseColor("#" + colorString);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return color;
    }

    public static void sendActionToContext(Context context, JSONObject jsonObject) {
        try {
            ((IHeroContext) context).on(jsonObject);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static boolean isChildOfTableView(View view) {
        int maxLevel = 6;
        ViewParent parent = view.getParent();
        while (parent != null) {
            if (maxLevel <= 0) break;
            if (parent instanceof HeroTableView) {
                return true;
            }
            parent = parent.getParent();
            maxLevel --;
        }
        return false;
    }

}
