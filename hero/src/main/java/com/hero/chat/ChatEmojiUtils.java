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

package com.hero.chat;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ImageSpan;

import com.hero.R;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by xincai on 16-6-14.
 */
public class ChatEmojiUtils {

    public final static int EMOJI_COUNT = 87;
    public final static int EMOJI_COUNT_PER_PAGE = 20;
    public final static String DELETE_ICON_NAME = "chat_delete_emoticon";

    public static final String expression_1 = "[微笑]";
    public static final String expression_2 = "[皱眉]";
    public static final String expression_3 = "[色]";
    public static final String expression_4 = "[囧逼]";
    public static final String expression_5 = "[酷]";
    public static final String expression_6 = "[流泪]";
    public static final String expression_7 = "[羞涩]";
    public static final String expression_8 = "[闭嘴]";
    public static final String expression_9 = "[睡]";
    public static final String expression_10 = "[大哭]";
    public static final String expression_11 = "[怕怕]";
    public static final String expression_12 = "[怒了]";
    public static final String expression_13 = "[调皮]";
    public static final String expression_14 = "[呲牙]";
    public static final String expression_15 = "[吃惊]";
    public static final String expression_16 = "[难过]";
    public static final String expression_17 = "[非常酷]";
    public static final String expression_18 = "[脸红]";
    public static final String expression_19 = "[抓狂]";
    public static final String expression_20 = "[吐]";
    public static final String expression_21 = "[偷笑]";
    public static final String expression_22 = "[羞羞]";
    public static final String expression_23 = "[好奇]";
    public static final String expression_24 = "[不耻]";
    public static final String expression_25 = "[饿]";
    public static final String expression_26 = "[懵]";
    public static final String expression_27 = "[吓惨了]";
    public static final String expression_28 = "[汗]";
    public static final String expression_29 = "[憨笑]";
    public static final String expression_30 = "[悠闲]";
    public static final String expression_31 = "[奋斗]";
    public static final String expression_32 = "[大骂]";
    public static final String expression_33 = "[疑问]";
    public static final String expression_34 = "[嘘嘘]";
    public static final String expression_35 = "[晕]";
    public static final String expression_36 = "[钱没了]";
    public static final String expression_37 = "[衰]";
    public static final String expression_38 = "[骷髅]";
    public static final String expression_39 = "[打击]";
    public static final String expression_40 = "[再见]";
    public static final String expression_41 = "[流汗]";
    public static final String expression_42 = "[扣鼻]";
    public static final String expression_43 = "[鼓掌]";
    public static final String expression_44 = "[吓傻了]";
    public static final String expression_45 = "[呲牙]";
    public static final String expression_46 = "[左哼哼]";
    public static final String expression_47 = "[右哼哼]";
    public static final String expression_48 = "[困]";
    public static final String expression_49 = "[鄙视]";
    public static final String expression_50 = "[委屈]";
    public static final String expression_51 = "[伤心]";
    public static final String expression_52 = "[猥琐]";
    public static final String expression_53 = "[亲亲]";
    public static final String expression_54 = "[吓]";
    public static final String expression_55 = "[萌]";
    public static final String expression_56 = "[刀]";
    public static final String expression_57 = "[西瓜]";
    public static final String expression_58 = "[美味]";
    public static final String expression_59 = "[篮球]";
    public static final String expression_60 = "[乒乓球]";
    public static final String expression_61 = "[咖啡]";
    public static final String expression_62 = "[饭]";
    public static final String expression_63 = "[猪头]";
    public static final String expression_64 = "[玫瑰]";
    public static final String expression_65 = "[凋谢]";
    public static final String expression_66 = "[红唇]";
    public static final String expression_67 = "[爱心]";
    public static final String expression_68 = "[心碎]";
    public static final String expression_69 = "[蛋糕]";
    public static final String expression_70 = "[闪电]";
    public static final String expression_71 = "[炸弹]";
    public static final String expression_72 = "[小刀]";
    public static final String expression_73 = "[足球]";
    public static final String expression_74 = "[虫]";
    public static final String expression_75 = "[便便]";
    public static final String expression_76 = "[月亮]";
    public static final String expression_77 = "[太阳]";
    public static final String expression_78 = "[礼物]";
    public static final String expression_79 = "[抱抱]";
    public static final String expression_80 = "[强]";
    public static final String expression_81 = "[弱]";
    public static final String expression_82 = "[合作]";
    public static final String expression_83 = "[胜利]";
    public static final String expression_84 = "[抱拳]";
    public static final String expression_85 = "[勾引]";
    public static final String expression_86 = "[拳头]";
    public static final String expression_87 = "[小样]";

