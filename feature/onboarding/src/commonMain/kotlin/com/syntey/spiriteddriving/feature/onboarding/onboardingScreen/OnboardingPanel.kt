package com.syntey.spiriteddriving.feature.onboarding.onboardingScreen

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Constraints
import com.syntey.spiriteddriving.core.resources.*
import com.syntey.spiriteddriving.core.ui.components.AppButton
import com.syntey.spiriteddriving.core.ui.components.AppChip
import com.syntey.spiriteddriving.core.ui.glass.GlassSurface
import com.syntey.spiriteddriving.core.ui.theme.AppTheme
import com.syntey.spiriteddriving.core.ui.theme.Grid
import com.syntey.spiriteddriving.core.ui.theme.UiPlatform
import dev.chrisbanes.haze.HazeState
import dev.icerock.moko.resources.StringResource
import dev.icerock.moko.resources.compose.painterResource
import dev.icerock.moko.resources.compose.stringResource
import kotlin.math.abs
import kotlin.math.roundToInt

/**
 * The glass panel at the bottom of First launch: the pages, which slide with [position], the page dots, the current page's
 * buttons and, on the first page, the terms line.
 */
@Composable
internal fun Panel(
    step: OnboardingStep,
    position: PagePosition,
    actions: OnboardingScreen.Actions,
    hazeState: HazeState,
    modifier: Modifier = Modifier,
) {
    val style = AppTheme.platformStyle.bottomPanel
    val systemBottom = WindowInsets.safeDrawing.asPaddingValues().calculateBottomPadding()
    GlassSurface(
        state = hazeState,
        shape = style.shape,
        modifier = modifier
            .fillMaxWidth()
            .padding(start = style.inset, end = style.inset, bottom = style.inset),
    ) {
        PanelLayout(
            termsShown = { termsShown(position.value) },
            pages = {
                for (page in OnboardingStep.entries) {
                    Page(page = page, isCurrent = page == step, position = position)
                }
            },
            dots = { PageDots(position = position, step = step) },
            buttons = {
                Buttons(
                    shown = position.nearest,
                    actions = actions,
                    modifier = Modifier.padding(horizontal = style.contentSides),
                )
            },
            terms = {
                TermsLine(
                    isCurrent = step == OnboardingStep.WhatItIs,
                    shown = { termsShown(position.value) },
                    modifier = Modifier.padding(horizontal = style.contentSides),
                )
            },
            modifier = Modifier.padding(
                top = style.contentTop,
                bottom = style.contentBottom + (systemBottom - style.inset).coerceAtLeast(Grid.d0),
            ),
        )
    }
}

// The terms line is on the first page only, so Skip passes it too.
private fun termsShown(position: Float) = (1 - position).coerceIn(0f, 1f)

/**
 * The panel keeps one height on every page, set by the tallest page, so it doesn't jump while swiping. Below the pages
 * are the dots, the buttons and the terms line. As the terms line fades out after the first page, the dots and the buttons
 * move down into its place. When the panel reaches the top of the screen, the pages scroll.
 */
@Composable
private fun PanelLayout(
    termsShown: () -> Float,
    pages: @Composable () -> Unit,
    dots: @Composable () -> Unit,
    buttons: @Composable () -> Unit,
    terms: @Composable () -> Unit,
    modifier: Modifier = Modifier,
) {
    Layout(contents = listOf(pages, dots, buttons, terms), modifier = modifier.clipToBounds()) { measurables, constraints ->
        val loose = constraints.copy(minWidth = 0, minHeight = 0)
        val dotsPlaceable = measurables[1].single().measure(loose)
        val buttonsPlaceable = measurables[2].single().measure(loose)
        val termsPlaceable = measurables[3].single().measure(loose)
        val belowPages = Grid.d6.roundToPx()
        val belowDots = Grid.d4.roundToPx()
        val actionsHeight = belowPages + dotsPlaceable.height + belowDots + buttonsPlaceable.height + termsPlaceable.height

        val pageConstraints = Constraints(
            maxWidth = constraints.maxWidth,
            maxHeight = (constraints.maxHeight - actionsHeight).coerceAtLeast(0),
        )
        val pagePlaceables = measurables[0].map { it.measure(pageConstraints) }
        val pagesHeight = pagePlaceables.maxOf { it.height }

        layout(constraints.maxWidth, (pagesHeight + actionsHeight).coerceAtMost(constraints.maxHeight)) {
            pagePlaceables.forEach { it.place(0, 0) }
            var y = pagesHeight + belowPages + (termsPlaceable.height * (1 - termsShown())).roundToInt()
            dotsPlaceable.place((constraints.maxWidth - dotsPlaceable.width) / 2, y)
            y += dotsPlaceable.height + belowDots
            buttonsPlaceable.place(0, y)
            termsPlaceable.place(0, y + buttonsPlaceable.height)
        }
    }
}

