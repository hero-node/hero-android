package com.hero;

import android.graphics.Color;
import android.net.Uri;

import java.security.MessageDigest;

/**
 * Created by liuguoping on 15/10/12.
 */

public class StringUtil {

    private final static String[] hexDigits = {"0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "a", "b", "c", "d", "e", "f"};

    public static String toURLEncoded(String paramString) {
        if (paramString == null || paramString.equals("")) {
            return "";
        }
        String str = Uri.encode(paramString);
        return str;
    }

    public static String MD5Encode(String origin) {
        String resultString = null;
        try {
            resultString = new String(origin);
            MessageDigest md = MessageDigest.getInstance("MD5");
            resultString = byteArrayToHexString(md.digest(resultString.getBytes()));
        } catch (Exception ex) {

        }
        return resultString;
    }

    public static String byteArrayToHexString(byte[] b) {
        StringBuffer resultSb = new StringBuffer();
        for (int i = 0; i < b.length; i++) {
            resultSb.append(byteToHexString(b[i]));
        }
        return resultSb.toString();
    }

    private static String byteToHexString(byte b) {
        int n = b;
        if (n < 0) n = 256 + n;
        int d1 = n / 16;
        int d2 = n % 16;
        return hexDigits[d1] + hexDigits[d2];
    }

    public static int stringToHex(String s) {
        int value = -1;
        try {
            value = Integer.parseInt(s, 16);
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }
        return value;
    }

    public static int stringToColor(String s) {
        int color = -1;
        try {
            color = HeroView.parseColor("#" + s);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return color;
    }
}