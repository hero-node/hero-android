package com.hero.depandency.journeyapps.barcodescanner;

import android.os.Looper;

/**
 *
 */
public class Util {
    public static void validateMainThread() {
        if (Looper.getMainLooper() != Looper.myLooper()) {
            throw new IllegalStateException("Must be called from the main thread.");
        }
    }
}
