package com.hero;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ScrollView;

import com.hero.depandency.XRefreshLayout;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by liuguoping on 15/9/23.
 */
public class HeroScrollView extends XRefreshLayout implements IHero {
    private FrameLayout contentView;
    private ScrollView scrollView;
    protected JSONArray pullActions;

    public HeroScrollView(Context context) {
        super(context);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public HeroScrollView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr);
    }

    public HeroScrollView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public HeroScrollView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void init(Context context) {
        super.init(context);
        LayoutInflater.from(getContext()).inflate(R.layout.inner_scrollview, this, true);
        scrollView = (ScrollView) findViewById(R.id.scrollView);
        contentView = (FrameLayout) this.findViewById(R.id.scrollInnerLayout);
        scrollView.setScrollBarStyle(SCROLLBARS_INSIDE_OVERLAY);
        setScrollView(scrollView);
    }

    @Override
    public void addView(View child) {
        if (contentView != null) {
            contentView.addView(child);
        } else {
            super.addView(child);
        }
    }


    @Override
    public void on(JSONObject jsonObject) throws JSONException {
        HeroView.on(this, jsonObject);
        if (jsonObject.has("contentSize")) {
            String x = jsonObject.getJSONObject("contentSize").getString("x");
            String y = jsonObject.getJSONObject("contentSize").getString("y");
            Log.w("not implements", "contentSize");
        }
        if (jsonObject.has("contentOffset")) {
            final String x = jsonObject.getJSONObject("contentOffset").getString("x");
            final String y = jsonObject.getJSONObject("contentOffset").getString("y");
            postDelayed(new Runnable() {
                @Override
                public void run() {
                    scrollView.scrollTo(Integer.parseInt(x), Integer.parseInt(y));
                }
            }, 1000);
        }
        if (jsonObject.has("pullRefresh")) {
            this.setPullRefreshEnable(true);
            JSONObject pullObj = (JSONObject) jsonObject.get("pullRefresh");
            if (pullObj.has("action")) {
                pullActions = pullObj.getJSONArray("action");
                this.setOnRefreshListener(new OnRefreshListener() {
                    @Override
                    public void onRefresh(XRefreshLayout pullToRefreshLayout) {
                        if (pullActions != null && pullActions.length() > 0) {
                            for (int i = 0; i < pullActions.length(); i++) {
                                JSONObject action = null;
                                try {
                                    action = (JSONObject) (pullActions.get(i));
                                    if (action != null) {
                                        ((IHeroContext) getContext()).on(action);
                                    }
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    }
                });
            }
            if (pullObj.has("idle")) {
                getHeader().setHintString(pullObj.getString("idle"), "idle");
            }
            if (pullObj.has("pulling")) {
                getHeader().setHintString(pullObj.getString("pulling"), "pulling");
            }
            if (pullObj.has("refreshing")) {
                getHeader().setHintString(pullObj.getString("refreshing"), "refreshing");
            }
        }
        if (jsonObject.has("method")) {
            if ("stop".equals(jsonObject.getString("method")) || "closeRefresh".equals(jsonObject.getString("method"))) {
                // close refresh
                stopRefresh();
            }
        }

    }
}
