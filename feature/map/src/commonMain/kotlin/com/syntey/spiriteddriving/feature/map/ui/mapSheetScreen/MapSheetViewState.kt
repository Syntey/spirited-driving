package com.syntey.spiriteddriving.feature.map.ui.mapSheetScreen

import com.syntey.spiriteddriving.core.domain.geo.GeoBounds
import com.syntey.spiriteddriving.core.domain.geo.GeoPoint
import com.syntey.spiriteddriving.core.domain.geo.Length
import com.syntey.spiriteddriving.core.domain.route.Route
import com.syntey.spiriteddriving.core.domain.savedroute.SavedRoute
import com.syntey.spiriteddriving.core.domain.settings.DistanceUnit
import com.syntey.spiriteddriving.core.platform.binding.LocationAccess
import com.syntey.spiriteddriving.core.resources.*
import com.syntey.spiriteddriving.feature.map.model.ui.distanceText
import com.syntey.spiriteddriving.feature.map.model.ui.DriveDialog
import com.syntey.spiriteddriving.feature.map.model.ui.LocateButton
import com.syntey.spiriteddriving.feature.map.model.ui.MyLocationRow
import com.syntey.spiriteddriving.feature.map.model.ui.OneRouteOrigin
import com.syntey.spiriteddriving.feature.map.model.ui.OneRouteStep
import com.syntey.spiriteddriving.feature.map.model.ui.RatingStep
import com.syntey.spiriteddriving.feature.map.model.ui.RecentPlaceRow
import com.syntey.spiriteddriving.feature.map.model.ui.ReminderBubble
import com.syntey.spiriteddriving.feature.map.model.ui.RouteLine
import com.syntey.spiriteddriving.feature.map.model.ui.RouteMapState
import com.syntey.spiriteddriving.feature.map.model.ui.RouteNameDialog
import com.syntey.spiriteddriving.feature.map.model.ui.SearchState
import com.syntey.spiriteddriving.feature.map.model.ui.SheetHeight
import com.syntey.spiriteddriving.feature.map.model.ui.SheetStep
import com.syntey.spiriteddriving.feature.map.model.ui.StartSearch
import com.syntey.spiriteddriving.feature.map.model.ui.SuggestionRow
import com.syntey.spiriteddriving.feature.map.model.ui.allowsLocation
import com.syntey.spiriteddriving.feature.map.tools.Constants
import com.syntey.spiriteddriving.feature.map.tools.matchedRanges
import dev.icerock.moko.resources.desc.ResourceFormatted
import dev.icerock.moko.resources.desc.StringDesc
import dev.icerock.moko.resources.desc.desc
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList

/**
 * @property baseMap The map from the component's own location, coverage and following, without pins; [map] adds the pins.
 * @property locationAccess What the user allows; null until it's read on start.
 * @property isPreciseLocationNoteClosed Whether the user closed the "Precise location is off" note. True until it's read, so
 *   the note doesn't flash.
 * @property sheetHeight How far the sheet is pulled up: by the user, or by focusing or closing the search.
 * @property distanceUnit The unit distances are shown in.
 * @property startSearch The start search, open over route planning; null otherwise. Projected from planning for the search's field
 *   and rows.
 * @property driveDialog The dialog about a drive shown over the trip; null when none is.
 * @property isFirstDriveExplained Whether the user went past the first-drive dialog.
 * @property sheetStep The step that takes the sheet's place of the search and shows a trip or a route: route planning, the rating
 *   step after a drive, or a one-route step of a saved route or a past drive; null while the sheet shows the destination search.
 * @property reminder The reminder about the drive not rated yet; null when there's none.
 * @property isSignedIn Whether a user is signed in; Save needs one.
 * @property savedRoutes The kept saved routes, which the selected route is checked against.
 * @property isSavingRoute Whether a route is being saved or removed; Save shows its progress meanwhile.
 * @property nameDialog The dialog asking for a name, shown when the destination's name is taken; null when there's none.
 */
