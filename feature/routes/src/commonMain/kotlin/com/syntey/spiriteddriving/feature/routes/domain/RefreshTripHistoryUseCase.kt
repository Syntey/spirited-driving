package com.syntey.spiriteddriving.feature.routes.domain

import app.futured.arkitekt.crusecases.UseCase
import com.syntey.spiriteddriving.core.domain.drive.DriveNotRatableException
import com.syntey.spiriteddriving.core.domain.drive.DriveRater
import com.syntey.spiriteddriving.core.domain.drive.TripHistoryCache
import com.syntey.spiriteddriving.core.domain.drive.TripHistorySource
import com.syntey.spiriteddriving.core.domain.error.OfflineException
import com.syntey.spiriteddriving.feature.routes.tools.Constants
import kotlinx.coroutines.CancellationException
import org.koin.core.annotation.Factory

/**
 * Sends the ratings kept on the device, then keeps the first page of the signed-in user's trip history from the backend, in
 * place of the kept list. Throws what the source throws; an offline sending of ratings isn't an error here.
 */
@Factory
internal class RefreshTripHistoryUseCase(
    private val tripHistorySource: TripHistorySource,
    private val tripHistoryCache: TripHistoryCache,
    private val driveRater: DriveRater,
) : UseCase<Unit, Unit> {

    override suspend fun build(args: Unit) {
        sendUnsentRatings()
        tripHistoryCache.replaceAll(tripHistorySource.list(cursor = null, limit = Constants.TripHistory.PAGE_SIZE))
    }

    // Stops at the first OfflineException, leaving the rest for later, and the list call then throws it as before. Any other
    // failure skips that rating, which is tried again on the next refresh, and the ratings after it still go now.
    @Suppress("TooGenericExceptionCaught", "SwallowedException")
    private suspend fun sendUnsentRatings() {
        for ((id, rating) in tripHistoryCache.unsentRatings()) {
            val sent = try {
                driveRater.rate(id, rating)
                true
            } catch (error: OfflineException) {
                return
            } catch (error: DriveNotRatableException) {
                // The backend won't take this rating ever, so it's marked sent to stop the retries
                true
            } catch (error: CancellationException) {
                throw error
            } catch (error: Exception) {
                false
            }
            if (sent) tripHistoryCache.markRatingSent(id, rating)
        }
    }
}
