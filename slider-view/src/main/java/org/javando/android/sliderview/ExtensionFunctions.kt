package org.javando.android.sliderview

import android.util.TypedValue
import android.view.View
import kotlin.math.roundToInt

fun View.getPxFromDp(dp: Float): Int =
    TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, context.resources.displayMetrics)
        .roundToInt()