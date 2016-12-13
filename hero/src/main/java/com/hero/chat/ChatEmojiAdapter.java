package com.hero.chat;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;

import java.util.List;
import com.hero.R;

/**
 * Created by xincai on 16-6-16.
 */
public class ChatEmojiAdapter extends ArrayAdapter<String> {
    Context context;
    private int resId;

    public ChatEmojiAdapter(Context context, int resourceId, List<String> objects) {
        super(context, resourceId, objects);
        resId = resourceId;
        this.context = context;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = View.inflate(getContext(), resId, null);
        }

        ImageView imageView = (ImageView) convertView.findViewById(R.id.imgEmoji);
        String filename = getItem(position);
        int resId = ChatEmojiUtils.getResIdByName(context, filename);
        imageView.setImageResource(resId);
        return convertView;
    }
}