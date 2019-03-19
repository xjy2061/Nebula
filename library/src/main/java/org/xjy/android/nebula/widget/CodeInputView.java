package org.xjy.android.nebula.widget;

import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.text.InputType;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.inputmethod.BaseInputConnection;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputConnection;
import android.view.inputmethod.InputMethodManager;

import org.xjy.android.nebula.R;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.Stack;

import androidx.annotation.IntDef;
import androidx.annotation.IntRange;
import androidx.annotation.Nullable;

public class CodeInputView extends View {
    @Retention(RetentionPolicy.SOURCE)
    @IntDef({InputType.TYPE_CLASS_NUMBER, InputType.TYPE_CLASS_TEXT})
    @interface CodeInputType {}

    @IntRange(from = 1)
    private int mCodeCount = 4;
    private int mTextSize;
    private int mTextColor;
    private int mTextMarginBottom;
    private int mUnderlineWidth;
    private int mUnderlineHeight;
    private int mUnderlineGap;
    private int mUnderlineColor;
    private int mUnderlineSelectedColor;
    @CodeInputType
    private int mInputType = InputType.TYPE_CLASS_NUMBER;

    private Paint mTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private Paint.FontMetrics mFontMetrics = new Paint.FontMetrics();
    private Paint mUnderLinePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private RectF mUnderlineRect = new RectF();

    private Stack<String> mText = new Stack<>();
    private int mAnimatingUnderlineWidth = -1;
    private ValueAnimator mUnderlineAnimator;

    private OnCompleteListener mOnCompleteListener;
    private OnCodeChangedListener mOnCodeChangedListener;

    public CodeInputView(Context context) {
        this(context, null);
    }

