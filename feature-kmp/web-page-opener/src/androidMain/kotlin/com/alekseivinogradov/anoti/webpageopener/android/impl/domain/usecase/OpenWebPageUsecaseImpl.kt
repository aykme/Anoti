package com.alekseivinogradov.anoti.webpageopener.android.impl.domain.usecase

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import com.alekseivinogradov.anoti.celebrity.kmp.api.domain.ANOTI_TAG
import com.alekseivinogradov.anoti.webpageopener.kmp.api.domain.usecase.OpenWebPageUsecase

private const val TAG = "OpenWebPageUsecase"

/**
 * Opens a web page in whichever browser the system picks, in a task of its own.
 *
 * @param appContext the application context the page is started from.
 */
class OpenWebPageUsecaseImpl(private val appContext: Context) : OpenWebPageUsecase {

    override suspend fun execute(url: String): Boolean {
        val intent = Intent(
            /* action = */
            Intent.ACTION_VIEW,
            /* uri = */
            Uri.parse(url)
        ).addCategory(Intent.CATEGORY_BROWSABLE)
            // The application context starts an activity only into a new task.
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        // No resolveActivity check: without <queries> it hides every browser on API 30+.
        return try {
            appContext.startActivity(intent)
            println("$ANOTI_TAG $TAG: a web page is handed to the system")
            true
        } catch (_: ActivityNotFoundException) {
            println("$ANOTI_TAG $TAG: no app opens web pages")
            false
        }
    }
}