// A page slides with the position, a full panel width per step. Only the current page is read by screen readers.
@Composable
private fun Page(page: OnboardingStep, isCurrent: Boolean, position: PagePosition, modifier: Modifier = Modifier) {
    val sides = AppTheme.platformStyle.bottomPanel.contentSides
    Column(
        modifier = modifier
            .fillMaxWidth()
            .graphicsLayer {
                val offset = page.ordinal - position.value
                translationX = offset * size.width
                alpha = if (abs(offset) < 1f) 1f else 0f
            }
            .then(if (isCurrent) Modifier else Modifier.clearAndSetSemantics {})
            .verticalScroll(rememberScrollState())
            .padding(horizontal = sides),
    ) {
        if (page == OnboardingStep.WhatItIs) {
            CoverageLabel()
            Spacer(Modifier.height(Grid.d4))
        }
        Text(
            text = stringResource(page.title()),
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Text(
            text = stringResource(page.body()),
            modifier = Modifier.padding(top = Grid.d2),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        if (page == OnboardingStep.Budget) {
            BudgetChips(modifier = Modifier.padding(top = Grid.d4))
        }
    }
}

@Composable
@ReadOnlyComposable
private fun OnboardingStep.title(): StringResource {
    val ios = AppTheme.platform == UiPlatform.Ios
    return when (this) {
        OnboardingStep.WhatItIs -> MR.strings.onboarding_what_title
        OnboardingStep.Budget -> MR.strings.onboarding_budget_title
        OnboardingStep.HandOff -> if (ios) MR.strings.onboarding_handoff_title_ios else MR.strings.onboarding_handoff_title_android
        OnboardingStep.Location -> MR.strings.onboarding_location_title
        OnboardingStep.Vignette -> MR.strings.onboarding_vignette_title
    }
}

@Composable
@ReadOnlyComposable
private fun OnboardingStep.body(): StringResource {
    val ios = AppTheme.platform == UiPlatform.Ios
    return when (this) {
        OnboardingStep.WhatItIs -> MR.strings.onboarding_what_body
        OnboardingStep.Budget -> MR.strings.onboarding_budget_body
        OnboardingStep.HandOff -> if (ios) MR.strings.onboarding_handoff_body_ios else MR.strings.onboarding_handoff_body_android
        OnboardingStep.Location -> if (ios) MR.strings.onboarding_location_body_ios else MR.strings.onboarding_location_body_android
        OnboardingStep.Vignette -> MR.strings.onboarding_vignette_body
    }
}

// The covered area, as a hint: tertiary is the theme's colour for hints.
@Composable
private fun CoverageLabel(modifier: Modifier = Modifier) {
    val color = MaterialTheme.colorScheme.onTertiaryContainer
    Row(
        modifier = modifier
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.tertiaryContainer)
            .heightIn(min = Grid.d7)
            .padding(start = Grid.d2, end = Grid.d3),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Grid.d1_5),
    ) {
        Icon(painter = painterResource(MR.images.place), contentDescription = null, modifier = Modifier.size(Grid.d3_5), tint = color)
        Text(text = stringResource(MR.strings.onboarding_coverage), style = MaterialTheme.typography.labelMedium, color = color)
    }
}

// The first chips of Route planning, with the budget the text names selected. They can't be tapped.
@Suppress("MagicNumber")
@Composable
private fun BudgetChips(modifier: Modifier = Modifier) {
    val selected = stringResource(MR.strings.budget_minutes, 30)
    val description = stringResource(MR.strings.onboarding_budget_chips, selected)
    FlowRow(
        modifier = modifier.clearAndSetSemantics { contentDescription = description },
        horizontalArrangement = Arrangement.spacedBy(Grid.d2),
        verticalArrangement = Arrangement.spacedBy(Grid.d2),
    ) {
        AppChip(label = stringResource(MR.strings.budget_minutes, 15), selected = false)
        AppChip(label = selected, selected = true)
        AppChip(label = stringResource(MR.strings.budget_minutes, 45), selected = false)
        AppChip(label = stringResource(MR.strings.budget_hours, "1"), selected = false)
    }
}

