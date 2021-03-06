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

import android.Manifest;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.webkit.DownloadListener;
import android.webkit.JavascriptInterface;
import android.webkit.MimeTypeMap;
import android.webkit.PermissionRequest;
import android.webkit.WebChromeClient;
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
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
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
    private boolean pause = false;
    private HashMap modules;
    private String providerUrl = "https://mainnet.infura.io/33USgHxvCp3UoDItBSRs";
//    private String providerUrl = "https://ropsten.infura.io/v3/719be1b239a24d1e87a2e326be6c4384";
//    private String providerUrl = "http://localhost:8545";
    public HeroWebView(Context context, int initColor, final boolean injectHero) {
        this(context);
        this.setInjectHero(injectHero);
        this.setBackgroundColor(initColor);
    }

    public HeroWebView(Context context) {
        super(context);
        try {
            this.getSettings().setJavaScriptEnabled(true);
            this.getSettings().setJavaScriptCanOpenWindowsAutomatically(true);
        } catch (Exception e) {
            e.printStackTrace();
        }
        this.addJavascriptInterface(this, "native");
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
            @TargetApi(Build.VERSION_CODES.LOLLIPOP)
            @RequiresApi(api = Build.VERSION_CODES.KITKAT)
            @Nullable
            @Override
            public WebResourceResponse shouldInterceptRequest(WebView view, final WebResourceRequest request) {
                if (pause) return super.shouldInterceptRequest(view, request);
                String url = request.getUrl().toString();
                if (url.startsWith("https://localhost:3000") ||  url.startsWith("http://localhost:3000")){
                    try {
                        InputStream in = getResources().getAssets().open(request.getUrl().getPath().substring(1));
                        BufferedReader br = new BufferedReader(new InputStreamReader(in));
                        StringBuilder response = new StringBuilder();
                        String inputLine;
                        while ((inputLine = br.readLine()) != null)
                            response.append(inputLine+"\n");
                        in.close();
                        String _content = response.toString();
                        _content = _content.replaceAll("https://localhost:3001",providerUrl);
                        InputStream newIn = new ByteArrayInputStream(_content.getBytes(StandardCharsets.UTF_8));
                        WebResourceResponse resourceResponse = new WebResourceResponse(
                                MimeTypeMap.getSingleton().getMimeTypeFromExtension(MimeTypeMap.getFileExtensionFromUrl(url))
                                , "UTF-8", newIn);
                        return resourceResponse;
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }else if(injectHero ){
                    if (url.endsWith("js") || url.endsWith("css") || url.endsWith("png") || url.endsWith("jpg") || url.endsWith("jpeg")|| url.endsWith("svg") || url.endsWith("jpeg")|| url.endsWith("ico")|| url.endsWith("tiff")|| url.endsWith("ttf")|| url.endsWith("json")|| url.endsWith("gif")|| url.endsWith("woff2")){
                        return super.shouldInterceptRequest(view, request);
                    }
                    if (mUrl.split(request.getUrl().getHost()).length <= 1){
                        return super.shouldInterceptRequest(view, request);
                    }
                    URL contentUrl;
                    try {
                        contentUrl = new URL(url);
                        InputStream is = contentUrl.openStream();
                        InputStreamReader ir = new InputStreamReader(is);
                        BufferedReader br = new BufferedReader(ir);
                        StringBuilder response = new StringBuilder();
                        String inputLine;
                        while ((inputLine = br.readLine()) != null)
                            response.append(inputLine+"\n");
                        br.close();
                        String _content = response.toString();
                        _content = _content.replaceAll("<head>","<head><script src='https://localhost:3000/hero-home/hero-provider.js'></script>");
                        _content = _content.replaceAll("Content-Security-Policy","Hero Web3 Provider");
                        InputStream stream = new ByteArrayInputStream(_content.getBytes(StandardCharsets.UTF_8));
                        WebResourceResponse resourceResponse = new WebResourceResponse(
                                "text/html"
                                , "UTF-8", stream);
                        return resourceResponse;
                    } catch (MalformedURLException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
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
                if (failingUrl.equalsIgnoreCase(mUrl)){
                    InputStream page404 = getResources().openRawResource(R.raw.page_404);
                    String content = "{}" ;
                    if (page404 != null) {
                        try {
                            content = inputStreamTOString(page404);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    try {
                        JSONObject object = new JSONObject(content);
                        HeroView.sendActionToContext(getContext(), object);
                        object = new JSONObject("{common:'webViewDidFinishLoad'}");
                        HeroView.sendActionToContext(getContext(), object);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                setWindowAttribute();
                HeroFragment.evaluateJavaScript(view, HeroFragment.VIEW_WILL_APPEAR_EXPRESSION);
                try {
                    JSONObject object = new JSONObject("{command:'webViewDidFinishLoad'}");
                    HeroView.sendActionToContext(getContext(), object);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
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
            public void onPermissionRequest(final PermissionRequest request) {
                if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.CAMERA)
                        != PackageManager.PERMISSION_GRANTED
                        || ContextCompat.checkSelfPermission(getContext(), Manifest.permission.RECORD_AUDIO)
                        != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions((HeroActivity)getContext(), new String[]{Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO},
                            100);
                } else {
                    request.grant(request.getResources());
                }
                if (myHandler != null) {
                    myHandler.setPermissionRequest(request);
                } else {
                    myHandler = new MyHandler(request);
                }
            }

            @Override
            public void onPermissionRequestCanceled(PermissionRequest request) {
                super.onPermissionRequestCanceled(request);
            }



            @Override
            public void onReceivedTitle(WebView view, String title) {
                super.onReceivedTitle(view, title);
                if (view.getParent() != null && parentFragment != null) {
                    if (!TextUtils.isEmpty(title) && !title.contains(".html")) {
                        parentFragment.setNavigationBarHidden(true);
                        parentFragment.setTitle(title);
                    }
                }
            }
        });
    }

    public static MyHandler myHandler;

    public class MyHandler extends Handler {

        PermissionRequest permissionRequest;

        MyHandler (PermissionRequest permissionRequest) {
            this.permissionRequest = permissionRequest;
        }

        public PermissionRequest getPermissionRequest() {
            return permissionRequest;
        }

        public void setPermissionRequest(PermissionRequest permissionRequest) {
            this.permissionRequest = permissionRequest;
        }

        @Override
        public void handleMessage(Message msg) {

            switch (msg.what) {
                case 1:
                    permissionRequest.grant(permissionRequest.getResources());
                    break;
            }
            super.handleMessage(msg);
        }
    }

    public void pause(){
        pause = true;
        this.onPause();
    }
    public void resume(){
        pause = false;
        this.onResume();
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
        in.close();
        return new String(outStream.toByteArray(),"UTF-8");
    }


    @JavascriptInterface
    public void on(String jsonStr) {
        if (this.pause) return;
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
        if (this.pause) return;
        try {
            String str = jsonStr.replaceAll("heronpc://","");
            String module = str.split("\\?")[0];
            JSONObject json = (JSONObject)new JSONTokener(str.split("\\?")[1]).nextValue();
            if (modules == null){
                modules = new HashMap();
            }
            if (!modules.containsKey(module)){
                json.put("class",module);
                IHero m = HeroView.fromJson(this.getContext(),json);
                modules.put(module,m);
            }
            final String fModule = module;
            final JSONObject fJson = json;
            this.post(new Runnable() {
                @Override
                public void run() {
                    try {
                        ((IHero)modules.get(fModule)).on(fJson);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            });
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
        url = url.startsWith("http")?url :"http://"+url;
        if (BuildConfig.DEBUG) {
            this.setWebContentsDebuggingEnabled(true);
            if (url.contains("?")) {
                url = url + "&test=true";
            } else {
                url = url + "?test=true";
            }
        }
        if (!isUrlAuthenticated(url)) {
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
        mUrl = this.getUrl();

    }
    @Override
    public void on(JSONObject jsonObject) throws JSONException {
        if (this.pause) return;
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
