package com.syntey.spiriteddriving.feature.routes.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.syntey.spiriteddriving.core.domain.drive.Rating
import com.syntey.spiriteddriving.core.domain.drive.RatingReason
import com.syntey.spiriteddriving.core.domain.drive.Thumb
import com.syntey.spiriteddriving.core.resources.*
import com.syntey.spiriteddriving.core.ui.components.AppButton
import com.syntey.spiriteddriving.core.ui.components.AppChip
import com.syntey.spiriteddriving.core.ui.components.ChipDefaultColors
import com.syntey.spiriteddriving.core.ui.components.IconAction
import com.syntey.spiriteddriving.core.ui.components.RatingThumbs
import com.syntey.spiriteddriving.core.ui.components.SheetHeader
import com.syntey.spiriteddriving.core.ui.components.Showcase
import com.syntey.spiriteddriving.core.ui.components.ThumbContent
import com.syntey.spiriteddriving.core.ui.preview.ComponentPreviews
import com.syntey.spiriteddriving.core.ui.theme.Grid
import com.syntey.spiriteddriving.core.ui.theme.UiPlatform
import com.syntey.spiriteddriving.feature.routes.model.ui.RatingSheet
import com.syntey.spiriteddriving.feature.routes.ui.tripHistoryScreen.TripHistoryScreen
import com.syntey.spiriteddriving.feature.routes.ui.tripHistoryScreen.TripHistoryScreen.Actions.Companion.noOpActions
import dev.icerock.moko.resources.compose.localized
import dev.icerock.moko.resources.compose.painterResource
import dev.icerock.moko.resources.compose.stringResource
import dev.icerock.moko.resources.desc.ResourceFormatted
import dev.icerock.moko.resources.desc.StringDesc
import dev.icerock.moko.resources.desc.desc

/**
 * The rating sheet over the list, shared by both platforms: its header with a close button, the thumbs, and after a thumb is
 * picked, the thanks, the reasons of that thumb and Done. It takes the height its content needs, so the sheet fits it.
 */
@Composable
fun RatingSheetContent(sheet: RatingSheet, actions: TripHistoryScreen.Actions, modifier: Modifier = Modifier) {
    val style = RoutesPlatformStyle.current
    Column(
        modifier = modifier.fillMaxWidth().padding(style.sheetContentPadding),
        verticalArrangement = Arrangement.spacedBy(style.sheetSpacing),
    ) {
        SheetHeader(
            title = sheet.title.localized(),
            close = IconAction(
                icon = painterResource(MR.images.close),
                label = stringResource(MR.strings.generic_close),
                onClick = actions::onRatingSheetClose,
            ),
            subtitle = sheet.subtitle.localized(),
        )
        RatingThumbs(
            groupLabel = stringResource(MR.strings.rating_label),
            good = ThumbContent(
                label = stringResource(MR.strings.rating_good),
                icon = painterResource(MR.images.thumb_up),
                pickedIcon = painterResource(MR.images.thumb_up_filled),
                picked = sheet.rating?.thumb == Thumb.Up,
                onClick = { actions.onRatingThumbClick(Thumb.Up) },
            ),
            notGood = ThumbContent(
                label = stringResource(MR.strings.rating_not_good),
                icon = painterResource(MR.images.thumb_down),
                pickedIcon = painterResource(MR.images.thumb_down_filled),
                picked = sheet.rating?.thumb == Thumb.Down,
                onClick = { actions.onRatingThumbClick(Thumb.Down) },
            ),
        )
        if (sheet.rating != null) {
            sheet.thanks?.let { thanks ->
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
                sheet.reasonChips.forEach { chip ->
                    AppChip(
                        label = chip.label.localized(),
                        selected = chip.isSelected,
                        onClick = { actions.onRatingReasonClick(chip.reason) },
                        colors = ChipDefaultColors.filter(),
                        checkIcon = painterResource(MR.images.check),
                    )
                }
            }
            AppButton(
                text = stringResource(MR.strings.drive_feedback_done),
                onClick = actions::onRatingSheetClose,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@ComponentPreviews
@Composable
private fun RatingSheetNoThumbPreview() = Showcase {
    RatingSheetContent(sheet = previewSheet(), actions = noOpActions())
}

@ComponentPreviews
@Composable
private fun RatingSheetNoThumbIosPreview() = Showcase(platform = UiPlatform.Ios) {
    RatingSheetContent(sheet = previewSheet(), actions = noOpActions())
}

@ComponentPreviews
@Composable
private fun RatingSheetGoodPreview() = Showcase {
    RatingSheetContent(
        sheet = previewSheet(Rating(Thumb.Up, setOf(RatingReason.CurvyRoads, RatingReason.NiceViews))),
        actions = noOpActions(),
    )
}

@ComponentPreviews
@Composable
private fun RatingSheetGoodIosPreview() = Showcase(platform = UiPlatform.Ios) {
    RatingSheetContent(
        sheet = previewSheet(Rating(Thumb.Up, setOf(RatingReason.CurvyRoads, RatingReason.NiceViews))),
        actions = noOpActions(),
    )
}

@ComponentPreviews
@Composable
private fun RatingSheetNotGoodPreview() = Showcase {
    RatingSheetContent(
        sheet = previewSheet(Rating(Thumb.Down, setOf(RatingReason.TookTooLong))),
        actions = noOpActions(),
    )
}

@ComponentPreviews
@Composable
private fun RatingSheetNotGoodIosPreview() = Showcase(platform = UiPlatform.Ios) {
    RatingSheetContent(
        sheet = previewSheet(Rating(Thumb.Down, setOf(RatingReason.TookTooLong))),
        actions = noOpActions(),
    )
}

private fun previewSheet(rating: Rating? = null) = RatingSheet(
    driveId = "preview-drive",
    title = StringDesc.ResourceFormatted(MR.strings.drive_feedback_question, "Křivoklát Castle"),
    subtitle = "Yesterday, 16:40 · 1 h 20 · 74 km".desc(),
    rating = rating,
)
