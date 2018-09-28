package com.hero.signature.entity;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.hero.R;

import java.util.List;

/**
 * Created by Aron on 2018/5/9.
 */
public class GridViewAdapter extends BaseAdapter{

    // 图标和名称数据列表
    private List<DataBean> dataList;

    public GridViewAdapter(List<DataBean> datas) {
        dataList = datas;

    }
    @Override
    public int getCount() {
        return dataList.size();
    }
    @Override
    public Object getItem(int i) {
        return dataList.get(i);
    }
    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View itemView, ViewGroup viewGroup) {
        ViewHolder mHolder;
        if (itemView == null) {
            mHolder = new ViewHolder();
            itemView = LayoutInflater.from(viewGroup.getContext())
                    .inflate(R.layout.gridview, viewGroup, false);
            mHolder.iv_img = (ImageView) itemView.findViewById(R.id.iv_img);
            mHolder.tv_text = (TextView) itemView.findViewById(R.id.tv_text);
            itemView.setTag(mHolder);
        }
        else {
            mHolder = (ViewHolder) itemView.getTag();
        }
        DataBean bean = dataList.get(i);
        if (bean != null) {
            mHolder.iv_img.setImageResource(bean.getIconId());
            mHolder.tv_text.setText(bean.getIconName());
        }
        return itemView;
    }

    private class ViewHolder {

        private ImageView iv_img;

        private TextView tv_text;

    }
}
