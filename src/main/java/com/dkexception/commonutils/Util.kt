package com.dkexception.commonutils

import android.util.Log
import androidx.core.util.PatternsCompat

/**
 * Detects whether the current [text] contains any valid html anchor `(<a>)` link
 *
 * @param text text to check
 */
fun hasAnchorLink(text: String) =
    text.contains("<a", true) && text.contains("href", true)

/**
 * Extracts URLs from given [text], using WebUrl pattern matcher
 *
 * @param text text to check
 * @return list of URLs found in this [text]
 */
fun extractLinks(text: String): List<String> {
    val links = mutableListOf<String>()
    try {
        val m = PatternsCompat.WEB_URL.matcher(text)
        while (m.find()) {
            val link = m.group()
            if (link.startsWith("http") || link.startsWith("www"))
                links.add(link)
        }
    } catch (e: Exception) {
        Log.e(TAG, "${e.message}")
    }
    return links.toList()
}
