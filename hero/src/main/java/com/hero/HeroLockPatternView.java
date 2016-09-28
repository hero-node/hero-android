package com.hero;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.ShapeDrawable;
import android.os.Debug;
import android.os.Handler;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.util.Log;
import android.view.HapticFeedbackConstants;
import android.view.MotionEvent;
import android.view.View;
import android.view.accessibility.AccessibilityEvent;
import android.widget.FrameLayout;

import com.hero.depandency.LockPatternUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;


/**
 * Displays and detects the user's unlock attempt, which is a drag of a finger
 * across 9 regions of the screen.
 *
 * Is also capable of displaying a static pattern in "in progress", "wrong" or
 * "correct" states.
 *
 * @author way
 */
public class HeroLockPatternView extends View implements IHero {
    // TODO: make this common with PhoneWindow
    static final int STATUS_BAR_HEIGHT = 25;
    private static final String TAG = "HeroLockPatternView";
    // Aspect to use when rendering this view
    private static final int ASPECT_SQUARE = 0; // View will be the minimum of
    // width/height
    private static final int ASPECT_LOCK_WIDTH = 1; // Fixed width; height will
    // be minimum of (w,h)
    // be minimum of (w,h)
    private static final int ASPECT_LOCK_HEIGHT = 2; // Fixed height; width will
    private static final boolean PROFILE_DRAWING = false;
    /**
     * How many milliseconds we spend animating each circle of a lock pattern if
     * the animating mode is set. The entire animation should take this constant
     * * the length of the pattern to complete.
     */
    private static final int MILLIS_PER_CIRCLE_ANIMATING = 700;
    private final Path mCurrentPath = new Path();
    private final Rect mInvalidate = new Rect();
    private boolean mDrawingProfilingStarted = false;
    private Paint mPaint = new Paint();
    private Paint mPathPaint = new Paint();
    private int lineColor;
    private OnPatternListener mOnPatternListener;
    private ArrayList<LockPatternUtils.Cell> mPattern = new ArrayList<LockPatternUtils.Cell>(9);
    /**
     * Lookup table for the circles of the pattern we are currently drawing.
     * This will be the cells of the complete pattern unless we are animating,
     * in which case we use this to hold the cells we are drawing for the in
     * progress animation.
     */
    private boolean[][] mPatternDrawLookup = new boolean[3][3];
    /**
     * the in progress point: - during interaction: where the user's finger is -
     * during animation: the current tip of the animating line
     */
    private float mInProgressX = -1;
    private float mInProgressY = -1;
    private long mAnimatingPeriodStart;
    private DisplayMode mPatternDisplayMode = DisplayMode.Correct;
    private boolean mInputEnabled = true;
    private boolean mInStealthMode = false;
    private boolean mEnableHapticFeedback = true;
    private boolean mPatternInProgress = false;
    private float mDiameterFactor = 0.10f; // TODO: move to attrs
    private float mHitFactor = 0.6f;
    private float mSquareWidth;
    private float mSquareHeight;

    private Drawable mDrawableCircleDefault;
    private Drawable mDrawableCircleGreen;
    private Drawable mDrawableCircleRed;
    private Drawable mDrawableArrowGreenUp;
    private Drawable mDrawableArrowRedUp;

    private int mCellWidth;
    private int mCellHeight;
    private int mAspect;
    private boolean showBigTrack;
    private JSONObject actionObject;
    private int tintColor;
    private int normalColor;
    private Handler handler = new Handler();
    private final long delayTime = 1000;

    public HeroLockPatternView(Context context) {
        this(context, null);
        init(null);
    }

