package com.hero;

import com.hero.depandency.IImagePickHandler;

import org.json.JSONException;

/**
 * Created by liuguoping on 15/9/23.
 */
public interface IHeroContext {
    void on(final Object object) throws JSONException;

    void setImagePickHandler(IImagePickHandler handler);
}
