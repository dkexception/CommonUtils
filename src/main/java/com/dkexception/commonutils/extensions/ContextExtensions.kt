package com.dkexception.commonutils.extensions

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.SystemClock
import android.text.style.ClickableSpan
import android.util.Log
import android.view.View
import android.webkit.URLUtil
import android.widget.Toast
import androidx.annotation.StringRes
import com.dkexception.commonutils.*
import java.util.*

/**
 * Shows Toast message on behalf of this Activity, message taken from String resources
 *
 * @param message String resource ID to show the resource string
 * @param longDurationEnabled duration of the toast to be shown
 */
fun Context.showToast(
    @StringRes message: Int,
    longDurationEnabled: Boolean = false
) = showToast(getString(message), longDurationEnabled)

/**
 * Shows Toast message on behalf of this Activity, message passed as it is
 *
 * @param message String message to be shown
 * @param longDurationEnabled duration of the toast to be shown
 */
fun Context.showToast(
    message: String,
    longDurationEnabled: Boolean = false
) {
    Toast.makeText(
        this,
        message,
        if (longDurationEnabled) Toast.LENGTH_LONG else Toast.LENGTH_SHORT
    ).show()
}

/**
 * Launches dialler with given [phoneNumber]
 *
 * @param [phoneNumber] phone number string to launch
 */
fun Context.launchDiallerWithPhoneNumber(phoneNumber: String) {
    Intent(Intent.ACTION_DIAL).apply {
        data = Uri.parse("$DIALLER_CONSTANT$phoneNumber")
    }.also { intent ->
        packageManager.let {
            // Make sure that the user has a dialler app installed on their device.
            intent.resolveActivity(it)?.run {
                startActivity(intent)
            } ?: Log.e(
                TAG,
                "No activity found to launch dialler"
            )
        }
    }
}

/**
 * Returns the string from String resources dynamically accessing by name, null if no such
 * string was found
 *
 * @param name name of string resource identifier to search
 */
fun Context.getStringByName(name: String?): String? {
    name ?: return null
    return try {
        getString(resources.getIdentifier(name.lowercase(Locale.ENGLISH), "string", packageName))
    } catch (e: Exception) {
        null
    }
}

/**
 * Opens the default PDF viewer to open PDF specified by [pdfUri]
 *
 * @param pdfUri URI of PDF file to open
 * @param onFailAction action to be taken if something goes wrong in launching PDF viewer
 */
fun Context.launchPdfViewerForUri(
    pdfUri: Uri,
    onFailAction: Runnable? = null
) {
    Intent(Intent.ACTION_VIEW).apply {
        data = pdfUri
        // This flag gives the started app read access to the file.
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }.also { intent ->
        packageManager.let {
            // Make sure that the user has a PDF viewer app installed on their device.
            intent.resolveActivity(it)?.run {
                startActivity(intent)
            } ?: onFailAction?.run()
        }
    }
}

/**
 * Opens the Google PlayStore for the given package name specified by [pName]
 * If PlayStore can't be launched, automatically opens the PlayStore's web page
 * with given package name
 *
 * @param pName Package name of the application to open PlayStore page for
 */
fun Context.openPlayStoreForApplicationPage(
    pName: String = packageName
) {
    Intent(
        Intent.ACTION_VIEW,
        Uri.parse("$MARKET_URL$pName")
    ).also { intent ->
        packageManager?.let {
            intent.resolveActivity(it)?.let {
                startActivity(intent)
            } ?: run {
                startActivity(
                    Intent(
                        Intent.ACTION_VIEW,
                        Uri.parse("$PLAY_STORE_WEB_URL$pName")
                    )
                )
            }
        }
    }
}

/**
 * Returns [ClickableSpan] object with given [url] span as click target
 *
 * @param url URL as a click target
 * @param shouldOpenInExternalBrowser whether to directly open this URL in an external browser
 * bypassing application's handling
 * @param internalHandlingAction action to handle the click on the url span
 */
internal fun Context.getURLClickableSpan(
    url: String,
    shouldOpenInExternalBrowser: Boolean,
    internalHandlingAction: ((String) -> Unit)?
): ClickableSpan = object : ClickableSpan() {

    private var lastTimeClicked: Long = 0

    override fun onClick(widget: View) {
        if (SystemClock.elapsedRealtime() - lastTimeClicked < DEFAULT_INTER_CLICK_DURATION) {
            return
        }
        lastTimeClicked = SystemClock.elapsedRealtime()
        try {
            if (shouldOpenInExternalBrowser) {
                startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(URLUtil.guessUrl(url))))
                return
            }
            internalHandlingAction?.invoke(url)
        } catch (e: Exception) {
            Log.e(TAG, "${e.message}")
        }
    }
}
