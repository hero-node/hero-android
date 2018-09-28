package com.hero.signature.entity;

import java.io.Serializable;

/**
 * Created by Aron on 2018/5/9.
 */

public class DataBean implements Serializable {

    // 图标名称
    private String iconName;

    // 图标ID
    private int iconId;

    // 图标链接
    private String iconUrl;

    public DataBean () {
    }

    public DataBean (String iconName, int iconId, String iconUrl) {
        this.iconName = iconName;
        this.iconId = iconId;
        this.iconUrl = iconUrl;
    }

    public String getIconName() {
        return iconName;
    }

    public void setIconName(String iconName) {
        this.iconName = iconName;
    }

    public int getIconId() {
        return iconId;
    }

    public void setIconId(int iconId) {
        this.iconId = iconId;
    }

    public String getIconUrl() {
        return iconUrl;
    }

    public void setIconUrl(String iconUrl) {
        this.iconUrl = iconUrl;
    }
}
