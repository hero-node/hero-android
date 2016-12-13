package com.hero;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.PointF;
import android.hardware.Camera;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.hero.depandency.MPermissionUtils;
import com.hero.depandency.QRCodeReaderView;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by liuguoping on 15/9/23.
 */
public class HeroScanBarCodeView extends FrameLayout implements IHero, QRCodeReaderView.OnQRCodeReadListener {
    private FrameLayout previewLayout;
    private QRCodeReaderView mydecoderview;

    public HeroScanBarCodeView(Context context) {
        super(context);
        View view = LayoutInflater.from(context).inflate(R.layout.scan, this, true);
        previewLayout = (FrameLayout) view.findViewById(R.id.previewLayout);
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    @Override
    public void on(final JSONObject jsonObject) throws JSONException {
        HeroView.on(this, jsonObject);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (!requestCameraPermission()) {
            Toast.makeText(getContext(), R.string.open_camera_fail, Toast.LENGTH_SHORT).show();
            return;
        }
        if (Camera.getNumberOfCameras() > 0) {
            mydecoderview = new QRCodeReaderView(this.getContext());
            mydecoderview.setOnQRCodeReadListener(this);
            if (previewLayout != null) {
                previewLayout.addView(mydecoderview, new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
            } else {
                this.addView(mydecoderview, new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
            }
            mydecoderview.getCameraManager().startPreview();
        }
    }


    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (mydecoderview != null) {
            mydecoderview.getCameraManager().stopPreview();
        }
    }

    @Override
    public void onQRCodeRead(String text, PointF[] points) {
        JSONObject json = HeroView.getJson(this);
        if (json != null && json.has("getBarCode")) {
            try {
                JSONObject code = json.getJSONObject("getBarCode");
                code.put("value", text);
                ((IHeroContext) this.getContext()).on(code);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

    }

    @Override
    public void cameraNotFound() {
        Toast.makeText(getContext(), R.string.open_camera_fail, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void QRCodeNotFoundOnCamImage() {

    }

    private boolean requestCameraPermission() {
        return MPermissionUtils.checkAndRequestPermission(getContext(), Manifest.permission.CAMERA, MPermissionUtils.HERO_PERMISSION_CAMERA);
    }

}
