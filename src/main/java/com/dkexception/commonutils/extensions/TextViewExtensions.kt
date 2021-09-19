package com.dkexception.commonutils.extensions

import android.annotation.SuppressLint
import android.os.Build
import android.text.*
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.text.style.ImageSpan
import android.text.style.URLSpan
import android.text.util.Linkify
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.widget.TextView
import androidx.annotation.DrawableRes
import androidx.core.content.ContextCompat
import androidx.core.text.HtmlCompat
import com.dkexception.commonutils.TAG
import com.dkexception.commonutils.extractLinks
import com.dkexception.commonutils.hasAnchorLink

/**
 * Makes this TextView Visible, and assign [text] to it
 *
 * @param text text to assign to this TextView
 */
fun TextView.showText(text: String?) {
    visibility = View.VISIBLE
    this.text = text
}

/**
 * Adds click actions specified by [spanActions] to given textView, at [spans] locations in [originalString]
 * @param originalString entire message to be added in TextView
 * @param spans parts of [originalString] needs to be clickable
 * @param spanActions actions on click of spans
 */
fun TextView.prepareSpannedTextView(
    originalString: String,
    spans: List<String>,
    spanActions: List<Runnable>? = null
) {
    val hereClickableSpan = SpannableString(originalString)
    try {
        spans.forEachIndexed { i, it ->
            val start = originalString.indexOf(it)
            val end = start + it.length
            hereClickableSpan.setSpan(
                object : ClickableSpan() {
                    override fun onClick(widget: View) {
                        spanActions?.getOrNull(i)?.run()
                    }
                },
                start,
                end,
                SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE
            )
        }
    } catch (e: Exception) {
        Log.e(TAG, "${e.message}")
    } finally {
        this.text = hereClickableSpan
        this.movementMethod = LinkMovementMethod.getInstance()
    }
}

/**
 * Adds any URLs found in [originalString] to this textView as clickable span and assigns the
 * click action of these spans. By default opens all the URLs in external browser
 *
 * @param originalString entire message to be added in TextView
 * @param shouldOpenInExternalBrowser indicates that the url is to be opened directly in
 * external browser
 * @param internalHandlingAction callback with given URL as parameter that can be handled by
 * consumer app
 */
fun TextView.prepareTextViewWithFoundLinks(
    originalString: String,
    shouldOpenInExternalBrowser: Boolean = true,
    internalHandlingAction: ((String) -> Unit)? = null
) {
    var hereClickableSpan: SpannableString? = null
    try {
        hereClickableSpan =
            SpannableString(
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
                    HtmlCompat.fromHtml(originalString, HtmlCompat.FROM_HTML_MODE_LEGACY)
                else Html.fromHtml(originalString)
            )
        if (hasAnchorLink(originalString)) {
            hereClickableSpan.getSpans(0, originalString.length, URLSpan::class.java).forEach {
                val clickableSpan =
                    context.getURLClickableSpan(
                        it.url,
                        shouldOpenInExternalBrowser,
                        internalHandlingAction
                    )
                hereClickableSpan.setSpan(
                    clickableSpan,
                    hereClickableSpan.getSpanStart(it),
                    hereClickableSpan.getSpanEnd(it),
                    SpannableString.SPAN_EXCLUSIVE_INCLUSIVE
                )
                hereClickableSpan.removeSpan(it)
            }
        } else {
            var start: Int
            var end: Int
            val foundLinks = extractLinks(originalString)
            if (foundLinks.isNotEmpty()) {
                foundLinks.forEach {
                    val clickableSpan =
                        context.getURLClickableSpan(
                            it,
                            shouldOpenInExternalBrowser,
                            internalHandlingAction
                        )
                    start = originalString.indexOf(it)
                    end = start + it.length
                    hereClickableSpan.setSpan(
                        clickableSpan,
                        start,
                        end,
                        SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE
                    )
                }
            }
        }
    } catch (e: Exception) {
        Log.e(TAG, "${e.message}")
        linkifyTextView(originalString)
    } finally {
        text = hereClickableSpan
        movementMethod = LinkMovementMethod.getInstance()
    }
}

/**
 * Fallback method if [prepareTextViewWithFoundLinks] fails. Uses [Linkify] for doing the job!
 * @param originalString entire message to be added in TextView
 */
fun TextView.linkifyTextView(originalString: String) {
    text = originalString
    Linkify.addLinks(this, Linkify.ALL)
}

fun TextView.appendInlineDrawable(
    textToSet: String,
    @DrawableRes relevantDrawable: Int,
    optionalDrawableSpaceCount: Int = 2,
    optionalOnClickAction: Runnable? = null
) {
    setText(
        SpannableStringBuilder().apply {
            append(textToSet)
            repeat(optionalDrawableSpaceCount) {
                append(" ")
            }
            append(
                SpannableString(" ").also {
                    ContextCompat.getDrawable(context, relevantDrawable)?.apply {
                        setBounds(0, 0, intrinsicHeight, intrinsicHeight)
                        it.setSpan(
                            ImageSpan(this), 0,
                            1,
                            Spannable.SPAN_INCLUSIVE_INCLUSIVE
                        )
                    }
                    if (optionalOnClickAction != null) {
                        it.setSpan(
                            object : ClickableSpan() {
                                override fun onClick(widget: View) {
                                    optionalOnClickAction.run()
                                }
                            }, 0,
                            1,
                            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                        )
                    }
                }
            )
        },
        TextView.BufferType.SPANNABLE
    )
    movementMethod = object : LinkMovementMethod() {
        override fun onTouchEvent(
            widget: TextView?,
            buffer: Spannable?,
            event: MotionEvent?
        ): Boolean {
            Selection.removeSelection(buffer)
            return super.onTouchEvent(widget, buffer, event)
        }
    }
}

/**
 * Sets the click action for the left or right drawable set to this TextView
 *
 * @param isLeft specifies drawable's placement - left or right
 * @param actionOnClick click action handler
 */
@SuppressLint("ClickableViewAccessibility")
fun TextView.setOnHorizontalDrawableClickListener(isLeft: Boolean, actionOnClick: Runnable) {
    setOnTouchListener { _, event ->
        val left = 0
        val right = 2

        if (event.action == MotionEvent.ACTION_UP) {
            if (isLeft) {
                if (event.rawX <= (getLeft() + compoundDrawables[left].bounds.width() + totalPaddingLeft)) {
                    // your action here
                    actionOnClick.run()
                    return@setOnTouchListener true
                }
            } else {
                if (event.rawX >= (getRight() - compoundDrawables[right].bounds.width() + totalPaddingRight)) {
                    // your action here
                    actionOnClick.run()
                    return@setOnTouchListener true
                }
            }
        }
        true
    }
}
