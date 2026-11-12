package com.syntey.spiriteddriving.feature.map.ui.tripPlanScreen

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.AnchoredDraggableDefaults
import androidx.compose.foundation.gestures.AnchoredDraggableState
import androidx.compose.foundation.gestures.DraggableAnchors
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.anchoredDraggable
import androidx.compose.foundation.gestures.animateTo
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.layout
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.unit.Dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.JointType
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.android.gms.maps.model.RoundCap
import com.google.maps.android.compose.CameraMoveStartedReason
import com.google.maps.android.compose.CameraPositionState
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerComposable
import com.google.maps.android.compose.Polygon
import com.google.maps.android.compose.Polyline
import com.google.maps.android.compose.rememberCameraPositionState
import com.google.maps.android.compose.rememberUpdatedMarkerState
import com.syntey.spiriteddriving.core.domain.geo.GeoBounds
import com.syntey.spiriteddriving.core.domain.geo.GeoPoint
import com.syntey.spiriteddriving.core.domain.geo.GeoPolygon
import com.syntey.spiriteddriving.core.platform.binding.LocationAccess
import com.syntey.spiriteddriving.core.resources.*
import com.syntey.spiriteddriving.core.ui.components.DragHandle
import com.syntey.spiriteddriving.core.ui.components.RouteLabel
import com.syntey.spiriteddriving.core.ui.components.Showcase
import com.syntey.spiriteddriving.core.ui.preview.ScreenPreviews
import com.syntey.spiriteddriving.core.ui.theme.AppTheme
import com.syntey.spiriteddriving.core.ui.theme.Grid
import com.syntey.spiriteddriving.feature.map.model.ui.LocateButton
import com.syntey.spiriteddriving.feature.map.model.ui.RouteLine
import com.syntey.spiriteddriving.feature.map.model.ui.RouteMapState
import com.syntey.spiriteddriving.feature.map.model.ui.SheetHeight
import com.syntey.spiriteddriving.feature.map.model.ui.TripPlanPreviewStates
import com.syntey.spiriteddriving.feature.map.ui.components.MapStyles
import com.syntey.spiriteddriving.feature.map.ui.components.routeColor
import com.syntey.spiriteddriving.feature.map.ui.tripPlanScreen.TripPlanScreen.Actions.Companion.noOpActions
import dev.icerock.moko.resources.compose.localized
import dev.icerock.moko.resources.compose.painterResource
import dev.icerock.moko.resources.compose.stringResource
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

private const val FOLLOW_ZOOM = 15f
private const val FOLLOW_MIN_ZOOM = 12f
private const val FOLLOW_ANIMATION_MS = 500
private const val DIM_ALPHA_LIGHT = 0.2f
private const val DIM_ALPHA_DARK = 0.5f
private const val UNSELECTED_ALPHA = 0.5f

// The selected line's casing is its line with a border on each side, which has no Grid token
private val SELECTED_CASING_WIDTH = Grid.d1_5 + Grid.d0_375 * 2

// A route label is centred on its point, so the pill sits on the line
private val LABEL_ANCHOR = Offset(0.5f, 0.5f)

// How far either side of a route line a tap still picks it, as on iOS
private val ROUTE_TAP_WIDTH = Grid.d11

// The whole map, as a ring the covered area is cut out of. Google Maps joins points the short way round, so the ring
// stops just short of the antimeridian instead of meeting itself there.
private val WORLD = listOf(
    LatLng(85.0, -179.99),
    LatLng(85.0, 0.0),
    LatLng(85.0, 179.99),
    LatLng(-85.0, 179.99),
    LatLng(-85.0, 0.0),
    LatLng(-85.0, -179.99),
)

/**
 * The map screen on Android: the map, its buttons and the sheet, whose content is [TripPlanSheetUi], shared with iOS.
 */
@Composable
fun MapScreenUi(screen: TripPlanScreen, modifier: Modifier = Modifier) {
    val viewState by screen.viewState.collectAsStateWithLifecycle()
    Content(viewState = viewState, actions = screen.actions, sheet = { onCollapsedHeightChange ->
        TripPlanSheetUi(screen = screen, onCollapsedHeightChange = onCollapsedHeightChange)
    }, modifier = modifier)
}

