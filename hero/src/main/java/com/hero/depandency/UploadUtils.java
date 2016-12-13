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

 * Neither the name Facebook nor the names of its contributors may be used to
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

package com.hero.depandency;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.webkit.CookieManager;

import com.hero.HeroApplication;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class UploadUtils {

    public static final int UPLOAD_FAIL_CODE = 0;
    public static final int UPLOAD_SUCCESS_CODE = 1;
    public static final int UPLOAD_FILE_NOT_EXISTS_CODE = 2;
    public static final int UPLOAD_SERVER_ERROR_CODE = 3;
    protected static final int WHAT_TO_UPLOAD = 1;
    protected static final int WHAT_UPLOAD_DONE = 2;
    private static UploadUtils uploadUtil;
    private static UploadListener uploadListener;
    private static int requestTime = 0;
    private int readTimeOut = 10 * 1000;
    private int connectTimeout = 10 * 1000;

    public static UploadUtils getInstance() {
        if (null == uploadUtil) {
            uploadUtil = new UploadUtils();
        }
        return uploadUtil;
    }



    public static String getDomainAddress(String url) {
        int index = url.indexOf("://");
        if (index != -1) {
            url = url.substring(index + "://".length());
        }
        index = url.indexOf("/");
        if (index != -1) {
            url = url.substring(0, index);
        }
        return url;
    }


    public static String getSessionId(String url) {
        if (!TextUtils.isEmpty(url)) {

            String domain = getDomainAddress(url);
            String cookie = CookieManager.getInstance().getCookie(domain);
            if (cookie != null) {
                return getSessionIdFromCookie(cookie);

            }
        }
        return null;
    }

    public static String getSessionIdFromCookie(String cookie) {
        String session;
        int index = cookie.indexOf("JSESSIONID=");
        if (index >= 0) {
            session = cookie.substring(index + "JSESSIONID=".length());
        } else {
            return null;
        }
        index = session.indexOf(";");
        if (index >= 0) {
            session = session.substring(0, index);
        }
        return session;
    }
    public static String uploadImage(String url, String filepath, String uploadName, UploadListener listener)
    {
        return uploadImage(url,filepath,uploadName,listener,getSessionId(url));
    }

    public static String uploadImage(String url, String filepath, String uploadName,  UploadListener listener, String jSessionId) {
        UploadListener uploadListener = listener;
        File file = new File(filepath);
        Log.i("UploadUtils", "url:" + url);
        Log.i("UploadUtils", "filepath:" + filepath);

        if (!file.exists() || url == null) {
            if (uploadListener != null) {
                uploadListener.onUploadFailed("upload not implemented", null);
            }
            return null;
        }

        MultipartEntityBuilder multipartEntityBuilder = MultipartEntityBuilder.create();
        multipartEntityBuilder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);
        multipartEntityBuilder.addBinaryBody(uploadName, file, ContentType.create("image/jpeg"), file.getName());
        HttpEntity entity = multipartEntityBuilder.build();

        String returnValue = null;
        String errorMsg = "upload failed";

        String result = executeHttpPost(url, multipartEntityBuilder, jSessionId, HeroApplication.getInstance().getHttpReferer(), uploadListener);

        JSONObject jsonObject = null;

        errorMsg = result;
        //        Log.i("UploadUtils", "upload result: " + result);
        if (result != null) {
            try {
                jsonObject = new JSONObject(result);
                if (jsonObject != null) {
                    if (jsonObject.has("result")) {
                        returnValue = jsonObject.getString("result");
                    }
                    if (jsonObject.has("errors")) {
                        Object error = jsonObject.get("errors");
                        if (error instanceof JSONArray && ((JSONArray) error).length() > 0) {
                            errorMsg = ((JSONArray) error).getString(0);
                        }
                    } else if (result.contains("ErrorMessage")) {
                        errorMsg = result;
                    }
                }
            } catch (JSONException e) {
                errorMsg = "";
                e.printStackTrace();
            }

            if ("success".equals(returnValue)) {
                // upload success
                if (uploadListener != null) {
                    uploadListener.onUploadSuccess(returnValue, jsonObject.optJSONObject("content"));
                }
                return result;
            }
        }

        if (uploadListener != null) {
            uploadListener.onUploadFailed(errorMsg, jsonObject);
        }
        return returnValue;
    }

    public static String uploadImage(String url, String filepath, String uploadName, List<NameValuePair> params, UploadListener listener, String jSessionId) {
        UploadListener uploadListener = listener;
        Log.i("UploadUtils", "url:" + url);
        Log.i("UploadUtils", "filepath:" + filepath);

        File file = new File(filepath);
        if (!file.exists() || url == null) {
            if (uploadListener != null) {
                uploadListener.onUploadFailed("upload not implemented", null);
            }
            return null;
        }
        MultipartEntityBuilder multipartEntityBuilder = MultipartEntityBuilder.create();
        multipartEntityBuilder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);
        multipartEntityBuilder.addBinaryBody(uploadName, file, ContentType.create("image/jpeg"), file.getName());
        ContentType contentType = ContentType.create(HTTP.PLAIN_TEXT_TYPE, HTTP.UTF_8);
        StringBody stringBody;
        for (NameValuePair i:params)
        {
            stringBody = new StringBody(i.getValue(),contentType);
            multipartEntityBuilder.addPart(i.getName(), stringBody);
        }


        HttpEntity entity = multipartEntityBuilder.build();

        String returnValue = null;
        String errorMsg = "upload failed";

        String result = executeHttpPost(url, multipartEntityBuilder, jSessionId, HeroApplication.getInstance().getHttpReferer(), uploadListener);
        JSONObject jsonObject = null;
        errorMsg = result;
        //        Log.i("UploadUtils", "upload result: " + result);
        if (result != null) {
            try {
                jsonObject = new JSONObject(result);
                if (jsonObject != null) {
                    if (jsonObject.has("result")) {
                        returnValue = jsonObject.getString("result");
                    }
                    if (jsonObject.has("errors")) {
                        Object error = jsonObject.get("errors");
                        if (error instanceof JSONArray && ((JSONArray) error).length() > 0) {
                            errorMsg = ((JSONArray) error).getString(0);
                        }
                    } else if (result.contains("ErrorMessage")) {
                        errorMsg = result;
                    }
                }
            } catch (JSONException e) {
                errorMsg = "";
                e.printStackTrace();
            }

            if ("success".equals(returnValue)) {
                // upload success
                if (uploadListener != null) {
                    uploadListener.onUploadSuccess(returnValue, jsonObject.optJSONObject("content"));
                }
                return result;
            }
        }

        if (uploadListener != null) {
            uploadListener.onUploadFailed(errorMsg, jsonObject);
        }
        return returnValue;
    }

    public static String uploadFiles(Context context, String url, JSONObject content, JSONArray files, UploadListener listener) {
        UploadListener uploadListener = listener;

        JSONObject jsonObject = null;
        String fileKey = null;
        if (content == null || url == null) {
            if (uploadListener != null) {
                uploadListener.onUploadFailed("upload not implemented", null);
            }
            return null;
        }

        MultipartEntityBuilder multipartEntityBuilder = MultipartEntityBuilder.create();
        multipartEntityBuilder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);
        Iterator it = content.keys();
        while (it.hasNext()) {
            String key = (String) it.next();
            try {
                String value = content.getString(key);
                if (value.equals("files")) {
                    fileKey = key;
                    continue;
                }
                if(key.equals("feedback"))
                {
                    try {
                        value=new String(value.getBytes("utf-8"));
                    }
                    catch (Exception e)
                    {
                        e.printStackTrace();
                    }
                }
                ContentType contentType = ContentType.create(HTTP.PLAIN_TEXT_TYPE, HTTP.UTF_8);
                StringBody stringBody = new StringBody(value,contentType);
                multipartEntityBuilder.addPart(key, stringBody);
                //                multipartEntityBuilder.addTextBody(key, value);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        if (!TextUtils.isEmpty(fileKey) && files != null) {
            for (int i = 0; i < files.length(); i++) {
                try {
                    String fileName = files.getString(i);
                    File file = new File(getImageFilePath(context) + "/" + fileName);
                    if (file.exists() && file.canRead()) {
                        multipartEntityBuilder.addBinaryBody(fileKey, file, ContentType.create("image/jpeg"), fileName);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }

        String returnValue = null;
        String errorMsg = "upload failed";

        String result = executeHttpPost(url, multipartEntityBuilder, null, HeroApplication.getInstance().getHttpReferer(), uploadListener);

        if (result != null) {
            try {
                jsonObject = new JSONObject(result);
                if (jsonObject != null) {
                    if (jsonObject.has("result")) {
                        returnValue = jsonObject.getString("result");
                    }
                    if (uploadListener != null) {
                        uploadListener.onUploadSuccess(returnValue, jsonObject);
                    }
                    return returnValue;
                }
            } catch (JSONException e) {
                errorMsg = e.getMessage();
                e.printStackTrace();
            }
        }
        if (uploadListener != null) {
            uploadListener.onUploadFailed(errorMsg, jsonObject);
        }
        return errorMsg;
    }


    public static String  executeHttpPost(String url, MultipartEntityBuilder builder, String sessionId, String referer, final UploadListener uploadListener) {
        String apiReturn = null;
        HttpClient client = ConnectionUtil.getHttpClient();
        HttpPost post = new HttpPost(url);
        HttpEntity entity = builder.build();

        final long totalSize = entity.getContentLength();

        ProgressHttpEntity progressHttpEntity = new ProgressHttpEntity(entity, new ProgressHttpEntity.ProgressListener() {
            @Override
            public void transferred(long transferredBytes, long total) {
                int process;
                if (uploadListener != null) {
                    process = (int) (transferredBytes * 100 / totalSize);
                    uploadListener.onUploadProcess("process", process);
                }

            }


        });


        String cookie = CookieManager.getInstance().getCookie(HeroApplication.getDomainAddress(url));
        if (!TextUtils.isEmpty(cookie)) {
            post.addHeader("Cookie", cookie);
        } else {
            if (sessionId != null) {
                post.addHeader("Cookie", "JSESSIONID=" + sessionId);
            }
        }
        post.addHeader(progressHttpEntity.getContentType());
        post.addHeader("accept", "*/*");
        addMapToHeader(post, HeroApplication.getInstance().getExtraHttpHeader());
        if (referer != null) {
            post.addHeader("referer", referer);
        }
        post.setEntity(progressHttpEntity);
        try {
            HttpResponse response = client.execute(post);
            int statusCode = response.getStatusLine().getStatusCode();
            String result = EntityUtils.toString(response.getEntity(), "utf-8");

            apiReturn = result;
            Log.i("UploadUtils", "upload result: " + result);
            if (statusCode == 200) {
                return apiReturn;
            }
        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();

        } catch (Exception e) {
            e.printStackTrace();

        }
        return apiReturn;
    }

    public static HttpRequestBase buildHttpBaseRequest(String type, final String url, final JSONObject params) {
        List<NameValuePair> paramList = new ArrayList<NameValuePair>();
        if (params != null && params.length() > 0) {
            try {
                Iterator it = params.keys();
                while (it.hasNext()) {
                    String key = (String) it.next();
                    String value = params.getString(key);
                    Log.i("UploadUtils",key+":"+value);
                    paramList.add(new BasicNameValuePair(key, value));

                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        if (type.equals(HttpGet.METHOD_NAME)) {
            String param = null;
            if (paramList.size() > 0) {
               param = URLEncodedUtils.format(paramList, "UTF-8");
            }
            return new HttpGet(url + ((param == null) ? "" : ("?" + param)));
        } else {
            HttpPost post = new HttpPost(url);
            try {
                post.setEntity(new UrlEncodedFormEntity(paramList, HTTP.UTF_8));
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            return post;
        }
    }

    public static void httpGetRequest(final String url, final Handler handler, final JSONObject params) {
        HttpRequestBase request = buildHttpBaseRequest(HttpGet.METHOD_NAME, url, params);
        baseRequest(request, url, handler);
    }

    public static void httpPostRequest(final String url, final Handler handler, final JSONObject params) {
        HttpRequestBase request = buildHttpBaseRequest(HttpPost.METHOD_NAME, url, params);
        baseRequest(request, url, handler);
    }

    private static void baseRequest(final HttpRequestBase request, final String url, final Handler handler) {
        new Thread() {
            @Override
            public void run() {
                HttpClient client = ConnectionUtil.getHttpClient();

                String cookie = CookieManager.getInstance().getCookie(HeroApplication.getDomainAddress(url));
                if (!TextUtils.isEmpty(cookie)) {
                    request.addHeader("Cookie", cookie);
                }
                request.addHeader("Content-Type", "application/x-www-form-urlencoded; charset=utf-8");
                request.addHeader("referer", HeroApplication.getInstance().getHttpReferer());
                addMapToHeader(request, HeroApplication.getInstance().getExtraHttpHeader());

                HttpResponse response = null;
                try {
                    response = client.execute(request);
                } catch (IOException e) {
                    e.printStackTrace();
                    Message msg = handler.obtainMessage(UPLOAD_FAIL_CODE);
                    msg.sendToTarget();
                    return;
                }

                String result = null;
                try {
                    result = EntityUtils.toString(response.getEntity(), "utf-8");
                    int statusCode = response.getStatusLine().getStatusCode();
                    JSONObject jsonObject;

                    if (statusCode == 200) {
                        int code = UPLOAD_FAIL_CODE;
                        jsonObject = new JSONObject(result);
                        String returnValue;
                        if (jsonObject != null) {
                            if (jsonObject.has("errors")) {
                                Object error = jsonObject.get("errors");
                                if (error instanceof JSONArray && ((JSONArray) error).length() > 0) {
                                    String errorMsg = ((JSONArray) error).getString(0);
                                    code = UPLOAD_SERVER_ERROR_CODE;
                                }
                            }
                            if (jsonObject.has("result")) {
                                returnValue = jsonObject.getString("result");
                                if ("success".equals(returnValue)) {
                                    code = UPLOAD_SUCCESS_CODE;
                                }
                            }
                        }
                        Message msg = handler.obtainMessage(code, jsonObject);
                        msg.sendToTarget();
                    } else {
                        Message msg = handler.obtainMessage(UPLOAD_FAIL_CODE);
                        msg.sendToTarget();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }.start();
    }

    public void addCookies(Context context, java.net.CookieManager manager, String url) {
        String webCookie;
        webCookie = ((HeroApplication) context.getApplicationContext()).getCookieManager().getCookie(url);

        Map<String, List<String>> cookies = new java.util.HashMap<String, List<String>>();
        if (webCookie != null) {
            cookies.put("Cookie", Arrays.asList(webCookie));
            try {
                manager.put(new URI(url), cookies);
            } catch (IOException e) {
                e.printStackTrace();
            } catch (URISyntaxException e) {
                e.printStackTrace();
            }
        }
    }

    public static void addMapToHeader(HttpRequestBase req, Map headerMap) {
        if (!(req == null || headerMap == null || headerMap.size() == 0)) {
            Iterator iter = headerMap.entrySet().iterator();
            while (iter.hasNext()) {
                Map.Entry entry = (Map.Entry) iter.next();
                Object key = entry.getKey();
                Object val = entry.getValue();
                req.addHeader((String) key, (String) val);
            }
        }
    }

    public static interface UploadListener {
        void onUploadSuccess(String message, Object apiReturn);
        void onUploadProcess(String message, Object apiReturn);
        void onUploadFailed(String message, Object apiReturn);
    }

    public static String getImageFilePath(Context context) {
        return ContextUtils.getLocalPath(context) + "/Images";
    }
}