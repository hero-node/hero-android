package com.hero.depandency;

import android.annotation.SuppressLint;
import android.content.Context;
import android.support.v4.widget.NestedScrollView;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.view.animation.DecelerateInterpolator;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Scroller;
import android.widget.TextView;

import com.hero.R;

import java.text.SimpleDateFormat;
import java.util.Date;

public class XRefreshLayout extends LinearLayout implements XListViewHeader.OnStopListener {
    private final static int SCROLL_DURATION = IRefreshHeader.SCROLL_DURATION;

    private Context context;
    private Scroller scroller;

    private float lastY;
    private View scrollView;
    protected IRefreshHeader headerView;
    private TextView txtTime;
    protected int headerViewHeight;
    private boolean mEnablePullRefresh = false;

    private OnRefreshListener listener;

    public XRefreshLayout(Context context) {
        super(context);
        init(context);
    }

    public XRefreshLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public XRefreshLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    protected void init(Context context) {
        this.context = context;
        this.setOrientation(VERTICAL);
        this.setGravity(Gravity.CENTER_HORIZONTAL);
        scroller = new Scroller(context, new DecelerateInterpolator());
        addHeaderView();
    }

    public void stopRefresh() {
        headerView.setState(XListViewHeader.STATE_REFRESH_STOPPED);
    }

    public void postStopRefresh() {
        headerView.setState(XListViewHeader.STATE_NORMAL);
        resetRefreshHeader();
    }

    public void setOnRefreshListener(OnRefreshListener listener) {
        this.listener = listener;
    }

    public void setRefreshTime(long time) {
        final String FORMAT_DATE_TIME = "yyy-MM-dd HH:mm:ss";
        SimpleDateFormat dateTimeFormatter = new SimpleDateFormat(FORMAT_DATE_TIME);
        txtTime.setText(dateTimeFormatter.format(new Date(time)));
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN: {
                lastY = ev.getY();
                break;
            }
            case MotionEvent.ACTION_MOVE: {
                float deltaY = ev.getY() - lastY;
                if (isRefreshViewScroll(deltaY)) {
                    return true;
                }
            }
            default:
                break;
        }
        return super.onInterceptTouchEvent(ev);
    }

    private boolean isRefreshViewScroll(float deltaY) {
        if (isRefreshing() || !mEnablePullRefresh) {
            return false;
        }

        if (deltaY > 0 && scrollView.getScrollY() == 0 || headerView.getVisibleHeight() > 0) {
            return true;
        }
        return false;
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN: {
                break;
            }
            case MotionEvent.ACTION_MOVE: {
                float deltaY = ev.getY() - lastY;
                lastY = ev.getY();
                updateRefreshHeader(deltaY);
                break;
            }
            default: {
                if (headerView.getVisibleHeight() > headerViewHeight) {
                    headerView.setState(XListViewHeader.STATE_REFRESHING);
                    if (listener != null) {
                        listener.onRefresh(this);
                    }
                }
                resetRefreshHeader();
                break;
            }
        }
        return super.onTouchEvent(ev);
    }

    private void updateRefreshHeader(float deltaY) {
        if (!mEnablePullRefresh) return;

        headerView.setVisibleHeight((int) (deltaY / 2) + headerView.getVisibleHeight());

        if (!isRefreshing()) {
            if (headerView.getVisibleHeight() < headerViewHeight) {
                headerView.setState(XListViewHeader.STATE_NORMAL);
            } else {
                headerView.setState(XListViewHeader.STATE_READY);
            }
        }
    }

    private boolean isRefreshing() {
        return headerView.getState() == XListViewHeader.STATE_REFRESHING;
    }

    private void resetRefreshHeader() {
        int height = headerView.getVisibleHeight();
        if (height == 0 || !mEnablePullRefresh) {
            return;
        }
        if (isRefreshing() && height <= headerViewHeight) {
            return;
        }
        int finalHeight = 0;
        if (isRefreshing() && height > headerViewHeight) {
            finalHeight = headerViewHeight;
        }
        scroller.startScroll(0, height, 0, finalHeight - height, SCROLL_DURATION);
        invalidate();
    }

    @Override
    protected void onFinishInflate() {
        for (int i = 0; i < getChildCount(); i++) {
            View view = getChildAt(i);
            if (view instanceof ScrollView || view instanceof NestedScrollView) {
                scrollView = view;
            }
        }
        if (scrollView == null) {
            throw new IllegalArgumentException("not found ScrollView in the PullToRefreshLayout!");
        }
        super.onFinishInflate();
    }

    protected void addHeaderView() {
        headerView = new XListViewHeader(context);
        txtTime = (TextView) ((View)headerView).findViewById(R.id.xlistview_header_time);
        final View headerViewContent = ((View)headerView).findViewById(R.id.xlistview_header_content);
        addView(((View) headerView));
        headerView.setOnStopListener(this);
        ((View)headerView).getViewTreeObserver().addOnGlobalLayoutListener(new OnGlobalLayoutListener() {

            @SuppressWarnings("deprecation")
            @Override
            public void onGlobalLayout() {
                headerViewHeight = headerViewContent.getHeight();
                ((View)headerView).getViewTreeObserver().removeGlobalOnLayoutListener(this);
            }
        });
    }

    @Override
    public void computeScroll() {
        if (scroller.computeScrollOffset()) {
            headerView.setVisibleHeight(scroller.getCurrY());
            invalidate();
        }
    }

    /**
     * 我的财富优化，解决在MI NOTE LETE财富底部有黄色条纹问题
     */
    public void updateHeaderBackground(int color) {
        if (headerView != null) {
            ((View)headerView).setBackgroundColor(color);
        }
    }

    public void setScrollView(View view) {
        scrollView = view;
    }

    public IRefreshHeader getHeader() {
        return headerView;
    }

    public void setPullRefreshEnable(boolean enable) {
        mEnablePullRefresh = enable;
        if (!mEnablePullRefresh) { // disable, hide the content
            ((View)headerView).setVisibility(View.INVISIBLE);
        } else {
            ((View)headerView).setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onStopCompleted(IRefreshHeader header) {
        postStopRefresh();
    }

    public interface OnRefreshListener {
        void onRefresh(XRefreshLayout pullToRefreshLayout);
    }
}
