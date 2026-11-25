package com.syntey.spiriteddriving.feature.map.domain

import app.futured.arkitekt.crusecases.FlowUseCase
import com.syntey.spiriteddriving.core.domain.settings.DistanceUnit
import com.syntey.spiriteddriving.core.domain.settings.DistanceUnitSettings
import com.syntey.spiriteddriving.core.domain.settings.observeUnit
import com.syntey.spiriteddriving.core.platform.binding.Platform
import kotlinx.coroutines.flow.Flow
import org.koin.core.annotation.Factory

/**
 * Emits the unit distances read in: the one the user picked in Settings, now and after each pick. Until they pick, the
 * phone's region decides.
 */
@Factory
internal class ObserveDistanceUnitUseCase(private val distanceUnitSettings: DistanceUnitSettings, private val platform: Platform) :
    FlowUseCase<Unit, DistanceUnit> {

    override fun build(args: Unit): Flow<DistanceUnit> = distanceUnitSettings.observeUnit(platform.regionCode)
}
