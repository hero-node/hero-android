package com.hero.chat;

import org.json.JSONException;
import org.json.JSONObject;

public class ChatMsgEntity {

    public static final boolean DEFAULT_SHOW_USER_NAME = true;

    public static final int MESSAGE_TYPE_RECEIVED_TEXT = 0;
    public static final int MESSAGE_TYPE_SENT_TEXT = 1;
    public static final int MESSAGE_TYPE_SYSTEM = 2;
    public static final int MESSAGE_TYPE_RECEIVED_UI = 3;
    public static final int MESSAGE_TYPE_SENT_UI = 4;
    public static final int MESSAGE_TYPE_COUNT = 5;

    public static final String MESSAGE_KEY_SYSTEM = "systemMsg";
    public static final String MESSAGE_KEY_MESSAGE = "text";
    public static final String MESSAGE_KEY_UI = "ui";
    public static final String MESSAGE_KEY_IMAGE = "image";

    private int messageType = MESSAGE_TYPE_RECEIVED_TEXT;
    private String nickname;
    private String avatarUrl;
    private String text;
    private boolean showUserName = DEFAULT_SHOW_USER_NAME;
    //    private JSONObject data;
    private JSONObject ui;
    private String imageUrl;

    public String getName() {
        return nickname;
    }

    public void setName(String name) {
        this.nickname = name;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getAvatarUrl() {
        return this.avatarUrl;
    }

    public void setAvatarUrl(String url) {
        this.avatarUrl = url;
    }

    public JSONObject getUIObject() {
        return this.ui;
    }

    public String getImageUrl() {
        return this.imageUrl;
    }

    public boolean isShowUserName() {
        return showUserName;
    }

    public int getMsgType() {
        return messageType;
    }

    public void setMsgType(int type) {
        messageType = type;
    }

    public ChatMsgEntity() {
        this.showUserName = DEFAULT_SHOW_USER_NAME;
    }

    public ChatMsgEntity(int type) {
        this.messageType = type;
    }

    public ChatMsgEntity(int type, String text) {
        this.messageType = type;
        this.text = text;
    }

    public ChatMsgEntity(String name, String avatar, String text, int type) {
        this.nickname = name;
        this.text = text;
        this.messageType = type;
        this.avatarUrl = avatar;
    }

    public ChatMsgEntity(JSONObject object) {
        //        this.data = object;
        initEntity(object);
    }

    private void initEntity(JSONObject object) {
        if (object.has(MESSAGE_KEY_SYSTEM)) {
            this.messageType = MESSAGE_TYPE_SYSTEM;
            this.text = object.optString(MESSAGE_KEY_SYSTEM);
        } else if (object.has(MESSAGE_KEY_MESSAGE)) {
            if (object.optBoolean("self", false)) {
                this.messageType = MESSAGE_TYPE_SENT_TEXT;
            } else {
                this.messageType = MESSAGE_TYPE_RECEIVED_TEXT;
            }
            this.text = object.optString(MESSAGE_KEY_MESSAGE);
            this.avatarUrl = object.optString("avatar");
            this.nickname = object.optString("nickname");
        } else if (object.has(MESSAGE_KEY_UI) || object.has(MESSAGE_KEY_IMAGE)) {
            if (object.optBoolean("self", false)) {
                this.messageType = MESSAGE_TYPE_SENT_UI;
            } else {
                this.messageType = MESSAGE_TYPE_RECEIVED_UI;
            }
            this.avatarUrl = object.optString("avatar");
            this.nickname = object.optString("nickname");
            if (object.has(MESSAGE_KEY_UI)) {
                this.ui = object.optJSONObject(MESSAGE_KEY_UI);
            } else {
                this.imageUrl = object.optString(MESSAGE_KEY_IMAGE);
            }
        }
    }

    public static JSONObject composeOutgoingMessage(String message) throws JSONException {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("msg", message);
        return jsonObject;
    }
}
