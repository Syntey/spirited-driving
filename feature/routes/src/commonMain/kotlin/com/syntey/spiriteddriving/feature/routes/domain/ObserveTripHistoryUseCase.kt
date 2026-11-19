package com.syntey.spiriteddriving.feature.routes.domain

import app.futured.arkitekt.crusecases.FlowUseCase
import com.syntey.spiriteddriving.core.domain.drive.PastDrivePage
import com.syntey.spiriteddriving.core.domain.drive.TripHistoryCache
import kotlinx.coroutines.flow.Flow
import org.koin.core.annotation.Factory

/** Emits the kept trip history, now and after each change; `null` while nothing is kept. */
@Factory
internal class ObserveTripHistoryUseCase(private val tripHistoryCache: TripHistoryCache) : FlowUseCase<Unit, PastDrivePage?> {

    override fun build(args: Unit): Flow<PastDrivePage?> = tripHistoryCache.observe()
}
