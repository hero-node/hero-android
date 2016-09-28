package com.hero.depandency;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.model.GlideUrl;
import com.bumptech.glide.load.model.LazyHeaders;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.hero.HeroApplication;
import com.hero.HeroImageView;
import com.hero.HeroView;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.CookieManager;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by xincai on 16-5-24.
 */
public class ImageLoadUtils {


    public static void LoadImage(ImageView view, String url) {
        if (view != null) {
            Context context = view.getContext();
            Context application = context.getApplicationContext();
            if (application instanceof HeroApplication&&url.startsWith("http")) {
                String domain = HeroApplication.getDomainAddress(url);
                String cookieString = ((HeroApplication) application).getCookieManager().getCookie(domain);
                if (cookieString != null) {
                    Glide.with(context).load(getUrlWithCookie(url, cookieString)).diskCacheStrategy(DiskCacheStrategy.SOURCE).into(view);
                    return;
                }
            }
            Glide.with(context).load(url).diskCacheStrategy(DiskCacheStrategy.SOURCE).into(view);

        }
    }



    public static void LoadImage(ImageView view, String url,int defaultpic) {
        if (view != null) {
            Context context = view.getContext();
            Context application = context.getApplicationContext();
            if (application instanceof HeroApplication&&url.startsWith("http")) {
                String domain = HeroApplication.getDomainAddress(url);
                String cookieString = ((HeroApplication) application).getCookieManager().getCookie(domain);
                if (cookieString != null) {

                    Glide.with(context).load(getUrlWithCookie(url, cookieString)).diskCacheStrategy(DiskCacheStrategy.SOURCE).placeholder(defaultpic).error(defaultpic).into(view);
                    return;
                }
            }
            Glide.with(context).load(url).diskCacheStrategy(DiskCacheStrategy.SOURCE).placeholder(defaultpic).error(defaultpic).into(view);

        }
    }


    public static void LoadImage(ImageView view, String url, int width, int height) {
        if (view != null) {
            Context context = view.getContext();
            Context application = context.getApplicationContext();



            if (application instanceof HeroApplication&&url.startsWith("http")) {
                String domain = HeroApplication.getDomainAddress(url);
                String cookieString = ((HeroApplication) application).getCookieManager().getCookie(domain);
                if (cookieString != null) {
                    Glide.with(context).load(getUrlWithCookie(url, cookieString)).diskCacheStrategy(DiskCacheStrategy.SOURCE).override(width, height).into(view);
                    return;
                }
            }
            Glide.with(context).load(url).diskCacheStrategy(DiskCacheStrategy.SOURCE).override(width, height).into(view);


        }
    }

    public static void LoadImage(ImageView view, String url, int width, int height,int defaultpic) {
        if (view != null) {
            Context context = view.getContext();
            Context application = context.getApplicationContext();
            if (application instanceof HeroApplication&&url.startsWith("http")) {
                String domain = HeroApplication.getDomainAddress(url);
                String cookieString = ((HeroApplication) application).getCookieManager().getCookie(domain);
                if (cookieString != null) {

                    Glide.with(context).load(getUrlWithCookie(url, cookieString)).diskCacheStrategy(DiskCacheStrategy.SOURCE).override(width, height).placeholder(defaultpic).error(defaultpic).into(view);
                    return;
                }
            }
            Glide.with(context).load(url).diskCacheStrategy(DiskCacheStrategy.SOURCE).override(width, height).placeholder(defaultpic).error(defaultpic).into(view);


        }
    }

    public static void LoadImageWithThumbnailCenterCrop(ImageView view, String url, int width, int height,int defaultpic) {
        if (view != null) {
            Context context = view.getContext();
            Context application = context.getApplicationContext();
            if (application instanceof HeroApplication&&url.startsWith("http")) {
                String domain = HeroApplication.getDomainAddress(url);
                String cookieString = ((HeroApplication) application).getCookieManager().getCookie(domain);
                if (cookieString != null) {

                    Glide.with(context).load(getUrlWithCookie(url, cookieString)).diskCacheStrategy(DiskCacheStrategy.SOURCE).override(width, height).placeholder(defaultpic).error(defaultpic).thumbnail(0.2f).centerCrop().into(view);
                    return;
                }
            }
            Glide.with(context).load(url).diskCacheStrategy(DiskCacheStrategy.SOURCE).override(width, height).placeholder(defaultpic).error(defaultpic).thumbnail(0.2f).centerCrop().into(view);


        }
    }

