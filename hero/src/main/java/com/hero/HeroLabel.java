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

 * Neither the name Hero nor the names of its contributors may be used to
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
import android.graphics.Typeface;
import android.text.Html;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.AbsoluteSizeSpan;
import android.text.style.ForegroundColorSpan;
import android.text.style.StrikethroughSpan;
import android.view.Gravity;
import android.widget.FrameLayout;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Iterator;

/**
 * Created by liuguoping on 15/9/23.
 */
public class HeroLabel extends TextView implements IHero {
    private boolean isAutoHeight = false;
    private boolean isAutoWidth = false;
    private String oldText;
    private static final int INVALID_VALUE = -9999;

    public HeroLabel(Context context) {
        super(context);
        this.setGravity(Gravity.CENTER_VERTICAL);
        this.setIncludeFontPadding(false);
    }

    //    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    @Override
    public void on(JSONObject jsonObject) throws JSONException {
        HeroView.on(this, jsonObject);

        if (jsonObject.has("hAuto")) {
            if (jsonObject.getBoolean("hAuto")) {
                isAutoHeight = true;
            }
        }

        if (jsonObject.has("wAuto")) {
            if (jsonObject.getBoolean("wAuto")) {
                isAutoWidth = true;
            }
        }

        if (jsonObject.has("textAndroid")) {
            this.setText(Html.fromHtml(jsonObject.getString("textAndroid")));
        }

        if (jsonObject.has("alignment")) {
            String alignment = jsonObject.getString("alignment");
            if (alignment != null && alignment.equalsIgnoreCase("center")) {
                this.setGravity(Gravity.CENTER);
            } else if (alignment != null && alignment.equalsIgnoreCase("left")) {
                this.setGravity(Gravity.LEFT | Gravity.CENTER_VERTICAL);
            } else if (alignment != null && alignment.equalsIgnoreCase("right")) {
                this.setGravity(Gravity.RIGHT | Gravity.CENTER_VERTICAL);
            }
        }

        if (jsonObject.has("verticalAlignment")) {
            String alignment = jsonObject.getString("verticalAlignment");
            if (alignment != null && alignment.equalsIgnoreCase("top")) {
                this.setGravity(this.getGravity() | Gravity.TOP);
            } else if (alignment != null && alignment.equalsIgnoreCase("bottom")) {
                this.setGravity(this.getGravity() | Gravity.BOTTOM);
            }
        }

        if (jsonObject.has("textColor")) {
            this.setTextColor(HeroView.parseColor("#" + jsonObject.getString("textColor")));
        }
        if (jsonObject.has("size")) {
            this.setTextSize(jsonObject.getInt("size"));
        }
        if (jsonObject.has("font")) {
            String font = jsonObject.optString("font");
            if ("bold".equals(font)) {
                this.setTypeface(null, Typeface.BOLD);
            } else if ("italic".equals(font)) {
                this.setTypeface(null, Typeface.ITALIC);
            }
        }
        if (jsonObject.has("numberOfLines")) {
            int lines = jsonObject.getInt("numberOfLines");
            if (lines == 1){
                setSingleLine();
            }
            else if (lines > 1){
                this.setLines(lines);
            }
        }

        if (jsonObject.has("text")) {
            this.setText(jsonObject.getString("text"));
        }

        if (jsonObject.has("attribute")) {
            Object attribute = jsonObject.get("attribute");
            if (attribute instanceof JSONObject) {
                if (((JSONObject) attribute).has("gap")) {
                    String gap = ((JSONObject) attribute).optString("gap");
                    if (gap != null) {
                        try {
                            setLineGap(Integer.parseInt(gap));
                        } catch (NumberFormatException e) {
                            e.printStackTrace();
                        }
                    }
                }
                if (((JSONObject) attribute).has("alignment")) {
                    String alignment = ((JSONObject) attribute).optString("alignment");
                    if (alignment != null) {
                        if (alignment.equalsIgnoreCase("center")) {
                            this.setGravity(Gravity.CENTER);
                        } else if (alignment.equalsIgnoreCase("left")) {
                            this.setGravity(Gravity.LEFT | Gravity.CENTER_VERTICAL);
                        } else if (alignment.equalsIgnoreCase("right")) {
                            this.setGravity(Gravity.RIGHT | Gravity.CENTER_VERTICAL);
                        }
                    }
                }
                SpannableString attrText = buildAttributedText(getText().toString(), (JSONObject) attribute);
                if (attrText != null) {
                    setText(attrText);
                }
            }
        }
    }

