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
import android.util.DisplayMetrics;
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
    private float mInsetShadow;
    private int mShadowStartColor;
    private int mShadowEndColor;

    private boolean mCardDirty = true;
    private RectF mCardBounds = new RectF();
    private Path mCardPath = new Path();
    private Paint mCardPaint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG);
    private boolean mShadowDirty = true;
    private Path mCornerShadowPath = new Path();
    private Paint mCornerShadowPaint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.DITHER_FLAG);
    private Paint mEdgeShadowPaint = new Paint(Paint.DITHER_FLAG);

    public RoundRectBackgroundBlurDrawableWithShadow(View view) {
        mView = new WeakReference<>(view);
        DisplayMetrics displayMetrics = view.getContext().getResources().getDisplayMetrics();
        mCornerRadius = DimensionUtils.dpToFloatPx(6, displayMetrics);
        mShadowSize = DimensionUtils.dpToFloatPx(10, displayMetrics);
        mInsetShadow = DimensionUtils.dpToFloatPx(4, displayMetrics);
        mShadowStartColor = 0x14000000;
        mShadowEndColor = Color.TRANSPARENT;
    }

    public void setBackground(View backgroundView, int backgroundColor) {
        View oldBackgroundView = mBackgroundView == null ? null : mBackgroundView.get();
        if (oldBackgroundView != backgroundView || mBackgroundColor != backgroundColor) {
            mBackgroundView = backgroundView == null ? null : new WeakReference<>(backgroundView);
            mBackgroundColor = backgroundColor;
            mCardDirty = true;
            invalidateSelf();
        }
    }

    public void setCornerRadius(float cornerRadius) {
        if (cornerRadius < 0f) {
            throw new IllegalArgumentException("Invalid radius " + cornerRadius + ". Must be >= 0");
        }
        if (mCornerRadius != cornerRadius) {
            mCornerRadius = cornerRadius;
            mCardDirty = true;
            mShadowDirty = true;
            invalidateSelf();
        }
    }

    public void setShadowSize(float shadowSize) {
        if (shadowSize < 0f) {
            throw new IllegalArgumentException("Invalid shadow size " + shadowSize + ". Must be >= 0");
        }
        if (mShadowSize != shadowSize) {
            mShadowSize = shadowSize;
            mCardDirty = true;
            mShadowDirty = true;
            invalidateSelf();
        }
    }

    public void setInsetShadow(float insetShadow) {
        if (insetShadow < 0f) {
            throw new IllegalArgumentException("Invalid inset shadow " + insetShadow + ". Must be >= 0");
        }
        if (insetShadow > mShadowSize) {
            insetShadow = mShadowSize;
        }
        if (mInsetShadow != insetShadow) {
            mInsetShadow = insetShadow;
            mCardDirty = true;
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
        mCardDirty = true;
        invalidateSelf();
    }

    @Override
    protected void onBoundsChange(Rect bounds) {
        super.onBoundsChange(bounds);
        mCardDirty = true;
        mShadowDirty = true;
    }

    @Override
    public void draw(@NonNull Canvas canvas) {
        Rect bounds = getBounds();

        if (mCardDirty) {
            float offset = mShadowSize - mInsetShadow;
            mCardBounds.set(bounds.left + offset, bounds.top + offset, bounds.right - offset, bounds.bottom - mShadowSize);
            mCardPath.reset();
            mCardPath.setFillType(Path.FillType.EVEN_ODD);
            mCardPath.addRoundRect(mCardBounds, mCornerRadius, mCornerRadius, Path.Direction.CW);
            if (mBackgroundView == null) {
                mCardPaint.setShader(null);
                mCardPaint.setColor(mBackgroundColor);
                mCardDirty = false;
            } else {
                View view = mView.get();
                View backgroundView = mBackgroundView.get();
                if (view != null && backgroundView != null) {
                    Drawable backgroundDrawable = backgroundView.getBackground();
                    if (backgroundDrawable != null) {
                        int width = (int) mCardBounds.width();
                        int height = (int) mCardBounds.height();
                        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
                        Canvas tempCanvas = new Canvas(bitmap);
                        int[] location = new int[2];
                        backgroundView.getLocationOnScreen(location);
                        int dx = location[0];
                        int dy = location[1];
                        view.getLocationOnScreen(location);
                        int save = tempCanvas.save();
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
                        mCardPaint.setShader(new BitmapShader(finalBitmap, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP));
                        mCardDirty = false;
                    }
                }
            }
        }

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

        canvas.translate(0, mInsetShadow);
        float edgeShadowTop = -outerRadius;
        float inset = mCornerRadius + mInsetShadow;
        float edgeShadowInset = inset * 2;
        float hEdgeShadowWidth = mCardBounds.width() - edgeShadowInset;
        float vEdgeShadowWidth = mCardBounds.height() - edgeShadowInset;
        //LT
        int save = canvas.save();
        canvas.translate(mCardBounds.left + inset, mCardBounds.top + inset);
        canvas.drawPath(mCornerShadowPath, mCornerShadowPaint);
        if (hEdgeShadowWidth > 0) {
            canvas.drawRect(0, edgeShadowTop, hEdgeShadowWidth, -mCornerRadius, mEdgeShadowPaint);
        }
        canvas.restoreToCount(save);
        //RT
        save = canvas.save();
        canvas.translate(mCardBounds.right - inset, mCardBounds.top + inset);
        canvas.rotate(90);
        canvas.drawPath(mCornerShadowPath, mCornerShadowPaint);
        if (vEdgeShadowWidth > 0) {
            canvas.drawRect(0, edgeShadowTop, vEdgeShadowWidth, -mCornerRadius, mEdgeShadowPaint);
        }
        canvas.restoreToCount(save);
        //RB
        save = canvas.save();
        canvas.translate(mCardBounds.right - inset, mCardBounds.bottom - inset);
        canvas.rotate(180);
        canvas.drawPath(mCornerShadowPath, mCornerShadowPaint);
        if (hEdgeShadowWidth > 0) {
            canvas.drawRect(0, edgeShadowTop, hEdgeShadowWidth, -mCornerRadius, mEdgeShadowPaint);
        }
        canvas.restoreToCount(save);
        //LB
        save = canvas.save();
        canvas.translate(mCardBounds.left + inset, mCardBounds.bottom - inset);
        canvas.rotate(270);
        canvas.drawPath(mCornerShadowPath, mCornerShadowPaint);
        if (vEdgeShadowWidth > 0) {
            canvas.drawRect(0, edgeShadowTop, vEdgeShadowWidth, -mCornerRadius, mEdgeShadowPaint);
        }
        canvas.restoreToCount(save);
        canvas.translate(0, -mInsetShadow);

        canvas.drawPath(mCardPath, mCardPaint);
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
