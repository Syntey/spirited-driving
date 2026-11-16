package com.syntey.spiriteddriving.shared.logging

import com.syntey.spiriteddriving.shared.crashreporting.CrashlyticsReporter
import com.syntey.spiriteddriving.data.network.result.NetworkError
import co.touchlab.kermit.LogWriter
import co.touchlab.kermit.Logger
import co.touchlab.kermit.Severity
import kotlin.coroutines.cancellation.CancellationException

/**
 * This object takes care of KMP logging setup.
 *
 * We utilize `Kermit`(https://kermit.touchlab.co/) logger for debug logging during development.
 */
internal object AppLogging {

    fun initialize(
        isDebugBuild: Boolean,
        crashlytics: CrashlyticsReporter,
    ) {
        with(Logger) {
            setMinSeverity(Severity.Debug)
            setTag("SpiritedDriving")
            setLogWriters(getPlatformLogWriters(isDebugBuild) + getCrashlyticsLogWriter(crashlytics))
        }
    }

    private fun getCrashlyticsLogWriter(crashlytics: CrashlyticsReporter) = CrashlyticsKermitLogWriter(
        crashlytics = crashlytics,
        minSeverity = Severity.Info,
        minCrashSeverity = Severity.Warn,
        isLoggable = ::throwableFilter,
    )

    private fun throwableFilter(throwable: Throwable): Boolean = when (throwable) {
        is NetworkError.ConnectionError -> false
        is CancellationException -> false
        else -> true
    }
}

/**
 * Returns list of [LogWriter] instances that shared [Logger] instance should use.
 * This way, we can customize logging behaviour per-platform
 * (eg. Android should not use any log writers in production builds, etc).
 */
internal expect fun getPlatformLogWriters(isDebug: Boolean): List<LogWriter>
