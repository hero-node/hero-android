package com.hero.chat;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.hero.HeroChatMsgView;

import java.util.ArrayList;
import java.util.List;

public class ChatMessageAdapter extends BaseAdapter {

    private List<ChatMsgEntity> chatMsgEntities;

    private Context context;

    private LayoutInflater inflater;

    public ChatMessageAdapter(Context context, List<ChatMsgEntity> messages) {
        this.context = context;
        if (messages != null) {
            this.chatMsgEntities = messages;
        } else {
            this.chatMsgEntities = new ArrayList<>();
        }
        inflater = LayoutInflater.from(context);
    }

    public ChatMessageAdapter(Context context) {
        this.context = context;
        this.chatMsgEntities = new ArrayList<>();
        inflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        return chatMsgEntities.size();
    }

    @Override
    public Object getItem(int position) {
        return chatMsgEntities.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public void addItem(ChatMsgEntity item) {
        chatMsgEntities.add(item);
        notifyDataSetChanged();
    }

    public void addItem(int position, ChatMsgEntity item) {
        chatMsgEntities.add(position, item);
        notifyDataSetChanged();
    }

    @Override
    public int getItemViewType(int position) {
        ChatMsgEntity entity = chatMsgEntities.get(position);
        return entity.getMsgType();
    }

    @Override
    public int getViewTypeCount() {
        return ChatMsgEntity.MESSAGE_TYPE_COUNT;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ChatMsgEntity entity = chatMsgEntities.get(position);
        return HeroChatMsgView.getView(context, convertView, entity, inflater);
    }

}
