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
import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;
import android.graphics.Xfermode;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.support.v4.content.FileProvider;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Toast;

import com.hero.depandency.BitmapUtils;
import com.hero.depandency.ContextUtils;
import com.hero.depandency.ImageLoadUtils;
import com.hero.depandency.IntentResolver;
import com.hero.depandency.MPermissionUtils;
import com.hero.depandency.MyFileUtils;
import com.hero.depandency.TouchImageView;
import com.hero.depandency.UploadUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static android.app.Activity.RESULT_OK;

/**
 * Created by liuguoping on 15/9/23.
 */
public class HeroImageView extends ImageView implements IHero, HeroFragmentActivity.IRequestView {

    public static final int BIG_FILE_SIZE = 1 * 1024 * 1024; // 1M
    public static final int BIG_IMAGE_QUALITY = 60; // %60
    public static final int LARGE_FILE_SIZE = 2 * 1024 * 1024; // 2M
    public static final int LARGE_IMAGE_QUALITY = 40; // %40
    public static final long LARGE_IMAGE_SIZE = 1024 * 1024 * 6;
    public static final int DELETE_ANIME_DURATION = 200;
    private static final int ITEM_CAMERA = 0;
    private static final int ITEM_GALLERY = 1;
    private static final String TEMP_FILE_NAME = "dr_tmp.jpg";
    public static boolean needCrop = false;
    private String localImageName;
    private String uploadUrl;
    private String localFilePath;
    private Bitmap bitmap;
    private Uri pickUri;
    private ProgressDialog progressDialog;
    private String sessionId;
    private JSONObject localImageReadyObj;
    private JSONObject deleteObject;
    private JSONObject imageDataObject;
    private String uploadName;
    private String imageExtension = "";//.png";
    private boolean showDelete = false;
    private boolean showBig = false;
    private static final int DELETE_ICON_ID = R.drawable.delete;
    private ImageView deleteView;
    private int compressQuality = 0;
    private Dialog bigImageDialog;
    private Bitmap bigBitmap;
    private boolean needLoadingImageNative = false;

    private AnimationDrawable anim;

    public static final int MESSAGE_UPLOAD_SUCCESS = 0;
    public static final int MESSAGE_UPLOAD_FAIL = 1;
    public static final int MESSAGE_FILE_DELETED = 10;

    // for rounded corner image view
    private static final int DEFAULT_BORDER_COLOR = Color.WHITE;
    private Paint paint;
    private int borderColor;
    private int borderWidth = -1;
    private int roundWidth = -1;
    private RectF drawRect;
    private Path drawPath;

    private HeroFragmentActivity mHost;

