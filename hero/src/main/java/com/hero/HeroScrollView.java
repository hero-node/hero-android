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
        contentView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                View focusedView = v.findFocus();
                if (focusedView != null) {
                    focusedView.clearFocus();
                }
            }
        });
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
            String x = jsonObject.getJSONObject("contentSize").optString("x");
            String y = jsonObject.getJSONObject("contentSize").optString("y");
            Log.w("not implements", "contentSize");
        }
        if (jsonObject.has("contentOffset")) {
            final int x = jsonObject.getJSONObject("contentOffset").optInt("x");
            final int y = jsonObject.getJSONObject("contentOffset").optInt("y");
            postDelayed(new Runnable() {
                @Override
                public void run() {
                    scrollView.smoothScrollTo(x, y);
                }
            }, 100);
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

    @Override
    public void removeAllViews() {
        if (contentView != null) {
            contentView.removeAllViews();
        } else {
            super.removeAllViews();
        }
    }

    @Override
    public void removeView(View view) {
        if (contentView != null) {
            contentView.removeView(view);
        } else {
            super.removeView(view);
        }
    }
}