    public HeroLockPatternView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs);
    }

    private void init(AttributeSet attrs) {
        lineColor = getResources().getColor(R.color.c0);

        if (attrs != null) {
            TypedArray a = getContext().obtainStyledAttributes(attrs, R.styleable.LockPatternView);

            final String aspect = a.getString(R.styleable.LockPatternView_aspect);

            if ("square".equals(aspect)) {
                mAspect = ASPECT_SQUARE;
            } else if ("lock_width".equals(aspect)) {
                mAspect = ASPECT_LOCK_WIDTH;
            } else if ("lock_height".equals(aspect)) {
                mAspect = ASPECT_LOCK_HEIGHT;
            } else {
                mAspect = ASPECT_SQUARE;
            }

            int cellSize = a.getDimensionPixelSize(R.styleable.LockPatternView_cellSize, 0);
            if (0 == cellSize) {
                throw new IllegalArgumentException("The cellSize attr is required.");
            }
            mCellHeight = cellSize;
            mCellWidth = cellSize;

            setClickable(true);

            mPathPaint.setAntiAlias(true);
            mPathPaint.setDither(true);
            mPathPaint.setColor(lineColor); // TODO this should be from the style
            mPathPaint.setStyle(Paint.Style.STROKE);
            mPathPaint.setStrokeJoin(Paint.Join.ROUND);
            mPathPaint.setStrokeCap(Paint.Cap.ROUND);

            mDrawableCircleDefault = a.getDrawable(R.styleable.LockPatternView_circleNormal);
            mDrawableCircleGreen = a.getDrawable(R.styleable.LockPatternView_circleCorrect);
            mDrawableCircleRed = a.getDrawable(R.styleable.LockPatternView_circleError);
            mDrawableArrowGreenUp = a.getDrawable(R.styleable.LockPatternView_arrowCorrectUp);
            mDrawableArrowRedUp = a.getDrawable(R.styleable.LockPatternView_arrowErrorUp);
            a.recycle();
        } else {
            Resources res = getResources();
            mAspect = ASPECT_SQUARE;
            int cellSize = res.getDimensionPixelSize(R.dimen.gesture_pattern_item_size);
            mCellHeight = cellSize;
            mCellWidth = cellSize;

            setClickable(true);

            mPathPaint.setAntiAlias(true);
            mPathPaint.setDither(true);
            mPathPaint.setColor(lineColor); // TODO this should be from the style
            mPathPaint.setStyle(Paint.Style.STROKE);
            mPathPaint.setStrokeJoin(Paint.Join.ROUND);
            mPathPaint.setStrokeCap(Paint.Cap.ROUND);

            mDrawableCircleDefault = res.getDrawable(R.drawable.gesture_pattern_item_off);
            mDrawableCircleGreen = res.getDrawable(R.drawable.gesture_pattern_item_on);
            mDrawableCircleRed = res.getDrawable(R.drawable.gesture_pattern_item_error);
            mDrawableArrowGreenUp = res.getDrawable(R.drawable.gesture_pattern_item_on);
            mDrawableArrowRedUp = res.getDrawable(R.drawable.gesture_pattern_item_error);
        }
    }

    /**
     * @return Whether the view is in stealth mode.
     */
    public boolean isInStealthMode() {
        return mInStealthMode;
    }

    /**
     * Set whether the view is in stealth mode. If true, there will be no
     * visible feedback as the user enters the pattern.
     *
     * @param inStealthMode Whether in stealth mode.
     */
    public void setInStealthMode(boolean inStealthMode) {
        mInStealthMode = inStealthMode;
    }

    /**
     * @return Whether the view has tactile feedback enabled.
     */
    public boolean isTactileFeedbackEnabled() {
        return mEnableHapticFeedback;
    }

    /**
     * Set whether the view will use tactile feedback. If true, there will be
     * tactile feedback as the user enters the pattern.
     *
     * @param tactileFeedbackEnabled Whether tactile feedback is enabled
     */
    public void setTactileFeedbackEnabled(boolean tactileFeedbackEnabled) {
        mEnableHapticFeedback = tactileFeedbackEnabled;
    }

    /**
     * Set the call back for pattern detection.
     *
     * @param onPatternListener The call back.
     */
    public void setOnPatternListener(OnPatternListener onPatternListener) {
        mOnPatternListener = onPatternListener;
    }

    /**
     * Set the pattern explicitely (rather than waiting for the user to input a
     * pattern).
     *
     * @param displayMode How to display the pattern.
     * @param pattern     The pattern.
     */
    public void setPattern(DisplayMode displayMode, List<LockPatternUtils.Cell> pattern) {
        mPattern.clear();
        mPattern.addAll(pattern);
        clearPatternDrawLookup();
        for (LockPatternUtils.Cell cell : pattern) {
            mPatternDrawLookup[cell.getRow()][cell.getColumn()] = true;
        }

        setDisplayMode(displayMode);
    }

    /**
     * Set the display mode of the current pattern. This can be useful, for
     * instance, after detecting a pattern to tell this view whether change the
     * in progress result to correct or wrong.
     *
     * @param displayMode The display mode.
     */
    public void setDisplayMode(DisplayMode displayMode) {
        mPatternDisplayMode = displayMode;
        if (displayMode == DisplayMode.Animate) {
            if (mPattern.size() == 0) {
                throw new IllegalStateException("you must have a pattern to " + "animate if you want to set the display mode to animate");
            }
            mAnimatingPeriodStart = SystemClock.elapsedRealtime();
            final LockPatternUtils.Cell first = mPattern.get(0);
            mInProgressX = getCenterXForColumn(first.getColumn());
            mInProgressY = getCenterYForRow(first.getRow());
            clearPatternDrawLookup();
        }
        invalidate();
    }

    private void notifyCellAdded() {
        if (mOnPatternListener != null) {
            mOnPatternListener.onPatternCellAdded(mPattern);
        }
        sendPattern(PatternType.ADD, mPattern);
        //        sendAccessEvent(R.string.lockPatternView_cellAdded);
    }

    private void notifyPatternStarted() {
        if (mOnPatternListener != null) {
            mOnPatternListener.onPatternStart();
        }
        //        sendAccessEvent(R.string.lockPatternView_start);
    }

    private void notifyPatternDetected() {
        if (mOnPatternListener != null) {
            mOnPatternListener.onPatternDetected(mPattern);
        }
        sendPattern(PatternType.END, mPattern);
        postClearPattern(delayTime);
        //        sendAccessEvent(R.string.lockPatternView_detected);
    }

    private void notifyPatternCleared() {
        if (mOnPatternListener != null) {
            mOnPatternListener.onPatternCleared();
        }
        //        sendAccessEvent(R.string.lockPatternView_cleared);
    }

    /**
     * Clear the pattern.
     */
    public void clearPattern() {
        resetPattern();
    }

    /**
     * Reset all pattern state.
     */
    private void resetPattern() {
        mPattern.clear();
        clearPatternDrawLookup();
        mPatternDisplayMode = DisplayMode.Correct;
        invalidate();
    }

    /**
     * Clear the pattern lookup table.
     */
    private void clearPatternDrawLookup() {
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                mPatternDrawLookup[i][j] = false;
            }
        }
    }

    /**
     * Disable input (for instance when displaying a message that will timeout
     * so user doesn't get view into messy state).
     */
    public void disableInput() {
        mInputEnabled = false;
    }

    /**
     * Enable input.
     */
    public void enableInput() {
        mInputEnabled = true;
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        final int width = w - getPaddingLeft() - getPaddingRight();
        mSquareWidth = width / 3.0f;

        final int height = h - getPaddingTop() - getPaddingBottom();
        mSquareHeight = height / 3.0f;
    }

    private int resolveMeasured(int measureSpec, int desired) {
        int result = 0;
        int specSize = MeasureSpec.getSize(measureSpec);
        switch (MeasureSpec.getMode(measureSpec)) {
            case MeasureSpec.UNSPECIFIED:
                result = desired;
                break;
            case MeasureSpec.AT_MOST:
                result = Math.max(specSize, desired);
                break;
            case MeasureSpec.EXACTLY:
            default:
                result = specSize;
        }
        return result;
    }

    @Override
    protected int getSuggestedMinimumWidth() {
        // View should be large enough to contain 3 side-by-side target bitmaps
        return 3 * mCellWidth;
    }

    @Override
    protected int getSuggestedMinimumHeight() {
        // View should be large enough to contain 3 side-by-side target bitmaps
        return 3 * mCellWidth;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        final int minimumWidth = getSuggestedMinimumWidth();
        final int minimumHeight = getSuggestedMinimumHeight();
        int viewWidth = resolveMeasured(widthMeasureSpec, minimumWidth);
        int viewHeight = resolveMeasured(heightMeasureSpec, minimumHeight);

        switch (mAspect) {
            case ASPECT_SQUARE:
                viewWidth = viewHeight = Math.min(viewWidth, viewHeight);
                break;
            case ASPECT_LOCK_WIDTH:
                viewHeight = Math.min(viewWidth, viewHeight);
                break;
            case ASPECT_LOCK_HEIGHT:
                viewWidth = Math.min(viewWidth, viewHeight);
                break;
        }
        Log.v(TAG, "LockPatternView dimensions: " + viewWidth + "x" + viewHeight);
        setMeasuredDimension(viewWidth, viewHeight);
    }

    /**
     * Determines whether the point x, y will add a new point to the current
     * pattern (in addition to finding the cell, also makes heuristic choices
     * such as filling in gaps based on current pattern).
     *
     * @param x The x coordinate.
     * @param y The y coordinate.
     */
    private LockPatternUtils.Cell detectAndAddHit(float x, float y) {
        final LockPatternUtils.Cell cell = checkForNewHit(x, y);
        if (cell != null) {

            // check for gaps in existing pattern
            LockPatternUtils.Cell fillInGapCell = null;
            final ArrayList<LockPatternUtils.Cell> pattern = mPattern;
            if (!pattern.isEmpty()) {
                final LockPatternUtils.Cell lastCell = pattern.get(pattern.size() - 1);
                int dRow = cell.row - lastCell.row;
                int dColumn = cell.column - lastCell.column;

                int fillInRow = lastCell.row;
                int fillInColumn = lastCell.column;

                if (Math.abs(dRow) == 2 && Math.abs(dColumn) != 1) {
                    fillInRow = lastCell.row + ((dRow > 0) ? 1 : -1);
                }

                if (Math.abs(dColumn) == 2 && Math.abs(dRow) != 1) {
                    fillInColumn = lastCell.column + ((dColumn > 0) ? 1 : -1);
                }

                fillInGapCell = LockPatternUtils.Cell.of(fillInRow, fillInColumn);
            }

            if (fillInGapCell != null && !mPatternDrawLookup[fillInGapCell.row][fillInGapCell.column]) {
                addCellToPattern(fillInGapCell);
            }
            addCellToPattern(cell);
            if (mEnableHapticFeedback) {
                performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY, HapticFeedbackConstants.FLAG_IGNORE_VIEW_SETTING | HapticFeedbackConstants.FLAG_IGNORE_GLOBAL_SETTING);
            }
            return cell;
        }
        return null;
    }

    private void addCellToPattern(LockPatternUtils.Cell newCell) {
        mPatternDrawLookup[newCell.getRow()][newCell.getColumn()] = true;
        mPattern.add(newCell);
        notifyCellAdded();
    }

    // helper method to find which cell a point maps to
    private LockPatternUtils.Cell checkForNewHit(float x, float y) {

        final int rowHit = getRowHit(y);
        if (rowHit < 0) {
            return null;
        }
        final int columnHit = getColumnHit(x);
        if (columnHit < 0) {
            return null;
        }

        if (mPatternDrawLookup[rowHit][columnHit]) {
            return null;
        }
        return LockPatternUtils.Cell.of(rowHit, columnHit);
    }

    /**
     * Helper method to find the row that y falls into.
     *
     * @param y The y coordinate
     * @return The row that y falls in, or -1 if it falls in no row.
     */
    private int getRowHit(float y) {

        final float squareHeight = mSquareHeight;
        float hitSize = squareHeight * mHitFactor;

        float offset = getPaddingTop() + (squareHeight - hitSize) / 2f;
        for (int i = 0; i < 3; i++) {

            final float hitTop = offset + squareHeight * i;
            if (y >= hitTop && y <= hitTop + hitSize) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Helper method to find the column x fallis into.
     *
     * @param x The x coordinate.
     * @return The column that x falls in, or -1 if it falls in no column.
     */
    private int getColumnHit(float x) {
        final float squareWidth = mSquareWidth;
        float hitSize = squareWidth * mHitFactor;

        float offset = getPaddingLeft() + (squareWidth - hitSize) / 2f;
        for (int i = 0; i < 3; i++) {

            final float hitLeft = offset + squareWidth * i;
            if (x >= hitLeft && x <= hitLeft + hitSize) {
                return i;
            }
        }
        return -1;
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (!mInputEnabled || !isEnabled()) {
            return false;
        }

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                handleActionDown(event);
                return true;
            case MotionEvent.ACTION_UP:
                handleActionUp(event);
                return true;
            case MotionEvent.ACTION_MOVE:
                handleActionMove(event);
                return true;
            case MotionEvent.ACTION_CANCEL:
                resetPattern();
                mPatternInProgress = false;
                notifyPatternCleared();
                if (PROFILE_DRAWING) {
                    if (mDrawingProfilingStarted) {
                        Debug.stopMethodTracing();
                        mDrawingProfilingStarted = false;
                    }
                }
                return true;
        }
        return false;
    }

    private void handleActionMove(MotionEvent event) {
        // Handle all recent motion events so we don't skip any cells even when
        // the device
        // is busy...
        final int historySize = event.getHistorySize();
        for (int i = 0; i < historySize + 1; i++) {
            final float x = i < historySize ? event.getHistoricalX(i) : event.getX();
            final float y = i < historySize ? event.getHistoricalY(i) : event.getY();
            final int patternSizePreHitDetect = mPattern.size();
            LockPatternUtils.Cell hitCell = detectAndAddHit(x, y);
            final int patternSize = mPattern.size();
            if (hitCell != null && patternSize == 1) {
                mPatternInProgress = true;
                notifyPatternStarted();
            }
            // note current x and y for rubber banding of in progress patterns
            final float dx = Math.abs(x - mInProgressX);
            final float dy = Math.abs(y - mInProgressY);
            if (dx + dy > mSquareWidth * 0.01f) {
                float oldX = mInProgressX;
                float oldY = mInProgressY;

                mInProgressX = x;
                mInProgressY = y;

                if (mPatternInProgress && patternSize > 0) {
                    final ArrayList<LockPatternUtils.Cell> pattern = mPattern;
                    final float radius = mSquareWidth * mDiameterFactor * 0.5f;

                    final LockPatternUtils.Cell lastCell = pattern.get(patternSize - 1);

                    float startX = getCenterXForColumn(lastCell.column);
                    float startY = getCenterYForRow(lastCell.row);

                    float left;
                    float top;
                    float right;
                    float bottom;

                    final Rect invalidateRect = mInvalidate;

                    if (startX < x) {
                        left = startX;
                        right = x;
                    } else {
                        left = x;
                        right = startX;
                    }

                    if (startY < y) {
                        top = startY;
                        bottom = y;
                    } else {
                        top = y;
                        bottom = startY;
                    }

                    // Invalidate between the pattern's last cell and the
                    // current location
                    invalidateRect.set((int) (left - radius), (int) (top - radius), (int) (right + radius), (int) (bottom + radius));

                    if (startX < oldX) {
                        left = startX;
                        right = oldX;
                    } else {
                        left = oldX;
                        right = startX;
                    }

                    if (startY < oldY) {
                        top = startY;
                        bottom = oldY;
                    } else {
                        top = oldY;
                        bottom = startY;
                    }

                    // Invalidate between the pattern's last cell and the
                    // previous location
                    invalidateRect.union((int) (left - radius), (int) (top - radius), (int) (right + radius), (int) (bottom + radius));

                    // Invalidate between the pattern's new cell and the
                    // pattern's previous cell
                    if (hitCell != null) {
                        startX = getCenterXForColumn(hitCell.column);
                        startY = getCenterYForRow(hitCell.row);

                        if (patternSize >= 2) {
                            // (re-using hitcell for old cell)
                            hitCell = pattern.get(patternSize - 1 - (patternSize - patternSizePreHitDetect));
                            oldX = getCenterXForColumn(hitCell.column);
                            oldY = getCenterYForRow(hitCell.row);

                            if (startX < oldX) {
                                left = startX;
                                right = oldX;
                            } else {
                                left = oldX;
                                right = startX;
                            }

                            if (startY < oldY) {
                                top = startY;
                                bottom = oldY;
                            } else {
                                top = oldY;
                                bottom = startY;
                            }
                        } else {
                            left = right = startX;
                            top = bottom = startY;
                        }

                        final float widthOffset = mSquareWidth / 2f;
                        final float heightOffset = mSquareHeight / 2f;

                        invalidateRect.set((int) (left - widthOffset), (int) (top - heightOffset), (int) (right + widthOffset), (int) (bottom + heightOffset));
                    }

                    invalidate(invalidateRect);
                } else {
                    invalidate();
                }
            }
        }
    }

    private void sendAccessEvent(int resId) {
        setContentDescription(getContext().getString(resId));
        sendAccessibilityEvent(AccessibilityEvent.TYPE_VIEW_SELECTED);
        setContentDescription(null);
    }

    private void handleActionUp(MotionEvent event) {
        // report pattern detected
        if (!mPattern.isEmpty()) {
            mPatternInProgress = false;
            notifyPatternDetected();
            invalidate();
        }
        if (PROFILE_DRAWING) {
            if (mDrawingProfilingStarted) {
                Debug.stopMethodTracing();
                mDrawingProfilingStarted = false;
            }
        }
    }

    private void handleActionDown(MotionEvent event) {
        resetPattern();
        final float x = event.getX();
        final float y = event.getY();
        final LockPatternUtils.Cell hitCell = detectAndAddHit(x, y);
        if (hitCell != null) {
            mPatternInProgress = true;
            mPatternDisplayMode = DisplayMode.Correct;
            notifyPatternStarted();
        } else {
            mPatternInProgress = false;
            notifyPatternCleared();
        }
        if (hitCell != null) {
            final float startX = getCenterXForColumn(hitCell.column);
            final float startY = getCenterYForRow(hitCell.row);

            final float widthOffset = mSquareWidth / 2f;
            final float heightOffset = mSquareHeight / 2f;

            invalidate((int) (startX - widthOffset), (int) (startY - heightOffset), (int) (startX + widthOffset), (int) (startY + heightOffset));
        }
        mInProgressX = x;
        mInProgressY = y;
        if (PROFILE_DRAWING) {
            if (!mDrawingProfilingStarted) {
                Debug.startMethodTracing("LockPatternDrawing");
                mDrawingProfilingStarted = true;
            }
        }
    }

    private float getCenterXForColumn(int column) {
        return getPaddingLeft() + column * mSquareWidth + mSquareWidth / 2f;
    }

    private float getCenterYForRow(int row) {
        return getPaddingTop() + row * mSquareHeight + mSquareHeight / 2f;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        final ArrayList<LockPatternUtils.Cell> pattern = mPattern;
        final int count = pattern.size();
        final boolean[][] drawLookup = mPatternDrawLookup;

        if (mPatternDisplayMode == DisplayMode.Animate) {

            // figure out which circles to draw

            // + 1 so we pause on complete pattern
            final int oneCycle = (count + 1) * MILLIS_PER_CIRCLE_ANIMATING;
            final int spotInCycle = (int) (SystemClock.elapsedRealtime() - mAnimatingPeriodStart) % oneCycle;
            final int numCircles = spotInCycle / MILLIS_PER_CIRCLE_ANIMATING;

            clearPatternDrawLookup();
            for (int i = 0; i < numCircles; i++) {
                final LockPatternUtils.Cell cell = pattern.get(i);
                drawLookup[cell.getRow()][cell.getColumn()] = true;
            }

            // figure out in progress portion of ghosting line

            final boolean needToUpdateInProgressPoint = numCircles > 0 && numCircles < count;

            if (needToUpdateInProgressPoint) {
                final float percentageOfNextCircle = ((float) (spotInCycle % MILLIS_PER_CIRCLE_ANIMATING)) / MILLIS_PER_CIRCLE_ANIMATING;

                final LockPatternUtils.Cell currentCell = pattern.get(numCircles - 1);
                final float centerX = getCenterXForColumn(currentCell.column);
                final float centerY = getCenterYForRow(currentCell.row);

                final LockPatternUtils.Cell nextCell = pattern.get(numCircles);
                final float dx = percentageOfNextCircle * (getCenterXForColumn(nextCell.column) - centerX);
                final float dy = percentageOfNextCircle * (getCenterYForRow(nextCell.row) - centerY);
                mInProgressX = centerX + dx;
                mInProgressY = centerY + dy;
            }
            // TODO: Infinite loop here...
            invalidate();
        }

        final float squareWidth = mSquareWidth;
        final float squareHeight = mSquareHeight;

        float radius = (squareWidth * mDiameterFactor * 0.25f);
        mPathPaint.setStrokeWidth(radius);

        final Path currentPath = mCurrentPath;
        currentPath.rewind();

        // TODO: the path should be created and cached every time we hit-detect
        // a cell
        // only the last segment of the path should be computed here
        // draw the path of the pattern (unless the user is in progress, and
        // we are in stealth mode)
        final boolean drawPath = (!mInStealthMode || mPatternDisplayMode == DisplayMode.Wrong);

        // draw the arrows associated with the path (unless the user is in
        // progress, and
        // we are in stealth mode)
        boolean oldFlag = (mPaint.getFlags() & Paint.FILTER_BITMAP_FLAG) != 0;
        mPaint.setFilterBitmap(true); // draw with higher quality since we
        // render with transforms
        // draw the lines
        if (drawPath) {
            boolean anyCircles = false;
            for (int i = 0; i < count; i++) {
                LockPatternUtils.Cell cell = pattern.get(i);

                // only draw the part of the pattern stored in
                // the lookup table (this is only different in the case
                // of animation).
                if (!drawLookup[cell.row][cell.column]) {
                    break;
                }
                anyCircles = true;

                float centerX = getCenterXForColumn(cell.column);
                float centerY = getCenterYForRow(cell.row);
                if (i == 0) {
                    currentPath.moveTo(centerX, centerY);
                } else {
                    currentPath.lineTo(centerX, centerY);
                }
            }

            // add last in progress section
            if ((mPatternInProgress || mPatternDisplayMode == DisplayMode.Animate) && anyCircles) {
                currentPath.lineTo(mInProgressX, mInProgressY);
            }
            // chang the line color in different DisplayMode
            if (mPatternDisplayMode == DisplayMode.Wrong) {
                mPathPaint.setColor(Color.RED);
            } else {
                mPathPaint.setColor(lineColor);
            }
            //            if (!showBigTrack) {
            canvas.drawPath(currentPath, mPathPaint);
            //            }
        }

        // draw the circles
        final int paddingTop = getPaddingTop();
        final int paddingLeft = getPaddingLeft();

        for (int i = 0; i < 3; i++) {
            float topY = paddingTop + i * squareHeight;
            // float centerY = mPaddingTop + i * mSquareHeight + (mSquareHeight
            // / 2);
            for (int j = 0; j < 3; j++) {
                float leftX = paddingLeft + j * squareWidth;
                drawCircle(canvas, (int) leftX, (int) topY, drawLookup[i][j]);
            }
        }

        if (drawPath) {
            for (int i = 0; i < count - 1; i++) {
                LockPatternUtils.Cell cell = pattern.get(i);
                LockPatternUtils.Cell next = pattern.get(i + 1);

                // only draw the part of the pattern stored in
                // the lookup table (this is only different in the case
                // of animation).
                if (!drawLookup[next.row][next.column]) {
                    break;
                }

                float leftX = paddingLeft + cell.column * squareWidth;
                float topY = paddingTop + cell.row * squareHeight;

                drawArrow(canvas, leftX, topY, cell, next);
            }
        }

        mPaint.setFilterBitmap(oldFlag); // restore default flag
    }

    private void drawArrow(Canvas canvas, float leftX, float topY, LockPatternUtils.Cell start, LockPatternUtils.Cell end) {
        boolean green = mPatternDisplayMode != DisplayMode.Wrong;

        final int endRow = end.row;
        final int startRow = start.row;
        final int endColumn = end.column;
        final int startColumn = start.column;

        // offsets for centering the bitmap in the cell
        final int offsetX = ((int) mSquareWidth - mCellWidth) / 2;
        final int offsetY = ((int) mSquareHeight - mCellHeight) / 2;

        // compute transform to place arrow bitmaps at correct angle inside
        // circle.
        // This assumes that the arrow image is drawn at 12:00 with it's top
        // edge
        // coincident with the circle bitmap's top edge.
        Drawable arrowDrawable = green ? mDrawableArrowGreenUp : mDrawableArrowRedUp;
        final int cellWidth = mCellWidth;
        final int cellHeight = mCellHeight;

        // the up arrow bitmap is at 12:00, so find the rotation from x axis and
        // add 90 degrees.
        final float theta = (float) Math.atan2(endRow - startRow, endColumn - startColumn);
        final float angle = (float) Math.toDegrees(theta) + 90.0f;

        // compose matrix
        float sx = Math.min(mSquareWidth / mCellWidth, 1.0f);
        float sy = Math.min(mSquareHeight / mCellHeight, 1.0f);

        int realWidth = (int) ((float) cellWidth * sx);
        int realHeight = (int) ((float) cellHeight * sy);
        int realLeft = (int) leftX + offsetX;
        int realTop = (int) topY + offsetY;
        arrowDrawable.setBounds(realLeft, realTop, realLeft + realWidth, realTop + realHeight);
        arrowDrawable.draw(canvas);
    }

    /**
     * @param partOfPattern Whether this circle is part of the pattern.
     */
    private void drawCircle(Canvas canvas, int leftX, int topY, boolean partOfPattern) {
        Drawable outerCircle;
        Drawable innerCircle;

        if (!partOfPattern || (mInStealthMode && mPatternDisplayMode != DisplayMode.Wrong)) {
            // unselected circle
            outerCircle = mDrawableCircleDefault;
            innerCircle = null;
        } else if (mPatternInProgress) {
            // user is in middle of drawing a pattern
            outerCircle = mDrawableCircleDefault;
            innerCircle = mDrawableCircleGreen;
        } else if (mPatternDisplayMode == DisplayMode.Wrong) {
            // the pattern is wrong
            outerCircle = mDrawableCircleDefault;
            innerCircle = mDrawableCircleRed;
        } else if (mPatternDisplayMode == DisplayMode.Correct || mPatternDisplayMode == DisplayMode.Animate) {
            // the pattern is correct
            outerCircle = mDrawableCircleDefault;
            innerCircle = mDrawableCircleGreen;
        } else {
            throw new IllegalStateException("unknown display mode " + mPatternDisplayMode);
        }

        final int width = mCellWidth;
        final int height = mCellHeight;

        final float squareWidth = mSquareWidth;
        final float squareHeight = mSquareHeight;

        int offsetX = (int) ((squareWidth - width) / 2f);
        int offsetY = (int) ((squareHeight - height) / 2f);

        // Allow circles to shrink if the view is too small to hold them.
        float sx = Math.min(mSquareWidth / mCellWidth, 1.0f);
        float sy = Math.min(mSquareHeight / mCellHeight, 1.0f);

        if (innerCircle == null) {
            int realWidth = (int) ((float) width * sx);
            int realHeight = (int) ((float) height * sy);
            int realLeft = leftX + offsetX;
            int realTop = topY + offsetY;
            outerCircle.setBounds(realLeft, realTop, realLeft + realWidth, realTop + realHeight);
            outerCircle.draw(canvas);
        } else {
            int realWidth = (int) ((float) width * sx);
            int realHeight = (int) ((float) height * sy);
            int realLeft = leftX + offsetX;
            int realTop = topY + offsetY;
            innerCircle.setBounds(realLeft, realTop, realLeft + realWidth, realTop + realHeight);
            innerCircle.draw(canvas);
        }

    }

    @Override
    protected Parcelable onSaveInstanceState() {
        Parcelable superState = super.onSaveInstanceState();
        return new SavedState(superState, LockPatternUtils.patternToString(mPattern), mPatternDisplayMode.ordinal(), mInputEnabled, mInStealthMode, mEnableHapticFeedback);
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        final SavedState ss = (SavedState) state;
        super.onRestoreInstanceState(ss.getSuperState());
        setPattern(DisplayMode.Correct, LockPatternUtils.stringToPattern(ss.getSerializedPattern()));
        mPatternDisplayMode = DisplayMode.values()[ss.getDisplayMode()];
        mInputEnabled = ss.isInputEnabled();
        mInStealthMode = ss.isInStealthMode();
        mEnableHapticFeedback = ss.isTactileFeedbackEnabled();
    }

    // Hero related methods
    @Override
    public void on(JSONObject jsonObject) throws JSONException {
        HeroView.on(this, jsonObject);

        if (jsonObject.has("frame")) {
            FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) this.getLayoutParams();
            int width = params.width;
            int height = params.height;
            if (width < mCellWidth * 3 || height < mCellHeight * 3) {
                mCellHeight = height / 5;
                mCellWidth = width / 5;
            }
        }

        if (jsonObject.has("track")) {
            boolean needTrack = jsonObject.getBoolean("track");
            if (!needTrack) {
                mInStealthMode = true;
            }
        }

        if (jsonObject.has("bigTrack")) {
            showBigTrack = jsonObject.getBoolean("bigTrack");
            if (showBigTrack) {
                mDrawableCircleGreen = getResources().getDrawable(R.drawable.gesture_pattern_fill_on);
                mDrawableCircleRed = getResources().getDrawable(R.drawable.gesture_pattern_fill_error);
                mDrawableArrowGreenUp = getResources().getDrawable(R.drawable.gesture_pattern_fill_on);
                mDrawableArrowRedUp = getResources().getDrawable(R.drawable.gesture_pattern_fill_error);
            }
        }

        if (jsonObject.has("values")) {
            JSONArray values = jsonObject.getJSONArray("values");
            setPattern(DisplayMode.Correct, LockPatternUtils.arrayToPattern(values));
        }

        if (jsonObject.has("tintColor")) {
            String color = jsonObject.getString("tintColor");
            tintColor = HeroView.parseColor(color);
            changeDrawableColor(mDrawableCircleGreen, tintColor);
            changeDrawableColor(mDrawableArrowGreenUp, tintColor);
            lineColor = tintColor;
            mPathPaint.setColor(tintColor);
        }

        if (jsonObject.has("color")) {
            String color = jsonObject.getString("color");
            normalColor = HeroView.parseColor(color);
            changeDrawableStrokeColor(mDrawableCircleDefault, normalColor);
        }

        if (jsonObject.has("action")) {
            actionObject = jsonObject.getJSONObject("action");
        }
    }

    private void sendPattern(PatternType type, ArrayList<LockPatternUtils.Cell> pattern) {
        JSONObject jsonObject = new JSONObject();
        JSONArray data = LockPatternUtils.patternToArray(mPattern);

        if (actionObject != null) {
            try {
                jsonObject.put("values", data);
                jsonObject.put("end", (type == PatternType.END) ? true : false);
                HeroView.putValueToJson(actionObject, jsonObject);
                ((IHeroContext) getContext()).on(actionObject);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    private void postClearPattern(long time) {
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (!mPatternInProgress) {
                    clearPattern();
                }
            }
        }, time);
    }

    private void changeDrawableColor(Drawable drawable, int color) {
        if (drawable instanceof ShapeDrawable) {
            ((ShapeDrawable)drawable).getPaint().setColor(color);
        } else if (drawable instanceof GradientDrawable) {
            ((GradientDrawable)drawable).setColor(color);
        }
    }

    private void changeDrawableStrokeColor(Drawable drawable, int color) {
        if (drawable instanceof GradientDrawable) {
            ((GradientDrawable) drawable).setStroke(HeroView.dip2px(getContext(), 1), color, 0, 0);
        }
    }

    // Hero related methods end

    /**
     * How to display the current pattern.
     */
    public enum DisplayMode {

        /**
         * The pattern drawn is correct (i.e draw it in a friendly color)
         */
        Correct,

        /**
         * Animate the pattern (for demo, and help).
         */
        Animate,

        /**
         * The pattern is wrong (i.e draw a foreboding color)
         */
        Wrong
    }

    public enum PatternType {
        ADD,
        CLEAR,
        END
    }

    /**
     * The call back interface for detecting patterns entered by the user.
     */
    public static interface OnPatternListener {

        /**
         * A new pattern has begun.
         */
        void onPatternStart();

        /**
         * The pattern was cleared.
         */
        void onPatternCleared();

        /**
         * The user extended the pattern currently being drawn by one cell.
         *
         * @param pattern The pattern with newly added cell.
         */
        void onPatternCellAdded(List<LockPatternUtils.Cell> pattern);

        /**
         * A pattern was detected from the user.
         *
         * @param pattern The pattern.
         */
        void onPatternDetected(List<LockPatternUtils.Cell> pattern);
    }

    /**
     * The parecelable for saving and restoring a lock pattern view.
     */
    private static class SavedState extends BaseSavedState {

        @SuppressWarnings("unused")
        public static final Creator<SavedState> CREATOR = new Creator<SavedState>() {
            public SavedState createFromParcel(Parcel in) {
                return new SavedState(in);
            }

            public SavedState[] newArray(int size) {
                return new SavedState[size];
            }
        };
        private final String mSerializedPattern;
        private final int mDisplayMode;
        private final boolean mInputEnabled;
        private final boolean mInStealthMode;
        private final boolean mTactileFeedbackEnabled;

        /**
         * Constructor called from {@link HeroLockPatternView#onSaveInstanceState()}
         */
        private SavedState(Parcelable superState, String serializedPattern, int displayMode, boolean inputEnabled, boolean inStealthMode, boolean tactileFeedbackEnabled) {
            super(superState);
            mSerializedPattern = serializedPattern;
            mDisplayMode = displayMode;
            mInputEnabled = inputEnabled;
            mInStealthMode = inStealthMode;
            mTactileFeedbackEnabled = tactileFeedbackEnabled;
        }

        /**
         * Constructor called from {@link #CREATOR}
         */
        private SavedState(Parcel in) {
            super(in);
            mSerializedPattern = in.readString();
            mDisplayMode = in.readInt();
            mInputEnabled = (Boolean) in.readValue(null);
            mInStealthMode = (Boolean) in.readValue(null);
            mTactileFeedbackEnabled = (Boolean) in.readValue(null);
        }

        public String getSerializedPattern() {
            return mSerializedPattern;
        }

        public int getDisplayMode() {
            return mDisplayMode;
        }

        public boolean isInputEnabled() {
            return mInputEnabled;
        }

        public boolean isInStealthMode() {
            return mInStealthMode;
        }

        public boolean isTactileFeedbackEnabled() {
            return mTactileFeedbackEnabled;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            super.writeToParcel(dest, flags);
            dest.writeString(mSerializedPattern);
            dest.writeInt(mDisplayMode);
            dest.writeValue(mInputEnabled);
            dest.writeValue(mInStealthMode);
            dest.writeValue(mTactileFeedbackEnabled);
        }

    }
}
