/**
 * @file XListViewHeader.java
 * @create Apr 18, 2012 5:22:27 PM
 * @author Maxwin
 * @description XListView's header
 */
package com.hero.depandency;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.hero.R;

public class XListViewHeader extends LinearLayout implements IRefreshHeader {
    private final int ROTATE_ANIM_DURATION = 180;
    private LinearLayout mContainer;
    private ImageView mArrowImageView;
    private ProgressBar mProgressBar;
    private TextView mHintTextView;
    private int mState = STATE_NORMAL;
    private Animation mRotateUpAnim;
    private Animation mRotateDownAnim;
    private int visiableHeight;

    private String idleString;
    private String pullingString;
    private String refreshString;
    private String loadingString;
    private String noMoreDataString;
    private OnStopListener listener;

    public XListViewHeader(Context context) {
        super(context);
        initView(context);
    }

    /**
     * @param context
     * @param attrs
     */
    public XListViewHeader(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView(context);
    }

    public XListViewHeader(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView(context);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public XListViewHeader(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        initView(context);
    }

    private void initView(Context context) {
        // 初始情况，设置下拉刷新view高度为0
        LayoutParams lp = new LayoutParams(LayoutParams.MATCH_PARENT, 0);
        mContainer = (LinearLayout) LayoutInflater.from(context).inflate(R.layout.xlistview_header, null);
        addView(mContainer, lp);
        setGravity(Gravity.BOTTOM);

        mArrowImageView = (ImageView) findViewById(R.id.xlistview_header_arrow);
        mHintTextView = (TextView) findViewById(R.id.xlistview_header_hint_textview);
        mProgressBar = (ProgressBar) findViewById(R.id.xlistview_header_progressbar);

        mRotateUpAnim = new RotateAnimation(0.0f, -180.0f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        mRotateUpAnim.setDuration(ROTATE_ANIM_DURATION);
        mRotateUpAnim.setFillAfter(true);
        mRotateDownAnim = new RotateAnimation(-180.0f, 0.0f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        mRotateDownAnim.setDuration(ROTATE_ANIM_DURATION);
        mRotateDownAnim.setFillAfter(true);
    }

    public int getVisibleHeight() {
        // return mContainer.getHeight();
        return visiableHeight;
    }

    @Override
    public int getHeaderFixHeight() {
        View view = findViewById(R.id.xlistview_header_content);
        if (view != null) {
            return view.getHeight();
        }
        return 0;
    }

    public void setVisibleHeight(int height) {
        if (height < 0) {
            height = 0;
        }
        LayoutParams lp = (LayoutParams) mContainer.getLayoutParams();
        lp.height = height;
        visiableHeight = height;
        mContainer.setLayoutParams(lp);
    }

    public int getState() {
        return mState;
    }

    public void setState(int state) {
        if (state == mState) {
            return;
        }

        if (state == STATE_REFRESHING) { // 显示进度
            mArrowImageView.clearAnimation();
            mArrowImageView.setVisibility(View.INVISIBLE);
            mProgressBar.setVisibility(View.VISIBLE);
        } else { // 显示箭头图片
            mArrowImageView.setVisibility(View.VISIBLE);
            mProgressBar.setVisibility(View.INVISIBLE);
        }

        switch (state) {
            case STATE_NORMAL:
                if (mState == STATE_READY) {
                    mArrowImageView.startAnimation(mRotateDownAnim);
                }
                if (mState == STATE_REFRESHING) {
                    mArrowImageView.clearAnimation();
                }
                if (!TextUtils.isEmpty(idleString)) {
                    mHintTextView.setText(idleString);
                } else {
                    mHintTextView.setText(R.string.listviewHeader_hintNormal);
                }
                break;
            case STATE_READY:
                if (mState != STATE_READY) {
                    mArrowImageView.clearAnimation();
                    mArrowImageView.startAnimation(mRotateUpAnim);
                    if (!TextUtils.isEmpty(pullingString)) {
                        mHintTextView.setText(pullingString);
                    } else {
                        mHintTextView.setText(R.string.listviewHeader_hintReady);
                    }
                }
                break;
            case STATE_REFRESHING:
                if (!TextUtils.isEmpty(refreshString)) {
                    mHintTextView.setText(refreshString);
                } else {
                    mHintTextView.setText(R.string.listviewHeader_hintLoading);
                }
                break;
            case STATE_REFRESH_STOPPED:
                if(null != listener){
                    listener.onStopCompleted(this);
                }
                break;
            default:
        }

        mState = state;
    }

    public void setHintString(String string, String type) {
        if (type.equals("idle")) {
            idleString = string;
        } else if (type.equals("pulling")) {
            pullingString = string;
        } else if (type.equals("refreshing")) {
            refreshString = string;
        } else if (type.equals("loading")) {
            loadingString = string;
        } else if (type.equals("noMoreData")) {
            noMoreDataString = string;
        }
    }

    @Override
    public void setOnStopListener(OnStopListener listener){
        this.listener = listener;
    }

    public OnStopListener getOnStopListener(){
        return this.listener;
    }
}
