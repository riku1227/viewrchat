package com.riku1227.viewrchat.util

import android.widget.TextView
import androidx.core.content.ContextCompat

fun TextView.setTextViewDrawableColor(colorId: Int) {
    for(drawable in this.compoundDrawables) {
        drawable?.setTint(ContextCompat.getColor(this.context, colorId))
    }
}