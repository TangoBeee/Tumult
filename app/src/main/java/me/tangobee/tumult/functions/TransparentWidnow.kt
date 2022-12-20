package me.tangobee.tumult.functions

import android.view.Window

class TransparentWidnow {

    fun transparentWindow(window: Window, color: Int) {
        window.statusBarColor = color
    }

}