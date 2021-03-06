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

import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.Checkable;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.TextView;

import com.hero.depandency.ImageLoadUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.InvocationTargetException;

/**
 * Created by liuguoping on 15/9/24.
 */
public class HeroTableViewCell extends FrameLayout implements IHero, Checkable {

    public HeroTableViewCell(Context context) {
        super(context);
    }

    public static View getView(Context c, View convertView, JSONObject json) {
        ViewHolder holder;

        if (convertView == null) {
            holder = new ViewHolder();
            convertView = (View) initViewCell(c, json, holder);
            if (convertView != null) {
                convertView.setTag(R.id.kViewContent, holder);
            }
        } else {
            holder = (ViewHolder) convertView.getTag(R.id.kViewContent);
            if (holder == null) {
//                holder = new ViewHolder();
            } else if (holder.clazz != null) {
                if (convertView instanceof HeroTableViewCell) {
                    int layoutType = getLayoutType(json);
                    if (holder.layoutId == layoutType) {
                        AbsListView.LayoutParams p;
                        try {
                            p = new AbsListView.LayoutParams(HeroView.getScreenWidth(c), json.has("height") ? HeroView.dip2px(c, json.getInt("height")) : c.getResources().getDimensionPixelSize(R.dimen.list_default_height));
                            if (hasSelfLayoutHeight(layoutType)) {
                                p.height = AbsListView.LayoutParams.WRAP_CONTENT;
                            }
                            convertView.setLayoutParams(p);
                            ((HeroTableViewCell) convertView).on(json);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        return convertView;
                    }
                } else {
                    try {
                        if (json.has("class") && holder.clazz.getSimpleName().equals(json.get("class"))) {
                            if (convertView instanceof IHero) {
                                ((IHero) convertView).on(json);
                                return convertView;
                            }
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
            holder = new ViewHolder();
            convertView = (View) initViewCell(c, json, holder);
            if (convertView != null) {
                convertView.setTag(R.id.kViewContent, holder);
            }
        }
        return convertView;
    }

    private static View initViewCell(Context context, JSONObject json, ViewHolder holder) {
        HeroTableViewCell cell = null;
        try {
            AbsListView.LayoutParams p;
            p = new AbsListView.LayoutParams(HeroView.getScreenWidth(context), json.has("height") ? HeroView.dip2px(context, json.getInt("height")) : context.getResources().getDimensionPixelSize(R.dimen.list_default_height));
            if (json.has("sectionView")) {
                JSONObject titleView = json.getJSONObject("sectionView");
                IHero tView = HeroView.fromJson(context, titleView);
                if (tView != null) {
                    holder.clazz = tView.getClass();
                    holder.layoutId = 0;
                    tView.on(titleView);
                    FrameLayout layout = new FrameLayout(context);
                    layout.addView((View) tView);
                    layout.setLayoutParams(p);
                    return layout;
                }
                return null;
            } else if (json.has("class")) {
                try {
                    IHero v = HeroView.fromJson(context, json);
                    holder.clazz = v.getClass();
                    holder.layoutId = 0;
                    v.on(json);
                    if (v instanceof IHeroCell) {
                        if (((IHeroCell) v).hasSelfHeight()) {
                            p.height = LayoutParams.WRAP_CONTENT;
                        }
                    }
                    if (json.has("frame")) {
                        FrameLayout layout = new FrameLayout(context);
                        layout.addView((View) v);
                        // if the cell has no specific height but has a frame, the outer layout should WRAP_CONTENT too
                        if (!json.has("height")) {
                            p.height = LayoutParams.WRAP_CONTENT;
                        }
                        layout.setLayoutParams(p);
                        return layout;
                    } else {
                        ((View) v).setLayoutParams(p);
                        return (View) v;
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                    cell = new HeroTableViewCell(context);
                }
            } else if (json.has("res")) {
                int layoutId = getResId(context, json.getString("res"));
                if (layoutId != 0) {
                    IHero v = (IHero) LayoutInflater.from(context).inflate(layoutId, null);
                    holder.clazz = v.getClass();
                    holder.layoutId = layoutId;
                    ((View) v).setLayoutParams(p);
                    v.on(json);
                    return (View)v;
                } else {
                    return new HeroTableViewCell(context);
                }
            } else {
                cell = new HeroTableViewCell(context);
                int layoutType = getLayoutType(json);
                LayoutInflater.from(context).inflate(layoutType, cell);
                holder.clazz = cell.getClass();
                holder.layoutId = layoutType;
                if (hasSelfLayoutHeight(layoutType)) {
                    p.height = AbsListView.LayoutParams.WRAP_CONTENT;
                }
                cell.setLayoutParams(p);
                cell.on(json);
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
        return cell;
    }

    public static int getLayoutType(JSONObject json) {
        if (json.has("sectionTitle")) {
            return R.layout.hero_list_section_title;
        } else if (json.has("sectionFootTitle")) {
            return R.layout.hero_list_section_footer;
        } else if (json.has("emptySeparator")) {
            return R.layout.hero_list_section_separator;
        }
        else if (json.has("AccessoryType")) {
            // Deprecated
            try {
                String type = json.getString("AccessoryType");
                return R.layout.hero_listcell;
            } catch (JSONException e) {
                e.printStackTrace();
            }
        } else if (json.has("accessoryType")) {
            try {
                String type = json.getString("accessoryType");
                return R.layout.hero_listcell;
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return R.layout.hero_listcell;
    }

    public static int getResId(Context context, String name) {
        String packageName = context.getApplicationInfo().packageName;
        String lowerCaseName = name.toLowerCase();
        return context.getResources().getIdentifier(lowerCaseName, "layout", packageName);
    }

    @Override
    public boolean isChecked() {
        return false;
    }

    @Override
    public void setChecked(boolean b) {

    }

    @Override
    public void toggle() {

    }

    @Override
    public void on(JSONObject jsonObject) throws JSONException {
        HeroView.on(this, jsonObject);
        if (jsonObject.has("sectionTitle")) {
            TextView textView = (TextView) findViewById(R.id.sectionTitle);
            if (textView != null) {
                textView.setText(jsonObject.getString("sectionTitle"));
            }
        } else if (jsonObject.has("sectionFootTitle")) {
            TextView textView = (TextView) findViewById(R.id.sectionTitle);
            if (textView != null) {
                textView.setText(jsonObject.getString("sectionFootTitle"));
            }
        } else {
            TextView textView = (TextView) findViewById(R.id.sectionTitle);
            if (textView != null) {
                textView.setText("");
            }
        }
        if (jsonObject.has("title")) {
            TextView textView = (TextView) findViewById(R.id.textView);
            if (textView != null) {
                textView.setText(jsonObject.getString("title"));
            }
        } else {
            TextView textView = (TextView) findViewById(R.id.textView);
            if (textView != null) {
                textView.setText("");
            }
        }
        if (jsonObject.has("textValue")) {
            TextView textView = (TextView) findViewById(R.id.textView2);
            if (textView != null) {
                textView.setText(jsonObject.getString("textValue"));
            }
        } else {
            TextView textView = (TextView) findViewById(R.id.textView2);
            if (textView != null) {
                textView.setText("");
            }
        }

        if (jsonObject.has("accessoryType")) {
            ImageView textView = (ImageView) findViewById(R.id.arrow);
            if (textView != null && jsonObject.getString("accessoryType").equals("DisclosureIndicator")) {
                findViewById(R.id.arrow).setVisibility(View.VISIBLE);
            }
        } else {
            ImageView textView = (ImageView) findViewById(R.id.arrow);
            if (textView != null) {
                findViewById(R.id.arrow).setVisibility(View.GONE);
            }
        }
        
        if (jsonObject.has("detail")) {
            TextView textView = (TextView) findViewById(R.id.textViewDetail);
            if (textView != null) {
                textView.setVisibility(View.VISIBLE);
                textView.setText(jsonObject.getString("detail"));
            }
        } else {
            TextView textView = (TextView) findViewById(R.id.textViewDetail);
            if (textView != null) {
                textView.setVisibility(View.GONE);
                textView.setText("");
            }
        }

        if (jsonObject.has("boolValue")) {
            Switch switchButton = (Switch) findViewById(R.id.switchButton);
            if (switchButton != null) {
                switchButton.setVisibility(View.VISIBLE);
                switchButton.setChecked(jsonObject.getBoolean("boolValue"));
                findViewById(R.id.arrow).setVisibility(View.GONE);
            }
        } else {
            Switch switchButton = (Switch) findViewById(R.id.switchButton);
            if (switchButton != null) {
                switchButton.setVisibility(View.GONE);
                switchButton.setChecked(false);
            }
        }
        if (jsonObject.has("image")) {
            ImageView imageView = (ImageView) findViewById(R.id.imageView);
            if (imageView != null) {
                int imageSize = jsonObject.has("height") ? HeroView.dip2px(getContext(), jsonObject.getInt("height")) : getContext().getResources().getDimensionPixelSize(R.dimen.list_default_height);
                imageSize = imageSize*2/3;
                RelativeLayout.LayoutParams p;
                p = new RelativeLayout.LayoutParams(imageSize, imageSize);
                p.leftMargin = imageSize/4;
                p.topMargin = imageSize/4;
                imageView.setLayoutParams(p);
                imageView.setVisibility(VISIBLE);
                ImageLoadUtils.LoadImage(imageView, jsonObject.getString("image"));
            }
        } else {
            ImageView imageView = (ImageView) findViewById(R.id.imageView);
            if (imageView != null) {
                imageView.setVisibility(GONE);
            }
        }
        if (jsonObject.has("needPadding")) {
            LinearLayout linearLayout = (LinearLayout) findViewById(R.id.separatorLine);
            if (linearLayout != null) {
                FrameLayout.LayoutParams p;
                p = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,1);
                p.gravity = Gravity.BOTTOM;
                p.setMargins(80,0,0,0);
                linearLayout.setLayoutParams(p);
                linearLayout.setVisibility(VISIBLE);
            }
        } else {
            // TODO:Aron
//            LinearLayout linearLayout = (LinearLayout) findViewById(R.id.separatorLine);
//            linearLayout.setVisibility(VISIBLE);
        }
    }

    public static boolean hasSelfLayoutHeight(int layoutType) {
        int types[] = {R.layout.hero_list_section_footer,
                R.layout.hero_list_section_title,
                R.layout.hero_list_section_separator};

        for (int type : types) {
            if (layoutType == type) {
                return true;
            }
        }
        return false;
    }

    static class ViewHolder {
        int layoutId;
        Class clazz;
    }
}
