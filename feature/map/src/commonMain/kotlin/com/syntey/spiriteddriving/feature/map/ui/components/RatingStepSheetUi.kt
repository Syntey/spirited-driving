package com.syntey.spiriteddriving.feature.map.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.syntey.spiriteddriving.core.domain.drive.Rating
import com.syntey.spiriteddriving.core.domain.drive.RatingReason
import com.syntey.spiriteddriving.core.domain.drive.Thumb
import com.syntey.spiriteddriving.core.resources.*
import com.syntey.spiriteddriving.core.ui.components.AppButton
import com.syntey.spiriteddriving.core.ui.components.AppChip
import com.syntey.spiriteddriving.core.ui.components.ButtonDefaultColors
import com.syntey.spiriteddriving.core.ui.components.ButtonDefaultStyles
import com.syntey.spiriteddriving.core.ui.components.ChipDefaultColors
import com.syntey.spiriteddriving.core.ui.components.IconAction
import com.syntey.spiriteddriving.core.ui.components.NoteCard
import com.syntey.spiriteddriving.core.ui.components.RatingThumbs
import com.syntey.spiriteddriving.core.ui.components.RouteBadge
import com.syntey.spiriteddriving.core.ui.components.SheetHeader
import com.syntey.spiriteddriving.core.ui.components.Showcase
import com.syntey.spiriteddriving.core.ui.components.ThumbContent
import com.syntey.spiriteddriving.core.ui.preview.ComponentPreviews
import com.syntey.spiriteddriving.core.ui.theme.Grid
import com.syntey.spiriteddriving.core.ui.theme.UiPlatform
import com.syntey.spiriteddriving.feature.map.model.ui.MapSheetPreviewStates
import com.syntey.spiriteddriving.feature.map.ui.mapSheetScreen.Content
import com.syntey.spiriteddriving.feature.map.ui.mapSheetScreen.MapSheetScreen
import com.syntey.spiriteddriving.feature.map.ui.mapSheetScreen.MapSheetScreen.Actions.Companion.noOpActions
import com.syntey.spiriteddriving.feature.map.ui.mapSheetScreen.MapSheetViewState
import dev.icerock.moko.resources.compose.localized
import dev.icerock.moko.resources.compose.painterResource
import dev.icerock.moko.resources.compose.stringResource

/**
 * The top of the rating step: its question, a close button, and below them the route's badge with the drive's time,
 * planned duration and distance.
 */
@Composable
internal fun RatingStepHeader(viewState: MapSheetViewState, actions: MapSheetScreen.Actions, modifier: Modifier = Modifier) {
    // Both texts are null only without the step, which the sheet doesn't show the header for
    val number = viewState.ratingStep?.question?.routeNumber ?: return
    SheetHeader(
        title = viewState.ratingTitle?.localized().orEmpty(),
        close = IconAction(
            icon = painterResource(MR.images.close),
            label = stringResource(MR.strings.generic_close),
            onClick = actions::onRatingClose,
        ),
        subtitle = viewState.ratingDriveText?.localized().orEmpty(),
        subtitleLeading = { RatingRouteBadge(number = number) },
        modifier = modifier,
    )
}

/** The route's badge in the route's colour, as the rating step and the reminder bubble show it. */
@Composable
internal fun RatingRouteBadge(number: Int, modifier: Modifier = Modifier) {
    RouteBadge(
        color = routeColor(number),
        contentDescription = stringResource(MR.strings.route_badge, number),
        number = number,
        modifier = modifier,
    )
}

/**
 * Everything below the header: the thumbs, then "I didn't drive it" before a thumb is picked, or the thanks, the reason
 * chips and Done after it, with the attribution at the end. It scrolls when the sheet is shorter than its content.
 */
