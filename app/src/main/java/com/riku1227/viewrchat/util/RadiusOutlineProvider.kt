package com.riku1227.viewrchat.util

import android.graphics.Outline
import android.view.View
import android.view.ViewOutlineProvider

class RadiusOutlineProvider(private val radius: Float) : ViewOutlineProvider() {
    override fun getOutline(view: View?, outline: Outline?) {
        view?.let {
            outline?.setRoundRect(0, 0, it.width, it.height, radius)
            it.clipToOutline = true
        }
    }
}