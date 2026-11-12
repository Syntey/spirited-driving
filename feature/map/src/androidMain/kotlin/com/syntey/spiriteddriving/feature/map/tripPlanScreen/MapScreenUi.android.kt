package com.syntey.spiriteddriving.feature.map.tripPlanScreen

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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.layout
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.unit.Dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.maps.android.compose.CameraMoveStartedReason
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
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
import com.syntey.spiriteddriving.core.ui.components.Showcase
import com.syntey.spiriteddriving.core.ui.preview.ScreenPreviews
import com.syntey.spiriteddriving.core.ui.theme.Grid
import com.syntey.spiriteddriving.feature.map.tripPlanScreen.TripPlanScreen.Actions.Companion.noOpActions
import dev.icerock.moko.resources.compose.painterResource
import dev.icerock.moko.resources.compose.stringResource
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

private const val FOLLOW_ZOOM = 15f
private const val FOLLOW_MIN_ZOOM = 12f
private const val FOLLOW_ANIMATION_MS = 500
private const val DIM_ALPHA_LIGHT = 0.2f
private const val DIM_ALPHA_DARK = 0.5f

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
        // The collapsed height, at every height of the sheet, so the camera doesn't move as the sheet is dragged
        Map(
            map = viewState.map,
            onPanned = actions::onMapPanned,
            contentPadding = PaddingValues(top = statusBarHeight, bottom = collapsedHeight),
            modifier = Modifier.fillMaxSize(),
        )
        ProfileButton(
            onClick = actions::onProfileClick,
            modifier = Modifier.align(Alignment.TopEnd).windowInsetsPadding(WindowInsets.statusBars).padding(top = Grid.d2, end = Grid.d4),
        )
        AnimatedVisibility(
            visible = viewState.sheetHeight == SheetHeight.Collapsed,
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
private fun ProfileButton(onClick: () -> Unit, modifier: Modifier = Modifier) {
    Surface(
        onClick = onClick,
        shape = CircleShape,
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
        contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
        shadowElevation = Grid.d1,
        modifier = modifier.size(Grid.d12),
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
 * The Google map. It shows the covered area until it follows the user, and stops following when the user pans it.
 */
@Composable
private fun Map(map: RouteMapState, onPanned: () -> Unit, contentPadding: PaddingValues, modifier: Modifier = Modifier) {
    // The map SDK doesn't render in previews
    if (LocalInspectionMode.current) {
        Box(modifier.background(MaterialTheme.colorScheme.surfaceContainerHighest))
        return
    }
    val isDark = isSystemInDarkTheme()
    val cameraPositionState = rememberCameraPositionState()
    val currentMap by rememberUpdatedState(map)
    val currentOnPanned by rememberUpdatedState(onPanned)
    val scope = rememberCoroutineScope()
    val overviewPaddingPx = with(LocalDensity.current) { Grid.d6.roundToPx() }
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
            map.destination?.let { Destination(point = it) }
        },
        onMapLoaded = {
            isLoaded = true
            val loaded = currentMap
            if (!loaded.followsUser || loaded.userLocation == null) {
                scope.launch {
                    cameraPositionState.move(CameraUpdateFactory.newLatLngBounds(loaded.overview.toLatLngBounds(), overviewPaddingPx))
                }
            }
        },
    )

    // A request moves the camera to the overview; before the map loads, onMapLoaded shows it
    LaunchedEffect(map.overviewRequest) {
        if (isLoaded && !currentMap.followsUser) {
            val update = CameraUpdateFactory.newLatLngBounds(currentMap.overview.toLatLngBounds(), overviewPaddingPx)
            cameraPositionState.animate(update, FOLLOW_ANIMATION_MS)
        }
    }

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
    val state = TripPlanScreenPreviews.viewState()
    Showcase {
        Content(
            viewState = state,
            actions = noOpActions(),
            sheet = { onCollapsedHeightChange ->
                TripPlanSheetUi(screen = TripPlanScreenPreviews.screen(state), onCollapsedHeightChange = onCollapsedHeightChange)
            },
        )
    }
}

@ScreenPreviews
@Composable
private fun MapScreenNotFollowingPreview() {
    val state = TripPlanScreenPreviews.viewState(followsUser = false)
    Showcase {
        Content(
            viewState = state,
            actions = noOpActions(),
            sheet = { onCollapsedHeightChange ->
                TripPlanSheetUi(screen = TripPlanScreenPreviews.screen(state), onCollapsedHeightChange = onCollapsedHeightChange)
            },
        )
    }
}

@ScreenPreviews
@Composable
private fun MapScreenOutsidePreview() {
    val state = TripPlanScreenPreviews.outsideViewState()
    Showcase {
        Content(
            viewState = state,
            actions = noOpActions(),
            sheet = { onCollapsedHeightChange ->
                TripPlanSheetUi(screen = TripPlanScreenPreviews.screen(state), onCollapsedHeightChange = onCollapsedHeightChange)
            },
        )
    }
}

@ScreenPreviews
@Composable
private fun MapScreenNoAccessPreview() {
    val state = TripPlanScreenPreviews.viewState(locationAccess = LocationAccess.Denied)
    Showcase {
        Content(
            viewState = state,
            actions = noOpActions(),
            sheet = { onCollapsedHeightChange ->
                TripPlanSheetUi(screen = TripPlanScreenPreviews.screen(state), onCollapsedHeightChange = onCollapsedHeightChange)
            },
        )
    }
}
