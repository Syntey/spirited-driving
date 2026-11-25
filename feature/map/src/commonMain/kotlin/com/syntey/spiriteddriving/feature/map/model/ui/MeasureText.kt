package com.syntey.spiriteddriving.feature.map.model.ui

import com.syntey.spiriteddriving.core.domain.geo.Length
import com.syntey.spiriteddriving.core.domain.settings.DistanceUnit
import dev.icerock.moko.resources.desc.StringDesc
import com.syntey.spiriteddriving.core.resources.measure.distanceText as resourcesDistanceText
import com.syntey.spiriteddriving.core.resources.measure.heightText as resourcesHeightText

/** [distance] as the sheet shows it, in [unit]. */
internal fun distanceText(distance: Length, unit: DistanceUnit, isApproximate: Boolean): StringDesc =
    resourcesDistanceText(distance.inMetres, inMiles = unit == DistanceUnit.Miles, isApproximate = isApproximate)

/** [height] as the sheet shows a climb, in feet when [unit] is miles. */
internal fun heightText(height: Length, unit: DistanceUnit): StringDesc =
    resourcesHeightText(height.inMetres, inFeet = unit == DistanceUnit.Miles)
