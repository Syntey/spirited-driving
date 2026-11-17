package com.syntey.spiriteddriving.shared.domain

import app.futured.arkitekt.crusecases.FlowUseCase
import co.touchlab.kermit.Logger
import com.syntey.spiriteddriving.core.domain.drive.DriveClaimer
import com.syntey.spiriteddriving.core.domain.drive.StartedDrives
import com.syntey.spiriteddriving.core.platform.binding.Auth
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import org.koin.core.annotation.Factory
import org.koin.core.annotation.Provided

/**
 * Claims the drives recorded while signed out, after each sign-in and once at start when already signed in. A failed
 * claim is logged and the next sign-in tries again.
 */
@Factory
internal class ClaimDrivesOnSignInUseCase(
    @Provided private val auth: Auth,
    private val startedDrives: StartedDrives,
    private val driveClaimer: DriveClaimer,
) : FlowUseCase<Unit, Unit> {

    private val logger = Logger.withTag("ClaimDrivesOnSignInUseCase")

    override fun build(args: Unit): Flow<Unit> = auth.user
        .map { it?.id }
        .distinctUntilChanged()
        .filterNotNull()
        .onEach { claimRecordedDrives() }
        .map { }

    @Suppress("TooGenericExceptionCaught")
    private suspend fun claimRecordedDrives() {
        try {
            // Unrecorded drives are left out: they are recorded once online with the ID token, so they already belong to the user.
            // Drives marked not driven are left out too, as they're removed next and the claim would only add them back.
            val ids = startedDrives.all().filter { it.isRecorded && !it.isNotDriven }.map { it.id }.distinct()
            if (ids.isEmpty()) return
            // The phone keeps at most 100 drives and the backend accepts 500, so one request always fits
            driveClaimer.claim(ids)
        } catch (error: Exception) {
            if (error is CancellationException) throw error
            logger.e(error) { "Drives not claimed after sign-in" }
        }
    }
}
