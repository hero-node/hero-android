package com.hero;

import android.content.Context;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.hero.depandency.ImageLoadUtils;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by R9L7NGH on 2015/12/22.
 */
public class HeroToast extends View implements IHero {

    private Context context;
    private int iconId;
    private String text;
    private String iconUrl;

    public HeroToast(Context c) {
        super(c);
        context = c;
    }

    public static void show(Context context, String text, int icon, String url) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View layout = inflater.inflate(R.layout.toast, null);

        TextView tv = (TextView) layout.findViewById(R.id.text);
        tv.setText(text);

        ImageView imageView = (ImageView) layout.findViewById(R.id.image);
        if (icon != -1) {
            imageView.setImageResource(icon);
        } else if (url != null){
            ImageLoadUtils.LoadImage(imageView, url);
        } else {
            imageView.setVisibility(GONE);
        }

        Toast toast = new Toast(context);
        toast.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL, 0, 0);
        toast.setDuration(Toast.LENGTH_SHORT);
        toast.setView(layout);
        toast.show();
    }

    @Override
    public void on(final JSONObject jsonObject) throws JSONException {
        HeroView.on(this, jsonObject);
        String theText = null, url = null;
        int resId = -1;
        boolean isContentChanged = false;

        if (jsonObject.has("icon")) {
            String imageName = jsonObject.getString("icon");
            if (imageName.startsWith("http")) {
                url = imageName;
            } else {
                iconId = ImageLoadUtils.getLocalImageIdByName(getContext(), imageName);
            }
        }

        if (jsonObject.has("text")) {
            theText = jsonObject.getString("text");
        }

        if (!TextUtils.isEmpty(theText)) {
            //            if (!theText.equals(text)) {
            isContentChanged = true;
            //            }
            text = theText;
        }

        if (resId != -1 || url != null) {
            isContentChanged = true;
            iconId = resId;
            iconUrl = url;
        }

        if (isContentChanged) {
            show(context, text, iconId, iconUrl);
        }
    }
}
