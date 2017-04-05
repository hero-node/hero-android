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

import android.animation.Animator;
import android.animation.LayoutTransition;
import android.content.Context;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by liuguoping on 15/9/25.
 * 这个view只是为了兼容iOS命名
 */
public class UIView extends HeroView {

    public UIView(Context context) {
        super(context);
    }

    @Override
    public void on(JSONObject jsonObject) {
        super.on(jsonObject);

        if (jsonObject.has("animation") && this.getLayoutTransition() == null) {
            if (jsonObject.has("animationType")) {
                // animationType handled in HeroView
                return;
            }
            float animTime = 0;
            try {
                String animation = jsonObject.getString("animation");
                animTime = Float.parseFloat(animation);
                animTime *= 1000;
            } catch (JSONException e) {
                e.printStackTrace();
            } catch (NumberFormatException ex) {
                ex.printStackTrace();
            }

            if (animTime > 0) {
                final LayoutTransition transition = new LayoutTransition();
                Animator defaultAppearingAnim = transition.getAnimator(LayoutTransition.APPEARING);
                Animator defaultDisappearingAnim = transition.getAnimator(LayoutTransition.DISAPPEARING);
                Animator defaultChangingAppearingAnim = transition.getAnimator(LayoutTransition.CHANGE_APPEARING);
                Animator defaultChangingDisappearingAnim = transition.getAnimator(LayoutTransition.CHANGE_DISAPPEARING);
                transition.setAnimator(LayoutTransition.APPEARING, null);
                transition.setAnimator(LayoutTransition.DISAPPEARING, null);
                transition.setAnimator(LayoutTransition.CHANGE_APPEARING, defaultChangingAppearingAnim);
                transition.setAnimator(LayoutTransition.CHANGE_DISAPPEARING, defaultChangingDisappearingAnim);
                transition.setDuration((long) animTime);
                setLayoutTransition(transition);
            }
        }
    }
}
