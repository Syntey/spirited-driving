package com.syntey.spiriteddriving.core.domain.settings

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map

/** The distance unit the user picked in Settings, kept on the device. Until they pick, the phone's region decides. */
interface DistanceUnitSettings {

    /** The picked unit, now and after each change; `null` while the user hasn't picked. */
    fun observe(): Flow<DistanceUnit?>

    suspend fun set(unit: DistanceUnit)
}

/** The unit distances read in, now and after each pick: the one the user picked, else the unit of [regionCode]. */
fun DistanceUnitSettings.observeUnit(regionCode: String?): Flow<DistanceUnit> = observe()
    .map { it ?: DistanceUnit.of(regionCode) }
    .distinctUntilChanged()
