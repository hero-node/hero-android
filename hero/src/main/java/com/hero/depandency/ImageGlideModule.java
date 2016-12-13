package com.hero.depandency;

import android.content.Context;

import com.bumptech.glide.Glide;
import com.bumptech.glide.GlideBuilder;
import com.bumptech.glide.load.engine.bitmap_recycle.LruBitmapPool;
import com.bumptech.glide.load.engine.cache.ExternalCacheDiskCacheFactory;
import com.bumptech.glide.load.engine.cache.LruResourceCache;
import com.bumptech.glide.module.GlideModule;

/**
 * Created by drjr92 on 16-7-25.
 */
public class ImageGlideModule implements GlideModule {

    @Override
    public void applyOptions(Context context, GlideBuilder builder) {
//
        builder.setDiskCache(new ExternalCacheDiskCacheFactory(context, "image", 200*1024*1024));
        builder.setBitmapPool(new LruBitmapPool(10*1024*1024));
        builder.setMemoryCache(new LruResourceCache(50*1024*1024));
    }

    @Override
    public void registerComponents(Context context, Glide glide) {
    }
}
