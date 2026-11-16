package com.syntey.spiriteddriving.core.ui.components

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.height
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.progressBarRangeInfo
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxState
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.CustomAccessibilityAction
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.customActions
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import com.syntey.spiriteddriving.core.ui.preview.ComponentPreviews
import com.syntey.spiriteddriving.core.ui.preview.previewIcon
import com.syntey.spiriteddriving.core.ui.theme.AppTheme
import com.syntey.spiriteddriving.core.ui.theme.Grid
import com.syntey.spiriteddriving.core.ui.theme.ListStyle
import com.syntey.spiriteddriving.core.ui.theme.UiPlatform

/** Where a row sits in its group, so grouped rows round only the group's outer corners and separate the rows between. */
enum class GroupPosition {
    Alone,
    First,
    Middle,
    Last,
    ;

    companion object {
        /** The position of row [index] in a group of [count], for rows in a lazy list. */
        fun of(index: Int, count: Int): GroupPosition {
            require(index in 0 until count) { "Row $index is not in a group of $count" }
            return when {
                count == 1 -> Alone
                index == 0 -> First
                index == count - 1 -> Last
                else -> Middle
            }
        }
    }
}

/** The colours of a [ListRow]'s text, taken from [ListRowDefaultColors]. */
@Immutable
data class ListRowColors(val headlineColor: Color, val supportingColor: Color)

/**
 * The colours of [ListRow]: plain text for most rows, primary for an action, error for a destructive row.
 */
object ListRowDefaultColors {

    /** Most rows. */
    @Composable
    fun default(): ListRowColors = ListRowColors(
        headlineColor = MaterialTheme.colorScheme.onSurface,
        supportingColor = MaterialTheme.colorScheme.onSurfaceVariant,
    )

    /** An action row, such as Sign out on iOS. */
    @Composable
    fun action(): ListRowColors = ListRowColors(
        headlineColor = MaterialTheme.colorScheme.primary,
        supportingColor = MaterialTheme.colorScheme.onSurfaceVariant,
    )

    /** A destructive row, such as Delete account. */
    @Composable
    fun error(): ListRowColors = ListRowColors(
        headlineColor = MaterialTheme.colorScheme.error,
        supportingColor = MaterialTheme.colorScheme.onSurfaceVariant,
    )
}

/** The card behind grouped rows, taken from [ListGroupDefaultColors]: on both platforms, rows sit on it. */
@Immutable
data class ListGroupColors(val containerColor: Color)

/**
 * The card behind grouped rows: solid on a plain screen, translucent on the map's glass sheet.
 */
object ListGroupDefaultColors {

    /** On a solid sheet or screen, such as Settings. */
    @Composable
    fun opaque(): ListGroupColors = ListGroupColors(AppTheme.customColors.listGroup)

    /** On a glass sheet over the map, such as search results. */
    @Composable
    fun translucent(): ListGroupColors =
        ListGroupColors(AppTheme.customColors.listGroup.copy(alpha = TRANSLUCENT_GROUP_ALPHA))
}

/**
 * A row of a list: an optional leading icon or image, the headline, an optional supporting line and value, and an
 * optional trailing control. With [ListStyle.isOnCard] it sits on the grouped card at [position]: iOS separates rows
 * with hairlines, Android with small gaps.
 *
 * This overload takes a plain [headline]; see the [AnnotatedString] overload for a styled one.
 */
@Composable
fun ListRow(
    headline: String,
    modifier: Modifier = Modifier,
    supporting: String? = null,
    value: String? = null,
    leading: (@Composable () -> Unit)? = null,
    trailing: (@Composable () -> Unit)? = null,
    onClick: (() -> Unit)? = null,
    swipeDelete: IconAction? = null,
    chevron: Painter? = null,
    position: GroupPosition = GroupPosition.Alone,
    colors: ListRowColors = ListRowDefaultColors.default(),
    groupColors: ListGroupColors = ListGroupDefaultColors.opaque(),
    isDimmed: Boolean = false,
    isLoading: Boolean = false,
    tabularSupporting: Boolean = false,
    contentDescription: String? = null,
    style: ListStyle = AppTheme.platformStyle.list,
) {
    ListRow(
        headline = AnnotatedString(headline),
        modifier = modifier,
        supporting = supporting,
        value = value,
        leading = leading,
        trailing = trailing,
        onClick = onClick,
        swipeDelete = swipeDelete,
        chevron = chevron,
        position = position,
        colors = colors,
        groupColors = groupColors,
        isDimmed = isDimmed,
        isLoading = isLoading,
        tabularSupporting = tabularSupporting,
        contentDescription = contentDescription,
        style = style,
    )
}

