package org.standardnotes.notes

import android.util.TypedValue

fun Int.dpToPixels(): Int {
    val metrics = SApplication.instance!!.resources.displayMetrics
    return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, this.toFloat(), metrics).toInt()
}