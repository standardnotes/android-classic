package org.standardnotes.notes

import android.app.Activity
import android.content.Context
import android.support.v7.app.AlertDialog
import android.util.TypedValue
import android.view.inputmethod.InputMethodManager
import android.widget.EditText

fun Int.dpToPixels(): Int {
    val metrics = SApplication.instance.resources.displayMetrics
    return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, this.toFloat(), metrics).toInt()
}

fun EditText.showKeyboard() {
    (this.context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager).showSoftInput(this, InputMethodManager.SHOW_IMPLICIT)
}

fun Activity.showErrorDialog(title: CharSequence, message: CharSequence) {
    AlertDialog.Builder(this)
            .setIcon(android.R.drawable.stat_notify_error)
            .setMessage(message)
            .setTitle(title)
            .setPositiveButton(R.string.action_ok, null)
            .show()
}