/**
 * A row of a list: an optional leading icon or image, the headline, an optional supporting line and value, and an
 * optional trailing control. With [ListStyle.isOnCard] it sits on the grouped card at [position]: iOS separates rows
 * with hairlines, Android with small gaps.
 *
 * @param headline The headline, possibly with styled spans, such as the matched part of a place's name in bold.
 * @param value A setting's current value, such as "km": after the label on iOS, below it on Android.
 * @param chevron A row that opens a screen passes the chevron; it shows on iOS only, where such rows end in one.
 * @param isDimmed Fades the row, such as a place outside the covered area. It stays tappable.
 * @param isLoading The [CarSpinner] takes the headline's place and taps are ignored, such as while deleting the account.
 * @param swipeDelete Lets the row be swiped left to delete it, on both platforms. Its icon shows in the red area, and
 *   screen readers get its label as an action instead. A row that stays, such as when offline, slides back.
 * @param tabularSupporting Sets the [supporting] line's digits to equal width, so distances in a column line up.
 * @param contentDescription What screen readers say for the whole row instead of its texts, when they don't make a good
 *   sentence on their own, such as a place's name, kind, distance and area. Null keeps the texts.
 * @param style [PlatformStyle.list][com.syntey.spiriteddriving.core.ui.theme.PlatformStyle.list] on a screen;
 *   [PlatformStyle.sheetList][com.syntey.spiriteddriving.core.ui.theme.PlatformStyle.sheetList] on a sheet over the map.
 */
@Composable
fun ListRow(
    headline: AnnotatedString,
    modifier: Modifier = Modifier,
    supporting: String? = null,
    value: String? = null,
    leading: (@Composable () -> Unit)? = null,
    trailing: (@Composable () -> Unit)? = null,
    onClick: (() -> Unit)? = null,
    swipeDelete: IconAction? = null,
    chevron: Painter? = null,
    position: GroupPosition = GroupPosition.Alone,
    colors: ListRowColors = ListRowDefaultColors.default(),
    groupColors: ListGroupColors = ListGroupDefaultColors.opaque(),
    isDimmed: Boolean = false,
    isLoading: Boolean = false,
    tabularSupporting: Boolean = false,
    contentDescription: String? = null,
    style: ListStyle = AppTheme.platformStyle.list,
) {
    val row: @Composable () -> Unit = {
        ListRowSurface(
            style = style,
            headline = headline,
            supporting = supporting,
            value = value,
            leading = leading,
            trailing = trailing,
            onClick = onClick,
            swipeDelete = swipeDelete,
            chevron = chevron,
            position = position,
            colors = colors,
            groupColors = groupColors,
            isDimmed = isDimmed,
            isLoading = isLoading,
            tabularSupporting = tabularSupporting,
            contentDescription = contentDescription,
        )
    }
    // The inset and the clip stay still while the row moves, so the card's corners hold during a swipe
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = style.groupInset)
            .padding(bottom = if (position == GroupPosition.First || position == GroupPosition.Middle) style.rowGap else Grid.d0)
            .clip(position.shape(style.outerCorner, style.innerCorner)),
    ) {
        if (swipeDelete != null) {
            SwipeDeleteBox(delete = swipeDelete, content = row)
        } else {
            row()
        }
    }
}