@Composable
private fun Content(
    viewState: TripPlanViewState,
    actions: TripPlanScreen.Actions,
    sheet: @Composable (onCollapsedHeightChange: (Dp) -> Unit) -> Unit,
    modifier: Modifier = Modifier,
) {
    // The notes and search field, as the sheet's content measures them; until then about the field's height
    var collapsedContentHeight by remember { mutableStateOf(Grid.d20) }
    var handleHeight by remember { mutableStateOf(Grid.d0) }
    val density = LocalDensity.current
    val statusBarHeight = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
    val navigationBarHeight = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
    val collapsedHeight = handleHeight + collapsedContentHeight + navigationBarHeight
    BoxWithConstraints(modifier.fillMaxSize()) {
        // The part of the map the sheet covers at its settled height; null when it covers the map
        val sheetCover: Dp? = when (viewState.sheetHeight) {
            SheetHeight.Collapsed -> collapsedHeight
            SheetHeight.Half -> maxHeight / 2
            SheetHeight.Full -> null
        }
        // While planning, the routes are framed above the sheet; the collapsed height otherwise, so the camera doesn't move as the
        // sheet is dragged
        val fitBottomPadding = sheetCover.takeIf { viewState.planning != null }
        Map(
            map = viewState.map,
            routeLines = viewState.routeLines,
            routeBounds = viewState.routeBounds,
            fitBottomPadding = fitBottomPadding,
            onPanned = actions::onMapPanned,
            onLongPress = actions::onMapLongPress,
            onRouteClick = actions::onRouteClick,
            contentPadding = PaddingValues(top = statusBarHeight, bottom = fitBottomPadding ?: collapsedHeight),
            modifier = Modifier.fillMaxSize(),
        )
        AnimatedVisibility(
            visible = viewState.showsMapButtons,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier.align(Alignment.TopEnd).windowInsetsPadding(WindowInsets.statusBars).padding(top = Grid.d2, end = Grid.d4),
        ) {
            ProfileButton(onClick = actions::onProfileClick)
        }
        AnimatedVisibility(
            visible = viewState.sheetHeight == SheetHeight.Collapsed && viewState.showsMapButtons,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier.align(Alignment.BottomEnd).padding(end = Grid.d4, bottom = collapsedHeight + Grid.d4),
        ) {
            LocateControl(locateButton = viewState.locateButton, onClick = actions::onLocateClick)
        }
        MapSheet(
            height = viewState.sheetHeight,
            onHeightChange = actions::onSheetHeightChange,
            collapsedHeight = collapsedHeight,
            fullHeight = maxHeight - statusBarHeight,
            halfHeight = maxHeight / 2,
            modifier = Modifier.align(Alignment.BottomCenter),
        ) {
            Column(
                Modifier.fillMaxSize().windowInsetsPadding(WindowInsets.navigationBars),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                DragHandle(Modifier.onSizeChanged { handleHeight = with(density) { it.height.toDp() } })
                sheet { collapsedContentHeight = it }
            }
        }
    }
}

/**
 * The sheet over the map, at one of three heights. It's only as tall as the part that shows, so its list ends at the bottom
 * of the screen at every height. A drag settles at the nearest height and reports it; a new [height] moves the sheet there.
 */
@Composable
private fun MapSheet(
    height: SheetHeight,
    onHeightChange: (SheetHeight) -> Unit,
    collapsedHeight: Dp,
    halfHeight: Dp,
    fullHeight: Dp,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    val density = LocalDensity.current
    // The offset is the visible height, so it grows as the sheet is dragged up: hence reverseDirection
    val state = remember { AnchoredDraggableState(initialValue = height) }
    val collapsedPx = with(density) { collapsedHeight.toPx() }
    val halfPx = with(density) { halfHeight.toPx() }
    val fullPx = with(density) { fullHeight.toPx() }
    LaunchedEffect(collapsedPx, halfPx, fullPx) {
        state.updateAnchors(
            DraggableAnchors {
                SheetHeight.Collapsed at collapsedPx
                SheetHeight.Half at halfPx
                SheetHeight.Full at fullPx
            },
            newTarget = state.targetValue,
        )
    }
    // Focusing the field and closing the search move the sheet
    LaunchedEffect(height) {
        if (state.settledValue != height) state.animateTo(height)
    }
    val currentHeight by rememberUpdatedState(height)
    val currentOnHeightChange by rememberUpdatedState(onHeightChange)
    LaunchedEffect(state) {
        snapshotFlow { state.settledValue }.collect { settled -> if (settled != currentHeight) currentOnHeightChange(settled) }
    }
    Surface(
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        shape = RoundedCornerShape(topStart = Grid.d8, topEnd = Grid.d8),
        shadowElevation = Grid.d2,
        modifier = modifier
            .fillMaxWidth()
            .layout { measurable, constraints ->
                val visible = state.offset.takeUnless { it.isNaN() } ?: collapsedPx
                val sheetHeight = visible.roundToInt().coerceIn(0, constraints.maxHeight)
                val placeable = measurable.measure(constraints.copy(minHeight = sheetHeight, maxHeight = sheetHeight))
                layout(placeable.width, sheetHeight) { placeable.place(0, 0) }
            }
            .anchoredDraggable(
                state = state,
                reverseDirection = true,
                orientation = Orientation.Vertical,
                flingBehavior = AnchoredDraggableDefaults.flingBehavior(state),
            ),
    ) {
        content()
    }
}

