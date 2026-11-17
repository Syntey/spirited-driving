package com.syntey.spiriteddriving.feature.map.tools

import com.syntey.spiriteddriving.core.domain.drive.RatingReason
import com.syntey.spiriteddriving.core.resources.*
import dev.icerock.moko.resources.desc.StringDesc
import dev.icerock.moko.resources.desc.desc

/** The chip's text of the reason. */
internal fun RatingReason.label(): StringDesc = when (this) {
    RatingReason.CurvyRoads -> MR.strings.reason_curvy_roads.desc()
    RatingReason.NiceViews -> MR.strings.reason_nice_views.desc()
    RatingReason.LittleTraffic -> MR.strings.reason_little_traffic.desc()
    RatingReason.SmoothSurface -> MR.strings.reason_smooth_surface.desc()
    RatingReason.WorthTheExtraTime -> MR.strings.reason_worth_the_extra_time.desc()
    RatingReason.BoringRoads -> MR.strings.reason_boring_roads.desc()
    RatingReason.TooManyTowns -> MR.strings.reason_too_many_towns.desc()
    RatingReason.TooNarrow -> MR.strings.reason_too_narrow.desc()
    RatingReason.RoughSurface -> MR.strings.reason_rough_surface.desc()
    RatingReason.HeavyTraffic -> MR.strings.reason_heavy_traffic.desc()
    RatingReason.Roadworks -> MR.strings.reason_roadworks.desc()
    RatingReason.TookTooLong -> MR.strings.reason_took_too_long.desc()
    RatingReason.MapsAppWentAnotherWay -> MR.strings.reason_maps_app_went_another_way.desc()
}