@Composable
private fun ListRowSurface(
    style: ListStyle,
    headline: AnnotatedString,
    supporting: String?,
    value: String?,
    leading: (@Composable () -> Unit)?,
    trailing: (@Composable () -> Unit)?,
    onClick: (() -> Unit)?,
    swipeDelete: IconAction?,
    chevron: Painter?,
    position: GroupPosition,
    colors: ListRowColors,
    groupColors: ListGroupColors,
    isDimmed: Boolean,
    isLoading: Boolean,
    tabularSupporting: Boolean,
    contentDescription: String?,
) {
    // iOS separators start at the text, so they don't cut through the leading icons
    val separatorStart = style.rowPadding.calculateStartPadding(LocalLayoutDirection.current) +
        if (leading != null) style.iconSize + Grid.d3 else Grid.d0

    Row(
        modifier = Modifier
            .rowSurface(style, position, groupColors, separatorStart, onClick, swipeDelete, isLoading)
            .heightIn(min = minRowHeight(style, supporting, value))
            .padding(style.rowPadding)
            .alpha(if (isDimmed) DIMMED_ALPHA else 1f)
            .then(
                if (contentDescription != null) {
                    Modifier.semantics(mergeDescendants = true) { this.contentDescription = contentDescription }
                } else {
                    Modifier
                },
            ),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Grid.d3),
    ) {
        leading?.invoke()
        ListRowText(
            headline = headline,
            supporting = supporting,
            value = value,
            isLoading = isLoading,
            colors = colors,
            tabularSupporting = tabularSupporting,
            modifier = Modifier.weight(1f),
        )
        if (style.valueAfterLabel && value != null) {
            Text(text = value, style = style.headlineStyle, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        trailing?.invoke()
        if (chevron != null && style.showsChevrons) {
            Icon(
                painter = chevron,
                contentDescription = null,
                modifier = Modifier.size(Grid.d5).mirrorIn(LocalLayoutDirection.current),
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

// Swipe left deletes the row. The screen decides whether it goes; a row it keeps slides back.
@Composable
private fun SwipeDeleteBox(delete: IconAction, content: @Composable () -> Unit) {
    val state = rememberSwipeToDismissBoxState()
    val currentOnDelete by rememberUpdatedState(delete.onClick)
    LaunchedEffect(state.currentValue) {
        if (state.currentValue == SwipeToDismissBoxValue.EndToStart) {
            currentOnDelete()
            state.reset()
        }
    }
    SwipeToDismissBox(
        state = state,
        backgroundContent = { RevealedDelete(state, delete.icon) },
        enableDismissFromStartToEnd = false,
        content = { content() },
    )
}

@Composable
private fun RevealedDelete(state: SwipeToDismissBoxState, icon: Painter) {
    val revealed = with(LocalDensity.current) { revealedPx(state).toDp() }
    // Only the revealed part is red: a full-size layer would show through translucent rows over the map
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.CenterEnd) {
        Box(
            Modifier
                .fillMaxHeight()
                .width(revealed)
                .clipToBounds()
                .background(MaterialTheme.colorScheme.error),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                painter = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onError,
            )
        }
    }
}

// The offset isn't set until the first layout and reading it before then throws; nothing is revealed until then
private fun revealedPx(state: SwipeToDismissBoxState): Float =
    runCatching { -state.requireOffset() }.getOrDefault(0f).coerceAtLeast(0f)

/** A round icon at the start of a row, such as a place's kind. Its size follows the platform's lists. */
@Composable
fun ListRowIcon(
    icon: Painter,
    modifier: Modifier = Modifier,
    containerColor: Color = MaterialTheme.colorScheme.secondaryContainer,
    contentColor: Color = MaterialTheme.colorScheme.onSecondaryContainer,
) {
    val iconSize = AppTheme.platformStyle.list.iconSize
    Box(
        modifier = modifier.size(iconSize).clip(CircleShape).background(containerColor),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            painter = icon,
            contentDescription = null,
            modifier = Modifier.size(iconSize * ICON_SCALE),
            tint = contentColor,
        )
    }
}

/** Collects the rows of a [ListGroup]. */
interface ListGroupScope {
    /** A row; pass its [GroupPosition] on to [ListRow]. */
    fun item(content: @Composable (position: GroupPosition) -> Unit)
}

/**
 * A group of rows with an optional header and footer, as in Settings. For a long or loading list, put [ListRow]s in a
 * lazy list instead, with [GroupPosition.of]. Space between groups is up to the screen.
 */
@Composable
fun ListGroup(
    modifier: Modifier = Modifier,
    header: String? = null,
    footer: String? = null,
    content: ListGroupScope.() -> Unit,
) {
    val style = AppTheme.platformStyle.list
    val items = mutableListOf<@Composable (GroupPosition) -> Unit>()
    val scope = object : ListGroupScope {
        override fun item(content: @Composable (position: GroupPosition) -> Unit) {
            items.add(content)
        }
    }
    scope.content()

    val textPadding = Modifier.padding(start = style.groupInset + Grid.d4, end = style.groupInset + Grid.d4)
    Column(modifier.fillMaxWidth()) {
        if (header != null) {
            ListHeader(text = header)
        }
        items.forEachIndexed { index, item ->
            item(GroupPosition.of(index, items.size))
        }
        if (footer != null) {
            Text(
                text = footer,
                style = style.footerStyle,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = textPadding.padding(top = Grid.d1_5),
            )
        }
    }
}

/**
 * A group's header, also for a header item in a lazy list, such as the "Recent" above the places on the map's sheet.
 */
@Composable
fun ListHeader(text: String, modifier: Modifier = Modifier, style: ListStyle = AppTheme.platformStyle.list) {
    Text(
        text = text,
        style = style.headerStyle,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = modifier
            .padding(start = style.groupInset + Grid.d4, end = style.groupInset + Grid.d4, top = Grid.d2, bottom = Grid.d1_5)
            .semantics { heading() },
    )
}

/**
 * A row standing in for one still loading: a circle and two bars where the icon and text will be, pulsing softly, and
 * still with reduced motion. It sits in a group like a [ListRow]. Screen readers hear [announcement] on the row that has
 * it, and skip the others.
 *
 * @param groupColors The card behind it on iOS, as for [ListRow].
 * @param announcement What screen readers hear, such as "Loading"; pass it on the first row only, so a list is announced once.
 * @param style As for [ListRow].
 */
@Composable
fun PlaceholderRow(
    modifier: Modifier = Modifier,
    position: GroupPosition = GroupPosition.Alone,
    groupColors: ListGroupColors = ListGroupDefaultColors.opaque(),
    announcement: String? = null,
    style: ListStyle = AppTheme.platformStyle.list,
) {
    val shade = MaterialTheme.colorScheme.onSurface.copy(alpha = PLACEHOLDER_ALPHA)
    // Only with reduced motion off: with it on, the row is still and no animation ticks for nothing
    val pulse = if (AppTheme.reduceMotion) {
        null
    } else {
        rememberInfiniteTransition(label = "placeholderPulse").animateFloat(
            initialValue = 1f,
            targetValue = PULSE_MIN_ALPHA,
            animationSpec = infiniteRepeatable(tween(PULSE_MILLIS), RepeatMode.Reverse),
            label = "placeholderPulseAlpha",
        )
    }
    val separatorStart = style.rowPadding.calculateStartPadding(LocalLayoutDirection.current) + style.iconSize + Grid.d3
    val loadingSemantics = Modifier.clearAndSetSemantics {
        if (announcement != null) {
            contentDescription = announcement
            progressBarRangeInfo = ProgressBarRangeInfo.Indeterminate
        }
    }

    // The inset and the clip stay still, as in ListRow
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = style.groupInset)
            .padding(bottom = if (position == GroupPosition.First || position == GroupPosition.Middle) style.rowGap else Grid.d0)
            .clip(position.shape(style.outerCorner, style.innerCorner)),
    ) {
        // The card stays solid while the content pulses, so only the placeholder shapes fade
        Row(
            modifier = Modifier
                .rowSurface(
                    style = style,
                    position = position,
                    groupColors = groupColors,
                    separatorStart = separatorStart,
                    onClick = null,
                    swipeDelete = null,
                    isLoading = false,
                )
                .height(style.twoLineHeight)
                .then(loadingSemantics),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(style.rowPadding)
                    .graphicsLayer { alpha = pulse?.value ?: 1f },
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(Grid.d3),
            ) {
                Box(Modifier.size(style.iconSize).clip(CircleShape).background(shade)) {}
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(Grid.d2)) {
                    Box(Modifier.fillMaxWidth(FIRST_BAR_WIDTH).height(Grid.d3).clip(CircleShape).background(shade)) {}
                    Box(Modifier.fillMaxWidth(SECOND_BAR_WIDTH).height(Grid.d2_5).clip(CircleShape).background(shade)) {}
                }
            }
        }
    }
}

