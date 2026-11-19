package com.syntey.spiriteddriving.shared.domain

import app.futured.arkitekt.crusecases.FlowUseCase
import co.touchlab.kermit.Logger
import com.syntey.spiriteddriving.core.domain.error.OfflineException
import com.syntey.spiriteddriving.core.domain.savedroute.SavedRoutesCache
import com.syntey.spiriteddriving.core.domain.savedroute.SavedRoutesSource
import com.syntey.spiriteddriving.core.platform.binding.Auth
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.withIndex
import org.koin.core.annotation.Factory
import org.koin.core.annotation.Provided

/**
 * Keeps the saved routes on the device in step with the account. Every sign-in, sign-out and account switch clears them,
 * and so does a start signed out, so one user's routes never show to the next; only the user already signed in at start
 * keeps them, to see them offline. Then, while signed in, the list is loaded from the backend, so Save shows Saved for a
 * route saved on another phone. One flow does both, so a load never lands before the clear. Failures are logged, and the
 * next change tries again.
 */
@Factory
internal class SyncSavedRoutesOnAccountChangeUseCase(
    @Provided private val auth: Auth,
    private val savedRoutesSource: SavedRoutesSource,
    private val savedRoutesCache: SavedRoutesCache,
) : FlowUseCase<Unit, Unit> {

    private val logger = Logger.withTag("SyncSavedRoutesOnAccountChangeUseCase")

    override fun build(args: Unit): Flow<Unit> = auth.user
        .map { it?.id }
        .distinctUntilChanged()
        .withIndex()
        .map { (index, id) ->
            if (index > 0 || id == null) clear()
            if (id != null) load()
        }

    @Suppress("TooGenericExceptionCaught")
    private suspend fun clear() {
        try {
            savedRoutesCache.clear()
        } catch (error: Exception) {
            if (error is CancellationException) throw error
            logger.e(error) { "Saved routes not cleared" }
        }
    }

    // Offline is expected here, so it isn't logged; the list loads again when it opens
    @Suppress("TooGenericExceptionCaught")
    private suspend fun load() {
        try {
            savedRoutesCache.replaceAll(savedRoutesSource.list())
        } catch (_: OfflineException) {
        } catch (error: CancellationException) {
            throw error
        } catch (error: Exception) {
            logger.e(error) { "Saved routes not loaded" }
        }
    }
}
