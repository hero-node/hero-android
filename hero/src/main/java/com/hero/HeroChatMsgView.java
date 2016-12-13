package com.hero;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.hero.chat.ChatEmojiUtils;
import com.hero.chat.ChatMsgEntity;
import com.hero.depandency.ImageLoadUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.InvocationTargetException;


public class HeroChatMsgView extends FrameLayout implements IHero {

    public HeroChatMsgView(Context context) {
        super(context);
    }

    public static View getView(Context context, View convertView, ChatMsgEntity entity, LayoutInflater inflater) {
        ViewHolder viewHolder = null;
        int messageType = entity.getMsgType();

        if (convertView == null) {
            viewHolder = new ViewHolder();
            convertView = new HeroChatMsgView(context);
            //            AbsListView.LayoutParams p;
            //            p = new AbsListView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            //            convertView.setLayoutParams(p);
            if (messageType == ChatMsgEntity.MESSAGE_TYPE_RECEIVED_TEXT) {
                inflater.inflate(R.layout.chat_item_msg_left, (ViewGroup) convertView, true);
            } else if (messageType == ChatMsgEntity.MESSAGE_TYPE_SENT_TEXT) {
                inflater.inflate(R.layout.chat_item_msg_right, (ViewGroup) convertView, true);
            } else if (messageType == ChatMsgEntity.MESSAGE_TYPE_SYSTEM) {
                inflater.inflate(R.layout.chat_item_msg_system, (ViewGroup) convertView, true);
            } else if (messageType == ChatMsgEntity.MESSAGE_TYPE_RECEIVED_UI) {
                inflater.inflate(R.layout.chat_item_ui_left, (ViewGroup) convertView, true);
                initMsgUIView(context, entity, convertView);
            } else if (messageType == ChatMsgEntity.MESSAGE_TYPE_SENT_UI) {
                inflater.inflate(R.layout.chat_item_ui_right, (ViewGroup) convertView, true);
                initMsgUIView(context, entity, convertView);
            }
            viewHolder.txtSystemMsg = (TextView) convertView.findViewById(R.id.txtSystemMsg);
            viewHolder.txtUserName = (TextView) convertView.findViewById(R.id.txtUserName);
            viewHolder.txtChatContent = (TextView) convertView.findViewById(R.id.txtChatContent);
            viewHolder.imgAvatar = (ImageView) convertView.findViewById(R.id.imgAvatar);
            viewHolder.uiContainer = convertView.findViewById(R.id.uiContainer);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        switch (messageType) {
            case ChatMsgEntity.MESSAGE_TYPE_RECEIVED_TEXT:
            case ChatMsgEntity.MESSAGE_TYPE_SENT_TEXT:
                viewHolder.txtChatContent.setText(ChatEmojiUtils.mixTextWithEmoji(context, entity.getText()));
                if (entity.isShowUserName() && !TextUtils.isEmpty(entity.getName())) {
                    viewHolder.txtUserName.setText(entity.getName());
                    viewHolder.txtUserName.setVisibility(View.VISIBLE);
                } else {
                    viewHolder.txtUserName.setVisibility(View.GONE);
                }
                loadAvatar(viewHolder.imgAvatar, entity.getAvatarUrl());
                break;
            case ChatMsgEntity.MESSAGE_TYPE_SYSTEM:
                viewHolder.txtSystemMsg.setText(entity.getText());
                break;
            case ChatMsgEntity.MESSAGE_TYPE_RECEIVED_UI:
            case ChatMsgEntity.MESSAGE_TYPE_SENT_UI:
                if (entity.isShowUserName() && !TextUtils.isEmpty(entity.getName())) {
                    viewHolder.txtUserName.setText(entity.getName());
                    viewHolder.txtUserName.setVisibility(View.VISIBLE);
                } else {
                    viewHolder.txtUserName.setVisibility(View.GONE);
                }
                loadAvatar(viewHolder.imgAvatar, entity.getAvatarUrl());
                initMsgUIView(context, entity, convertView);
                break;
            default:
                break;
        }

        return convertView;
    }

    private static void initMsgUIView(Context context, ChatMsgEntity entity, View view) {
        View root = view.findViewById(R.id.uiContainer);
        if (root != null && root instanceof ViewGroup) {
            ((ViewGroup) root).removeAllViews();
            if (!TextUtils.isEmpty(entity.getImageUrl())) {
                ImageView imageView = createImageView(context, entity.getImageUrl());
                ((ViewGroup) root).addView(imageView);
            } else {
                JSONObject ui = entity.getUIObject();
                if (ui != null) {
                    createUiView(context, ui, ((ViewGroup) root), null);
                    //                 edit.requestFocus();
                }
            }
        }
    }

    public static ImageView createImageView(Context context, String url) {
        boolean imageLoaded = false;
        ImageView imageView = new ImageView(context);
        imageView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        if (url.lastIndexOf("?") > 0) {
            String resolution = url.substring(url.lastIndexOf("?") + 1, url.length());
            if (resolution != null) {
                String[] widthHeight = resolution.split(",");
                if (widthHeight.length > 1) {
                    try {
                        int width = Integer.parseInt(widthHeight[0]);
                        int height = Integer.parseInt(widthHeight[1]);
                        width = HeroView.dip2px(context, width);
                        height = HeroView.dip2px(context, height);
                        loadImage(imageView, url, width, height);
                        imageLoaded = true;
                    } catch (NumberFormatException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        if (!imageLoaded) {
            loadImage(imageView, url);
        }
        return imageView;
    }

    public static IHero createUiView(Context context, JSONObject jsonObject, ViewGroup parent, LayoutParams params) {
        try {
            IHero uiView = HeroView.fromJson(context, jsonObject);
            if (parent != null) {
                parent.addView((View) uiView);
            }
            uiView.on(jsonObject);
            if (params != null) {
                LayoutParams orgParam = (LayoutParams) ((View) uiView).getLayoutParams();
                if (orgParam != null) {
                    orgParam.leftMargin = params.leftMargin;
                    orgParam.topMargin = params.topMargin;
                    if (params.width > 0) {
                        orgParam.width = params.width;
                    }
                    if (params.height > 0) {
                        orgParam.height = params.height;
                    }
                    ((View) uiView).setLayoutParams(orgParam);
                }
            }
            return uiView;
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
        return null;
    }

    private static void loadAvatar(ImageView imageView, String url) {
        if (!loadImage(imageView, url)) {
            if (imageView != null) {
                imageView.setImageResource(R.drawable.chat_avatar_default);
            }
        }
    }

    private static boolean loadImage(ImageView imageView, String url) {
        if (imageView == null || TextUtils.isEmpty(url)) {
            return false;
        }
        if (url.startsWith("http")) {
            ImageLoadUtils.LoadImage(imageView, url);
        } else {
            ImageLoadUtils.loadLocalImage(imageView, url);
        }
        return true;
    }

    private static boolean loadImage(ImageView imageView, String url, int width, int height) {
        if (imageView == null || TextUtils.isEmpty(url)) {
            return false;
        }
        imageView.setLayoutParams(new ViewGroup.LayoutParams(width, height));
        imageView.setScaleType(ImageView.ScaleType.FIT_XY);
        if (url.startsWith("http")) {
            ImageLoadUtils.LoadImage(imageView, url, width, height);
        } else {
            ImageLoadUtils.loadLocalImage(imageView, url);
        }
        return true;
    }

    @Override
    public void on(JSONObject jsonObject) throws JSONException {
        HeroView.on(this, jsonObject);
        if (jsonObject.has("ui")) {
            JSONObject ui = jsonObject.optJSONObject("ui");
            initMsgUIView(getContext(), new ChatMsgEntity(ui), HeroChatMsgView.this);
        }
    }

    static class ViewHolder {
        public TextView txtSystemMsg;
        public TextView txtUserName;
        public TextView txtChatContent;
        public ImageView imgAvatar;
        public View uiContainer;
        public int type;
    }
}