// The group's card when the rows sit on one, and on iOS the separators between rows
@Composable
private fun Modifier.rowSurface(
    style: ListStyle,
    position: GroupPosition,
    groupColors: ListGroupColors,
    separatorStart: Dp,
    onClick: (() -> Unit)?,
    swipeDelete: IconAction?,
    isLoading: Boolean,
): Modifier {
    val showsSeparator = style.showsSeparators && (position == GroupPosition.First || position == GroupPosition.Middle)
    val separatorColor = MaterialTheme.colorScheme.outlineVariant
    return this
        .fillMaxWidth()
        .then(if (style.isOnCard) Modifier.background(groupColors.containerColor) else Modifier)
        .then(
            if (showsSeparator) {
                Modifier.drawBehind {
                    val inset = separatorStart.toPx()
                    val isLtr = layoutDirection == LayoutDirection.Ltr
                    val startX = if (isLtr) inset else 0f
                    val endX = if (isLtr) size.width else size.width - inset
                    val y = size.height - HAIRLINE_HALF_PX
                    drawLine(
                        color = separatorColor,
                        start = Offset(startX, y),
                        end = Offset(endX, y),
                        strokeWidth = Stroke.HairlineWidth,
                    )
                }
            } else {
                Modifier
            },
        )
        // After the clip, so the ripple stays inside the card
        .then(
            if (onClick != null) {
                Modifier.clickable(enabled = !isLoading, role = Role.Button, onClick = onClick)
            } else {
                // A read-only row is one item for screen readers, as a clickable one already is
                Modifier.semantics(mergeDescendants = true) {}
            },
        )
        .then(
            if (swipeDelete != null) {
                Modifier.semantics {
                    customActions = listOf(
                        CustomAccessibilityAction(swipeDelete.label) {
                            swipeDelete.onClick()
                            true
                        },
                    )
                }
            } else {
                Modifier
            },
        )
}

