package com.example.gridheaderpager

import android.content.Context
import android.os.SystemClock
import android.util.TypedValue
import android.view.View

/**
 * Created by lei.jialin on 1/4/22
 */

fun Context.dp2px(dip: Int): Int {
    return dip.dp2px(this)
}

fun Context.dp2px(dip: Float): Int {
    return TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_DIP, dip,
        this.resources.displayMetrics
    ).toInt()
}

fun Int.dp2px(context: Context): Int {
    return this.toFloat().dp2px(context)
}

fun Float.dp2px(context: Context): Int {
    return TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_DIP, this,
        context.resources.displayMetrics
    ).toInt()
}


fun Context.dimen2px(dimenResId: Int): Int {
    return this.resources.getDimensionPixelSize(dimenResId)
}

fun View.onClick(invoke: (view: View) -> Unit) {
    var lastClickTime = 0L
    this.setOnClickListener {
        val diff = SystemClock.elapsedRealtime() - lastClickTime
        L.d(it, "onClick(): $it time take ${diff}, lastClickTime:${lastClickTime}")
        if (diff < 1000) {
            return@setOnClickListener
        }
        lastClickTime = SystemClock.elapsedRealtime()
        invoke.invoke(this)
    }
}