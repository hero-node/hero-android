package com.hero;

/**
 * Created by Yuri on 2019/1/3.
 */

public class HeroInstance {

    private static volatile HeroInstance heroInstance;

    private String public_key;

    private String private_key;

    private HeroInstance() {}

    public static HeroInstance getInstance() {
        if (heroInstance == null) {
            synchronized (HeroInstance.class) {
                if (heroInstance == null) {
                    heroInstance = new HeroInstance();
                }
            }
        }
        return heroInstance;
    }

    public String getPublic_key() {
        return public_key;
    }

    public void setPublic_key(String public_key) {
        this.public_key = public_key;
    }

    public String getPrivate_key() {
        return private_key;
    }

    public void setPrivate_key(String private_key) {
        this.private_key = private_key;
    }
}
