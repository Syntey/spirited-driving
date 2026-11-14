package com.syntey.spiriteddriving.feature.map.model.ui

import com.syntey.spiriteddriving.core.domain.route.Budget
import dev.icerock.moko.resources.desc.StringDesc

/** A budget chip as the sheet shows it. */
data class BudgetChip(val budget: Budget, val label: StringDesc, val isSelected: Boolean)