    public static List<String> getEmojiList(int count) {
        List<String> list = new ArrayList<String>();
        for (int x = 0; x <= count; x++) {
            String filename = "expression_" + (x + 1);
            list.add(filename);
        }
        return list;
    }

    private static final Spannable.Factory spannableFactory = Spannable.Factory.getInstance();

    private static final Map<String, Integer> emojiResMap = new HashMap<String, Integer>();

    static {
        addEmoji(emojiResMap, expression_1, R.drawable.expression_1);
        addEmoji(emojiResMap, expression_2, R.drawable.expression_2);
        addEmoji(emojiResMap, expression_3, R.drawable.expression_3);
        addEmoji(emojiResMap, expression_4, R.drawable.expression_4);
        addEmoji(emojiResMap, expression_5, R.drawable.expression_5);
        addEmoji(emojiResMap, expression_6, R.drawable.expression_6);
        addEmoji(emojiResMap, expression_7, R.drawable.expression_7);
        addEmoji(emojiResMap, expression_8, R.drawable.expression_8);
        addEmoji(emojiResMap, expression_9, R.drawable.expression_9);
        addEmoji(emojiResMap, expression_10, R.drawable.expression_10);
        addEmoji(emojiResMap, expression_11, R.drawable.expression_11);
        addEmoji(emojiResMap, expression_12, R.drawable.expression_12);
        addEmoji(emojiResMap, expression_13, R.drawable.expression_13);
        addEmoji(emojiResMap, expression_14, R.drawable.expression_14);
        addEmoji(emojiResMap, expression_15, R.drawable.expression_15);
        addEmoji(emojiResMap, expression_16, R.drawable.expression_16);
        addEmoji(emojiResMap, expression_17, R.drawable.expression_17);
        addEmoji(emojiResMap, expression_18, R.drawable.expression_18);
        addEmoji(emojiResMap, expression_19, R.drawable.expression_19);
        addEmoji(emojiResMap, expression_20, R.drawable.expression_20);
        addEmoji(emojiResMap, expression_21, R.drawable.expression_21);
        addEmoji(emojiResMap, expression_22, R.drawable.expression_22);
        addEmoji(emojiResMap, expression_23, R.drawable.expression_23);
        addEmoji(emojiResMap, expression_24, R.drawable.expression_24);
        addEmoji(emojiResMap, expression_25, R.drawable.expression_25);
        addEmoji(emojiResMap, expression_26, R.drawable.expression_26);
        addEmoji(emojiResMap, expression_27, R.drawable.expression_27);
        addEmoji(emojiResMap, expression_28, R.drawable.expression_28);
        addEmoji(emojiResMap, expression_29, R.drawable.expression_29);
        addEmoji(emojiResMap, expression_30, R.drawable.expression_30);
        addEmoji(emojiResMap, expression_31, R.drawable.expression_31);
        addEmoji(emojiResMap, expression_32, R.drawable.expression_32);
        addEmoji(emojiResMap, expression_33, R.drawable.expression_33);
        addEmoji(emojiResMap, expression_34, R.drawable.expression_34);
        addEmoji(emojiResMap, expression_35, R.drawable.expression_35);
        addEmoji(emojiResMap, expression_36, R.drawable.expression_36);
        addEmoji(emojiResMap, expression_37, R.drawable.expression_37);
        addEmoji(emojiResMap, expression_38, R.drawable.expression_38);
        addEmoji(emojiResMap, expression_39, R.drawable.expression_39);
        addEmoji(emojiResMap, expression_40, R.drawable.expression_40);
        addEmoji(emojiResMap, expression_41, R.drawable.expression_41);
        addEmoji(emojiResMap, expression_42, R.drawable.expression_42);
        addEmoji(emojiResMap, expression_43, R.drawable.expression_43);
        addEmoji(emojiResMap, expression_44, R.drawable.expression_44);
        addEmoji(emojiResMap, expression_45, R.drawable.expression_45);
        addEmoji(emojiResMap, expression_46, R.drawable.expression_46);
        addEmoji(emojiResMap, expression_47, R.drawable.expression_47);
        addEmoji(emojiResMap, expression_48, R.drawable.expression_48);
        addEmoji(emojiResMap, expression_49, R.drawable.expression_49);
        addEmoji(emojiResMap, expression_50, R.drawable.expression_50);
        addEmoji(emojiResMap, expression_51, R.drawable.expression_51);
        addEmoji(emojiResMap, expression_52, R.drawable.expression_52);
        addEmoji(emojiResMap, expression_53, R.drawable.expression_53);
        addEmoji(emojiResMap, expression_54, R.drawable.expression_54);
        addEmoji(emojiResMap, expression_55, R.drawable.expression_55);
        addEmoji(emojiResMap, expression_56, R.drawable.expression_56);
        addEmoji(emojiResMap, expression_57, R.drawable.expression_57);
        addEmoji(emojiResMap, expression_58, R.drawable.expression_58);
        addEmoji(emojiResMap, expression_59, R.drawable.expression_59);
        addEmoji(emojiResMap, expression_60, R.drawable.expression_60);
        addEmoji(emojiResMap, expression_61, R.drawable.expression_61);
        addEmoji(emojiResMap, expression_62, R.drawable.expression_62);
        addEmoji(emojiResMap, expression_63, R.drawable.expression_63);
        addEmoji(emojiResMap, expression_64, R.drawable.expression_64);
        addEmoji(emojiResMap, expression_65, R.drawable.expression_65);
        addEmoji(emojiResMap, expression_66, R.drawable.expression_66);
        addEmoji(emojiResMap, expression_67, R.drawable.expression_67);
        addEmoji(emojiResMap, expression_68, R.drawable.expression_68);
        addEmoji(emojiResMap, expression_69, R.drawable.expression_69);
        addEmoji(emojiResMap, expression_70, R.drawable.expression_70);
        addEmoji(emojiResMap, expression_71, R.drawable.expression_71);
        addEmoji(emojiResMap, expression_72, R.drawable.expression_72);
        addEmoji(emojiResMap, expression_73, R.drawable.expression_73);
        addEmoji(emojiResMap, expression_74, R.drawable.expression_74);
        addEmoji(emojiResMap, expression_75, R.drawable.expression_75);
        addEmoji(emojiResMap, expression_76, R.drawable.expression_76);
        addEmoji(emojiResMap, expression_77, R.drawable.expression_77);
        addEmoji(emojiResMap, expression_78, R.drawable.expression_78);
        addEmoji(emojiResMap, expression_79, R.drawable.expression_79);
        addEmoji(emojiResMap, expression_80, R.drawable.expression_80);
        addEmoji(emojiResMap, expression_81, R.drawable.expression_81);
        addEmoji(emojiResMap, expression_82, R.drawable.expression_82);
        addEmoji(emojiResMap, expression_83, R.drawable.expression_83);
        addEmoji(emojiResMap, expression_84, R.drawable.expression_84);
        addEmoji(emojiResMap, expression_85, R.drawable.expression_85);
        addEmoji(emojiResMap, expression_86, R.drawable.expression_86);
        addEmoji(emojiResMap, expression_87, R.drawable.expression_87);

    }

