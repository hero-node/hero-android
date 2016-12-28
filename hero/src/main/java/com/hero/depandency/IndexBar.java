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

/**
 * Created by xincai on 16-7-11.
 */

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.SectionIndexer;

import com.hero.HeroTableView;
import com.hero.R;

import org.json.JSONArray;
import org.json.JSONException;

public class IndexBar extends View {
    private static final boolean DRAW_BACKGROUND = false;
    private String[] indexData;
    private SectionIndexer sectionIndexer = null;
    private HeroTableView listView;
    private Paint textPaint;
    private Paint backgroundPaint;

    public IndexBar(Context context) {
        super(context);
        init(null);
    }

    public IndexBar(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(null);
    }

    public IndexBar(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(null);
    }

    public IndexBar(Context context, HeroTableView list) {
        super(context);
        listView = list;
        sectionIndexer = listView.getCustomAdapter();
        init(listView.getCustomAdapter().getIndexData());
        textPaint = new Paint();
        textPaint.setColor(getResources().getColor(R.color.indexBarColor));
        textPaint.setTextSize(getResources().getDimensionPixelSize(R.dimen.index_bar_font_size));
        Typeface font = Typeface.create(Typeface.DEFAULT, Typeface.BOLD);
        textPaint.setTypeface(font);
        textPaint.setFlags(Paint.ANTI_ALIAS_FLAG);
        textPaint.setTextAlign(Paint.Align.CENTER);
        backgroundPaint = new Paint();
        backgroundPaint.setColor(getResources().getColor(R.color.indexBarBackColor));
    }

    private void init(JSONArray data) {
        if (data == null) {
            indexData = new String[] {"A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M", "N", "O", "P", "Q", "R", "S", "T", "U", "V", "W", "X", "Y", "Z" };
        } else {
            indexData = jsonToArray(data);
        }
        if (indexData.length == 0) {
            indexData = new String[] {" " };
        }
    }

    public void addToTable(ViewGroup parent, int height) {
        Resources resources = getResources();
        int maxHeight = resources.getDimensionPixelSize(R.dimen.index_bar_item_max_height);
        int width = resources.getDimensionPixelSize(R.dimen.index_bar_width);
        int topMargin = resources.getDimensionPixelSize(R.dimen.index_bar_top_margin);
        if (indexData.length > 0) {
            int itemHeight = (height - (topMargin * 2)) / indexData.length;
            if (itemHeight > maxHeight) {
                topMargin = (height - (indexData.length * maxHeight)) / 2;
            }
        }
        if (parent instanceof FrameLayout) {
            FrameLayout.LayoutParams param = new FrameLayout.LayoutParams(width, height - topMargin * 2);
            param.topMargin = topMargin;
            param.gravity = Gravity.RIGHT;
            parent.addView(this, param);
        } else {
            parent.addView(this, width, height - topMargin * 2);
        }
    }

    public String[] jsonToArray(JSONArray array) {
        String strings[] = new String[array.length()];
        for (int i = 0; i < array.length(); i++) {
            try {
                strings[i] = array.getString(i);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return strings;
    }

    public boolean onTouchEvent(MotionEvent event) {
        super.onTouchEvent(event);
        int i = (int) event.getY();
        int height = getMeasuredHeight() / indexData.length;
        int index = i / height;
        if (index >= indexData.length) {
            index = indexData.length - 1;
        } else if (index < 0) {
            index = 0;
        }
        if (event.getAction() == MotionEvent.ACTION_DOWN || event.getAction() == MotionEvent.ACTION_MOVE) {
            if (sectionIndexer == null) {
                return true;
            }
            int position = sectionIndexer.getPositionForSection(index);
            if (position == -1) {
                return true;
            }
            listView.setSelection(position);
        }
        return true;
    }

    protected void onDraw(Canvas canvas) {
        int width = getMeasuredWidth();
        float widthCenter = width / 2;
        int itemHeight = getMeasuredHeight() / indexData.length;
        if (DRAW_BACKGROUND) {
            canvas.drawRect(width / 8, 0, width - width / 8, getMeasuredHeight(), backgroundPaint);
        }
        for (int i = 0; i < indexData.length; i++) {
            canvas.drawText(indexData[i], widthCenter, itemHeight + (i * itemHeight), textPaint);
        }
        super.onDraw(canvas);
    }
}
