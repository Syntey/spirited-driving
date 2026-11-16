package com.syntey.spiriteddriving.feature.profile.domain

import app.futured.arkitekt.crusecases.UseCase
import com.syntey.spiriteddriving.core.domain.settings.DistanceUnit
import com.syntey.spiriteddriving.core.domain.settings.DistanceUnitSettings
import org.koin.core.annotation.Factory

/** Keeps the distance unit the user picked. */
@Factory
internal class SetDistanceUnitUseCase(private val distanceUnitSettings: DistanceUnitSettings) : UseCase<DistanceUnit, Unit> {

    override suspend fun build(args: DistanceUnit) = distanceUnitSettings.set(args)
}
