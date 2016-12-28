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

    public static boolean isPermissionsGranted(Context context, String[] permissionsName) {
        for (String name : permissionsName) {
            if (!isPermissionGranted(context, name)) {
                return false;
            }
        }
        return true;
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