private fun minRowHeight(style: ListStyle, supporting: String?, value: String?): Dp =
    if (supporting != null || (value != null && !style.valueAfterLabel)) style.twoLineHeight else style.oneLineHeight

@Composable
private fun ListRowText(
    headline: AnnotatedString,
    supporting: String?,
    value: String?,
    isLoading: Boolean,
    colors: ListRowColors,
    tabularSupporting: Boolean,
    modifier: Modifier = Modifier,
) {
    val style = AppTheme.platformStyle.list
    Column(modifier) {
        Box(contentAlignment = Alignment.CenterStart) {
            // The headline keeps its size while loading, so the spinner sits over it without moving the row
            Text(
                text = headline,
                style = style.headlineStyle,
                color = colors.headlineColor,
                modifier = Modifier.alpha(if (isLoading) 0f else 1f),
            )
            if (isLoading) {
                CarSpinner(Modifier.size(Grid.d5), color = colors.headlineColor)
            }
        }
        if (supporting != null) {
            Text(
                text = supporting,
                style = if (tabularSupporting) style.supportingStyle.copy(fontFeatureSettings = TABULAR_DIGITS) else style.supportingStyle,
                color = colors.supportingColor,
            )
        }
        if (!style.valueAfterLabel && value != null) {
            Text(text = value, style = style.supportingStyle, color = colors.supportingColor)
        }
    }
}

private fun GroupPosition.shape(outer: Dp, inner: Dp): Shape = when (this) {
    GroupPosition.Alone -> RoundedCornerShape(outer)
    GroupPosition.First -> RoundedCornerShape(topStart = outer, topEnd = outer, bottomEnd = inner, bottomStart = inner)
    GroupPosition.Middle -> RoundedCornerShape(inner)
    GroupPosition.Last -> RoundedCornerShape(topStart = inner, topEnd = inner, bottomEnd = outer, bottomStart = outer)
}

