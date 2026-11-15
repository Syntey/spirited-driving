package com.syntey.spiriteddriving.core.platform.binding

import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

/** Coroutine view of [PlatformAppCheck], for `:data:*` classes. */
class AppCheck(private val binding: PlatformAppCheck) {

    /** A Firebase App Check token for a backend call, or null when none can be had. */
    suspend fun token(): String? = suspendCancellableCoroutine { continuation ->
        binding.token { token ->
            if (continuation.isActive) continuation.resume(token)
        }
    }
}
