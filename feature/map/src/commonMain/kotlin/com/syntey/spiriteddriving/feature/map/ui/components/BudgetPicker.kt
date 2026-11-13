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
import com.syntey.spiriteddriving.core.resources.*
import com.syntey.spiriteddriving.core.ui.components.AppChip
import com.syntey.spiriteddriving.core.ui.components.SegmentOption
import com.syntey.spiriteddriving.core.ui.components.SegmentedSwitch
import com.syntey.spiriteddriving.core.ui.theme.Grid
import com.syntey.spiriteddriving.feature.map.model.ui.BudgetChip
import dev.icerock.moko.resources.compose.localized
import dev.icerock.moko.resources.compose.stringResource
import kotlinx.collections.immutable.ImmutableList

/** The budget above the routes: its unit and a row of chips in that unit, which scrolls sideways when they don't fit. */
@Composable
internal fun BudgetPicker(
    kind: BudgetKind,
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
                        label = stringResource(MR.strings.planning_budget_kilometres),
                        accessibilityLabel = stringResource(MR.strings.planning_budget_kilometres_description),
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
