package com.dkexception.commonutils.extensions

import android.widget.ScrollView
import androidx.core.widget.NestedScrollView

/**
 * Flashes the scroll indicators for this ScrollView, by scrolling it 1 px up-down
 */
fun ScrollView.flashScrollIndicators() {
    scrollBy(0, 1)
    scrollBy(0, -1)
}

/**
 * Flashes the scroll indicators for this NestedScrollView, by scrolling it 1 px up-down
 */
fun NestedScrollView.flashScrollIndicators() {
    scrollBy(0, 1)
    scrollBy(0, -1)
}
