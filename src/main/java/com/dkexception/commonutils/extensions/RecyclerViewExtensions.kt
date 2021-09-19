package com.dkexception.commonutils.extensions

import android.graphics.Rect
import android.view.View
import androidx.annotation.DimenRes
import androidx.recyclerview.widget.RecyclerView
import com.dkexception.commonutils.R

/**
 * Sets the items of this [RecyclerView], to have margins in between them
 * Applies vertical margin specified by [spaceSize], and applies same margin horizontally iff
 * [shouldApplyHorizontalMargin] is `true`
 *
 * @param spaceSize dimen resource for margin
 * @param shouldApplyHorizontalMargin whether to apply the margin horizontally
 */
fun RecyclerView.setMarginItemDecoration(
    @DimenRes spaceSize: Int,
    shouldApplyHorizontalMargin: Boolean = false
) {
    val dimenSpaceSize = resources.getDimensionPixelSize(spaceSize)
    val zeroSpace = resources.getDimensionPixelSize(R.dimen.zero_dp)
    addItemDecoration(
        object : RecyclerView.ItemDecoration() {
            override fun getItemOffsets(
                outRect: Rect, view: View,
                parent: RecyclerView,
                state: RecyclerView.State
            ) {
                with(outRect) {
                    if (parent.getChildAdapterPosition(view) == 0) {
                        top = dimenSpaceSize
                    }
                    left = if (shouldApplyHorizontalMargin) dimenSpaceSize else zeroSpace
                    right = if (shouldApplyHorizontalMargin) dimenSpaceSize else zeroSpace
                    bottom = dimenSpaceSize
                }
            }
        }
    )
}
