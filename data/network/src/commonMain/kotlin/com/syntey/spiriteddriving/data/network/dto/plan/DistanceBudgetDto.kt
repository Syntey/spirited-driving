package com.syntey.spiriteddriving.data.network.dto.plan

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** A budget in distance (`DistanceBudget` schema). */
@Serializable
@SerialName("DISTANCE")
internal data class DistanceBudgetDto(@SerialName("extraMeters") val extraMeters: Int) : BudgetDto
