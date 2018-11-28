package com.hero.depandency;

import com.bumptech.glide.load.model.GlideUrl;

/**
 * Created by Aron on 2018/11/28.
 */

public class MyGlideUrl extends GlideUrl {


    private String mUrl;

    public MyGlideUrl(String url) {
        super(url);
        mUrl = url;
    }

    @Override
    public String getCacheKey() {
        return mUrl.replace(deleteToken(), "");
        // 通过 deleteToken() 从 带有token参数的图片Url地址中 去掉 token参数
    }

    // 分析1：deleteToken()
    private String deleteToken() {
        String tokenParam = "";
        int tokenKeyIndex = mUrl.indexOf("?token=") >= 0 ? mUrl.indexOf("?token=") : mUrl.indexOf("&token=");
        if (tokenKeyIndex != -1) {
            int nextAndIndex = mUrl.indexOf("&", tokenKeyIndex + 1);
            if (nextAndIndex != -1) {
                tokenParam = mUrl.substring(tokenKeyIndex + 1, nextAndIndex + 1);
            } else {
                tokenParam = mUrl.substring(tokenKeyIndex);
            }
        }
        return tokenParam;
    }
}
