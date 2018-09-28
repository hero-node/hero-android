package com.hero.utils;

import android.graphics.Bitmap;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.common.BitMatrix;
import com.hero.depandency.journeyapps.barcodescanner.BarcodeEncoder;

/**
 * Created by Aron on 2018/8/8.
 */

public class ZxingUtils {

    public static final Bitmap encodeAsBitmap(String content) throws Exception{
        return encodeAsBitmap(content, 400,400);
    }

    public static final Bitmap encodeAsBitmap(String content, int width, int height) throws Exception{
        Bitmap bitmap = null;
        BitMatrix result = null;
        MultiFormatWriter multiFormatWriter = new MultiFormatWriter();
        result = multiFormatWriter.encode(content, BarcodeFormat.QR_CODE, width, height);
        // 使用 ZXing Android Embedded 要写的代码
        BarcodeEncoder barcodeEncoder = new BarcodeEncoder();
        bitmap = barcodeEncoder.createBitmap(result);
        return bitmap;
    }

}
