package com.syntey.spiriteddriving.feature.map.model.ui

import com.syntey.spiriteddriving.core.domain.drive.PastDrive
import com.syntey.spiriteddriving.core.domain.geo.GeoPoint
import com.syntey.spiriteddriving.core.domain.place.Place
import com.syntey.spiriteddriving.core.domain.route.FakeRoutePlanner
import com.syntey.spiriteddriving.core.domain.route.RouteKind
import com.syntey.spiriteddriving.core.domain.savedroute.FakeSavedRoutesSource
import com.syntey.spiriteddriving.core.domain.savedroute.SavedRoute
import com.syntey.spiriteddriving.feature.map.ui.mapSheetScreen.MapSheetViewState
import kotlinx.collections.immutable.toImmutableList
import kotlin.time.Instant

/** Sample view states of the one-route step, for its previews: a saved route's and a past drive's. */
internal object OneRoutePreviewStates {
    /** A saved route's step: loaded from the Recorded saved route, which is saved, so Saved shows. */
    fun oneRouteViewState(
        status: OneRouteStatus = OneRouteStatus.Ready,
        sheetHeight: SheetHeight = SheetHeight.Half,
    ): MapSheetViewState {
        val saved = FakeSavedRoutesSource.Recorded
        val step = OneRouteStep(
            origin = OneRouteOrigin.Saved(saved),
            route = FakeRoutePlanner.Recorded.funRoutes.first().takeIf { status == OneRouteStatus.Ready },
            status = status,
        )
        return MapSheetPreviewStates.viewState().copy(
            sheetStep = step,
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
        val drive = PastDrive(
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
        val step = OneRouteStep(
            origin = OneRouteOrigin.Past(drive),
            route = route.takeIf { status == OneRouteStatus.Ready },
            status = status,
        )
        val saved: List<SavedRoute> = if (isSaved) {
            listOf(FakeSavedRoutesSource.Recorded.copy(route = FakeSavedRoutesSource.Recorded.route.copy(id = route.id)))
        } else {
            emptyList()
        }
        return MapSheetPreviewStates.viewState().copy(
            sheetStep = step,
            sheetHeight = sheetHeight,
            savedRoutes = saved.toImmutableList(),
        )
    }

    private val PRAGUE_START = GeoPoint(latitude = 50.0755, longitude = 14.4378)
}
