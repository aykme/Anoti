package com.alekseivinogradov.anoti.animebackgroundupdate.android.impl.domain.worker.fake

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters

/**
 * Stands in for a worker belonging to somebody else. WorkManager builds it by reflection once
 * no factory claims it, which is what makes it visible in an assertion.
 *
 * Internal rather than private: that reflective fallback reads a public constructor, and a
 * private top-level class does not have one.
 */
internal class OtherWorkerFake(
    appContext: Context,
    params: WorkerParameters
) : Worker(appContext, params) {
    override fun doWork(): Result = Result.success()
}
