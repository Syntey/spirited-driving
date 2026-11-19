package com.syntey.spiriteddriving.shared.work

import co.touchlab.kermit.Logger
import com.syntey.spiriteddriving.core.domain.work.AppWork
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import org.koin.core.annotation.Single

@Single
internal class ApplicationWork : AppWork {

    private val logger = Logger.withTag("ApplicationWork")

    // Never cancelled: it ends with the app's process. Without a dispatcher it runs on the default one
    private val scope = CoroutineScope(
        SupervisorJob() + CoroutineExceptionHandler { _, error -> logger.e(error) { "App work failed" } },
    )

    override fun launch(block: suspend () -> Unit) {
        scope.launch { block() }
    }
}
