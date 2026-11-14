package com.syntey.spiriteddriving.data.persistence.persistence.route

import com.syntey.spiriteddriving.core.domain.geo.Length.Companion.metres
import com.syntey.spiriteddriving.core.domain.route.Budget
import com.syntey.spiriteddriving.core.domain.route.BudgetChoice
import com.syntey.spiriteddriving.core.domain.route.BudgetKind
import kotlinx.serialization.Serializable
import kotlin.time.Duration.Companion.minutes

/**
 * A budget choice as kept on the device. Its shape is the stored format, so renaming a field loses the kept choice.
 */
@Serializable
internal data class StoredBudgetChoice(val timeMinutes: Int, val distanceMetres: Int, val kind: String)

internal fun BudgetChoice.toStored() = StoredBudgetChoice(
    timeMinutes = time.extra.inWholeMinutes.toInt(),
    distanceMetres = distance.extra.inWholeMetres,
    kind = when (kind) {
        BudgetKind.Time -> "time"
        BudgetKind.Distance -> "distance"
    },
)

internal fun StoredBudgetChoice.toDomain() = BudgetChoice(
    time = Budget.Time(timeMinutes.minutes),
    distance = Budget.Distance(distanceMetres.metres),
    kind = if (kind == "distance") BudgetKind.Distance else BudgetKind.Time,
)
