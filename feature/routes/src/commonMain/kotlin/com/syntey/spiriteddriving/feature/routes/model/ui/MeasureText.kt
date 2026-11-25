package com.syntey.spiriteddriving.feature.routes.model.ui

import com.syntey.spiriteddriving.core.domain.geo.Length
import com.syntey.spiriteddriving.core.domain.settings.DistanceUnit
import dev.icerock.moko.resources.desc.StringDesc
import com.syntey.spiriteddriving.core.resources.measure.distanceText as resourcesDistanceText

/** [distance] as the sheet shows it, in [unit]. */
internal fun distanceText(distance: Length, unit: DistanceUnit, isApproximate: Boolean): StringDesc =
    resourcesDistanceText(distance.inMetres, inMiles = unit == DistanceUnit.Miles, isApproximate = isApproximate)
