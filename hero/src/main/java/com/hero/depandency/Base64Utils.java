package com.hero.depandency;


import android.util.Base64;

public class Base64Utils {

    public static final int FLAG = Base64.NO_WRAP;// | Base64.URL_SAFE;

    public static byte[] encode(byte[] input) {
        return Base64.encode(input, FLAG);
    }

    public static String encodeToString(byte[] input) {
        return Base64.encodeToString(input, FLAG);
    }

    public static byte[] decode(byte[] input) {
        return Base64.decode(input, FLAG);
    }

    public static byte[] decode(String input, int flag) {
        return Base64.decode(input, FLAG);
    }
}
