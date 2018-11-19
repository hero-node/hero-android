package com.hero;

import android.annotation.TargetApi;
import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatDialog;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.GridLayout;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by drjr on 17-5-4.
 */

public class HeroActionSheetView extends View implements IHero {

    private AppCompatDialog mDialog;
    private Button mCancel;
    private String mLastCancelText = "取消";


    public HeroActionSheetView(Context context) {
        super(context);
    }

    public HeroActionSheetView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public HeroActionSheetView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @TargetApi(21)
    public HeroActionSheetView(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @Override
    public void on(JSONObject jsonObject) throws JSONException {
        HeroView.on(this, jsonObject);


        if (jsonObject.has("data")) {
            Object item = jsonObject.get("data");

            ViewGroup rootView = buildDialog();
            if (item instanceof JSONArray) {
                JSONArray items = (JSONArray) item;
                for (int i = 0; i < items.length(); i++) {
                    JSONObject buttonData = (JSONObject) items.get(i);
                    String btnTitle = buttonData.getString("title");
                    JSONObject btnAction = buttonData.getJSONObject("action");
                    addButton(rootView, btnTitle, btnAction);
                }
            } else if (item instanceof JSONObject) {
                JSONObject buttonData = (JSONObject) item;
                String btnTitle = buttonData.getString("title");
                JSONObject btnAction = buttonData.getJSONObject("action");
                addButton(rootView, btnTitle, btnAction);
            }
            addCancelButton(rootView);
        }

        if (jsonObject.has("show")) {
            onShow(jsonObject.optBoolean("show"));
        }
        if (jsonObject.has("cancelTitle")) {
            onCancelTitle(jsonObject.optString("cancelTitle"));
        }
    }

    private ViewGroup buildDialog() {
        if (mDialog != null && mDialog.isShowing()) {
            mDialog.dismiss();
        }
        mDialog = new AppCompatDialog(getContext(), R.style.ActionSheetDialogSlideAnim);
        LayoutInflater inflater = LayoutInflater.from(getContext());
        GridLayout rootView = (GridLayout) inflater.inflate(R.layout.hero_action_sheet_view, null);
        mDialog.setContentView(rootView);
        mDialog.getWindow().setLayout(GridLayout.LayoutParams.MATCH_PARENT, GridLayout.LayoutParams.WRAP_CONTENT);
        mDialog.getWindow().setGravity(Gravity.BOTTOM);
        return rootView;
    }

    public void onShow(Boolean show) {
        if (show) {
            if (mDialog != null) {
                mDialog.show();
            }
        } else {
            if (mDialog != null) {
                mDialog.dismiss();
            }
        }
    }

    public void onCancelTitle(String text) {
        if (mCancel != null) {
            mCancel.setText(text);
        }
        mLastCancelText = text;
    }

    private Button buildButton(ViewGroup rootView, int btnResource) {
        GridLayout.Spec row = GridLayout.spec(GridLayout.UNDEFINED, 1);
        GridLayout.Spec colspan = GridLayout.spec(GridLayout.UNDEFINED, 1);
        Button item = (Button) LayoutInflater.from(getContext()).inflate(btnResource, rootView, false);
        GridLayout.LayoutParams gridParam = new GridLayout.LayoutParams(row, colspan);
        gridParam.height = HeroView.dip2px(getContext(),50);
        gridParam.width = GridLayout.LayoutParams.MATCH_PARENT;
        gridParam.bottomMargin = HeroView.dip2px(getContext(), 1);
        rootView.addView(item, gridParam);
        return item;
    }

    private void addButton(ViewGroup rootView, String title, final JSONObject action) {
        Button item = buildButton(rootView, R.layout.hero_btn_action_sheet);
        item.setText(title);
        item.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mDialog != null) {
                    mDialog.dismiss();
                }
                try {
                    ((IHeroContext) getContext()).on(action);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void addCancelButton(ViewGroup rootView) {
        Button item = buildButton(rootView, R.layout.hero_btn_cancel_action_sheet);
        item.setText(mLastCancelText);
        item.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mDialog != null) {
                    mDialog.dismiss();
                }
            }
        });
        mCancel = item;
    }


}
