package com.syntey.spiriteddriving.core.domain.settings

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

/**
 * [DistanceUnitSettings] that keeps the unit in memory, `null` until [set] is called.
 */
class FakeDistanceUnitSettings(unit: DistanceUnit? = null) : DistanceUnitSettings {

    private val unit = MutableStateFlow(unit)

    override fun observe(): Flow<DistanceUnit?> = unit

    override suspend fun set(unit: DistanceUnit) {
        this.unit.value = unit
    }
}
