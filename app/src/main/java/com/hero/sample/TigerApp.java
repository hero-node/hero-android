package com.hero.sample;
import com.hero.HeroApplication;

/**
 * Created by liuguoping on 2017/10/12.
 */

public class TigerApp extends HeroApplication {
    public final static String HOME_ADDRESS = "http://10.9.27.3:3000";
    public final static String PATH = "/";

    @Override
    public void onCreate() {
        super.onCreate();

    }

    @Override
    public String getHomeAddress() {
        return HOME_ADDRESS;
    }


    @Override
    public String getHttpReferer() {
        return "";
    }
}
