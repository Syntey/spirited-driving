package com.syntey.spiriteddriving.shared.crashreporting

import com.syntey.spiriteddriving.core.platform.binding.PlatformFirebaseCrashlytics

/**
 * Class responsible for recording crashlytics errors using provided [PlatformFirebaseCrashlytics] binding.
 */
internal expect class CrashlyticsReporter(crashlytics: PlatformFirebaseCrashlytics) {
    fun logMessage(message: String)
    fun sendNonFatalException(throwable: Throwable)
}