    private Handler refreshHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {

            if (msg.what == MESSAGE_UPLOAD_SUCCESS) {
                hideProgressDialog();
                if (isLocalFileExists(localImageName)) {
                    openImageForDisplay(localImageName);
                    HeroImageView.this.setScaleType(ScaleType.FIT_XY);
                    HeroImageView.this.setBackgroundColor(getResources().getColor(android.R.color.transparent));
                    //                    HeroImageView.this.setOnClickListener(null);
                    if (showDelete) {
                        addDeleteView(true);
                    }
                    if (showBig) {
                        prepareBigImageDialog(getLocalFilePath(localImageName));
                    }
                    if (localImageReadyObj != null) {
                        try {
                            HeroView.putValueToJson(localImageReadyObj, true);
                            ((IHeroContext) getContext()).on(localImageReadyObj);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }
            } else if (msg.what == MESSAGE_UPLOAD_FAIL) {
                String error = (String) msg.obj;
                hideProgressDialog();
                if (isLocalFileExists(localImageName)) {
                    MyFileUtils.deleteFile(getLocalFilePath(localImageName));
                }
                if (!TextUtils.isEmpty(error)) {
                    JSONObject toast = new JSONObject();
                    try {
                        toast.put("name", "toast");
                        toast.put("text", error);
                        JSONObject data = new JSONObject();
                        data.put("datas", toast);
                        ((IHeroContext) getContext()).on(data);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                } else {
                    Toast.makeText(getContext(), R.string.upload_file_fail, Toast.LENGTH_SHORT).show();
                }
            } else if (msg.what == MESSAGE_FILE_DELETED) {
                if (!TextUtils.isEmpty(localImageName)) {
                    JSONObject object = new JSONObject();
                    try {
                        object.put("localImage", localImageName);
                        object.put("isDeleted", true);
                        HeroImageView.this.on(object);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }

            }
            super.handleMessage(msg);
        }
    };

    public HeroImageView(Context context) {
        super(context);
        init();
    }

    public HeroImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public HeroImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public HeroImageView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    private void init() {
        if (getContext() instanceof HeroFragmentActivity){
            mHost = (HeroFragmentActivity)getContext();
        }
        if (getId() == NO_ID){
            setId(IntentResolver.generateViewId());
        }
        this.setScaleType(ScaleType.FIT_XY);
        localFilePath = UploadUtils.getImageFilePath(getContext());
    }


    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        recycleBitmapIfNeeded(bitmap);
        bitmap = null;
        recycleBitmapIfNeeded(bigBitmap);
        bigBitmap = null;
        hideProgressDialog();
    }

    @Override
    public void on(JSONObject jsonObject) throws JSONException {
        HeroView.on(this, jsonObject);
        this.setAdjustViewBounds(false);

        if (jsonObject.has("cornerRadius")) {
            initRoundedCorner(jsonObject);
        }
        if (jsonObject.has("showBig")) {
            showBig = true;
        }
        if (jsonObject.has("image")) {
            String url = jsonObject.getString("image");
            if (url.startsWith("http")) {
                Log.i("HeroImageView", "load url " + jsonObject.getString("image"));
                if (showBig) {
                    prepareBigImageDialog(url);
                }
                //                if (url.startsWith("https")) {
                ImageLoadUtils.LoadImage(this, url);
            } else {
                if (needLoadingImageNative) {
                    ImageLoadUtils.loadLocalImageNative(this, url);
                } else {
                    ImageLoadUtils.loadLocalImage(this, url);
                }
            }
        } else if (jsonObject.has("base64image")) {
            String data = jsonObject.getString("base64image");
            if (data.startsWith("http")) {
                ImageLoadUtils.LoadBase64Image(getContext(), this, data);
            } else if (data.startsWith("data:")) {
                String regEx = "^data:image/\\w+;base64,";
                Pattern pat = Pattern.compile(regEx);
                Matcher mat = pat.matcher(data);
                String base64Data = mat.replaceFirst("");
                setLoadedImageBitmap(BitmapUtils.decodeBitmapFromBase64(base64Data));
            }
        }

        if (jsonObject.has("localImageReady")) {
            localImageReadyObj = jsonObject.getJSONObject("localImageReady");
        }
        if (jsonObject.has("getImageData")) {
            imageDataObject = jsonObject;
            String value = jsonObject.getString("getImageData");
            try {
                float percentage = Float.parseFloat(value);
                compressQuality = (int) (100 * percentage);
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        if (imageDataObject != null) {
                            //                            recompressImage(new File(getLocalFilePath(localImageName)), compressQuality);
                            try {
                                HeroView.putValueToJson(imageDataObject, MyFileUtils.encodeFileToBase64(getLocalFilePath(localImageName)));
                                ((IHeroContext) getContext()).on(imageDataObject);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }).start();
            } catch (NumberFormatException e) {
                e.printStackTrace();
            }
        }
        if (jsonObject.has("localImage")) {
            localImageName = jsonObject.getString("localImage") + imageExtension;
            if (!openImageForDisplay(localImageName)) {
                //                ((IHeroContext) getContext()).setImagePickHandler(this);
//                pickUri = Uri.fromFile(getTempFile());
                if (jsonObject.has("isDeleted")) {
                    Animation animation = new AlphaAnimation(1.0f, 0.1f);
                    animation.setDuration(DELETE_ANIME_DURATION);
                    animation.setAnimationListener(new Animation.AnimationListener() {
                        @Override
                        public void onAnimationStart(Animation animation) {

                        }

                        @Override
                        public void onAnimationEnd(Animation animation) {
                            setImageResource(R.drawable.ic_pick_image);
                            setScaleType(ScaleType.FIT_CENTER);
                            setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    buildImageDialog();
                                    //                        Toast.makeText(getContext(), "name:" + name, Toast.LENGTH_SHORT).show();
                                }
                            });
                            if (deleteObject != null) {
                                try {
                                    ((IHeroContext) getContext()).on(deleteObject);
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                        }

                        @Override
                        public void onAnimationRepeat(Animation animation) {

                        }
                    });
                    startAnimation(animation);
                } else {
                    this.setImageResource(R.drawable.ic_pick_image);
                    this.setScaleType(ScaleType.FIT_CENTER);
                    this.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            buildImageDialog();
                            //                        Toast.makeText(getContext(), "name:" + name, Toast.LENGTH_SHORT).show();
                        }
                    });
                    if (localImageReadyObj != null) {
                        HeroView.putValueToJson(localImageReadyObj, false);
                        ((IHeroContext) getContext()).on(localImageReadyObj);
                    }
                }
            } else {
                if (showBig) {
                    prepareBigImageDialog(getLocalFilePath(localImageName));
                }
                if (localImageReadyObj != null) {
                    HeroView.putValueToJson(localImageReadyObj, true);
                    ((IHeroContext) getContext()).on(localImageReadyObj);
                }
            }
        }

        if (jsonObject.has("localImageDelete")) {
            showDelete = true;
            deleteObject = jsonObject.getJSONObject("localImageDelete");
            if (showDelete) {
                addDeleteView(isLocalFileExists(localImageName));
            }
        }

        if (jsonObject.has("click")) {
            this.setTag(R.id.kAction, jsonObject.get("click"));
            this.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    try {
                        Object click = view.getTag(R.id.kAction);
                        if (click != null) {
                            ((IHeroContext) view.getContext()).on(click);
                            HeroFragment.hideSoftKeyboard(view.getContext());
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
        }

        if (jsonObject.has("uploadName")) {
            uploadName = jsonObject.getString("uploadName");
        }


        if (jsonObject.has("getImage")) {
            if (!TextUtils.isEmpty(jsonObject.getString("getImage"))) {
                localImageName = jsonObject.getString("getImage") + imageExtension;
            } else {
                localImageName = System.currentTimeMillis() + ".jpg";
            }
//            pickUri = Uri.fromFile(getTempFile());
            buildImageDialog();
        }

        if (jsonObject.has("uploadUrl")) {
            uploadUrl = jsonObject.getString("uploadUrl");
        }

        if (jsonObject.has("hidden")) {
            if (deleteView != null) {
                if (isLocalFileExists(localImageName)) {
                    deleteView.setVisibility(jsonObject.getBoolean("hidden") ? GONE : VISIBLE);
                } else {
                    deleteView.setVisibility(GONE);
                }
            }
        }

        if (jsonObject.has("frame") || jsonObject.has("center")) {
            if (deleteView != null) {
                moveDeleteView();
            }
        }
    }

    public int getResId(String name, String defType) {
        String packageName = getContext().getApplicationInfo().packageName;
        return getContext().getResources().getIdentifier(name, defType, packageName);
    }

    private boolean openImageForDisplay(String path) {
        String absolutePath = getLocalFilePath(path);
        File file = new File(absolutePath);
        if (!file.exists() || !file.canRead()) {
            return false;
        }
        int width, height;
        width = this.getWidth();
        height = this.getHeight();
        if (width == 0 || height == 0) {
            width = this.getLayoutParams().width;
            height = this.getLayoutParams().height;
        }
        recycleBitmapIfNeeded(bitmap);
        bitmap = MyFileUtils.decodeFile(absolutePath, true, width, height);
        if (bitmap != null) {
            this.setImageBitmap(bitmap);
            return true;
        }
        return false;
    }

    public void setLoadedImageBitmap(Bitmap bm) {
        if (bm != null) {
            bitmap = bm;
            setImageBitmap(bm);
        }
    }

    private void buildImageDialog() {
        if (!MPermissionUtils.checkAndRequestPermission(getContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE, MPermissionUtils.HERO_PERMISSION_SDCARD)) {
            return;
        }
        new AlertDialog.Builder(getContext()).setTitle(R.string.imageSelectDialogTitle).setItems(R.array.imageSelectDialogItems, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                String[] items = getResources().getStringArray(R.array.imageSelectDialogItems);
                if (ITEM_CAMERA == which) {
                    takePhoto();
                } else if (ITEM_GALLERY == which) {
                    pickPhoto();
                }
                dialog.dismiss();
            }
        }).create().show();
    }

    private Uri getTempFileUri(Intent intent,File file)
    {
        Uri uri;
        int currentapiVersion = android.os.Build.VERSION.SDK_INT;
        if (currentapiVersion<24){
            uri=Uri.fromFile(file);
        }else {
            uri = FileProvider.getUriForFile(getContext(), getContext().getPackageName()+".fileprovider", file);
            List<ResolveInfo> resInfoList= getContext().getPackageManager().queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);
            for (ResolveInfo resolveInfo : resInfoList) {
                String packageName = resolveInfo.activityInfo.packageName;
                getContext().grantUriPermission(packageName, uri,
                        Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
            }
        }
        return uri;
    }
    private void takePhoto() {
        if (!requestCameraPermission()) {
            return;
        }
        Intent intent = new Intent("android.media.action.IMAGE_CAPTURE");
        pickUri=getTempFileUri(intent,getTempFile());
        intent.putExtra(MediaStore.EXTRA_OUTPUT, pickUri);
        if (mHost != null) {
            mHost.startActivityForResult(this, intent, IntentResolver.REQUEST_CODE_CAMERA);
        }
    }

    private void pickPhoto() {
        Intent intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*");
        if (needCrop) {
            pickUri=getTempFileUri(intent,getTempFile());
            intent.putExtra("crop", "true");
            intent.putExtra("return-data", false);
            intent.putExtra("scale", true);
            intent.putExtra("noFaceDetection", true);
            intent.putExtra(MediaStore.EXTRA_OUTPUT, pickUri);
            intent.putExtra("outputFormat", Bitmap.CompressFormat.JPEG.toString());
        }
        if (mHost != null) {
            mHost.startActivityForResult(this, intent, IntentResolver.REQUEST_CODE_GALLERY);
        }
    }

    private void cropPhoto(String path) {
        Intent intent = new Intent("com.android.camera.action.CROP");
        pickUri=getTempFileUri(intent, new File(path));
        intent.setDataAndType(pickUri, "image/*");
        intent.putExtra("crop", "true");
        intent.putExtra("outputFormat", Bitmap.CompressFormat.JPEG.toString());
        intent.putExtra("noFaceDetection", true);
        intent.putExtra("output", pickUri);
        if (mHost != null) {
            mHost.startActivityForResult(this, intent, IntentResolver.REQUEST_CODE_PHOTO_CROP);
        }

    }

    public void handlePickResult(int type, String path) {
        // data not used currently
        if (needCrop && IntentResolver.REQUEST_CODE_CAMERA == type) {
            if (path != null) {
                cropPhoto(path);
            }
            return;
        }
        if (path != null) {
            final File sourceFile = new File(path);
            showProgressDialog();
            if (sourceFile.exists() && sourceFile.canRead()) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        String fileName = uploadName;
                        if (TextUtils.isEmpty(fileName)) {
                            fileName = localImageName;
                        }
                        File targetFile = recompressImage(sourceFile, getTempFile(), compressQuality);
                        MyFileUtils.copyFile(getContext(), targetFile.getAbsolutePath(), localImageName);
                        // not need to upload
                        if (TextUtils.isEmpty(uploadUrl)) {
                            deleteTempFile();
                            Message message = refreshHandler.obtainMessage();
                            message.what = MESSAGE_UPLOAD_SUCCESS;
                            refreshHandler.sendMessage(message);
                            return;
                        }
                        UploadUtils uploader = UploadUtils.getInstance();
                        uploader.uploadImage(uploadUrl, targetFile.getAbsolutePath(), fileName, new UploadUtils.UploadListener() {
                            @Override
                            public void onUploadSuccess(String msg, Object obj) {
                                //                                hideProgressDialog();
                                Message message = refreshHandler.obtainMessage();
                                message.what = MESSAGE_UPLOAD_SUCCESS;
                                refreshHandler.sendMessage(message);
                            }

                            @Override
                            public void onUploadProcess(String message, Object apiReturn) {

                            }

                            @Override
                            public void onUploadFailed(String error, Object object) {
                                //                                hideProgressDialog();
                                Message message = refreshHandler.obtainMessage();
                                message.what = MESSAGE_UPLOAD_FAIL;
                                message.obj = error;
                                refreshHandler.sendMessage(message);
                            }
                        }, null);
                        deleteTempFile();
                    }
                }).start();
            }
        }
    }

    private void deleteTempFile() {
        File tmpFile = getTempFile();
        if (tmpFile.exists()) {
            tmpFile.delete();
        }
    }

    private File getTempFile() {
        return new File(ContextUtils.getExternalPath() + "/" + TEMP_FILE_NAME);
    }

    private void showProgressDialog() {
        if (progressDialog == null) {
            progressDialog = new ProgressDialog(getContext());
            progressDialog.setIndeterminate(true);
            progressDialog.setCancelable(false);
        }
        progressDialog.show();
        progressDialog.setContentView(R.layout.layout_progress_dialog);
    }

    private void hideProgressDialog() {
        if (progressDialog != null) {
            progressDialog.dismiss();
        }
    }

    private File recompressImage(File file, File targetFile, int defQuality) {
        int quality = BIG_IMAGE_QUALITY;
        Bitmap bmp = null;
        if (file != null) {
            if (file.exists()) {
                if (file.length() < BIG_FILE_SIZE) {
                    return file;
                }
            }
            quality = file.length() > LARGE_FILE_SIZE ? LARGE_IMAGE_QUALITY : BIG_IMAGE_QUALITY;
            BitmapFactory.Options options = null;
            long imageSize = BitmapUtils.getBitmapResolution(file.getAbsolutePath());
            if (imageSize >= LARGE_IMAGE_SIZE) {
                options = new BitmapFactory.Options();
                options.inSampleSize = BitmapUtils.getSuitableSampleSize(imageSize, LARGE_FILE_SIZE);
                options.inDither = false;
                // since the sample size has been cut, let the quality higher
                quality = BIG_IMAGE_QUALITY;
            }
            try {
                bmp = BitmapFactory.decodeFile(file.getAbsolutePath(), options);
            } catch (Exception e) {
                e.printStackTrace();
            }
            //                bmp = BitmapFactory.decodeFile(file.getPath(), options);
        }
        if (bmp == null) return file;
        FileOutputStream fileOutStream = null;
        if (defQuality != 0) {
            quality = defQuality;
        }
        try {
            if (targetFile.exists()) {
                targetFile.delete();
            }
            fileOutStream = new FileOutputStream(targetFile);
            bmp.compress(Bitmap.CompressFormat.JPEG, quality, fileOutStream);
            fileOutStream.flush();
            fileOutStream.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            recycleBitmapIfNeeded(bmp);
        }
        return targetFile;
    }

    private String getLocalFilePath(String fileName) {
        return (localFilePath + "/" + fileName);
    }

    private boolean isLocalFileExists(String fileName) {
        if (TextUtils.isEmpty(fileName)) {
            return false;
        }
        return new File(getLocalFilePath(fileName)).exists();
    }

    private void prepareBigImageDialog(final String path) {
        this.setOnClickListener(new OnClickListener() {
            public void onClick(View paramView) {
                TouchImageView image;
                if (bigImageDialog == null) {
                    LayoutInflater inflater = LayoutInflater.from(getContext());
                    View imageLayout = inflater.inflate(R.layout.big_image_dialog, null);
                    bigImageDialog = new Dialog(getContext(), R.style.ImageDialog);
                    image = (TouchImageView) imageLayout.findViewById(R.id.image);

                    bigImageDialog.setContentView(imageLayout);
                    bigImageDialog.setCanceledOnTouchOutside(true);
                    bigImageDialog.setCancelable(true);
                    image.setOnClickListener(new OnClickListener() {
                        public void onClick(View paramView) {
                            recycleBitmapIfNeeded(bigBitmap);
                            bigBitmap = null;
                            bigImageDialog.cancel();
                        }
                    });
                } else {
                    image = (TouchImageView) bigImageDialog.findViewById(R.id.image);
                }
                if (image != null) {
                    image.resetZoom();
                    if (path.startsWith("http")) {
                        ImageLoadUtils.LoadImage(image, path);
                    } else {
                        recycleBitmapIfNeeded(bigBitmap);
                        bigBitmap = BitmapUtils.decodeSampledBitmapFromFile(path, HeroView.getScreenWidth(getContext()), HeroView.getScreenHeight(getContext()));
                        image.setImageBitmap(bigBitmap);
                    }
                }
                bigImageDialog.show();
            }
        });
    }

    private void addDeleteView(boolean show) {
        int width, height;
        if (deleteView != null && deleteView.getParent() != null) {
            if (show) {
                deleteView.setVisibility(VISIBLE);
            } else {
                deleteView.setVisibility(GONE);
            }
            return;
        }
        if (deleteView == null) {
            deleteView = new ImageView(getContext());
        }
        ViewGroup.LayoutParams params = this.getLayoutParams();
        FrameLayout.LayoutParams deleteParams = new FrameLayout.LayoutParams(0, 0);
        Drawable drawable = getResources().getDrawable(DELETE_ICON_ID);
        //        width = drawable.getIntrinsicWidth();
        //        height = drawable.getIntrinsicHeight();
        //        width = Math.min(width, MAX_DELETE_ICON_WIDTH);
        //        height = Math.min(height, MAX_DELETE_ICON_WIDTH);
        width = (int) getResources().getDimension(R.dimen.max_delete_button_width);
        height = (int) getResources().getDimension(R.dimen.max_delete_button_width);
        if (params instanceof FrameLayout.LayoutParams) {
            deleteParams.topMargin = ((FrameLayout.LayoutParams) params).topMargin - height / 2;
            deleteParams.leftMargin = ((FrameLayout.LayoutParams) params).leftMargin + params.width - width / 2;
            deleteParams.width = width;
            deleteParams.height = height;
        }
        deleteView.setImageResource(DELETE_ICON_ID);
        deleteView.setScaleType(ScaleType.FIT_CENTER);
        ViewGroup parent = (ViewGroup) this.getParent();
        parent.addView(deleteView, deleteParams);
        if (show) {
            deleteView.setVisibility(VISIBLE);
        } else {
            deleteView.setVisibility(GONE);
        }
        deleteView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                deleteLocalImage();
            }
        });
    }

    private void moveDeleteView() {
        int width, height;
        ViewGroup.LayoutParams params = this.getLayoutParams();
        FrameLayout.LayoutParams deleteParams = (FrameLayout.LayoutParams) deleteView.getLayoutParams();
        width = deleteParams.width;
        height = deleteParams.height;
        if (params instanceof FrameLayout.LayoutParams) {
            deleteParams.topMargin = ((FrameLayout.LayoutParams) params).topMargin - height / 2;
            deleteParams.leftMargin = ((FrameLayout.LayoutParams) params).leftMargin + params.width - width / 2;
        }
        deleteView.setLayoutParams(deleteParams);
    }

    private void removeDeleteView() {
        if (deleteView != null) {
            deleteView.setVisibility(GONE);
        }
    }

    private void deleteLocalImage() {
        if (new File(getLocalFilePath(localImageName)).exists()) {
            MyFileUtils.deleteFile(getLocalFilePath(localImageName));
        }
        removeDeleteView();
        Message message = refreshHandler.obtainMessage();
        message.what = MESSAGE_FILE_DELETED;
        refreshHandler.sendMessage(message);
    }

    private void recycleBitmapIfNeeded(Bitmap bitmap1) {
        if (bitmap1 != null && !bitmap1.isRecycled()) {
            bitmap1.recycle();
        }
    }

    /* we use imageLoader to load local images for better memory management, but imageLoader
       takes a few time on the first loading. In some cases we want the image to be shown
       immediately, such as the welcome screen. So please set the flag true to avoid using
       imageLoader.
    */
    public void setNeedLoadingImageNative(boolean isNative) {
        needLoadingImageNative = isNative;
    }

    // rounded corner functions
    private void initRoundedCorner(JSONObject jsonObject) {
        setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        borderColor = DEFAULT_BORDER_COLOR;
        if (jsonObject != null) {
            try {
                if (jsonObject.has("borderWidth")) {
                    borderWidth = HeroView.dip2px(getContext(), jsonObject.getInt("borderWidth"));
                }
                if (jsonObject.has("borderColor")) {
                    borderColor = HeroView.parseColor("#" + jsonObject.getString("borderColor"));
                }
                if (jsonObject.has("cornerRadius")) {
                    roundWidth = HeroView.dip2px(getContext(), (float) jsonObject.getDouble("cornerRadius"));
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        if (roundWidth > 0) {
            paint = new Paint();
            paint.setStyle(Paint.Style.STROKE);
            paint.setColor(borderColor);
            paint.setAntiAlias(true);
            paint.setStrokeWidth(borderWidth);
            Xfermode xfermode = new PorterDuffXfermode(PorterDuff.Mode.SRC);
            paint.setXfermode(xfermode);

            drawRect = new RectF();
            drawPath = new Path();
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        // no rounded corner
        if (roundWidth <= 0 || paint == null || drawRect == null || drawPath == null) {
            super.onDraw(canvas);
        } else {
            float borderWidth = this.borderWidth;
            float roundWidth = this.roundWidth;
            if (borderWidth < 0) {
                borderWidth = (getWidth() + getHeight()) * .02f;
            }

            paint.setStrokeWidth(borderWidth);
            drawRect.set(0 + borderWidth / 2, 0 + borderWidth / 2, getWidth() - borderWidth / 2, getHeight() - borderWidth / 2);

            drawPath.reset();
            drawPath.addRoundRect(drawRect, roundWidth, roundWidth, Path.Direction.CCW);

            canvas.save();
            canvas.clipPath(drawPath);
            super.onDraw(canvas);
            canvas.restore();

            if (borderWidth > 0) {
                canvas.drawRoundRect(drawRect, roundWidth, roundWidth, paint);
            }
        }
    }

    private void setAnimation(JSONArray array) {
        anim = new AnimationDrawable();

        for (int i = 0; i < array.length(); i++) {
            String picName = null;
            try {
                picName = array.getString(i);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            if (picName != null) {
                int id = HeroView.getResId(getContext(), picName, "drawable");
                if (id > 0) {
                    Drawable drawable = getResources().getDrawable(id);
                    anim.addFrame(drawable, 300);
                }
            }
        }
        anim.setOneShot(false);
        setBackgroundDrawable(anim);
        anim.start();
    }

    private boolean requestCameraPermission() {
        return MPermissionUtils.checkAndRequestPermissions(getContext(), new String[] {Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA}, MPermissionUtils.HERO_PERMISSION_CAMERA);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == IntentResolver.REQUEST_CODE_GALLERY && resultCode == RESULT_OK) {
            if (data != null) {
                Uri uri = data.getData();
                String filePath = HeroActivity.getPathFromURI(getContext(), uri);
                handlePickResult(requestCode, filePath);
            }
        }
        if (requestCode == IntentResolver.REQUEST_CODE_CAMERA || requestCode == IntentResolver.REQUEST_CODE_PHOTO_CROP) {
            handlePickResult(requestCode, getTempFile().getAbsolutePath());
        }

    }


}