@Composable
private fun ProfileButton(onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        shape = CircleShape,
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
        contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
        shadowElevation = Grid.d1,
        modifier = Modifier.size(Grid.d12),
    ) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Icon(
                painter = painterResource(MR.images.profile),
                contentDescription = stringResource(MR.strings.map_profile),
                modifier = Modifier.size(Grid.d6),
            )
        }
    }
}

@Composable
private fun LocateControl(locateButton: LocateButton, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val (image, tint, description) = when (locateButton) {
        LocateButton.Following -> Triple(MR.images.locate_filled, MaterialTheme.colorScheme.primary, MR.strings.map_locate_following)
        LocateButton.NotFollowing -> Triple(MR.images.locate, MaterialTheme.colorScheme.onSurfaceVariant, MR.strings.map_locate_show)
        LocateButton.NoAccess -> Triple(MR.images.locate_off, MaterialTheme.colorScheme.onSurfaceVariant, MR.strings.map_locate_no_access)
    }
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(Grid.d4),
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
        shadowElevation = Grid.d1,
        modifier = modifier.size(Grid.d14),
    ) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Icon(
                painter = painterResource(image),
                contentDescription = stringResource(description),
                modifier = Modifier.size(Grid.d6),
                tint = tint,
            )
        }
    }
}

/**
 * The Google map. It shows the covered area until it follows the user, and stops following when the user pans it. Touching and
 * holding it picks a point, with a haptic tap. While planning, it frames the routes above the sheet, again as the sheet settles.
 */
// The camera's requests, gestures and load each branch on the map's state, and splitting them would scatter one camera
@Suppress("CyclomaticComplexMethod")
@Composable
private fun Map(
    map: RouteMapState,
    routeLines: ImmutableList<RouteLine>,
    routeBounds: GeoBounds?,
    fitBottomPadding: Dp?,
    onPanned: () -> Unit,
    onLongPress: (GeoPoint) -> Unit,
    onRouteClick: (String) -> Unit,
    contentPadding: PaddingValues,
    modifier: Modifier = Modifier,
) {
    // The map SDK doesn't render in previews
    if (LocalInspectionMode.current) {
        Box(modifier.background(MaterialTheme.colorScheme.surfaceContainerHighest))
        return
    }
    val isDark = isSystemInDarkTheme()
    val cameraPositionState = rememberCameraPositionState()
    val currentMap by rememberUpdatedState(map)
    val currentRouteBounds by rememberUpdatedState(routeBounds)
    val currentOnPanned by rememberUpdatedState(onPanned)
    val currentOnLongPress by rememberUpdatedState(onLongPress)
    val haptic = LocalHapticFeedback.current
    val scope = rememberCoroutineScope()
    val overviewPaddingPx = with(LocalDensity.current) { Grid.d6.roundToPx() }
    // Wide enough that the start and destination pins at the routes' ends stay on screen
    val routesPaddingPx = with(LocalDensity.current) { Grid.d12.roundToPx() }
    var isLoaded by remember { mutableStateOf(false) }

    GoogleMap(
        modifier = modifier,
        cameraPositionState = cameraPositionState,
        contentPadding = contentPadding,
        properties = MapProperties(
            isMyLocationEnabled = map.showsUserLocation,
            mapStyleOptions = remember(isDark) { MapStyleOptions(if (isDark) MapStyles.DARK else MapStyles.LIGHT) },
            isIndoorEnabled = false,
            isBuildingEnabled = false,
        ),
        uiSettings = MapUiSettings(
            myLocationButtonEnabled = false,
            zoomControlsEnabled = false,
            mapToolbarEnabled = false,
            indoorLevelPickerEnabled = false,
        ),
        content = {
            CoveredArea(area = map.coveredArea)
            RouteLines(lines = routeLines, onClick = onRouteClick)
            map.destination?.let { Destination(point = it) }
        },
        onMapLongClick = { latLng ->
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            currentOnLongPress(GeoPoint(latitude = latLng.latitude, longitude = latLng.longitude))
        },
        onMapLoaded = {
            isLoaded = true
            val loaded = currentMap
            // While planning, the routes' fit takes over from the overview
            if (currentRouteBounds == null && (!loaded.followsUser || loaded.userLocation == null)) {
                scope.launch {
                    cameraPositionState.move(CameraUpdateFactory.newLatLngBounds(loaded.overview.toLatLngBounds(), overviewPaddingPx))
                }
            }
        },
    )

    // A request moves the camera to the overview; before the map loads, onMapLoaded shows it
    LaunchedEffect(map.overviewRequest) {
        if (isLoaded && !currentMap.followsUser && currentRouteBounds == null) {
            val update = CameraUpdateFactory.newLatLngBounds(currentMap.overview.toLatLngBounds(), overviewPaddingPx)
            cameraPositionState.animate(update, FOLLOW_ANIMATION_MS)
        }
    }

    FitRoutes(
        cameraPositionState = cameraPositionState,
        routeBounds = routeBounds,
        fitBottomPadding = fitBottomPadding,
        isLoaded = isLoaded,
        paddingPx = routesPaddingPx,
    )

    LaunchedEffect(map.followsUser, map.userLocation) {
        val location = map.userLocation
        if (map.followsUser && location != null) {
            val target = location.toLatLng()
            // Recentering from far out zooms in to the streets; while following, the zoom stays as the user set it
            val update = if (cameraPositionState.position.zoom < FOLLOW_MIN_ZOOM) {
                CameraUpdateFactory.newLatLngZoom(target, FOLLOW_ZOOM)
            } else {
                CameraUpdateFactory.newLatLng(target)
            }
            cameraPositionState.animate(update, FOLLOW_ANIMATION_MS)
        }
    }

    LaunchedEffect(cameraPositionState) {
        snapshotFlow { cameraPositionState.isMoving && cameraPositionState.cameraMoveStartedReason == CameraMoveStartedReason.GESTURE }
            .distinctUntilChanged()
            .filter { it }
            .collect { currentOnPanned() }
    }
}