    public CodeInputView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CodeInputView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.CodeInputView);
        mCodeCount = a.getInt(R.styleable.CodeInputView_codeCount, mCodeCount);
        mTextSize = a.getDimensionPixelSize(R.styleable.CodeInputView_android_textSize, mTextSize);
        mTextColor = a.getColor(R.styleable.CodeInputView_android_textColor, mTextColor);
        mTextMarginBottom = a.getDimensionPixelSize(R.styleable.CodeInputView_textMarginBottom, mTextMarginBottom);
        mUnderlineWidth = a.getDimensionPixelSize(R.styleable.CodeInputView_underlineWidth, mUnderlineWidth);
        mUnderlineHeight = a.getDimensionPixelSize(R.styleable.CodeInputView_underlineHeight, mUnderlineHeight);
        mUnderlineGap = a.getDimensionPixelSize(R.styleable.CodeInputView_underlineGap, mUnderlineGap);
        mUnderlineColor = a.getColor(R.styleable.CodeInputView_underlineColor, mUnderlineColor);
        mUnderlineSelectedColor = a.getColor(R.styleable.CodeInputView_underlineSelectedColor, mUnderlineSelectedColor);
        mInputType = a.getInt(R.styleable.CodeInputView_inputType, mInputType);
        a.recycle();

        mTextPaint.setTextAlign(Paint.Align.CENTER);
        setFocusable(true);
        setFocusableInTouchMode(true);

        mUnderlineAnimator = ValueAnimator.ofInt(0, mUnderlineWidth);
        mUnderlineAnimator.setDuration(150);
        mUnderlineAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
        mUnderlineAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                mAnimatingUnderlineWidth = (int) animation.getAnimatedValue();
                int left = (mUnderlineWidth + mUnderlineGap) * (mText.size() - 1);
                int bottom = getHeight();
                invalidate(left, bottom - mUnderlineHeight, left + mUnderlineWidth, bottom);
            }
        });
    }

    public void setCodeCount(@IntRange(from = 1) int codeCount) {
        if (mCodeCount != codeCount) {
            mCodeCount = codeCount;
            requestLayout();
            invalidate();
        }
    }

    public void setTextSize(int textSize) {
        if (mTextSize != textSize) {
            mTextSize = textSize;
            requestLayout();
            invalidate();
        }
    }

    public void setTextColor(int textColor) {
        if (mTextColor != textColor) {
            mTextColor = textColor;
            invalidate();
        }
    }

    public void setTextMarginBottom(int textMarginBottom) {
        if (mTextMarginBottom != textMarginBottom) {
            mTextMarginBottom = textMarginBottom;
            requestLayout();
            invalidate();
        }
    }

    public void setUnderlineWidth(int underlineWidth) {
        if (mUnderlineWidth != underlineWidth) {
            mUnderlineWidth = underlineWidth;
            stopUnderlineAnimation();
            requestLayout();
            invalidate();
        }
    }

    public void setUnderlineHeight(int underlineHeight) {
        if (mUnderlineHeight != underlineHeight) {
            mUnderlineHeight = underlineHeight;
            requestLayout();
            invalidate();
        }
    }

    public void setUnderlineGap(int underlineGap) {
        if (mUnderlineGap != underlineGap) {
            mUnderlineGap = underlineGap;
            requestLayout();
            invalidate();
        }
    }

    public void setUnderlineColor(int underlineColor) {
        if (mUnderlineColor != underlineColor) {
            mUnderlineColor = underlineColor;
            invalidate();
        }
    }

    public void setUnderlineSelectedColor(int underlineSelectedColor) {
        if (mUnderlineSelectedColor != underlineSelectedColor) {
            mUnderlineSelectedColor = underlineSelectedColor;
            invalidate();
        }
    }

    public void setInputType(@CodeInputType int inputType) {
        if (mInputType != inputType) {
            mInputType = inputType;
            invalidate();
        }
    }

    public void setOnCompleteListener(OnCompleteListener onCompleteListener) {
        mOnCompleteListener = onCompleteListener;
    }

    public void clear() {
        mText.clear();
        stopUnderlineAnimation();
        invalidate();
        if (mOnCodeChangedListener != null) {
            mOnCodeChangedListener.onCodeChanged();
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getActionMasked() == MotionEvent.ACTION_DOWN) {
            requestFocus();
            InputMethodManager imm = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm != null) {
                imm.showSoftInput(this, 0);
                imm.viewClicked(this);
            }
        }
        return super.onTouchEvent(event);
    }

    @Override
    public InputConnection onCreateInputConnection(EditorInfo outAttrs) {
        outAttrs.inputType = mInputType == InputType.TYPE_CLASS_TEXT ? mInputType | InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS : mInputType;
        return new BaseInputConnection(this, false);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_DEL && mText.size() > 0) {
            mText.pop();
            stopUnderlineAnimation();
            invalidate();
            if (mOnCodeChangedListener != null) {
                mOnCodeChangedListener.onCodeChanged();
            }
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        char typedChar = (char) event.getUnicodeChar();
        if (mText.size() < mCodeCount && mInputType == InputType.TYPE_CLASS_NUMBER ? Character.isDigit(typedChar) : Character.isLetterOrDigit(typedChar)) {
            mText.push(Character.toString(typedChar));
            stopUnderlineAnimation();
            startUnderlineAnimation();
            invalidate();
            if (mOnCompleteListener != null && mText.size() == mCodeCount) {
                StringBuilder text = new StringBuilder();
                for (int i = 0; i < mCodeCount; i++) {
                    text.append(mText.get(i));
                }
                mOnCompleteListener.onComplete(text.toString());
            }
            if (mOnCodeChangedListener != null) {
                mOnCodeChangedListener.onCodeChanged();
            }
            return true;
        }
        return super.onKeyUp(keyCode, event);
    }

    private void startUnderlineAnimation() {
        mUnderlineAnimator.setIntValues(0, mUnderlineWidth);
        mUnderlineAnimator.start();
    }

    private void stopUnderlineAnimation() {
        mUnderlineAnimator.cancel();
        mAnimatingUnderlineWidth = -1;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        mTextPaint.setTextSize(mTextSize);
        mTextPaint.getFontMetrics(mFontMetrics);
        int textHeight = (int) (mFontMetrics.bottom - mFontMetrics.top + 0.5f);
        setMeasuredDimension(mUnderlineWidth * mCodeCount + mUnderlineGap * (mCodeCount - 1),  textHeight + mTextMarginBottom + mUnderlineHeight);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        mTextPaint.setTextSize(mTextSize);
        mTextPaint.getFontMetrics(mFontMetrics);
        mTextPaint.setColor(mTextColor);
        int textCount = mText.size();
        for (int i = 0; i < mCodeCount; i++) {
            float x = (mUnderlineWidth + mUnderlineGap) * i;
            if (i < textCount) {
                canvas.drawText(mText.get(i), x + mUnderlineWidth / 2, -mFontMetrics.top, mTextPaint);
            }
            float bottom = getHeight();
            float top = bottom - mUnderlineHeight;
            float radius = mUnderlineHeight / 2;
            if (i == textCount - 1 && mAnimatingUnderlineWidth >= 0) {
                mUnderlineRect.set(x, top, x + mUnderlineWidth, bottom);
                mUnderLinePaint.setColor(mUnderlineColor);
                canvas.drawRoundRect(mUnderlineRect, radius, radius, mUnderLinePaint);
                mUnderlineRect.right = x + mAnimatingUnderlineWidth;
                mUnderLinePaint.setColor(mUnderlineSelectedColor);
                canvas.drawRoundRect(mUnderlineRect, radius, radius, mUnderLinePaint);
            } else {
                mUnderlineRect.set(x, top, x + mUnderlineWidth, bottom);
                if (i < textCount) {
                    mUnderLinePaint.setColor(mUnderlineSelectedColor);
                } else {
                    mUnderLinePaint.setColor(mUnderlineColor);
                }
                canvas.drawRoundRect(mUnderlineRect, radius, radius, mUnderLinePaint);
            }
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        stopUnderlineAnimation();
        super.onDetachedFromWindow();
    }

    interface OnCompleteListener {
        void onComplete(String code);
    }

    interface OnCodeChangedListener {
        void onCodeChanged();
    }
}
