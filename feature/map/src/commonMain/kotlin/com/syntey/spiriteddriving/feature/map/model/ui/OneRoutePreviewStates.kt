package com.syntey.spiriteddriving.feature.map.model.ui

import com.syntey.spiriteddriving.core.domain.drive.PastDrive
import com.syntey.spiriteddriving.core.domain.geo.GeoPoint
import com.syntey.spiriteddriving.core.domain.place.Place
import com.syntey.spiriteddriving.core.domain.route.FakeRoutePlanner
import com.syntey.spiriteddriving.core.domain.route.RouteKind
import com.syntey.spiriteddriving.core.domain.savedroute.FakeSavedRoutesSource
import com.syntey.spiriteddriving.core.domain.savedroute.SavedRoute
import com.syntey.spiriteddriving.feature.map.ui.mapSheetScreen.MapSheetViewState
import com.syntey.spiriteddriving.feature.map.ui.mapSheetScreen.oneRoute.OneRouteScreen
import com.syntey.spiriteddriving.feature.map.ui.mapSheetScreen.oneRoute.OneRouteViewState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.collections.immutable.toImmutableList
import kotlin.time.Instant

/** Sample view states of the one-route step, for its previews: a saved route's and a past drive's. */
internal object OneRoutePreviewStates {
    /** A saved route's step: loaded from the Recorded saved route, which is saved, so Saved shows. */
    fun savedStep(status: OneRouteStatus = OneRouteStatus.Ready): OneRouteStep = OneRouteStep(
        origin = OneRouteOrigin.Saved(FakeSavedRoutesSource.Recorded),
        route = FakeRoutePlanner.Recorded.funRoutes.first().takeIf { status == OneRouteStatus.Ready },
        status = status,
    )

    /** A past drive's step: the drive's route, loaded unless [status] says otherwise. */
    fun pastDriveStep(status: OneRouteStatus = OneRouteStatus.Ready): OneRouteStep = OneRouteStep(
        origin = OneRouteOrigin.Past(pastDrive()),
        route = FakeRoutePlanner.Recorded.funRoutes.first().takeIf { status == OneRouteStatus.Ready },
        status = status,
    )

    /** A screen of the one-route step [step] that shows its state and takes no taps. */
    fun screen(step: OneRouteStep): OneRouteScreen = object : OneRouteScreen {
        override val viewState: MutableStateFlow<OneRouteViewState> = MutableStateFlow(OneRouteViewState(step = step))
        override val actions: OneRouteScreen.Actions = OneRouteScreen.Actions.noOpActions()
    }

    /** A saved route's step over the sheet: loaded from the Recorded saved route, which is saved, so Saved shows. */
    fun oneRouteViewState(
        status: OneRouteStatus = OneRouteStatus.Ready,
        sheetHeight: SheetHeight = SheetHeight.Half,
    ): MapSheetViewState {
        val saved = FakeSavedRoutesSource.Recorded
        return MapSheetPreviewStates.viewState().copy(
            sheetStep = savedStep(status),
            sheetHeight = sheetHeight,
            savedRoutes = listOf(saved).toImmutableList(),
        )
    }

    /**
     * A past drive's step: the drive's route, loaded, and not saved unless [isSaved], when the route is saved under the drive's
     * route ID. Its start is Smíchov, and it started on 5 November.
     */
    fun pastDriveViewState(
        isSaved: Boolean = false,
        status: OneRouteStatus = OneRouteStatus.Ready,
        sheetHeight: SheetHeight = SheetHeight.Half,
    ): MapSheetViewState {
        val route = FakeRoutePlanner.Recorded.funRoutes.first()
        val saved: List<SavedRoute> = if (isSaved) {
            listOf(FakeSavedRoutesSource.Recorded.copy(route = FakeSavedRoutesSource.Recorded.route.copy(id = route.id)))
        } else {
            emptyList()
        }
        return MapSheetPreviewStates.viewState().copy(
            sheetStep = pastDriveStep(status),
            sheetHeight = sheetHeight,
            savedRoutes = saved.toImmutableList(),
        )
    }

    // The drive the past drive's step shows: started on 5 November from Smíchov, along the first fun route
    private fun pastDrive(): PastDrive {
        val route = FakeRoutePlanner.Recorded.funRoutes.first()
        return PastDrive(
            id = "preview-drive",
            startedAt = Instant.parse("2026-11-05T13:20:00Z"),
            routeId = route.id,
            kind = RouteKind.Fun,
            start = Place(name = null, area = "Smíchov, Prague", location = PRAGUE_START),
            destination = route.destination,
            duration = route.duration,
            distance = route.distance,
            funLevel = route.funLevel,
        )
    }

    private val PRAGUE_START = GeoPoint(latitude = 50.0755, longitude = 14.4378)
}
