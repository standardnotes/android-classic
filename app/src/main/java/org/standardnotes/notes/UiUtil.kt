package org.standardnotes.notes

import android.util.TypedValue

/**
 * Created by carl on 18/01/17.
 */

fun Int.dpToPixels(): Int {
    val metrics = SApplication.instance!!.resources.displayMetrics
    return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, this.toFloat(), metrics).toInt()
}