// One dot per intro page, and they fade out with Skip on the way to Location. The current page is a wider dot in primary;
// the dots stretch and fade between pages as they move.
@Composable
private fun PageDots(position: PagePosition, step: OnboardingStep, modifier: Modifier = Modifier) {
    val active = MaterialTheme.colorScheme.primary
    val inactive = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.35f)
    val dot = Grid.d1_5
    val gap = Grid.d1_5
    val stretch = Grid.d3_5
    val count = OnboardingStep.entries.count { it.isIntro }
    val description = if (step.isIntro) stringResource(MR.strings.onboarding_step, step.ordinal + 1, count) else null
    Canvas(
        modifier = modifier
            .size(width = dot * count + stretch + gap * (count - 1), height = dot)
            .graphicsLayer { alpha = position.introShown }
            .then(if (description != null) Modifier.semantics { contentDescription = description } else Modifier),
    ) {
        val dotPx = dot.toPx()
        val gapPx = gap.toPx()
        val stretchPx = stretch.toPx()
        val current = position.value
        val total = (0 until count).sumOf { i -> (dotPx + stretchPx * dotWeight(current, i)).toDouble() }.toFloat() + gapPx * (count - 1)
        var x = (size.width - total) / 2
        for (i in 0 until count) {
            val weight = dotWeight(current, i)
            val dotSize = Size(dotPx + stretchPx * weight, dotPx)
            drawRoundRect(color = inactive, topLeft = Offset(x, 0f), size = dotSize, cornerRadius = CornerRadius(dotPx / 2))
            drawRoundRect(color = active, topLeft = Offset(x, 0f), size = dotSize, cornerRadius = CornerRadius(dotPx / 2), alpha = weight)
            x += dotSize.width + gapPx
        }
    }
}

private fun dotWeight(position: Float, dot: Int) = (1 - abs(position - dot)).coerceAtLeast(0f)

// The buttons of the page nearest to the position, so they change halfway through a swipe.
@Composable
private fun Buttons(shown: OnboardingStep, actions: OnboardingScreen.Actions, modifier: Modifier = Modifier) {
    Box(modifier.fillMaxWidth()) {
        when (shown) {
            OnboardingStep.WhatItIs, OnboardingStep.Budget, OnboardingStep.HandOff -> AppButton(
                text = stringResource(MR.strings.onboarding_next),
                onClick = actions::onNext,
                modifier = Modifier.fillMaxWidth(),
            )

            OnboardingStep.Location -> AppButton(
                text = stringResource(MR.strings.onboarding_continue),
                onClick = actions::onContinue,
                modifier = Modifier.fillMaxWidth(),
            )

            // Both answers look the same, so neither looks preferred: the same style, width and height, even when a label wraps.
            OnboardingStep.Vignette -> Row(
                modifier = Modifier.height(IntrinsicSize.Min),
                horizontalArrangement = Arrangement.spacedBy(Grid.d2),
            ) {
                AppButton(
                    text = stringResource(MR.strings.onboarding_vignette_yes),
                    onClick = { actions.onVignetteAnswered(hasVignette = true) },
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                )
                AppButton(
                    text = stringResource(MR.strings.onboarding_vignette_no),
                    onClick = { actions.onVignetteAnswered(hasVignette = false) },
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                )
            }
        }
    }
}

@Composable
private fun TermsLine(isCurrent: Boolean, shown: () -> Float, modifier: Modifier = Modifier) {
    val terms = stringResource(MR.strings.onboarding_terms_link)
    val privacyPolicy = stringResource(MR.strings.onboarding_privacy_link)
    val text = stringResource(MR.strings.onboarding_terms, terms, privacyPolicy)
    val linkStyles = TextLinkStyles(style = SpanStyle(color = MaterialTheme.colorScheme.primary))
    val annotated = remember(text, terms, privacyPolicy, linkStyles) {
        buildAnnotatedString {
            append(text)
            val termsStart = text.indexOf(terms)
            val privacyStart = text.indexOf(privacyPolicy)
            if (termsStart >= 0) addLink(LinkAnnotation.Url(LegalPages.TERMS_URL, linkStyles), termsStart, termsStart + terms.length)
            if (privacyStart >= 0) {
                addLink(LinkAnnotation.Url(LegalPages.PRIVACY_POLICY_URL, linkStyles), privacyStart, privacyStart + privacyPolicy.length)
            }
        }
    }
    Text(
        text = annotated,
        modifier = modifier
            .fillMaxWidth()
            .padding(top = Grid.d3)
            .graphicsLayer { alpha = shown() }
            .then(if (isCurrent) Modifier else Modifier.clearAndSetSemantics {}),
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        textAlign = TextAlign.Center,
    )
}
