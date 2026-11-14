package com.syntey.spiriteddriving.core.platform.binding

import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

/**
 * [PlatformMapsApp] for coroutines.
 */
class MapsApp(private val binding: PlatformMapsApp) {

    /** Which maps app drives are handed to on this platform. */
    val kind: MapsAppKind get() = binding.kind

    /**
     * Opens [link] in the maps app, and returns whether it opened.
     */
    suspend fun open(link: String): MapsAppResult = suspendCancellableCoroutine { continuation ->
        binding.open(
            link,
            object : MapsAppListener {
                override fun onResult(result: MapsAppResult) {
                    if (continuation.isActive) continuation.resume(result)
                }
            },
        )
    }

    /** Opens the maps app's page in the app store. */
    fun openStore() = binding.openStore()
}