    public static void LoadImageCenterCrop(ImageView view, String url, int width, int height,int defaultpic) {
        if (view != null) {
            Context context = view.getContext();
            Context application = context.getApplicationContext();
            if (application instanceof HeroApplication&&url.startsWith("http")) {
                String domain = HeroApplication.getDomainAddress(url);
                String cookieString = ((HeroApplication) application).getCookieManager().getCookie(domain);
                if (cookieString != null) {

                    Glide.with(context).load(getUrlWithCookie(url, cookieString)).diskCacheStrategy(DiskCacheStrategy.SOURCE).override(width, height).placeholder(defaultpic).error(defaultpic).centerCrop().into(view);
                    return;
                }
            }
            Glide.with(context).load(url).diskCacheStrategy(DiskCacheStrategy.SOURCE).override(width, height).placeholder(defaultpic).error(defaultpic).centerCrop().into(view);


        }
    }
    public static void LoadImageCenterCrop(ImageView view, String url,int defaultpic) {
        if (view != null) {
            Context context = view.getContext();
            Context application = context.getApplicationContext();
            if (application instanceof HeroApplication&&url.startsWith("http")) {
                String domain = HeroApplication.getDomainAddress(url);
                String cookieString = ((HeroApplication) application).getCookieManager().getCookie(domain);
                if (cookieString != null) {

                    Glide.with(context).load(getUrlWithCookie(url, cookieString)).diskCacheStrategy(DiskCacheStrategy.SOURCE).placeholder(defaultpic).error(defaultpic).centerCrop().into(view);
                    return;
                }
            }
            Glide.with(context).load(url).diskCacheStrategy(DiskCacheStrategy.SOURCE).placeholder(defaultpic).error(defaultpic).centerCrop().into(view);

        }
    }

    public static void loadLocalImage(ImageView imageView, String name) {
        if (imageView != null) {
            Context context = imageView.getContext();
            int resId = getLocalImageIdByName(context, name);
            if (resId != -1 && resId != 0) {
                Glide.with(context).load(resId).dontAnimate().into(imageView);
            }
        }
    }

    public static void loadLocalImageNative(ImageView imageView, String name) {
        if (imageView != null) {
            Context context = imageView.getContext();
            int resId = getLocalImageIdByName(context, name);
            if (resId != -1 && resId != 0) {
                try {
                    imageView.setImageDrawable(context.getResources().getDrawable(resId));
                } catch (Resources.NotFoundException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static int getLocalImageIdByName(Context c, String picName) {
        if (picName.lastIndexOf(".") > 0) {
            picName = picName.substring(0, picName.lastIndexOf("."));
        }
        picName = picName.toLowerCase();
        picName = picName.replaceAll("-", "_");
        int resId = -1;
        resId = HeroView.getResId(c, picName, "drawable");
        return resId;
    }

    private static GlideUrl getUrlWithCookie(String url, String cookieString) {
        LazyHeaders.Builder builder = new LazyHeaders.Builder().addHeader("Cookie", cookieString);
        GlideUrl glideUrl = new GlideUrl(url, builder.build());
        return glideUrl;
    }

    public static void LoadBase64Image(final Context context, final HeroImageView view, String url) {
        Glide.with(context).load(url).downloadOnly(new SimpleTarget<File>() {

            @Override
            public void onLoadFailed(Exception e, Drawable errorDrawable) {
                super.onLoadFailed(e, errorDrawable);
            }

            @Override
            public void onResourceReady(File resource, GlideAnimation<? super File> glideAnimation) {
                if (resource != null) {
                    int length = (int) resource.length();
                    byte[] stringBuffer = null;
                    try {
                        InputStream in = new FileInputStream(resource);
                        stringBuffer = new byte[length];
                        in.read(stringBuffer, 0, length);
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    if (stringBuffer != null) {
                        try {
                            view.setLoadedImageBitmap(BitmapUtils.decodeBitmapFromBase64(new String(stringBuffer)));
                        } catch (Exception e1) {
                            e1.printStackTrace();
                        }
                    }
                }
            }
        });
    }

    public static void LoadImageAsBitmap(Context context, String url, final LoadFinishHandler handler) {
        Glide.with(context).load(url).asBitmap().into(new SimpleTarget<Bitmap>() {
            @Override
            public void onResourceReady(Bitmap bitmap, GlideAnimation<? super Bitmap> glideAnimation) {
                if (handler != null) {
                    if (bitmap != null) {
                        handler.onLoadFinished(bitmap);
                    } else {
                        handler.onLoadFailed();
                    }
                }
            }
        });
    }


    public static void storeCookies(CookieManager manager, URI uri, List<String> cookies) throws IOException {
        Map headers = new HashMap();
        headers.put("Set-Cookie", cookies);
        manager.put(uri, headers);
    }

    public static void storeCookie(CookieManager manager, URI uri, String webCookie) throws IOException {
        List<String> cookies = new ArrayList<String>();
        String[] cookieValues = webCookie.split(";");
        for (int i = 0; i < cookieValues.length; i++) {
            cookies.add(cookieValues[i].trim());
        }
        storeCookies(manager, uri, cookies);
    }

    public interface LoadFinishHandler {
        public void onLoadFinished(Bitmap bitmap);

        public void onLoadFailed();
    }

}
