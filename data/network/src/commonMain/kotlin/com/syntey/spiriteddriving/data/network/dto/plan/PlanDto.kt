package com.syntey.spiriteddriving.data.network.dto.plan

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** Response of `POST /plans` (`Plan` schema). */
@Serializable
internal data class PlanDto(
    @SerialName("expiresAt") val expiresAt: String,
    @SerialName("fastest") val fastest: RouteDto,
    @SerialName("funRoutes") val funRoutes: List<RouteDto>,
    @SerialName("closestFunRouteBudget") val closestFunRouteBudget: BudgetDto? = null,
)