@Composable
internal fun RatingStepBody(viewState: MapSheetViewState, actions: MapSheetScreen.Actions, modifier: Modifier = Modifier) {
    val rating = viewState.ratingStep?.rating
    val style = MapPlatformStyle.current
    Column(
        modifier = modifier.fillMaxWidth().verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(style.sheetSpacing),
    ) {
        RatingStepThumbs(rating = rating, actions = actions)
        if (rating == null) {
            AppButton(
                text = stringResource(MR.strings.drive_feedback_not_driven),
                onClick = actions::onNotDrivenClick,
                colors = ButtonDefaultColors.text(),
                style = ButtonDefaultStyles.medium(),
                modifier = Modifier.align(Alignment.CenterHorizontally),
            )
        } else {
            viewState.ratingThanks?.let { thanks ->
                Text(
                    text = thanks.localized(),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(Grid.d2),
                verticalArrangement = Arrangement.spacedBy(Grid.d2),
            ) {
                viewState.reasonChips.forEach { chip ->
                    AppChip(
                        label = chip.label.localized(),
                        selected = chip.isSelected,
                        onClick = { actions.onRatingReasonClick(chip.reason) },
                        colors = ChipDefaultColors.filter(),
                        checkIcon = painterResource(MR.images.check),
                    )
                }
            }
            if (viewState.showsReminderOffer) {
                ReminderOffer(actions = actions)
            }
            AppButton(
                text = stringResource(MR.strings.drive_feedback_done),
                onClick = actions::onRatingDone,
                modifier = Modifier.fillMaxWidth(),
            )
        }
        RouteAttribution()
    }
}

/**
 * The rating step when the sheet is collapsed: its header and the thumbs. It reports this height as the sheet's collapsed
 * height, so the sheet stops where the thumbs are.
 */
@Composable
internal fun CollapsedRatingStep(viewState: MapSheetViewState, actions: MapSheetScreen.Actions, modifier: Modifier = Modifier) {
    val step = viewState.ratingStep ?: return
    val style = MapPlatformStyle.current
    Column(
        modifier = modifier.fillMaxWidth().padding(style.sheetContentPadding),
        verticalArrangement = Arrangement.spacedBy(style.sheetSpacing),
    ) {
        RatingStepHeader(viewState = viewState, actions = actions)
        RatingStepThumbs(rating = step.rating, actions = actions)
    }
}

// The offer of the drive reminder, a hint with its own button; it's Android only, as iOS asks for the permission on its own
@Composable
private fun ReminderOffer(actions: MapSheetScreen.Actions, modifier: Modifier = Modifier) {
    NoteCard(
        text = stringResource(MR.strings.drive_feedback_offer_text),
        modifier = modifier,
        title = stringResource(MR.strings.drive_feedback_offer_title),
        icon = painterResource(MR.images.bell),
        actionLabel = stringResource(MR.strings.drive_feedback_offer_action),
        onAction = actions::onReminderOfferClick,
    )
}

// Good and Not good, with the drive's rating picked
@Composable
private fun RatingStepThumbs(rating: Rating?, actions: MapSheetScreen.Actions) {
    RatingThumbs(
        groupLabel = stringResource(MR.strings.rating_label),
        good = ThumbContent(
            label = stringResource(MR.strings.rating_good),
            icon = painterResource(MR.images.thumb_up),
            pickedIcon = painterResource(MR.images.thumb_up_filled),
            picked = rating?.thumb == Thumb.Up,
            onClick = { actions.onRatingThumbClick(Thumb.Up) },
        ),
        notGood = ThumbContent(
            label = stringResource(MR.strings.rating_not_good),
            icon = painterResource(MR.images.thumb_down),
            pickedIcon = painterResource(MR.images.thumb_down_filled),
            picked = rating?.thumb == Thumb.Down,
            onClick = { actions.onRatingThumbClick(Thumb.Down) },
        ),
    )
}

@ComponentPreviews
@Composable
private fun RatingStepPreview() = Showcase {
    Content(
        viewState = MapSheetPreviewStates.ratingViewState(),
        actions = noOpActions(),
        onCollapsedHeightChange = {},
        modifier = Modifier.height(Grid.d100),
    )
}

@ComponentPreviews
@Composable
private fun RatingStepIosPreview() = Showcase(platform = UiPlatform.Ios) {
    Content(
        viewState = MapSheetPreviewStates.ratingViewState(),
        actions = noOpActions(),
        onCollapsedHeightChange = {},
        modifier = Modifier.height(Grid.d100),
    )
}

@ComponentPreviews
@Composable
private fun RatingStepGoodPreview() = Showcase {
    Content(
        viewState = MapSheetPreviewStates.ratingViewState(
            Rating(thumb = Thumb.Up, reasons = setOf(RatingReason.CurvyRoads, RatingReason.NiceViews)),
        ),
        actions = noOpActions(),
        onCollapsedHeightChange = {},
        modifier = Modifier.height(Grid.d100),
    )
}

@ComponentPreviews
@Composable
private fun RatingStepGoodIosPreview() = Showcase(platform = UiPlatform.Ios) {
    Content(
        viewState = MapSheetPreviewStates.ratingViewState(
            Rating(thumb = Thumb.Up, reasons = setOf(RatingReason.CurvyRoads, RatingReason.NiceViews)),
        ),
        actions = noOpActions(),
        onCollapsedHeightChange = {},
        modifier = Modifier.height(Grid.d100),
    )
}

@ComponentPreviews
@Composable
private fun RatingStepNotGoodPreview() = Showcase {
    Content(
        viewState = MapSheetPreviewStates.ratingViewState(Rating(thumb = Thumb.Down, reasons = setOf(RatingReason.TookTooLong))),
        actions = noOpActions(),
        onCollapsedHeightChange = {},
        modifier = Modifier.height(Grid.d100),
    )
}

@ComponentPreviews
@Composable
private fun RatingStepNotGoodIosPreview() = Showcase(platform = UiPlatform.Ios) {
    Content(
        viewState = MapSheetPreviewStates.ratingViewState(Rating(thumb = Thumb.Down, reasons = setOf(RatingReason.TookTooLong))),
        actions = noOpActions(),
        onCollapsedHeightChange = {},
        modifier = Modifier.height(Grid.d100),
    )
}

@ComponentPreviews
@Composable
private fun RatingStepGoodReminderOfferPreview() = Showcase {
    Content(
        viewState = MapSheetPreviewStates.ratingViewState(
            Rating(thumb = Thumb.Up, reasons = setOf(RatingReason.CurvyRoads, RatingReason.NiceViews)),
            showsReminderOffer = true,
        ),
        actions = noOpActions(),
        onCollapsedHeightChange = {},
        modifier = Modifier.height(Grid.d100),
    )
}

@ComponentPreviews
@Composable
private fun RatingStepCollapsedPreview() = Showcase {
    CollapsedRatingStep(viewState = MapSheetPreviewStates.ratingViewState(), actions = noOpActions())
}
