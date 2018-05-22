package org.xjy.android.nebula.drawable;

import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PixelFormat;
import android.graphics.RadialGradient;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;

import org.xjy.android.common.BitmapUtils;
import org.xjy.android.common.DimensionUtils;

import java.lang.ref.WeakReference;

public class RoundRectBackgroundBlurDrawableWithShadow extends Drawable {
    private WeakReference<View> mView;
    private WeakReference<View> mBackgroundView;
    private int mBackgroundColor;
    private float mCornerRadius;
    private float mShadowSize;
    private int mShadowStartColor;
    private int mShadowEndColor;

    private Paint mPaint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG);
    private Paint mCornerShadowPaint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.DITHER_FLAG);
    private Paint mEdgeShadowPaint = new Paint(Paint.DITHER_FLAG);
    private Path mPath = new Path();
    private Path mCornerShadowPath = new Path();
    private boolean mDirty = true;
    private boolean mShadowDirty = true;

    public RoundRectBackgroundBlurDrawableWithShadow(View view) {
        mView = new WeakReference<>(view);
        mCornerRadius = DimensionUtils.dpToFloatPx(10, view.getContext());
        mShadowSize = mCornerRadius;
        mShadowStartColor = 0x07000000;
        mShadowEndColor = Color.TRANSPARENT;
    }

    public void setBackground(View backgroundView, int backgroundColor) {
        View oldBackgroundView = mBackgroundView == null ? null : mBackgroundView.get();
        if (oldBackgroundView != backgroundView || mBackgroundColor != backgroundColor) {
            mBackgroundView = backgroundView == null ? null : new WeakReference<>(backgroundView);
            mBackgroundColor = backgroundColor;
            mDirty = true;
            invalidateSelf();
        }
    }

    public void setCornerRadius(float cornerRadius) {
        if (mCornerRadius != cornerRadius) {
            mCornerRadius = cornerRadius;
            mDirty = true;
            mShadowDirty = true;
            invalidateSelf();
        }
    }

    public void setShadowSize(float shadowSize) {
        if (mShadowSize != shadowSize) {
            mShadowSize = shadowSize;
            mDirty = true;
            mShadowDirty = true;
            invalidateSelf();
        }
    }

    public void setShadowStartColor(int shadowStartColor) {
        if (mShadowStartColor != shadowStartColor) {
            mShadowStartColor = shadowStartColor;
            mShadowDirty = true;
            invalidateSelf();
        }
    }

    public void setShadowEndColor(int shadowEndColor) {
        if (mShadowEndColor != shadowEndColor) {
            mShadowEndColor = shadowEndColor;
            mShadowDirty = true;
            invalidateSelf();
        }
    }

    public void backgroundInvalid() {
        mDirty = true;
        invalidateSelf();
    }

    @Override
    public void draw(@NonNull Canvas canvas) {
        Rect bounds = getBounds();

        float outerRadius = mCornerRadius + mShadowSize;
        if (mShadowDirty) {
            mCornerShadowPath.reset();
            mCornerShadowPath.setFillType(Path.FillType.EVEN_ODD);
            RectF innerBounds = new RectF(-mCornerRadius, - mCornerRadius, mCornerRadius, mCornerRadius);
            RectF outerBounds = new RectF(innerBounds);
            outerBounds.inset(-mShadowSize, -mShadowSize);
            mCornerShadowPath.moveTo(-mCornerRadius, 0);
            mCornerShadowPath.rLineTo(-mShadowSize, 0);
            mCornerShadowPath.arcTo(outerBounds, 180, 90, false);
            mCornerShadowPath.arcTo(innerBounds, 270, -90, false);
            mCornerShadowPath.close();
            mCornerShadowPaint.setShader(new RadialGradient(0, 0, outerRadius, new int[]{mShadowStartColor, mShadowStartColor, mShadowEndColor},
                    new float[]{0, mCornerRadius / outerRadius, 1}, Shader.TileMode.CLAMP));
            mEdgeShadowPaint.setShader(new LinearGradient(0, -mCornerRadius, 0, -outerRadius, mShadowStartColor, mShadowEndColor, Shader.TileMode.CLAMP));
            mShadowDirty = false;
        }
        float edgeShadowInset = outerRadius * 2;
        float hEdgeShadowWidth = bounds.width() - edgeShadowInset;
        float vEdgeShadowWidth = bounds.height() - edgeShadowInset;
        //LT
        int save = canvas.save();
        canvas.translate(bounds.left + outerRadius, bounds.top + outerRadius);
        canvas.drawPath(mCornerShadowPath, mCornerShadowPaint);
        canvas.drawRect(0, -outerRadius, hEdgeShadowWidth, -mCornerRadius, mEdgeShadowPaint);
        canvas.restoreToCount(save);
        //RT
        save = canvas.save();
        canvas.translate(bounds.right - outerRadius, bounds.top + outerRadius);
        canvas.rotate(90);
        canvas.drawPath(mCornerShadowPath, mCornerShadowPaint);
        canvas.drawRect(0, -outerRadius, vEdgeShadowWidth, -mCornerRadius, mEdgeShadowPaint);
        canvas.restoreToCount(save);
        //RB
        save = canvas.save();
        canvas.translate(bounds.right - outerRadius, bounds.bottom - outerRadius);
        canvas.rotate(180);
        canvas.drawPath(mCornerShadowPath, mCornerShadowPaint);
        canvas.drawRect(0, -outerRadius, hEdgeShadowWidth, -mCornerRadius, mEdgeShadowPaint);
        canvas.restoreToCount(save);
        //LB
        save = canvas.save();
        canvas.translate(bounds.left + outerRadius, bounds.bottom - outerRadius);
        canvas.rotate(270);
        canvas.drawPath(mCornerShadowPath, mCornerShadowPaint);
        canvas.drawRect(0, -outerRadius, vEdgeShadowWidth, -mCornerRadius, mEdgeShadowPaint);
        canvas.restoreToCount(save);

        if (mDirty) {
            mPath.reset();
            mPath.setFillType(Path.FillType.EVEN_ODD);
            RectF rect = new RectF(bounds.left + mShadowSize, bounds.top + mShadowSize, bounds.right - mShadowSize, bounds.bottom - mShadowSize);
            mPath.addRoundRect(rect, mCornerRadius, mCornerRadius, Path.Direction.CW);
            if (mBackgroundView == null) {
                mPaint.setShader(null);
                mPaint.setColor(mBackgroundColor);
                mDirty = false;
            } else {
                View view = mView.get();
                View backgroundView = mBackgroundView.get();
                if (view != null && backgroundView != null) {
                    Drawable backgroundDrawable = backgroundView.getBackground();
                    if (backgroundDrawable != null) {
                        int width = (int) rect.width();
                        int height = (int) rect.height();
                        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
                        Canvas tempCanvas = new Canvas(bitmap);
                        int[] location = new int[2];
                        backgroundView.getLocationOnScreen(location);
                        int dx = location[0];
                        int dy = location[1];
                        view.getLocationOnScreen(location);
                        save = tempCanvas.save();
                        tempCanvas.translate((backgroundView.getWidth() == view.getWidth() ? 0 : dx - location[0]) - mShadowSize, dy - location[1] - mShadowSize);
                        backgroundDrawable.draw(tempCanvas);
                        tempCanvas.restoreToCount(save);
                        Bitmap finalBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
                        tempCanvas = new Canvas(finalBitmap);
                        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG);
                        if (mBackgroundColor != 0) {
                            BitmapUtils.blur(bitmap, 100);
                            tempCanvas.drawColor(mBackgroundColor);
                            paint.setAlpha(35);
                            tempCanvas.drawBitmap(bitmap, 0, 0, paint);
                        } else {
                            BitmapUtils.blur(bitmap, 50);
                            tempCanvas.drawBitmap(bitmap, 0, 0, paint);
                            tempCanvas.drawColor(0x1affffff);
                        }
                        mPaint.setShader(new BitmapShader(finalBitmap, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP));
                        mDirty = false;
                    }
                }
            }
        }
        canvas.drawPath(mPath, mPaint);
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
