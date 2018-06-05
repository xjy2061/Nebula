package org.xjy.android.common;

import android.content.Context;
import android.util.DisplayMetrics;
import android.util.TypedValue;

public class DimensionUtils {

    public static float dpToFloatPx(float dp, DisplayMetrics displayMetrics) {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, displayMetrics);
    }

    public static float dpToFloatPx(float dp, Context context) {
        return dpToFloatPx(dp, context.getResources().getDisplayMetrics());
    }

    public static int dpToIntPx(float dp, DisplayMetrics displayMetrics) {
        return (int) (dpToFloatPx(dp, displayMetrics) + 0.5);
    }

    public static int dpToIntPx(float dp, Context context) {
        return (int) (dpToFloatPx(dp, context) + 0.5);
    }

    public static float spToFloatPx(float sp, DisplayMetrics displayMetrics) {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, sp, displayMetrics);
    }

    public static float spToFloatPx(float sp, Context context) {
        return dpToFloatPx(sp, context.getResources().getDisplayMetrics());
    }

    public static int spToIntPx(float sp, DisplayMetrics displayMetrics) {
        return (int) (dpToFloatPx(sp, displayMetrics) + 0.5);
    }

    public static int spToIntPx(float sp, Context context) {
        return (int) (dpToFloatPx(sp, context) + 0.5);
    }
}
