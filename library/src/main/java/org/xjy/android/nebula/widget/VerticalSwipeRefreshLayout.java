package org.xjy.android.nebula.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.ViewConfiguration;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

public class VerticalSwipeRefreshLayout extends SwipeRefreshLayout {
    private int mTouchSlop;
    private float mDownX;

    public VerticalSwipeRefreshLayout(@NonNull Context context) {
        this(context, null);
    }

    public VerticalSwipeRefreshLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        mTouchSlop = ViewConfiguration.get(context).getScaledTouchSlop();
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        switch (ev.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                mDownX = ev.getX();
                break;
            case MotionEvent.ACTION_MOVE:
                if (Math.abs(ev.getX() - mDownX) > mTouchSlop) {
                    return false;
                }
                break;
        }
        return super.onInterceptTouchEvent(ev);
    }
}
