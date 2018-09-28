package com.hero;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.KeyEvent;
import android.view.View;

import com.hero.depandency.journeyapps.barcodescanner.CaptureManager;
import com.hero.depandency.journeyapps.barcodescanner.DecoratedBarcodeView;

/**
 * Created by Aron on 2018/8/15.
 */

public class HeroCustomCaptureActivity extends Activity {

    private CaptureManager capture;
    private DecoratedBarcodeView barcodeScannerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.hero_activity_custom_capture);// 自定义布局

        barcodeScannerView = (DecoratedBarcodeView) findViewById(R.id.dbv_custom);

        findViewById(R.id.custom_capture_back_iv).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                HeroCustomCaptureActivity.this.finish();
            }
        });

        capture = new CaptureManager(this, barcodeScannerView);
        capture.initializeFromIntent(getIntent(), savedInstanceState);
        capture.decode();
    }

    @Override
    protected void onResume() {
        super.onResume();
        capture.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        capture.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        capture.onDestroy();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        capture.onSaveInstanceState(outState);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        capture.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        return barcodeScannerView.onKeyDown(keyCode, event) || super.onKeyDown(keyCode, event);
    }
}
