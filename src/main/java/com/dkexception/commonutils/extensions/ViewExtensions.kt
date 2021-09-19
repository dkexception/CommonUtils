package com.dkexception.commonutils.extensions

import android.view.View

/**
 * Shows this view by setting its visibility as [View.VISIBLE]
 */
fun View.show() {
    visibility = View.VISIBLE
}

/**
 * Hides this view by setting its visibility as [View.GONE]
 */
fun View.hide() {
    visibility = View.GONE
}

/**
 * Makes this view invisible by setting its visibility as [View.INVISIBLE]
 */
fun View.makeInvisible() {
    visibility = View.INVISIBLE
}