    @Override
    protected void onTextChanged(final CharSequence text, final int start, final int before, final int after) {
        super.onTextChanged(text, start, before, after);
        if (isAutoHeight && isAutoWidth) {
            FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) this.getLayoutParams();
            if (params != null) {
                updateSelfWidthHeight(measureSelfWidth(params.height), measureSelfHeight(params.width));
            }
        } else if (isAutoHeight) {
            FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) this.getLayoutParams();
            if (params != null) {
                updateSelfWidthHeight(INVALID_VALUE, measureSelfHeight(params.width));
            }
        } else if (isAutoWidth) {
            FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) this.getLayoutParams();
            if (params != null) {
                updateSelfWidthHeight(measureSelfWidth(params.height), INVALID_VALUE);
            }
        }
        if (text != null) {
            oldText = text.toString();
        }
    }

    private int measureSelfHeight(int width) {
        int widthMeasureSpec = MeasureSpec.makeMeasureSpec(width, MeasureSpec.AT_MOST);
        int heightMeasureSpec = MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED);
        this.measure(widthMeasureSpec, heightMeasureSpec);
        int height = getMeasuredHeight();
        return height;
    }

    private int measureSelfWidth(int height) {
        int widthMeasureSpec = MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED);
        int heightMeasureSpec = MeasureSpec.makeMeasureSpec(height, MeasureSpec.AT_MOST);
        this.measure(widthMeasureSpec, heightMeasureSpec);
        int width = getMeasuredWidth();
        return width;
    }

    private void updateSelfWidthHeight(int width, int height) {
        if (!isAutoHeight && !isAutoWidth) {
            return;
        }

        JSONObject jsonObject = new JSONObject();
        try {
            Context context = getContext();
            FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) this.getLayoutParams();
            JSONObject frame = new JSONObject();
            frame.put("x", HeroView.px2dip(context, params.leftMargin));
            frame.put("y", HeroView.px2dip(context, params.topMargin));
            if (width == INVALID_VALUE) {
                frame.put("w", HeroView.px2dip(context, params.width));
            } else {
                frame.put("w", HeroView.px2dip(context, width));
            }
            if (height == INVALID_VALUE) {
                frame.put("h", HeroView.px2dip(context, params.height));
            } else {
                frame.put("h", HeroView.px2dip(context, height));
            }
            jsonObject.put("frame", frame);
            HeroView.on(HeroLabel.this, jsonObject);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public SpannableString buildAttributedText(String text, JSONObject attributes) {
        SpannableString spannableString = new SpannableString(text);
        Iterator keys = attributes.keys();
        try {
            while (keys.hasNext()) {
                String key = (String) keys.next();
                String content = attributes.getString(key);
                if (key.startsWith("color(")) {
                    String[] range = key.substring(6, key.length() - 1).split(",");
                    if (range.length > 1) {
                        int start = Integer.parseInt(range[0]);
                        int len = Integer.parseInt(range[1]);
                        addColoredSpan(spannableString, start, start + len, HeroView.parseColor(content));
                    }
                } else if (key.startsWith("size(")) {
                    String[] range = key.substring(5, key.length() - 1).split(",");
                    if (range.length > 1) {
                        int start = Integer.parseInt(range[0]);
                        int len = Integer.parseInt(range[1]);
                        addSizedSpan(spannableString, start, start + len, Integer.parseInt(content));
                    }
                } else if (key.startsWith("middleline(")) {
                    String[] range = key.substring("middleline(".length(), key.length() - 1).split(",");
                    if (range.length > 1) {
                        int start = Integer.parseInt(range[0]);
                        int len = Integer.parseInt(range[1]);
                        addStrikeSpan(spannableString, start, start + len);
                    }
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return spannableString;
    }

    public void addColoredSpan(SpannableString spannableString, int start, int end, int color) {
        if (start >= 0 && end >= 0 && end > start && end <= spannableString.toString().length()) {
            spannableString.setSpan(new ForegroundColorSpan(color), start, end, Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
        }
    }

    public void addSizedSpan(SpannableString spannableString, int start, int end, int size) {
        if (start >= 0 && end >= 0 && end > start && end <= spannableString.toString().length()) {
            int sizeInPx = HeroView.dip2px(getContext(), size);
            spannableString.setSpan(new AbsoluteSizeSpan(sizeInPx), start, end, Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
        }
    }

    public void addStrikeSpan(SpannableString spannableString, int start, int end) {
        if (start >= 0 && end >= 0 && end > start && end <= spannableString.toString().length()) {
            StrikethroughSpan span = new StrikethroughSpan();
            spannableString.setSpan(span, start, end, Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
        }
    }

    private void setLineGap(int gap) {
        this.setLineSpacing(gap, 1);
    }
}
