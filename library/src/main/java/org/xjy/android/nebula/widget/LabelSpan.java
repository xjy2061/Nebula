package org.xjy.android.nebula.widget;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.text.style.ReplacementSpan;

import androidx.annotation.NonNull;

public class LabelSpan extends ReplacementSpan {
    private int mTextColor;
    private float mTextSize;
    private float mBorderWidth;

    private int mPaddingLeft;
    private int mPaddingTop;
    private int mPaddingRight;
    private int mPaddingBottom;

    private Paint.FontMetrics mFontMetrics = new Paint.FontMetrics();

    private CharSequence mText;
    private int mSpanWidth;
    private int mSpanHeight;
    private float mSpanTop;

    public LabelSpan(int textColor, float textSize) {
        mTextColor = textColor;
        mTextSize = textSize;
        mBorderWidth = 2;
    }

    public void setTextColor(int textColor) {
        mTextColor = textColor;
    }

    public void setTextSize(float textSize) {
        mTextSize = textSize;
    }

    public void setBorderWidth(float borderWidth) {
        mBorderWidth = borderWidth;
    }

    public void setPadding(int left, int top, int right, int bottom) {
        mPaddingLeft = left;
        mPaddingTop = top;
        mPaddingRight = right;
        mPaddingBottom = bottom;
    }

    @Override
    public int getSize(@NonNull Paint paint, CharSequence text, int start, int end, Paint.FontMetricsInt fm) {
        if (mText == null) {
            mText = text.subSequence(start, end);
        }
        paint.getFontMetrics(mFontMetrics);
        mSpanTop = mFontMetrics.ascent - mFontMetrics.top;
        float height = mFontMetrics.descent - mFontMetrics.ascent;
        paint.setTextSize(mTextSize);
        mSpanWidth = (int) (paint.measureText(mText == null ? text : mText, start, end) + mPaddingLeft + mPaddingRight + 0.5f);
        paint.getFontMetrics(mFontMetrics);
        mSpanHeight = (int) (mFontMetrics.bottom - mFontMetrics.top + mPaddingTop + mPaddingBottom + 0.5f);
        mSpanTop += (height - mSpanHeight) / 2;
        return mSpanWidth;
    }

    @Override
    public void draw(@NonNull Canvas canvas, CharSequence text, int start, int end, float x, int top, int y, int bottom, @NonNull Paint paint) {
        Paint.Style style = paint.getStyle();
        paint.setColor(mTextColor);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(mBorderWidth);
        mSpanTop = mSpanTop + top;
        float borderInset = mBorderWidth / 2;
        canvas.drawRect(x + borderInset, mSpanTop + borderInset, x + mSpanWidth - borderInset, mSpanTop + mSpanHeight - borderInset, paint);
        paint.setTextSize(mTextSize);
        paint.setStyle(style);
        canvas.drawText(mText != null ? mText : text, start, end, x + mPaddingLeft, mSpanTop + mPaddingTop - paint.getFontMetrics().top, paint);
    }
}
