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