/**
 * Frames the planned routes above the sheet, again when the sheet settles at another height. Null [fitBottomPadding] means the
 * sheet covers the map, so there's nothing to frame above it.
 */
@Composable
private fun FitRoutes(
    cameraPositionState: CameraPositionState,
    routeBounds: GeoBounds?,
    fitBottomPadding: Dp?,
    isLoaded: Boolean,
    paddingPx: Int,
) {
    LaunchedEffect(routeBounds, fitBottomPadding, isLoaded) {
        if (isLoaded && routeBounds != null && fitBottomPadding != null) {
            cameraPositionState.animate(CameraUpdateFactory.newLatLngBounds(routeBounds.toLatLngBounds(), paddingPx), FOLLOW_ANIMATION_MS)
        }
    }
}

/**
 * Dims the world outside the covered area and draws its border. Holes in the covered area aren't covered, so they're
 * dimmed too.
 */
@Composable
private fun CoveredArea(area: List<GeoPolygon>) {
    if (area.isEmpty()) return
    val dim = MaterialTheme.colorScheme.scrim.copy(alpha = if (isSystemInDarkTheme()) DIM_ALPHA_DARK else DIM_ALPHA_LIGHT)
    val border = MaterialTheme.colorScheme.secondary
    val borderWidth = with(LocalDensity.current) { Grid.d0_375.toPx() }
    val outlines = remember(area) { area.map { polygon -> polygon.outline.map { it.toLatLng() } } }
    val holes = remember(area) { area.flatMap { it.holes }.map { hole -> hole.map { it.toLatLng() } } }
    Polygon(points = WORLD, holes = outlines, fillColor = dim, strokeWidth = 0f)
    holes.forEach { Polygon(points = it, fillColor = dim, strokeWidth = 0f) }
    (outlines + holes).forEach { Polyline(points = it, color = border, width = borderWidth) }
}

/**
 * The routes' lines, each a casing under its line. Unselected routes are dimmed; the selected one is drawn on top of them.
 */
