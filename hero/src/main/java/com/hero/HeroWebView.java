/**
 * BSD License
 * Copyright (c) Hero software.
 * All rights reserved.

 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:

 * Redistributions of source code must retain the above copyright notice, this
 * list of conditions and the following disclaimer.

 * Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.

 * Neither the name Hero nor the names of its contributors may be used to
 * endorse or promote products derived from this software without specific
 * prior written permission.

 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package com.hero;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.webkit.DownloadListener;
import android.webkit.JavascriptInterface;
import android.webkit.JsPromptResult;
import android.webkit.JsResult;
import android.webkit.MimeTypeMap;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.hero.depandency.ContextUtils;

import org.apache.http.client.methods.HttpPost;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by liuguoping on 15/9/24.
 */
@SuppressLint("SetJavaScriptEnabled")
public class HeroWebView extends WebView implements IHero {
    static final String TAG = "HeroWebView";
    public final static boolean NEED_VERIFY_URL_HOST = false;
    public final static String FRAGMENT_TAG_KEY = "fragment_tag";
    private HeroFragment parentFragment = null;
    private JSONArray hijackUrlArray;
    private String mUrl;
    private String postData;
    private String method;
    private static final String METHOD_POST = HttpPost.METHOD_NAME;
    private boolean injectHero;
    private HashMap modules;
    public HeroWebView(Context context, int initColor, final boolean injectHero) {
        this(context);
        try {
            this.getSettings().setJavaScriptEnabled(true);
            this.getSettings().setJavaScriptCanOpenWindowsAutomatically(true);
        } catch (Exception e) {
            e.printStackTrace();
        }
        this.addJavascriptInterface(this, "native");
        this.setInjectHero(injectHero);
        String userAgent = this.getSettings().getUserAgentString();
        userAgent += " Android/" + ContextUtils.getSystemVersion() + " hero-android/" + ContextUtils.getVersionCode(this.getContext()) + " imei/" + ContextUtils.getIMEI(context) + " androidId/" + ContextUtils.getAndroidId(context);
        userAgent += " Brand/" + ContextUtils.getDeviceBrand() +" Model/" + ContextUtils.getDeviceName();
        if (HeroApplication.getInstance() != null) {
            String extraUA = HeroApplication.getInstance().getExtraUserAgent();
            if (!TextUtils.isEmpty(extraUA)) {
                userAgent += " " + extraUA;
            }
        }
        this.getSettings().setUserAgentString(userAgent);
        this.getSettings().setDomStorageEnabled(true);
        String appCachePath = getContext().getCacheDir().getAbsolutePath();
        this.getSettings().setAppCachePath(appCachePath);
        this.getSettings().setAllowFileAccess(true);
        this.getSettings().setAppCacheEnabled(true);
        this.getSettings().setCacheMode(WebSettings.LOAD_DEFAULT);
        this.setDownloadListener(new MyWebViewDownLoadListener());
        final Context theContext = context;
        this.setWebViewClient(new WebViewClient() {
            @Nullable
            @Override
            public WebResourceResponse shouldInterceptRequest(WebView view, WebResourceRequest request) {
                String url = request.getUrl().toString();
                if (url.startsWith("https://localhost:3000")){
                    try {
                        InputStream in = getResources().getAssets().open(request.getUrl().getPath().substring(1));
                        WebResourceResponse resourceResponse = new WebResourceResponse(
                                MimeTypeMap.getSingleton().getMimeTypeFromExtension(MimeTypeMap.getFileExtensionFromUrl(url))
                                , "UTF-8", in);
                        return resourceResponse;
                    } catch (IOException e) {
                        Log.d("WebViewDebug",e.getMessage()+e.toString());
                        e.printStackTrace();
                    }
                }
                return super.shouldInterceptRequest(view, request);
            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                if (!isUrlAuthenticated(url)) {
                    return true;
                }

                if (hijackUrlArray != null) {
                    try {
                        JSONObject jsonObject = shouldHijackUrl(url);
                        if (jsonObject != null) {
                            JSONObject event = new JSONObject();
                            event.put("name", HeroView.getName(view));
                            event.put("url", jsonObject.optString("url"));
                            HeroView.sendActionToContext(theContext, event);

                            if (jsonObject.has("isLoad")) {
                                // isLoad: this url need to be loaded
                                if (!jsonObject.getBoolean("isLoad")) {
                                    return true;
                                }
                            }
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }

                if(!url.startsWith("http:") || url.startsWith("https:") ) {
                    return false;
                }

                view.loadUrl(url);
                return true;
            }

            @Override
            public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
                super.onReceivedError(view, errorCode, description, failingUrl);
                InputStream page404 = getResources().openRawResource(R.raw.page_404);
                if (page404 != null) {
                    String content = null;
                    try {
                        content = inputStreamTOString(page404);
                    } catch (Exception e) {
                        Log.d("Error", e.getMessage());
                    }
                    view.loadData(content, "text/html;charset=UTF-8", null);
                }
                try {
                    JSONObject object = new JSONObject("{common:'webViewDidFinishLoad'}");
                    HeroView.sendActionToContext(getContext(), object);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                setWindowAttribute();
                if (view.getParent() != null && parentFragment != null) {
                    parentFragment.showToolBar(true);
                }else{
                    HeroFragment.evaluateJavaScript(view, HeroFragment.VIEW_WILL_APPEAR_EXPRESSION);
                }
                try {
                    JSONObject object = new JSONObject("{command:'webViewDidFinishLoad'}");
                    HeroView.sendActionToContext(getContext(), object);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                super.onPageStarted(view, url, favicon);
            }

            @Override
            public boolean shouldOverrideKeyEvent(WebView view, KeyEvent event) {
                if ((event.getKeyCode() == KeyEvent.KEYCODE_BACK) && view.canGoBack()) {
                    view.goBack();
                    return true;
                }
                return false;
            }
        });
        this.setWebChromeClient(new WebChromeClient() {

            @Override
            public boolean onJsConfirm(WebView view, String url, String message, JsResult result) {
                return super.onJsConfirm(view, url, message, result);
            }

            @Override
            public boolean onJsPrompt(WebView view, String url, String message, String defaultValue, JsPromptResult result) {
                return super.onJsPrompt(view, url, message, defaultValue, result);
            }
            @Override
            public boolean onJsBeforeUnload(WebView view, String url, String message, JsResult result) {
                return super.onJsBeforeUnload(view, url, message, result);
            }
            @Override
            public void onReceivedTitle(WebView view, String title) {
                super.onReceivedTitle(view, title);
                if (view.getParent() != null && parentFragment != null) {
                    if (!TextUtils.isEmpty(title) && !title.contains(".html")) {
                        parentFragment.showToolBar(true);
                        parentFragment.setTitle(title);
                    }
                }
            }
            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                super.onProgressChanged(view, newProgress);
            }
        });
        this.setBackgroundColor(initColor);
    }

    public HeroWebView(Context context) {
        super(context);
    }

    public String getFromAssets(String fileName){
        String jsStr = "";
        try {
            InputStream in = getResources().getAssets().open(fileName);
            byte buff[] = new byte[1024];
            ByteArrayOutputStream fromFile = new ByteArrayOutputStream();
            do {
                int numRead = in.read(buff);
                if (numRead <= 0) {
                    break;
                }
                fromFile.write(buff, 0, numRead);
            } while (true);
            jsStr = fromFile.toString();
            in.close();
            fromFile.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return jsStr;
    }

    public static String inputStreamTOString(InputStream in) throws Exception{
        int BUFFER_SIZE = 1024;

        ByteArrayOutputStream outStream = new ByteArrayOutputStream();
        byte[] data = new byte[BUFFER_SIZE];
        int count = -1;
        while((count = in.read(data,0,BUFFER_SIZE)) != -1)
            outStream.write(data, 0, count);

        data = null;
        return new String(outStream.toByteArray(),"UTF-8");
    }


    @JavascriptInterface
    public void on(String jsonStr) {
        try {
            Object json = new JSONTokener(jsonStr).nextValue();
            if (parentFragment != null && parentFragment.getTag() != null) {
                if (json instanceof JSONObject) {
                    ((JSONObject) json).put(FRAGMENT_TAG_KEY, parentFragment.getTag());
                } else if (json instanceof JSONArray) {
                    JSONObject tag = new JSONObject();
                    tag.put(FRAGMENT_TAG_KEY, parentFragment.getTag());
                    ((JSONArray) json).put(tag);
                }
            }
            if (this.getContext() instanceof IHeroContext) {
                ((IHeroContext) this.getContext()).on(json);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @JavascriptInterface
    public void npc(String jsonStr) {
        Log.d("WebViewDebug","npc"+jsonStr);
        try {
            String str = jsonStr.replaceAll("heronpc://","");
            Log.d("WebViewDebug","npc"+str);
            String module = str.split("\\?")[0];
            Log.d("WebViewDebug","npc"+module);
            JSONObject json = (JSONObject)new JSONTokener(str.split("\\?")[1]).nextValue();
            if (modules == null){
                modules = new HashMap();
            }
            if (!modules.containsKey(module)){
                json.put("class",module);
                IHero m = HeroView.fromJson(this.getContext(),json);
                m.on(json);
                modules.put(module,m);
            }else{
                ((IHero)modules.get(module)).on(json);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }
    @Override
    public void loadUrl(String url) {
        if (BuildConfig.DEBUG && url.startsWith("http")) {
            this.setWebContentsDebuggingEnabled(true);
            if (url.contains("?")) {
                url = url + "&test=true";
            } else {
                url = url + "?test=true";
            }
        }
        mUrl = url;
        if (!isUrlAuthenticated(url)) {
            return;
        }
        if (injectHero){
            final String _url = url;
            final WebView _webview = this;
            new Thread() {
                public void run() {
                    try {
                        URL oracle = new URL(_url);
                        BufferedReader in = new BufferedReader(
                                new InputStreamReader(oracle.openStream()));
                        StringBuilder response = new StringBuilder();
                        String inputLine;
                        while ((inputLine = in.readLine()) != null)
                            response.append(inputLine+"\n");
                        in.close();
                        String _content = response.toString();
                        _content = _content.replaceAll("<head>","<head><script src='https://localhost:3000/hero-home/hero-provider.js'></script>");
                        _content = _content.replaceAll("Content-Security-Policy","Hero Web3 Provider");
                        final String  content = _content;
                        _webview.post(new Runnable() {
                            @Override
                            public void run() {
                                _webview.loadDataWithBaseURL(_url,content,"text/html","",null);
                            }
                        });
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }.start();
            return;
        }
        Map header = null;
        if (HeroApplication.getInstance() != null) {
            header = HeroApplication.getInstance().getExtraHttpHeader();
        }
        if (header != null && header.size() > 0) {
            super.loadUrl(url, header);
        } else {
            super.loadUrl(url);
        }
    }

    @Override
    public void on(JSONObject jsonObject) throws JSONException {
        HeroView.on(this, jsonObject);
        if (jsonObject.has("url")) {
            Object urlObject = jsonObject.get("url");
            if (urlObject instanceof JSONObject) {
                method = ((JSONObject) urlObject).optString("method");
                mUrl = ((JSONObject) urlObject).getString("url");
                postData = ((JSONObject) urlObject).optString("data");
                // if post data not null but method is not specified, think it as POST
                if (!TextUtils.isEmpty(postData) && TextUtils.isEmpty(method)) {
                    method = METHOD_POST;
                }
                if (METHOD_POST.equals(method)) {
                    byte data[] = TextUtils.isEmpty(postData) ? new byte[1] : postData.getBytes();
                    this.postUrl(mUrl, data);
                } else {
                    this.loadUrl(mUrl);
                }
            } else {
                mUrl = jsonObject.getString("url");
                this.loadUrl(mUrl);
            }
        }
        if (jsonObject.has("innerHtml")) {
            this.loadData(jsonObject.getString("innerHtml"), "text/html;charset=UTF-8", null);
        }
        if (jsonObject.has("hijackURLs")) {
            hijackUrlArray = jsonObject.optJSONArray("hijackURLs");
        }
    }

    @Override
    public void reload() {
        if (!TextUtils.isEmpty(postData) && METHOD_POST.equals(method) && !TextUtils.isEmpty(mUrl)) {
            this.postUrl(mUrl, postData.getBytes());
            return;
        }
        super.reload();
    }

    // if it's in the hijack list, return the object, else return null
    private JSONObject shouldHijackUrl(String url) throws JSONException {
        if (hijackUrlArray == null || hijackUrlArray.length() == 0) {
            return null;
        }
        for (int i = 0; i < hijackUrlArray.length(); i ++) {
            JSONObject item = hijackUrlArray.getJSONObject(i);
            if (TextUtils.equals(url, item.optString("url"))) {
                return item;
            }
        }
        return null;
    }

    public void setFragment(HeroFragment fragment) {
        parentFragment = fragment;
    }

    public void setInjectHero(boolean injectHero) {
        this.injectHero = injectHero;
    }

    private class MyWebViewDownLoadListener implements DownloadListener {

        @Override
        public void onDownloadStart(String url, String userAgent, String contentDisposition, String mimetype,
                                    long contentLength) {
            Uri uri = Uri.parse(url);
            Intent intent = new Intent(Intent.ACTION_VIEW, uri);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            getContext().startActivity(intent);
        }

    }

    private boolean isUrlAuthenticated(String url) {
        if (NEED_VERIFY_URL_HOST) {
            Context application = getContext().getApplicationContext();
            if (application instanceof HeroApplication) {
                String urlDomain = HeroApplication.getDomainAddress(url);
                String host = ((HeroApplication)application).getHomeAddress();
                String hostDomain = HeroApplication.getDomainAddress(host);
                if (urlDomain.equals(hostDomain)) {
                    return true;
                }
                return false;
            }
        }
        return true;
    }

    private void setWindowAttribute() {
        Context context = getContext();
        int scrHeightDp = HeroView.px2dip(context, HeroView.getScreenHeight(context));
        int scrWidthDp = HeroView.px2dip(context, HeroView.getScreenWidth(context));
        String script = String.format("window.deviceWidth=%d;window.deviceHeight=%d",scrWidthDp,scrHeightDp);
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            try {
                this.evaluateJavascript(script, null);
            } catch (IllegalStateException e) {
                this.loadUrl("javascript:" + script);
            }
        } else {
            this.loadUrl("javascript:" + script);
        }
    }
}
