package com.hero.signature.entity;

import android.support.v4.view.PagerAdapter;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

/**
 * Created by Aron on 2018/5/9.
 */
public class GridViewPageAdapter extends PagerAdapter {

    private List<View> views;

    public GridViewPageAdapter(List<View> views){
        this.views = views;
    }

    public void setViews(List<View> views){
        this.views = views;
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return views == null ? 0 : views.size();
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView((View) object);
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        container.addView(views.get(position), 0);
        return views.get(position);
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view.equals(object);
    }


}
