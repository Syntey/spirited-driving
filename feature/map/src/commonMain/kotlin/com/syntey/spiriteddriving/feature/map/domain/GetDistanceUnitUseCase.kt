package com.syntey.spiriteddriving.feature.map.domain

import app.futured.arkitekt.crusecases.UseCase
import com.syntey.spiriteddriving.core.domain.settings.DistanceUnit
import com.syntey.spiriteddriving.core.platform.binding.Platform
import org.koin.core.annotation.Factory
import org.koin.core.annotation.Provided

/** The unit distances read in: from the phone's region until Settings lets the user choose. */
@Factory
internal class GetDistanceUnitUseCase(@Provided private val platform: Platform) : UseCase<Unit, DistanceUnit> {

    override suspend fun build(args: Unit): DistanceUnit = DistanceUnit.of(platform.regionCode)
}
