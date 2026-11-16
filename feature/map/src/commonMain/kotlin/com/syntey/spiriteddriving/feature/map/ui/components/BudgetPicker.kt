package com.syntey.spiriteddriving.feature.map.ui.components

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.syntey.spiriteddriving.core.domain.route.Budget
import com.syntey.spiriteddriving.core.domain.route.BudgetKind
import com.syntey.spiriteddriving.core.domain.settings.DistanceUnit
import com.syntey.spiriteddriving.core.resources.*
import com.syntey.spiriteddriving.core.ui.components.AppChip
import com.syntey.spiriteddriving.core.ui.components.SegmentOption
import com.syntey.spiriteddriving.core.ui.components.SegmentedSwitch
import com.syntey.spiriteddriving.core.ui.theme.Grid
import com.syntey.spiriteddriving.feature.map.model.ui.BudgetChip
import dev.icerock.moko.resources.StringResource
import dev.icerock.moko.resources.compose.localized
import dev.icerock.moko.resources.compose.stringResource
import kotlinx.collections.immutable.ImmutableList

/**
 * The budget above the routes: its unit and a row of chips in that unit, which scrolls sideways when they don't fit. The
 * second segment names the distance unit, [unit].
 */
@Composable
internal fun BudgetPicker(
    kind: BudgetKind,
    unit: DistanceUnit,
    chips: ImmutableList<BudgetChip>,
    onKindChange: (BudgetKind) -> Unit,
    onBudgetClick: (Budget) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(Grid.d2)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = stringResource(
                    if (kind == BudgetKind.Time) MR.strings.planning_budget_time else MR.strings.planning_budget_distance,
                ),
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f),
            )
            SegmentedSwitch(
                options = listOf(
                    SegmentOption(
                        label = stringResource(MR.strings.planning_budget_minutes),
                        accessibilityLabel = stringResource(MR.strings.planning_budget_minutes_description),
                    ),
                    SegmentOption(
                        label = stringResource(distanceLabel(unit)),
                        accessibilityLabel = stringResource(distanceDescription(unit)),
                    ),
                ),
                selectedIndex = if (kind == BudgetKind.Time) 0 else 1,
                onSelect = { onKindChange(if (it == 0) BudgetKind.Time else BudgetKind.Distance) },
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(Grid.d2),
        ) {
            chips.forEach { chip ->
                AppChip(
                    label = chip.label.localized(),
                    selected = chip.isSelected,
                    onClick = { onBudgetClick(chip.budget) },
                )
            }
        }
    }
}

private fun distanceLabel(unit: DistanceUnit): StringResource = when (unit) {
    DistanceUnit.Kilometres -> MR.strings.planning_budget_kilometres
    DistanceUnit.Miles -> MR.strings.planning_budget_miles
}

private fun distanceDescription(unit: DistanceUnit): StringResource = when (unit) {
    DistanceUnit.Kilometres -> MR.strings.planning_budget_kilometres_description
    DistanceUnit.Miles -> MR.strings.planning_budget_miles_description
}
