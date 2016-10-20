package com.hero.depandency;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;

import java.util.ArrayList;

/**
 * Created by xincai on 16-4-14.
 */
public class MPermissionUtils {

    public static int HERO_PERMISSION_LOCATION = 1;
    public static int HERO_PERMISSION_CONTACTS = 2;
    public static int HERO_PERMISSION_CALLLOG = 3;
    public static int HERO_PERMISSION_AUDIO = 4;
    public static int HERO_PERMISSION_SDCARD = 5;
    public static int HERO_PERMISSION_CAMERA = 6;
    public static int HERO_PERMISSION_PHONE_STATE = 7;

    public static void requestPermission(Context context, String[] permissions, int requestCode) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            ActivityCompat.requestPermissions((Activity) context, permissions, requestCode);
        }
    }

    public static boolean isPermissionGranted(Context context, String permissionsName) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return true;
        }
        return ActivityCompat.checkSelfPermission(context, permissionsName) == PackageManager.PERMISSION_GRANTED;
    }

    // check and request
    public static boolean checkAndRequestPermission(Context context, String permissionName, int requestCode) {
        if (!isPermissionGranted(context, permissionName)) {
            requestPermission(context, new String[] {permissionName}, requestCode);
            return false;
        }
        return true;
    }

    public static boolean checkAndRequestPermissions(Context context, String[] permissionsName, int requestCode) {
        boolean granted = true;
        ArrayList<String> permissions = new ArrayList<String>();
        for (String name : permissionsName) {
            if (!isPermissionGranted(context, name)) {
                granted = false;
                permissions.add(name);
            }
        }

        if (!granted && permissions.size() > 0) {
            String[] requests = new String[permissions.size()];
            requests = permissions.toArray(requests);
            requestPermission(context, requests, requestCode);
            return false;
        }
        return true;
    }

    public static boolean isPermissionDeclined(Context context, String permission) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return false;
        }
        return ActivityCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED;
    }


    public static void openSettingsScreen(Context context) {
        Intent intent = new Intent();
        intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        Uri uri = Uri.parse("package:" + context.getPackageName());
        intent.setData(uri);
        context.startActivity(intent);
    }

}
