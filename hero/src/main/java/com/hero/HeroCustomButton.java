package com.hero;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.StateListDrawable;
import android.os.Build;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by liuguoping on 15/9/23.
 */
public class HeroCustomButton extends HeroButton {
    public HeroCustomButton(Context context) {
        super(context);
    }
}
