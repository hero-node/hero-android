package com.hero;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.view.Gravity;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by liuguoping on 15/9/23.
 */
public class HeroTextView extends HeroTextField implements IHero {
    public HeroTextView(Context context) {
        super(context);
        init();
    }

    public HeroTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public HeroTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public HeroTextView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    private void init() {
        this.setSingleLine(false);
        //        this.setInputType(InputType.TYPE_TEXT_FLAG_MULTI_LINE);
        this.setHorizontallyScrolling(false);
        this.setGravity(Gravity.LEFT | Gravity.TOP);
        this.setLineSpacing(0,1.1f);
        final HeroTextView self = this;
    }
//    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    @Override
    public void on(JSONObject jsonObject) throws JSONException {
        super.on(jsonObject);

        if (getLayoutParams() != null) {
            this.setHeight(getLayoutParams().height);
        }
        if (jsonObject.has("appendText")) {
            this.setText(this.getText() + "\r\n" + jsonObject.getString("appendText"));
        }
    }
}