@Composable
private fun RouteLines(lines: ImmutableList<RouteLine>, onClick: (String) -> Unit) {
    val casing = AppTheme.customColors.routeCasing
    val density = LocalDensity.current
    lines.forEach { line ->
        key(line.id) {
            val color = routeColor(line.number)
            val lineColor = if (line.isSelected) color else color.copy(alpha = UNSELECTED_ALPHA)
            val casingColor = if (line.isSelected) casing else casing.copy(alpha = UNSELECTED_ALPHA)
            val points = remember(line.points) { line.points.map { it.toLatLng() }.toImmutableList() }
            val lineWidth = if (line.isSelected) Grid.d1_5 else Grid.d1
            val casingWidth = if (line.isSelected) SELECTED_CASING_WIDTH else Grid.d1_5
            val zIndices = if (line.isSelected) Triple(3f, 4f, 6f) else Triple(1f, 2f, 5f)
            RoutePolyline(points, casingColor, with(density) { casingWidth.toPx() }, zIndices.first)
            RoutePolyline(points, lineColor, with(density) { lineWidth.toPx() }, zIndices.second)
            // Google Maps only takes taps on a line's stroke, too thin for a finger, so a clear wider line above takes them
            Polyline(
                points = points,
                color = Color.Transparent,
                width = with(density) { ROUTE_TAP_WIDTH.toPx() },
                zIndex = zIndices.third,
                clickable = true,
                onClick = { onClick(line.id) },
            )
            RouteLabelMarker(line = line, outline = casing, onClick = onClick)
        }
    }
}

// The label sits above every line, the selected one's above the others'
@Composable
private fun RouteLabelMarker(line: RouteLine, outline: Color, onClick: (String) -> Unit) {
    val text = line.label.localized()
    val color = routeColor(line.number)
    MarkerComposable(
        text,
        color,
        outline,
        line.isSelected,
        state = rememberUpdatedMarkerState(position = line.labelPoint.toLatLng()),
        contentDescription = text,
        anchor = LABEL_ANCHOR,
        zIndex = if (line.isSelected) 8f else 7f,
        onClick = {
            onClick(line.id)
            true
        },
    ) {
        RouteLabel(text = text, color = color, outline = outline)
    }
}

@Composable
private fun RoutePolyline(points: List<LatLng>, color: Color, width: Float, zIndex: Float) {
    Polyline(
        points = points,
        color = color,
        width = width,
        zIndex = zIndex,
        jointType = JointType.ROUND,
        startCap = RoundCap(),
        endCap = RoundCap(),
    )
}

/** The chosen destination's pin. */
@Composable
private fun Destination(point: GeoPoint) {
    Marker(state = rememberUpdatedMarkerState(position = point.toLatLng()))
}

private fun GeoBounds.toLatLngBounds(): LatLngBounds = LatLngBounds(southWest.toLatLng(), northEast.toLatLng())

private fun GeoPoint.toLatLng(): LatLng = LatLng(latitude, longitude)

@ScreenPreviews
@Composable
private fun MapScreenFollowingPreview() {
    val state = TripPlanPreviewStates.viewState()
    Showcase {
        Content(
            viewState = state,
            actions = noOpActions(),
            sheet = { onCollapsedHeightChange ->
                TripPlanSheetUi(screen = TripPlanPreviewStates.screen(state), onCollapsedHeightChange = onCollapsedHeightChange)
            },
        )
    }
}

@ScreenPreviews
@Composable
private fun MapScreenNotFollowingPreview() {
    val state = TripPlanPreviewStates.viewState(followsUser = false)
    Showcase {
        Content(
            viewState = state,
            actions = noOpActions(),
            sheet = { onCollapsedHeightChange ->
                TripPlanSheetUi(screen = TripPlanPreviewStates.screen(state), onCollapsedHeightChange = onCollapsedHeightChange)
            },
        )
    }
}

@ScreenPreviews
@Composable
private fun MapScreenOutsidePreview() {
    val state = TripPlanPreviewStates.outsideViewState()
    Showcase {
        Content(
            viewState = state,
            actions = noOpActions(),
            sheet = { onCollapsedHeightChange ->
                TripPlanSheetUi(screen = TripPlanPreviewStates.screen(state), onCollapsedHeightChange = onCollapsedHeightChange)
            },
        )
    }
}

@ScreenPreviews
@Composable
private fun MapScreenPlannedPreview() {
    val state = TripPlanPreviewStates.plannedViewState()
    Showcase {
        Content(
            viewState = state,
            actions = noOpActions(),
            sheet = { onCollapsedHeightChange ->
                TripPlanSheetUi(screen = TripPlanPreviewStates.screen(state), onCollapsedHeightChange = onCollapsedHeightChange)
            },
        )
    }
}

@ScreenPreviews
@Composable
private fun MapScreenNoAccessPreview() {
    val state = TripPlanPreviewStates.viewState(locationAccess = LocationAccess.Denied)
    Showcase {
        Content(
            viewState = state,
            actions = noOpActions(),
            sheet = { onCollapsedHeightChange ->
                TripPlanSheetUi(screen = TripPlanPreviewStates.screen(state), onCollapsedHeightChange = onCollapsedHeightChange)
            },
        )
    }
}
