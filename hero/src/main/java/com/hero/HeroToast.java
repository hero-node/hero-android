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
