package com.syntey.spiriteddriving.data.network.dto.plan

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** Request body of `POST /plans` (`PlanRequest`). */
@Serializable
internal data class PlanRequestDto(
    @SerialName("start") val start: PlaceInputDto,
    @SerialName("destination") val destination: PlaceInputDto,
    @SerialName("budget") val budget: BudgetDto,
    @SerialName("vignettes") val vignettes: Set<String>,
    @SerialName("maxHandOffWaypoints") val maxHandOffWaypoints: Int,
)
