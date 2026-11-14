package com.syntey.spiriteddriving.data.network.dto.plan

import kotlinx.serialization.Serializable

/**
 * A budget (`Budget` schema). Its `type` discriminator is what kotlinx.serialization writes by default, so each subclass
 * is sent with the spec's `type` value.
 */
@Serializable
internal sealed interface BudgetDto
