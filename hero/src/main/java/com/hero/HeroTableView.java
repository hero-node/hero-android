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

import android.content.Context;
import android.database.DataSetObserver;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.SectionIndexer;

import com.hero.depandency.IndexBar;
import com.hero.depandency.StringUtil;
import com.hero.depandency.XListView;
import com.hero.depandency.XListViewFooter;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

/**
 * Created by liuguoping on 15/9/23.
 */
public class HeroTableView extends XListView implements IHero {
    CustomerListAdapter adapter;
    private JSONArray pullActions;
    private JSONArray drawActions;
    private View customFooterView;
    private View customHeaderView;
//    HashMap<Integer, String> recycleEditContents = new HashMap<>();

    IXListViewListener refreshListener = new IXListViewListener() {
        @Override
        public void onRefresh() {
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

        @Override
        public void onLoadMore() {
            if (drawActions != null && drawActions.length() > 0) {
                for (int i = 0; i < drawActions.length(); i++) {
                    JSONObject action = null;
                    try {
                        action = (JSONObject) (drawActions.get(i));
                        if (action != null) {
                            ((IHeroContext) getContext()).on(action);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    };

    public HeroTableView(Context context) {
        this(context, null);
        adapter = new CustomerListAdapter(context);
        this.setAdapter(adapter);
        this.setOnItemClickListener(adapter);

        ColorDrawable colorDrawable = new ColorDrawable();
        colorDrawable.setColor(getContext().getResources().getColor(R.color.defaultListDividerColor));
        this.setDivider(colorDrawable);
        this.setDividerHeight(1);
//        this.setRecyclerListener(new RecyclerListener() {
//            @Override
//            public void onMovedToScrapHeap(View view) {
//                int pos = (Integer) view.getTag(R.id.kListViewPosition);
//                if (view instanceof EditText) {
//                    recycleEditContents.put(pos, ((EditText) view).getEditableText().toString());
//                }
//            }
//        });
    }


    public HeroTableView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public CustomerListAdapter getCustomAdapter() {
        return adapter;
    }

    @Override
    public void on(JSONObject jsonObject) throws JSONException {
        HeroView.on(this, jsonObject);
        if (jsonObject.has("data")) {
            adapter.setDataSource(jsonObject.getJSONArray("data"));
            adapter.notifyDataSetChanged();
        }

        if (jsonObject.has("dataIndex")) {
            JSONArray data = jsonObject.getJSONArray("dataIndex");
            adapter.setIndexData(data);
            IndexBar indexBar = new IndexBar(getContext(), HeroTableView.this);
            indexBar.addToTable((ViewGroup) (getParent()), HeroTableView.this.getLayoutParams().height);
            this.setVerticalScrollBarEnabled(false);
        }
        if (jsonObject.has("contentOffset")) {
            final int x = jsonObject.getJSONObject("contentOffset").optInt("x");
            final int y = jsonObject.getJSONObject("contentOffset").optInt("y");
            postDelayed(new Runnable() {
                @Override
                public void run() {
                    setSelection(y);
                }
            }, 500);
        }
        if (jsonObject.has("footer")) {
            JSONObject json = jsonObject.getJSONObject("footer");
//            if (this.getFooterViewsCount() > 0 && customFooterView != null) {
//                try {
//                    this.removeFooterView(customFooterView);
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
//            }
            customFooterView = generateCellView(json);
            if (customFooterView != null) {
                this.addFooterView(customFooterView);
                this.setFooterDividersEnabled(false);
            }
        }

        if (jsonObject.has("header")) {
            JSONObject json = jsonObject.getJSONObject("header");
//            if (this.getHeaderViewsCount() > 0 && customHeaderView != null) {
//                try {
//                    this.removeHeaderView(customHeaderView);
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
//            }
            customHeaderView = generateCellView(json);
            if (customHeaderView != null) {
                this.addHeaderView(customHeaderView);
            }
        }

        if (jsonObject.has("class")) {
            if (jsonObject.has("pullRefresh")) {
                this.setPullRefreshEnable(true);
                JSONObject pullObj = (JSONObject) jsonObject.get("pullRefresh");
                this.setXListViewListener(refreshListener);
                if (pullObj.has("action")) {
                    Object pullActionObj = pullObj.get("action");
                    if (pullActionObj instanceof JSONArray) {
                        pullActions = (JSONArray) pullActionObj;
                    } else {
                        pullActions = new JSONArray();
                        pullActions.put(pullActionObj);
                    }
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
            } else {
                setPullRefreshEnable(false);
            }

            if (jsonObject.has("drawMore")) {
                JSONObject drawObj = (JSONObject) jsonObject.get("drawMore");
                this.setXListViewListener(refreshListener);
                if (drawObj.has("action")) {
                    Object drawActionObj = drawObj.get("action");
                    if (drawActionObj instanceof JSONArray) {
                        drawActions = (JSONArray) drawActionObj;
                    } else {
                        drawActions = new JSONArray();
                        drawActions.put(drawActionObj);
                    }
                }
                if (drawObj.has("idle")) {
                    getFooter().setHintString(drawObj.getString("idle"), "idle");
                }
                if (drawObj.has("loading")) {
                    getFooter().setHintString(drawObj.getString("pulling"), "loading");
                }
                if (drawObj.has("noMoreData")) {
                    getFooter().setHintString(drawObj.getString("noMoreData"), "noMoreData");
                }
                this.setPullLoadEnable(true);
            } else {
                setPullLoadEnable(false);
            }
        }

        if (jsonObject.has("separatorColor")) {
            Drawable drawable = this.getDivider();
            ColorDrawable colorDrawable;
            if (drawable instanceof ColorDrawable) {
                colorDrawable = (ColorDrawable) drawable;
            } else {
                colorDrawable = new ColorDrawable();
            }
            String colorString = jsonObject.getString("separatorColor");
            colorDrawable.setColor(StringUtil.stringToColor(colorString));
            this.setDivider(colorDrawable);
            this.setDividerHeight(1);
        }
        if (jsonObject.has("fragmentData")) {
            JSONObject fragmentData = jsonObject.getJSONObject("fragmentData");
            String dataCommand = fragmentData.getString("command");
            int section = fragmentData.optInt("section");
            if (dataCommand != null && dataCommand.equalsIgnoreCase("append")) {
                Object data = fragmentData.get("data");
                adapter.appendDataSource(data);
                adapter.notifyDataSetChanged();
            } else if (dataCommand != null && dataCommand.equalsIgnoreCase("replace")) {

            }
        }
        if (jsonObject.has("method")) {
            if ("closeRefresh".equals(jsonObject.getString("method"))) {
                // close refresh
                stopRefresh();
            } else if ("closeLoadMore".equals(jsonObject.getString("method"))) {
                stopLoadMore();
            } else if ("resetMoreData".equals(jsonObject.getString("method"))) {
                stopLoadMore();
                getFooter().setState(XListViewFooter.STATE_NORMAL);
            } else if ("noMoreData".equals(jsonObject.getString("method"))) {
                stopLoadMore();
                getFooter().setState(XListViewFooter.STATE_NO_DATA);
            }
        }

    }

    private View generateCellView(JSONObject json) {
        IHero view = null;
        try {
            view = HeroView.fromJson(getContext(), json);
            HeroView.setFragmentTag((View) view, HeroView.getFragmentTag(HeroTableView.this));

            if (view != null) {
                view.on(json);
                AbsListView.LayoutParams p = new AbsListView.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
                if (json.has("height")) {
                    int height = HeroView.dip2px(getContext(), json.getInt("height"));
                    if (height > 0) {
                        p.height = height;
                    }
                }
                FrameLayout layout = new FrameLayout(getContext());
                layout.addView((View) view);
                layout.setLayoutParams(p);
                return layout;
            }
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
        return (View) view;
    }

    public IHero findViewInHeaderFooter(String name) {
        if (getHeaderViewsCount() > 0 && customHeaderView != null) {
            IHero view = HeroView.findViewByName(customHeaderView, name);
            if (view != null) {
                return view;
            }
        }
        if (getFooterViewsCount() > 0 && customFooterView != null) {
            IHero view = HeroView.findViewByName(customFooterView, name);
            if (view != null) {
                return view;
            }
        }
        return null;
    }

    public class CustomerListAdapter extends BaseAdapter implements OnItemClickListener, SectionIndexer {
        protected JSONArray dataSource;
        private Context context;
        private JSONArray indexData;
        private HashMap<String, Integer> sectionTitleMap;
        private int itemCount;

        public CustomerListAdapter(Context context) {
            super();
            this.context = context;
            dataSource = new JSONArray();
            sectionTitleMap = new HashMap<String, Integer>();
        }

        public void setIndexData(JSONArray data) {
            indexData = data;
        }

        public JSONArray getIndexData() {
            return indexData;
        }

        public void setDataSource(JSONArray jsonArray) {
            dataSource = jsonArray;
            if (dataSource != null) {
                itemCount = 0;
                for (int i = 0; i < dataSource.length(); i++) {
                    try {
                        Object item = dataSource.get(i);
                        if (item instanceof JSONObject) {
                            if (dataSource.length() > 1 || dataSource.getJSONObject(i).has("sectionTitle")) {
                                if (dataSource.getJSONObject(i).has("sectionTitle")) {
                                    String title = dataSource.getJSONObject(i).getString("sectionTitle");
                                    sectionTitleMap.put(title.toLowerCase(), itemCount);
                                }
                                itemCount++;
                            }
                            if (dataSource.getJSONObject(i).has("rows")) {
                                itemCount += dataSource.getJSONObject(i).getJSONArray("rows").length();
                            }
                            if (dataSource.getJSONObject(i).has("sectionFootTitle")) {
                                itemCount++;
                            }
                            if (dataSource.getJSONObject(i).has("sectionView")) {
                                itemCount++;
                            }
                            if (dataSource.getJSONObject(i).has("title")) {
                                itemCount++;
                            }
                        } else if (item instanceof JSONArray) {
                            JSONArray array = (JSONArray) item;
                            itemCount += array.length();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        }

        public void appendDataSource(Object data) {
            JSONArray array;
            if (dataSource != null) {
                array = dataSource;
                if (data instanceof JSONArray) {
                    int len = ((JSONArray) data).length();
                    for (int i = 0; i < len; i++) {
                        try {
                            array.put(((JSONArray) data).get(i));
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                } else {
                    array.put(data);
                }
            } else {
                if (data instanceof JSONArray) {
                    array = (JSONArray) data;
                } else {
                    array = new JSONArray();
                    array.put(data);
                }
            }
            setDataSource(array);
        }

        @Override
        public int getCount() {
            return itemCount;
        }

        @Override
        public Object getItem(int position) {
            JSONObject item = null;
            try {
                int count = 0;
                for (int i = 0; i < dataSource.length(); i++) {
                    //                    if (count == position) {
                    //                        return dataSource.get(i);
                    //                    }
                    //                    count++;
                    Object itemObject = dataSource.get(i);
                    if (itemObject instanceof JSONObject) {
                        if (dataSource.getJSONObject(i).has("sectionView")) {
                            if (count == position) {
                                return dataSource.getJSONObject(i);
                            }
                            count++;
                        }
                        if (dataSource.getJSONObject(i).has("sectionTitle")) {
                            if (count == position) {
                                return dataSource.getJSONObject(i);
                            }
                            count++;
                        } else {
                            // only show the separator when section more than 1
                            if (dataSource.length() > 1) {
                                if (count == position) {
                                    JSONObject json = new JSONObject();
                                    json.put("emptySeparator", "1");
                                    return json;
                                }
                                count++;
                            }
                        }
                        JSONArray rows = dataSource.getJSONObject(i).getJSONArray("rows");
                        for (int j = 0; j < rows.length(); j++) {
                            if (count == position) {
                                return rows.get(j);
                            }
                            count++;
                        }
                        if (dataSource.getJSONObject(i).has("sectionFootTitle")) {
                            JSONObject footer = new JSONObject();
                            footer.put("sectionFootTitle", dataSource.getJSONObject(i).get("sectionFootTitle"));
                            if (count == position) {
                                return footer;
                            }
                            count++;
                        }
                    } else if (itemObject instanceof JSONArray) {
                        JSONArray array = (JSONArray) itemObject;
                        for (int j = 0; j < array.length(); j++) {
                            if (count == position) {
                                return array.get(j);
                            }
                            count++;
                        }
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View view, ViewGroup parent) {
            JSONObject item = null;
            item = (JSONObject) this.getItem(position);
            View itemView = HeroTableViewCell.getView(context, view, item);
            HeroView.setFragmentTag(itemView, HeroView.getFragmentTag(HeroTableView.this));
            itemView.setTag(R.id.kListViewPosition, position);
//            if (recycleEditContents.get(position) != null) {
//                if (itemView instanceof EditText) {
//                    ((EditText) itemView).setText(recycleEditContents.get(position));
//                }
//            }
            return itemView;
        }

        @Override
        public boolean isEnabled(int position) {
            JSONObject item = (JSONObject) this.getItem(position);
            if (item == null) {
                return false;
            }
            if (item.has("rows")) {
                if (item.has("enable")) {
                    return true;
                }
                return false;
            }
            if (item.has("enable")) {
                try {
                    if (!item.getBoolean("enable")) {
                        return false;
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            return true;
        }

        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            int numHeaders = getHeaderViewsCount();
            if (position < numHeaders) {
                return;
            }

            final int actualPosition = position - numHeaders;
            JSONObject item = (JSONObject) this.getItem(actualPosition);
            if (item == null) return;
            if (item.has("action")) {
                try {
                    ((IHeroContext) context).on(item.getJSONObject("action"));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            } else {
                try {
                    ((IHeroContext) context).on(item);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }

        @Override
        public void unregisterDataSetObserver(DataSetObserver observer) {
            if (observer != null) {
                super.unregisterDataSetObserver(observer);
            }
        }

        @Override
        public Object[] getSections() {
            return sectionTitleMap.keySet().toArray();
        }

        @Override
        public int getPositionForSection(int sectionIndex) {
            if (sectionTitleMap != null && indexData != null) {
                try {
                    String item = indexData.getString(sectionIndex);
                    return sectionTitleMap.get(item.toLowerCase());
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            return 0;
        }

        @Override
        public int getSectionForPosition(int position) {
            if (sectionTitleMap != null) {
                List<Integer> valueList = new ArrayList(sectionTitleMap.values());
                Collections.sort(valueList);
                int index = 0;
                for (int value : valueList) {
                    if (position < value) {
                        return index - 1;
                    }
                    index++;
                }
                return valueList.size() - 1;
            }
            return 0;
        }
    }
}
