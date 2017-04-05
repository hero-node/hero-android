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
import android.content.Context;
import android.graphics.PointF;
import android.hardware.Camera;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.dlazaro66.qrcodereaderview.QRCodeReaderView;
import com.hero.depandency.MPermissionUtils;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by liuguoping on 15/9/23.
 */
public class HeroScanBarCodeView extends FrameLayout implements IHero, QRCodeReaderView.OnQRCodeReadListener {
    private FrameLayout previewLayout;
    private QRCodeReaderView qrCodeView;

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
            qrCodeView = new QRCodeReaderView(this.getContext());
            qrCodeView.setOnQRCodeReadListener(this);
            qrCodeView.setBackCamera();
            qrCodeView.setAutofocusInterval(2000L);
            if (previewLayout != null) {
                previewLayout.addView(qrCodeView, new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
            } else {
                this.addView(qrCodeView, new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
            }
            qrCodeView.startCamera();
        }
    }


    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (qrCodeView != null) {
            qrCodeView.stopCamera();
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

    private boolean requestCameraPermission() {
        return MPermissionUtils.checkAndRequestPermission(getContext(), Manifest.permission.CAMERA, MPermissionUtils.HERO_PERMISSION_CAMERA);
    }

}
