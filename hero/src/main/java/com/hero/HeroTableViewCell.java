package com.hero;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AbsListView;
import android.widget.Checkable;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.hero.depandency.ImageLoadUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.InvocationTargetException;

/**
 * Created by liuguoping on 15/9/24.
 */
public class HeroTableViewCell extends FrameLayout implements IHero, Checkable {
//    public static final int DEFAULT_CELL_HEIGHT = 44;

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
//            Log.i("HeroTableViewCell", "HeroTableViewCell init " + json);
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
//                Log.i("HeroTableViewCell", "HeroTableViewCell init from class " + json);
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

    private static int getLayoutType(JSONObject json) {
        if (json.has("sectionTitle")) {
            return R.layout.hero_list_section_title;
        } else if (json.has("sectionFootTitle")) {
            return R.layout.hero_list_section_footer;
        } else if (json.has("emptySeparator")) {
            return R.layout.hero_list_section_separator;
        } else if (json.has("AccessoryType")) {
            try {
                String type = json.getString("AccessoryType");
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
                findViewById(R.id.arrow).setVisibility(GONE);
            }
        } else {
            TextView textView = (TextView) findViewById(R.id.textView2);
            if (textView != null) {
                textView.setText("");
//                findViewById(R.id.arrow).setVisibility(VISIBLE);
            }
        }
        if (jsonObject.has("image")) {
            ImageView imageView = (ImageView) findViewById(R.id.imageView);
            if (imageView != null) {
                imageView.setVisibility(VISIBLE);
                ImageLoadUtils.LoadImage(imageView, jsonObject.getString("image"));
            }
        } else {
            ImageView imageView = (ImageView) findViewById(R.id.imageView);
            if (imageView != null) {
                imageView.setVisibility(GONE);
            }
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
