package org.javando.android.sliderview

import android.util.Log
import android.util.TypedValue
import android.view.View
import kotlin.math.roundToInt


fun View.getPxFromDp(dp: Float): Int =
    TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, context.resources.displayMetrics)
        .roundToInt()

sealed class Logger(defaultTag: String? = null) {
    protected val defaultTag: String = defaultTag ?: "[SliderView-DEBUG]"

    abstract fun log(string: String, tag: String = defaultTag)

    object LoggerDebug : Logger() {
        override fun log(string: String, tag: String) {
            Log.d(tag, string)
        }
    }

    object LoggerRelease : Logger() {
        override fun log(string: String, tag: String) {}
    }

    companion object {
        private val isDebugConfig = BuildConfig.DEBUG

        val instance: Logger by lazy {
            if(isDebugConfig)
                LoggerDebug
            else
                LoggerRelease
        }

    }
}