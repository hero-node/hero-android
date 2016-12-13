package com.hero.depandency;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Matrix;
import android.graphics.PointF;
import android.os.Build;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.widget.ImageView;

// referenced from com.github.jeremiemartinez.zoomable;
public class ZoomImageView extends ImageView {
    private float maxScale = 3f;
    private float minScale = 1f;
    private static final int MOVE_THRESHOLD = 10;

    private enum State {
        INIT, DRAG, ZOOM
    }

    private State state;
    private Matrix matrix;
    private float[] finalTransformation = new float[9];
    private PointF lastPoint = new PointF();
    private float currentScale = 1f;

    private int viewWidth;
    private int viewHeight;
    private float afterScaleDrawableWidth;
    private float afterScaleDrawableHeight;
    private boolean eventConsumed = false;

    private ScaleGestureDetector scaleDetector;

    public ZoomImageView(Context context) {
        super(context);
        setupView(context);
    }

    public ZoomImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setupView(context);
    }

    public ZoomImageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        setupView(context);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public ZoomImageView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr);
        setupView(context);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        viewWidth = MeasureSpec.getSize(widthMeasureSpec);
        viewHeight = MeasureSpec.getSize(heightMeasureSpec);

        if (hasDrawable()) {
            resetImage();
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int pointerCount = event.getPointerCount();
        scaleDetector.onTouchEvent(event);
        PointF current = new PointF(event.getX(), event.getY());

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                lastPoint.set(current);
                eventConsumed = false;
                break;

            case MotionEvent.ACTION_MOVE:
                if (pointerCount == 1 && state != State.DRAG) {
                    if (Math.abs(current.x - lastPoint.x) > MOVE_THRESHOLD || Math.abs(current.y - lastPoint.y) > MOVE_THRESHOLD) {
                        eventConsumed = true;
                        state = State.DRAG;
                    }
                } else if (pointerCount > 1) {
                    eventConsumed = true;
                }
                if (state == State.DRAG) {
                    drag(current);
                    lastPoint.set(current);
                }
                break;

            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_POINTER_UP:
                state = State.INIT;
                if (!eventConsumed) {
                    if (this.hasOnClickListeners()) {
                        try {
                            this.performClick();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    eventConsumed = true;
                    return false;
                }
                break;
        }
        setImageMatrix(matrix);
        invalidate();
        return true;
    }

    private void setupView(Context context) {
        super.setClickable(false);
        matrix = new Matrix();
        state = State.INIT;
        scaleDetector = new ScaleGestureDetector(context, new ScaleListener());
        setScaleType(ScaleType.MATRIX);
    }

    private void resetImage() {
        // Scale Image
        float scale = getScaleForDrawable();
        matrix.setScale(scale, scale);

        // Center Image
        float marginY = ((float) viewHeight - (scale * getDrawable().getIntrinsicHeight())) / 2;
        float marginX = ((float) viewWidth - (scale * getDrawable().getIntrinsicWidth())) / 2;
        matrix.postTranslate(marginX, marginY);

        afterScaleDrawableWidth = (float) viewWidth - 2 * marginX;
        afterScaleDrawableHeight = (float) viewHeight - 2 * marginY;

        setImageMatrix(matrix);
    }

    private void drag(PointF current) {
        float deltaX = getMoveDraggingDelta(current.x - lastPoint.x, viewWidth, afterScaleDrawableWidth * currentScale);
        float deltaY = getMoveDraggingDelta(current.y - lastPoint.y, viewHeight, afterScaleDrawableHeight * currentScale);
        matrix.postTranslate(deltaX, deltaY);
        limitDrag();
    }

    private void scale(float focusX, float focusY, float scaleFactor) {
        float lastScale = currentScale;
        float newScale = lastScale * scaleFactor;

        // Calculate next scale with resetting to max or min if required
        if (newScale > maxScale) {
            currentScale = maxScale;
            scaleFactor = maxScale / lastScale;
        } else if (newScale < minScale) {
            currentScale = minScale;
            scaleFactor = minScale / lastScale;
        } else {
            currentScale = newScale;
        }

        // Do scale
        if (requireCentering()) {
            matrix.postScale(scaleFactor, scaleFactor, (float) viewWidth / 2, (float) viewHeight / 2);
        } else matrix.postScale(scaleFactor, scaleFactor, focusX, focusY);

        limitDrag();
    }

    /**
     * This method permits to keep drag and zoom inside the drawable. It makes sure the drag is staying in bound.
     */
    private void limitDrag() {
        matrix.getValues(finalTransformation);
        float finalXTransformation = finalTransformation[Matrix.MTRANS_X];
        float finalYTransformation = finalTransformation[Matrix.MTRANS_Y];

        float deltaX = getScaleDraggingDelta(finalXTransformation, viewWidth, afterScaleDrawableWidth * currentScale);
        float deltaY = getScaleDraggingDelta(finalYTransformation, viewHeight, afterScaleDrawableHeight * currentScale);

        matrix.postTranslate(deltaX, deltaY);
    }

    private float getScaleDraggingDelta(float delta, float viewSize, float contentSize) {
        float minTrans = 0;
        float maxTrans = 0;

        if (contentSize <= viewSize) {
            maxTrans = viewSize - contentSize;
        } else {
            minTrans = viewSize - contentSize;
        }

        if (delta < minTrans) return minTrans - delta;
        else if (delta > maxTrans) return maxTrans - delta;
        else return 0;
    }

    private float getMoveDraggingDelta(float delta, float viewSize, float contentSize) {
        if (contentSize <= viewSize) {
            return 0;
        }
        return delta;
    }

    private float getScaleForDrawable() {
        float scaleX = (float) viewWidth / (float) getDrawable().getIntrinsicWidth();
        float scaleY = (float) viewHeight / (float) getDrawable().getIntrinsicHeight();
        return Math.min(scaleX, scaleY);
    }

    private boolean hasDrawable() {
        return getDrawable() != null && getDrawable().getIntrinsicWidth() != 0 && getDrawable().getIntrinsicHeight() != 0;
    }

    private boolean requireCentering() {
        return afterScaleDrawableWidth * currentScale <= (float) viewWidth || afterScaleDrawableHeight * currentScale <= (float) viewHeight;
    }

    private class ScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {
        @Override
        public boolean onScaleBegin(ScaleGestureDetector detector) {
            state = State.ZOOM;
            return true;
        }

        @Override
        public boolean onScale(ScaleGestureDetector detector) {
            scale(detector.getFocusX(), detector.getFocusY(), detector.getScaleFactor());
            return true;
        }
    }

}
