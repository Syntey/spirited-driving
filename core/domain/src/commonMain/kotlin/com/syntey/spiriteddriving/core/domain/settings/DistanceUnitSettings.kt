package com.syntey.spiriteddriving.core.domain.settings

import kotlinx.coroutines.flow.Flow

/** The distance unit the user picked in Settings, kept on the device. Until they pick, the phone's region decides. */
interface DistanceUnitSettings {

    /** The picked unit, now and after each change; `null` while the user hasn't picked. */
    fun observe(): Flow<DistanceUnit?>

    suspend fun set(unit: DistanceUnit)
}
