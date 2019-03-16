package org.xjy.android.nebula.drawable;

import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class RoundBulgeDrawable extends Drawable {
    private Paint mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private float mCornerRadius;
    private float mBulgeRadius;

    private RectF mCornerRect = new RectF();
    private RectF mBulgeRect = new RectF();

    private RoundBulgeDrawable(int color, float cornerRadius, float bulgeRadius) {
        mPaint.setColor(color);
        mCornerRadius = cornerRadius;
        mBulgeRadius = bulgeRadius;
        mPaint.setStyle(Paint.Style.FILL);
    }

    @Override
    public void draw(@NonNull Canvas canvas) {
        Rect bounds = getBounds();
        float y = bounds.top + mBulgeRadius;
        mCornerRect.set(bounds.left, y, bounds.right, bounds.bottom);
        canvas.drawRoundRect(mCornerRect, mCornerRadius, mCornerRadius, mPaint);
        float x = bounds.centerX();
        mBulgeRect.set(x - mBulgeRadius, bounds.top, x + mBulgeRadius, y + mBulgeRadius);
        canvas.drawArc(mBulgeRect, 0, -180, true, mPaint);
    }

    @Override
    public void setAlpha(int alpha) {}

    @Override
    public void setColorFilter(@Nullable ColorFilter colorFilter) {}

    @Override
    public int getOpacity() {
        return PixelFormat.UNKNOWN;
    }
}
