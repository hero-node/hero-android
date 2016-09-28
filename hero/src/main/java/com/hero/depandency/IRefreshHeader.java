package com.hero.depandency;

public interface IRefreshHeader {
    public final static int SCROLL_DURATION = 400;
    public final static int STATE_NORMAL = 0;
    public final static int STATE_READY = 1;
    public final static int STATE_REFRESHING = 2;
    public final static int STATE_REFRESH_STOPPED = 3;

    public int getVisibleHeight();

    public int getHeaderFixHeight();

    public void setVisibleHeight(int height);

    public int getState();

    public void setState(int state);

    public void setHintString(String string, String type);

    public void setOnStopListener(OnStopListener listener);

    public interface OnStopListener{
        void onStopCompleted(IRefreshHeader header);
    }
}
