package com.syntey.spiriteddriving.feature.map.domain

import app.futured.arkitekt.crusecases.UseCase
import com.syntey.spiriteddriving.core.domain.drive.PastDrive
import com.syntey.spiriteddriving.core.domain.drive.TripHistoryCache
import org.koin.core.annotation.Factory

/** The drive with the ID, as kept in Trip history on the device; null when it isn't kept. */
@Factory
internal class GetPastDriveUseCase(private val tripHistoryCache: TripHistoryCache) : UseCase<String, PastDrive?> {

    override suspend fun build(args: String): PastDrive? = tripHistoryCache.get()?.drives?.firstOrNull { it.id == args }
}
