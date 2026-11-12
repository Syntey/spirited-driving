package com.syntey.spiriteddriving.data.network.dto.plan

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** A budget in time (`TimeBudget` schema). */
@Serializable
@SerialName("TIME")
internal data class TimeBudgetDto(@SerialName("extraSeconds") val extraSeconds: Int) : BudgetDto