    public static void addEmoji(Map<String, Integer> map, String expression, int resId) {
        map.put(expression, resId);
    }

    public static int findEmoji(String expression) {
        if (emojiResMap.containsKey(expression)) {
            return emojiResMap.get(expression);
        }
        return -1;
    }

    public static SpannableString mixTextWithEmoji(Context context, String text) {

        SpannableString spannable = new SpannableString(text);

        if (text.indexOf("[") != -1) {
            int start = text.indexOf("[");
            int end;
            int emojiWidth = (int) (context.getResources().getDimension(R.dimen.emoji_size_in_text));
            while (start < text.length() && start != -1) {
                end = text.indexOf("]", start);
                if (end < text.length() && end != -1) {
                    String expression = text.substring(start, end + 1);
                    int resId = findEmoji(expression);
                    if (resId != -1) {
                        Drawable drawable = context.getResources().getDrawable(resId);
                        drawable.setBounds(0, 0, emojiWidth, emojiWidth);
                        ImageSpan span = new ImageSpan(drawable, ImageSpan.ALIGN_BOTTOM);
                        spannable.setSpan(span, start, start + expression.length(), Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
                    }
                } else {
                    break;
                }
                start = text.indexOf("[", end + 1);;
            }
        }
        return spannable;
    }

    public static boolean isEmojiExists(String key) {
        return emojiResMap.containsKey(key);
    }

    public static int getResIdByName(Context context, String name) {
        int resId = context.getResources().getIdentifier(name, "drawable", context.getPackageName());
        return resId;
    }

    public static String getEmojiExpression(String name) {
        try {
            Field f = ChatEmojiUtils.class.getField(name);
            if (f != null) {
                return (String) (f.get(null));
            }
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return null;
    }
}
