package org.xjy.android.nebula.widget;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.view.animation.Interpolator;
import android.widget.FrameLayout;
import android.widget.Scroller;

import java.util.ArrayList;

import androidx.annotation.AttrRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class SnapHeaderView extends FrameLayout {
    private static final int SCROLL_STATE_IDLE = 0;
    private static final int SCROLL_STATE_DRAGGING = 1;
    private static final int SCROLL_STATE_SETTLING = 2;

    private static final int MAX_SETTLE_DURATION = 600; // ms

    private static final Interpolator sInterpolator = new Interpolator() {
        @Override
        public float getInterpolation(float t) {
            t -= 1.0f;
            return t * t * t * t * t + 1.0f;
        }
    };
    private Scroller mScroller;
    private float mTouchSlop;

    private View mHeader;
    private int mHeaderHeight;
    private float mHeaderTranslateY;

    private float mLastX;
    private float mLastY;
    private int mScrollState;

    private ArrayList<OnHeadChangeListener> mOnHeadChangeListeners;

    public SnapHeaderView(@NonNull Context context) {
        this(context, null);
    }

    public SnapHeaderView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SnapHeaderView(@NonNull Context context, @Nullable AttributeSet attrs, @AttrRes int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mScroller = new Scroller(context, sInterpolator);
        mTouchSlop = ViewConfiguration.get(context).getScaledTouchSlop();
    }

    public void addHeader(View header) {
        ViewParent parent = header.getParent();
        if (parent instanceof ViewGroup) {
            ((ViewGroup) parent).removeView(header);
        }
        addView(header);
        mHeader = header;
        int height = header.getLayoutParams().height;
        if (height > 0) {
            mHeaderHeight = height;
            updateTranslateY(-height);
        }
    }

    public View getHeader() {
        return mHeader;
    }

    public View removeHeader() {
        View header = mHeader;
        if (mHeader != null) {
            mHeaderHeight = 0;
            updateTranslateY(0);
            removeView(mHeader);
            mHeader = null;
        }
        return header;
    }

    public void addOnHeadChangeListener(OnHeadChangeListener listener) {
        if (mOnHeadChangeListeners == null) {
            mOnHeadChangeListeners = new ArrayList<>();
        }
        mOnHeadChangeListeners.add(listener);
    }

    public void removeOnHeadChangeListener(OnHeadChangeListener listener) {
        if (mOnHeadChangeListeners != null) {
            mOnHeadChangeListeners.remove(listener);
        }
    }

    public void reset() {
        if (mHeader != null) {
            mScroller.abortAnimation();
            mScrollState = SCROLL_STATE_IDLE;
            updateTranslateY(-mHeaderHeight);
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        if (mHeader != null && mHeaderHeight <= 0) {
            mHeaderHeight = mHeader.getMeasuredHeight();
            updateTranslateY(-mHeaderHeight);
        }
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        if (mHeader == null) {
            return false;
        }
        int action = ev.getAction();
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                mLastX = ev.getX();
                mLastY = ev.getY();
                if (mScrollState == SCROLL_STATE_SETTLING) {
                    mScroller.abortAnimation();
                    getParent().requestDisallowInterceptTouchEvent(true);
                    mScrollState = SCROLL_STATE_DRAGGING;
                }
                break;
            case MotionEvent.ACTION_MOVE:
                if (mScrollState != SCROLL_STATE_DRAGGING) {
                    float x = ev.getX();
                    float y = ev.getY();
                    float dx = x - mLastX;
                    float dy = y - mLastY;
                    float deltaX = Math.abs(dx);
                    float deltaY = Math.abs(dy);
                    if (deltaY > mTouchSlop && deltaY > deltaX) {
                        if ((dy > 0 && (mHeaderTranslateY <= 0 && !canScroll(this, false, (int) dy, (int) x, (int) y)))
                                || (dy < 0 && mHeaderTranslateY > -mHeaderHeight)) {
                            mLastX = x;
                            mLastY = y;
                            getParent().requestDisallowInterceptTouchEvent(true);
                            mScrollState = SCROLL_STATE_DRAGGING;
                        }
                    }
                }
                break;
        }
        return mScrollState == SCROLL_STATE_DRAGGING;
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (mHeader == null) {
            return false;
        }
        int action = event.getAction();
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                mLastX = event.getX();
                mLastY = event.getY();
                break;
            case MotionEvent.ACTION_MOVE:
                float x = event.getX();
                float y = event.getY();
                float dx = x - mLastX;
                float dy = y - mLastY;
                if (mScrollState != SCROLL_STATE_DRAGGING) {
                    float deltaX = Math.abs(dx);
                    float deltaY = Math.abs(dy);
                    if (deltaY > mTouchSlop && deltaY > deltaX) {
                        if ((dy > 0 && (mHeaderTranslateY <= 0 && !canScroll(this, false, (int) dy, (int) x, (int) y)))
                                || (dy < 0 && mHeaderTranslateY > -mHeaderHeight)) {
                            getParent().requestDisallowInterceptTouchEvent(true);
                            mScrollState = SCROLL_STATE_DRAGGING;
                        }
                    }
                }
                if (mScrollState == SCROLL_STATE_DRAGGING) {
                    mLastX = x;
                    mLastY = y;
                    drag(dy);
                }
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                if (mScrollState == SCROLL_STATE_DRAGGING) {
                    scroll();
                }
                break;
        }
        return true;
    }

    @Override
    public void computeScroll() {
        if (!mScroller.isFinished() && mScroller.computeScrollOffset()) {
            int y = mScroller.getCurrY();
            updateTranslateY(y);
            postInvalidateOnAnimation();
            return;
        }
        if (mScrollState == SCROLL_STATE_SETTLING) {
            updateTranslateY(mHeaderTranslateY > -mHeaderHeight / 2f ? 0 : -mHeaderHeight);
            mScrollState = SCROLL_STATE_IDLE;
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        mScroller.abortAnimation();
        super.onDetachedFromWindow();
    }

    private void updateTranslateY(float translateY) {
        float newTranslateY = Math.max(-mHeaderHeight, Math.min(translateY, 0));
        if (newTranslateY != mHeaderTranslateY) {
            mHeaderTranslateY = newTranslateY;
            mHeader.setTranslationY(mHeaderTranslateY);
            float otherTranslateY = mHeaderHeight + mHeaderTranslateY;
            int childCount = getChildCount();
            for (int i = 0; i < childCount; i++) {
                View child = getChildAt(i);
                if (child != mHeader) {
                    child.setTranslationY(otherTranslateY);
                }
            }

            if (mHeaderHeight > 0 && mOnHeadChangeListeners != null) {
                boolean show = mHeaderTranslateY == 0;
                if (show || mHeaderTranslateY == -mHeaderHeight) {
                    for (int i = mOnHeadChangeListeners.size() - 1; i >= 0; i--) {
                        mOnHeadChangeListeners.get(i).onHeadStateChanged(show);
                    }
                }
            }
        }
    }

    private boolean canScroll(View v, boolean checkV, int dy, int x, int y) {
        if (v instanceof ViewGroup) {
            final ViewGroup group = (ViewGroup) v;
            final int scrollX = v.getScrollX();
            final int scrollY = v.getScrollY();
            final int count = group.getChildCount();
            for (int i = count - 1; i >= 0; i--) {
                final View child = group.getChildAt(i);
                if (x + scrollX >= child.getLeft() && x + scrollX < child.getRight()
                        && y + scrollY >= child.getTop() && y + scrollY < child.getBottom()
                        && canScroll(child, true, dy, x + scrollX - child.getLeft(),
                        y + scrollY - child.getTop())) {
                    return true;
                }
            }
        }

        return checkV && v.canScrollVertically(-dy);
    }

    private void drag(float dy) {
        updateTranslateY(mHeaderTranslateY + dy);
        if (mHeaderTranslateY <= -mHeaderHeight || mHeaderTranslateY >= 0) {
            mScrollState = SCROLL_STATE_IDLE;
        }
    }

    private void scroll() {
        mScrollState = SCROLL_STATE_SETTLING;
        float sy = mHeaderTranslateY;
        float dy = (mHeaderTranslateY < -mHeaderHeight / 2f ? -mHeaderHeight : 0) - sy;
        mScroller.startScroll(0, (int) sy, 0, (int) dy, (int) (Math.abs(dy) / mHeaderHeight * MAX_SETTLE_DURATION));
        postInvalidateOnAnimation();
    }

    public interface OnHeadChangeListener {
        void onHeadStateChanged(boolean show);
    }
}