// A row faded out keeps its place and taps, so it reads as unavailable rather than missing
private const val DIMMED_ALPHA = 0.5f

// The glass shows through the card, as cards in Apple Maps' sheets do
private const val TRANSLUCENT_GROUP_ALPHA = 0.6f

// Equal-width digits, so numbers such as distances line up down a list
private const val TABULAR_DIGITS = "tnum"

// The icon fills about half of its circle, so the circle reads as a container
private const val ICON_SCALE = 0.55f

// A hairline is one pixel; on the bottom edge the card's clip would cut half of it, so it's drawn half a pixel up
private const val HAIRLINE_HALF_PX = 0.5f

// A faint grey, so the placeholder reads as not yet there without looking like a disabled control
private const val PLACEHOLDER_ALPHA = 0.08f

// The pulse dips to half strength and back once a second, slow enough not to draw the eye from the rest of the screen
private const val PULSE_MIN_ALPHA = 0.5f
private const val PULSE_MILLIS = 1000

// Like a name and a shorter line below it
private const val FIRST_BAR_WIDTH = 0.6f
private const val SECOND_BAR_WIDTH = 0.4f

@ComponentPreviews
@Composable
private fun ListsPreview() = Showcase {
    ListExamples()
}

@ComponentPreviews
@Composable
private fun ListsIosPreview() = Showcase(platform = UiPlatform.Ios) {
    ListExamples()
}

@Composable
private fun ListExamples() {
    Column(
        Modifier.background(MaterialTheme.colorScheme.surfaceContainerLow).padding(vertical = Grid.d4),
        verticalArrangement = Arrangement.spacedBy(Grid.d6),
    ) {
        ListGroup(header = "Units") {
            item { position ->
                ListRow("Distance", value = "km", onClick = {}, position = position)
            }
        }
        ListGroup(
            header = "Motorway vignettes",
            footer = "Without a vignette, routes avoid motorways that need one.",
        ) {
            item { position ->
                ListRow(
                    "Czechia",
                    trailing = { ToggleSwitch(checked = true, onCheckedChange = null) },
                    onClick = {},
                    position = position,
                )
            }
        }
        ListGroup {
            item { position ->
                ListRow(
                    "Trip history",
                    supporting = "24 drives",
                    leading = { ListRowIcon(previewIcon()) },
                    onClick = {},
                    chevron = previewIcon(),
                    position = position,
                )
            }
            item { position ->
                ListRow(
                    "Settings",
                    supporting = "Units, vignettes, about the app",
                    leading = { ListRowIcon(previewIcon()) },
                    onClick = {},
                    chevron = previewIcon(),
                    position = position,
                )
            }
            item { position ->
                ListRow(
                    "Sign out",
                    colors = ListRowDefaultColors.action(),
                    onClick = {},
                    position = position,
                )
            }
        }
        ListGroup {
            item { position ->
                ListRow(
                    "Karlštejn Castle",
                    supporting = "28 km · Karlštejn, Central Bohemia",
                    leading = { ListRowIcon(previewIcon()) },
                    onClick = {},
                    swipeDelete = IconAction(icon = previewIcon(), label = "Delete", onClick = {}),
                    position = position,
                    groupColors = ListGroupDefaultColors.translucent(),
                )
            }
            item { position ->
                ListRow(
                    "Dresden",
                    supporting = "Not covered yet · Dresden, Saxony",
                    leading = { ListRowIcon(previewIcon()) },
                    onClick = {},
                    isDimmed = true,
                    position = position,
                    groupColors = ListGroupDefaultColors.translucent(),
                )
            }
        }
        ListGroup {
            item { position ->
                ListRow(
                    "Delete account",
                    colors = ListRowDefaultColors.error(),
                    onClick = {},
                    isLoading = true,
                    position = position,
                )
            }
        }
        ListGroup {
            item { position -> PlaceholderRow(position = position, announcement = "Loading") }
            item { position -> PlaceholderRow(position = position) }
            item { position -> PlaceholderRow(position = position) }
        }
    }
}