data class MapSheetViewState(
    val locationAccess: LocationAccess? = null,
    val baseMap: RouteMapState = RouteMapState(),
    val isPreciseLocationNoteClosed: Boolean = true,
    val sheetHeight: SheetHeight = SheetHeight.Collapsed,
    val search: SearchState = SearchState(),
    val distanceUnit: DistanceUnit = DistanceUnit.Kilometres,
    val startSearch: StartSearch? = null,
    val driveDialog: DriveDialog? = null,
    val isFirstDriveExplained: Boolean = false,
    val sheetStep: SheetStep? = null,
    val reminder: ReminderBubble? = null,
    val isSignedIn: Boolean = false,
    val savedRoutes: ImmutableList<SavedRoute> = persistentListOf(),
    val isSavingRoute: Boolean = false,
    val nameDialog: RouteNameDialog? = null,
) {
    /** The map the screen shows: [baseMap] with the pins of the step on top, when it shows a trip or a route. */
    val map: RouteMapState
        get() = sheetStep?.let { baseMap.copy(start = it.start, destination = it.destination) } ?: baseMap

    /** The rating step after a drive; null otherwise. */
    val ratingStep: RatingStep? get() = sheetStep as? RatingStep

    /** The one-route step of a saved route or a past drive; null otherwise. */
    val oneRouteStep: OneRouteStep? get() = sheetStep as? OneRouteStep

    val locateButton: LocateButton get() = LocateButton.of(locationAccess, map.followsUser)

    /** The profile and locate buttons hide while a step shows a trip or a route on the map, and during the rating step. */
    val showsMapButtons: Boolean get() = sheetStep == null

    /** The area the map frames, from the step on top; null without one. */
    val routeBounds: GeoBounds? get() = sheetStep?.routeBounds

    /** The line of each route the map draws, from the step on top; empty without one. */
    val routeLines: ImmutableList<RouteLine> get() = sheetStep?.routeLines ?: persistentListOf()

    /** The user's location can start a trip: precise, as an approximate start can be kilometres off, and in the covered area. */
    val hasUsableLocation: Boolean get() = locationAccess == LocationAccess.Precise && !map.isOutsideCoveredArea

    /** The My location row: in the start step while the field is blank; null otherwise. */
    val myLocationRow: MyLocationRow?
        get() {
            if (startSearch == null || search.text.isNotBlank()) return null
            return when {
                !locationAccess.allowsLocation -> MyLocationRow.NeedsLocationAccess
                locationAccess == LocationAccess.Approximate -> MyLocationRow.NeedsPreciseLocation
                map.isOutsideCoveredArea -> MyLocationRow.OutsideCoveredArea
                else -> MyLocationRow.Available
            }
        }

    /**
     * The "Precise location is off" note: with approximate access only, until the user closes it, and not while the user is
     * outside the covered area, whose note says more.
     */
    val showsPreciseLocationNote: Boolean
        get() = locationAccess == LocationAccess.Approximate && !isPreciseLocationNoteClosed && !map.isOutsideCoveredArea

    /** The recent places as the sheet shows them, in the same order. */
    val recentRows: ImmutableList<RecentPlaceRow>
        get() = search.shownRecentPlaces.map { place ->
            RecentPlaceRow(
                place = place,
                isCovered = map.covers(place.location),
                boldRanges = matchedRanges(name = place.name, text = search.text),
                distance = distanceFrom(place.location),
            )
        }.toImmutableList()

    /** The suggestions as the sheet shows them, in the same order. */
    val suggestionRows: ImmutableList<SuggestionRow>
        get() = search.shownSuggestions.map { suggestion ->
            SuggestionRow(
                suggestion = suggestion,
                boldRanges = matchedRanges(name = suggestion.place.name, text = search.text),
                distance = distanceFrom(suggestion.place.location),
            )
        }.toImmutableList()

    /**
     * The route Drive hands off, from the step on top. Null while there's none to drive, such as while the saved route is still
     * loading.
     */
    val driveRoute: Route? get() = sheetStep?.driveRoute

    /**
     * The saved route the route on screen is kept as, or null when it isn't saved: by its route ID for a plan, by the saved route's
     * own ID for a saved route's step, since saving it again makes a new one, and by its route ID for a past drive.
     */
    val savedRouteOnScreen: SavedRoute?
        get() {
            oneRouteStep?.let { step ->
                return when (val origin = step.origin) {
                    is OneRouteOrigin.Saved -> savedRoutes.firstOrNull { it.id == origin.savedRoute.id }
                    is OneRouteOrigin.Past -> savedRoutes.firstOrNull { it.route.id == origin.drive.routeId }
                }
            }
            val routeId = driveRoute?.id ?: return null
            return savedRoutes.firstOrNull { it.route.id == routeId }
        }

    /** Whether the route on screen is saved; see [savedRouteOnScreen]. */
    val isSelectedRouteSaved: Boolean get() = savedRouteOnScreen != null

    /** The route with the ID among the routes on screen: the step on top's, when it shows a trip or a route. */
    fun routeOnScreen(routeId: String): Route? = sheetStep?.routeWithId(routeId)

    /** Drive is offered once a route is selected, or a saved route's route is loaded. */
    val isDriveEnabled: Boolean get() = driveRoute != null

    /** The Saved routes row's second line in the search step: signed out, none, one, or how many. */
    val savedRoutesRowText: StringDesc
        get() = when {
            !isSignedIn -> MR.strings.sign_in_sheet_title_saved_routes.desc()
            savedRoutes.isEmpty() -> MR.strings.map_saved_routes_none.desc()
            savedRoutes.size == 1 -> MR.strings.map_saved_routes_one.desc()
            else -> StringDesc.ResourceFormatted(MR.strings.map_saved_routes_many, savedRoutes.size)
        }

    /** The Saved routes row shows in the search step while the field is blank, above the recent places. */
    val showsSavedRoutesRow: Boolean get() = startSearch == null && search.text.isBlank()

    /**
     * How far the selected route starts from the user, when that's more than [Constants.Drive.START_ELSEWHERE_DISTANCE]; null
     * otherwise, and without a selected route or the user's location.
     */
    val startDistance: Length?
        get() {
            val start = driveRoute?.start?.location ?: return null
            val from = map.userLocation ?: return null
            return from.distanceTo(start).takeIf { it > Constants.Drive.START_ELSEWHERE_DISTANCE }
        }

    /** The reminder shows on the search step with the sheet collapsed, and never over the rating step or planning. */
    val showsReminder: Boolean
        get() = reminder != null &&
            sheetStep == null &&
            sheetHeight == SheetHeight.Collapsed

    /** The reminder's second line: the destination's name, or the point on the map, and the day; null without a reminder. */
    val reminderText: StringDesc?
        get() {
            val reminder = reminder ?: return null
            val name = reminder.question.destination.name
                ?: MR.strings.search_point_on_map.desc()
            return StringDesc.ResourceFormatted(MR.strings.drive_feedback_reminder_text, name, reminder.day)
        }

    // Straight-line, so it's known without a route; with approximate access it's only whole units
    private fun distanceFrom(location: GeoPoint): StringDesc? {
        val from = map.userLocation
        return if (from == null || !locationAccess.allowsLocation) {
            null
        } else {
            distanceText(
                distance = from.distanceTo(location),
                unit = distanceUnit,
                isApproximate = locationAccess == LocationAccess.Approximate,
            )
        }
    }
}
