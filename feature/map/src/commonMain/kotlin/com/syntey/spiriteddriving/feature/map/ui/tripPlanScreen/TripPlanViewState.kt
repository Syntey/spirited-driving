package com.syntey.spiriteddriving.feature.map.ui.tripPlanScreen

import com.syntey.spiriteddriving.core.domain.geo.GeoBounds
import com.syntey.spiriteddriving.core.domain.geo.GeoPoint
import com.syntey.spiriteddriving.core.domain.drive.RatingReason
import com.syntey.spiriteddriving.core.domain.drive.Thumb
import com.syntey.spiriteddriving.core.domain.geo.Length
import com.syntey.spiriteddriving.core.domain.route.Budget
import com.syntey.spiriteddriving.core.domain.route.BudgetChoice
import com.syntey.spiriteddriving.core.domain.route.BudgetKind
import com.syntey.spiriteddriving.core.domain.route.Route
import com.syntey.spiriteddriving.core.domain.savedroute.SavedRoute
import com.syntey.spiriteddriving.core.domain.route.RouteKind
import com.syntey.spiriteddriving.core.domain.route.TripPlan
import com.syntey.spiriteddriving.core.domain.settings.DistanceUnit
import com.syntey.spiriteddriving.core.platform.binding.LocationAccess
import com.syntey.spiriteddriving.core.resources.*
import com.syntey.spiriteddriving.core.resources.datetime.driveDateTimeText
import com.syntey.spiriteddriving.feature.map.model.ui.BudgetChip
import com.syntey.spiriteddriving.feature.map.model.ui.DriveDialog
import com.syntey.spiriteddriving.feature.map.model.ui.MyLocationRow
import com.syntey.spiriteddriving.feature.map.model.ui.NoFunRouteMessage
import com.syntey.spiriteddriving.feature.map.model.ui.PlanningStatus
import com.syntey.spiriteddriving.feature.map.model.ui.RatingStep
import com.syntey.spiriteddriving.feature.map.model.ui.ReasonChip
import com.syntey.spiriteddriving.feature.map.model.ui.ReminderBubble
import com.syntey.spiriteddriving.feature.map.model.ui.RouteNameDialog
import com.syntey.spiriteddriving.feature.map.model.ui.UseBudgetButton
import com.syntey.spiriteddriving.feature.map.model.ui.LocateButton
import com.syntey.spiriteddriving.feature.map.model.ui.RecentPlaceRow
import com.syntey.spiriteddriving.feature.map.model.ui.RouteMapState
import com.syntey.spiriteddriving.feature.map.model.ui.OneRouteOrigin
import com.syntey.spiriteddriving.feature.map.model.ui.OneRouteStatus
import com.syntey.spiriteddriving.feature.map.model.ui.OneRouteStep
import com.syntey.spiriteddriving.feature.map.model.ui.RoutePlanningState
import com.syntey.spiriteddriving.feature.map.model.ui.RouteLine
import com.syntey.spiriteddriving.feature.map.model.ui.RouteRow
import com.syntey.spiriteddriving.feature.map.model.ui.SearchState
import com.syntey.spiriteddriving.feature.map.model.ui.SheetHeight
import com.syntey.spiriteddriving.feature.map.model.ui.StartSearch
import com.syntey.spiriteddriving.feature.map.model.ui.SuggestionRow
import com.syntey.spiriteddriving.feature.map.model.ui.allowsLocation
import com.syntey.spiriteddriving.feature.map.tools.budgetText
import com.syntey.spiriteddriving.feature.map.tools.distanceChips
import com.syntey.spiriteddriving.core.resources.measure.distanceText
import com.syntey.spiriteddriving.core.resources.measure.durationText
import com.syntey.spiriteddriving.core.resources.measure.heightText
import com.syntey.spiriteddriving.core.resources.rating.label
import com.syntey.spiriteddriving.feature.map.tools.matchedRanges
import com.syntey.spiriteddriving.feature.map.tools.midpointAlong
import dev.icerock.moko.resources.desc.ResourceFormatted
import dev.icerock.moko.resources.desc.desc
import dev.icerock.moko.resources.desc.StringDesc
import kotlin.math.ceil
import kotlin.time.Duration.Companion.minutes
import kotlin.time.DurationUnit
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import com.syntey.spiriteddriving.feature.map.tools.Constants

/**
 * @property locationAccess What the user allows; null until it's read on start.
 * @property isPreciseLocationNoteClosed Whether the user closed the "Precise location is off" note. True until it's read, so
 *   the note doesn't flash.
 * @property sheetHeight How far the sheet is pulled up: by the user, or by focusing or closing the search.
 * @property distanceUnit The unit distances are shown in.
 * @property planning The route planning step; null while the sheet shows the search.
 * @property startSearch The start step, open over route planning; null otherwise.
 * @property budgetChoice The budget planning starts with, and the last value in each unit.
 * @property driveDialog The dialog about a drive shown over the trip; null when none is.
 * @property isFirstDriveExplained Whether the user went past the first-drive dialog.
 * @property ratingStep The rating step after a drive; null otherwise. It takes the sheet's place of the search and route planning.
 * @property reminder The reminder about the drive not rated yet; null when there's none.
 * @property isSignedIn Whether a user is signed in; Save needs one.
 * @property savedRoutes The kept saved routes, which the selected route is checked against.
 * @property isSavingRoute Whether a route is being saved or removed; Save shows its progress meanwhile.
 * @property nameDialog The dialog asking for a name, shown when the destination's name is taken; null when there's none.
 * @property oneRouteStep The one-route step of a saved route or a past drive; null otherwise. It takes the sheet's place of the search
 *   and planning.
 */
data class TripPlanViewState(
    val locationAccess: LocationAccess? = null,
    val map: RouteMapState = RouteMapState(),
    val isPreciseLocationNoteClosed: Boolean = true,
    val sheetHeight: SheetHeight = SheetHeight.Collapsed,
    val search: SearchState = SearchState(),
    val distanceUnit: DistanceUnit = DistanceUnit.Kilometres,
    val planning: RoutePlanningState? = null,
    val startSearch: StartSearch? = null,
    val budgetChoice: BudgetChoice = Constants.Budgets.DEFAULT_CHOICE,
    val driveDialog: DriveDialog? = null,
    val isFirstDriveExplained: Boolean = false,
    val ratingStep: RatingStep? = null,
    val reminder: ReminderBubble? = null,
    val isSignedIn: Boolean = false,
    val savedRoutes: ImmutableList<SavedRoute> = persistentListOf(),
    val isSavingRoute: Boolean = false,
    val nameDialog: RouteNameDialog? = null,
    val oneRouteStep: OneRouteStep? = null,
) {
    val locateButton: LocateButton get() = LocateButton.of(locationAccess, map.followsUser)

    /** The unit the budget is in: the planning's budget's while planning, otherwise the choice's. */
    val budgetKind: BudgetKind
        get() = when (planning?.budget) {
            is Budget.Time -> BudgetKind.Time
            is Budget.Distance -> BudgetKind.Distance
            null -> budgetChoice.kind
        }

    /** The chips in the planning budget's unit; empty without planning. */
    val budgetChips: ImmutableList<BudgetChip>
        get() {
            val budget = planning?.budget ?: return persistentListOf()
            return budgetOptions(budget, distanceUnit).map { option ->
                BudgetChip(budget = option, label = budgetText(option, distanceUnit), isSelected = option == budget)
            }.toImmutableList()
        }

    /**
     * The message above the fastest route when no fun route fits the budget. Null unless planned without fun routes, so it
     * never shows while planning or with fun routes listed.
     */
    val noFunRouteMessage: NoFunRouteMessage?
        get() {
            val planning = planning ?: return null
            val plan = planning.plan ?: return null
            if (planning.status != PlanningStatus.Planned || plan.funRoutes.isNotEmpty()) return null
            return noFunRouteMessageFor(planning, plan)
        }

    /** The profile and locate buttons hide while planning, as the planned routes take the map, and during the rating step. */
    val showsMapButtons: Boolean get() = planning == null && ratingStep == null && oneRouteStep == null

    /**
     * The area the map frames: all the planned routes' lines once they have points; otherwise, and while planning, the start,
     * or the user's location when known, and the destination. Never smaller than the minimum span, so a single point still shows
     * its streets. During the rating step, the driven line. Null without planning or the rating step.
     */
    val routeBounds: GeoBounds?
        get() {
            ratingStep?.let { step ->
                // A stored line may be empty; the drive's ends frame it then
                val question = step.question
                return boundsOf(question.line.ifEmpty { listOf(question.start, question.destination.location) }).withMinimumSpan()
            }
            oneRouteStep?.let { step ->
                // Until the route is loaded, the start and destination of its origin frame the map
                val line = step.route?.line.orEmpty()
                return boundsOf(line.ifEmpty { listOf(step.origin.start.location, step.origin.destination.location) })
                    .withMinimumSpan()
            }
            val planning = planning ?: return null
            val points = if (planning.plan != null) planning.listedRoutes.flatMap { it.line } else emptyList()
            val bounds = if (points.isEmpty()) planningBounds(planning) else boundsOf(points)
            return bounds.withMinimumSpan()
        }

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

    /** The routes of the plan as the route list shows them; empty without a plan. */
    val routeRows: ImmutableList<RouteRow>
        get() {
            val planning = planning ?: return persistentListOf()
            // The fun routes come first in the listed routes, so a fun route's number is its place in the list
            return planning.listedRoutes.mapIndexed { index, route ->
                routeRow(
                    route = route,
                    number = (index + 1).takeIf { route.kind == RouteKind.Fun },
                    extra = extraText(route, planning.budget),
                    isSelected = route.id == planning.selectedRouteId,
                )
            }.toImmutableList()
        }

    /** The selected route as the route list shows it; null without a plan. */
    val selectedRouteRow: RouteRow? get() = routeRows.firstOrNull { it.isSelected }

    /** The saved route's route as its step's summary shows it: numbered 1, without an extra; null until it's loaded. */
    val oneRouteRow: RouteRow?
        get() {
            val route = oneRouteStep?.route ?: return null
            return routeRow(route = route, number = 1, extra = null, isSelected = true)
        }

    /**
     * The route Drive hands off: the saved route's while its step is open, else the selected planned route. Null while there's
     * none to drive, such as while the saved route is still loading.
     */
    val driveRoute: Route? get() = oneRouteStep?.route ?: planning?.selectedRoute

    /** The one-route step's header title: the saved route's name, or the past drive's destination. Null without the step. */
    val oneRouteTitle: String?
        get() = when (val origin = oneRouteStep?.origin ?: return null) {
            is OneRouteOrigin.Saved -> origin.savedRoute.name
            is OneRouteOrigin.Past -> origin.destination.name ?: origin.destination.area.orEmpty()
        }

    /**
     * The one-route step's header subtitle: the start's area, and for a past drive, when it started before that. Null without the
     * step, and without the start's area for a saved route.
     */
    val oneRouteSubtitle: StringDesc?
        get() = when (val origin = oneRouteStep?.origin ?: return null) {
            is OneRouteOrigin.Saved -> origin.start.shortArea?.let { StringDesc.ResourceFormatted(MR.strings.planning_from, it) }

            is OneRouteOrigin.Past -> {
                val started = driveDateTimeText(origin.drive.startedAt)
                origin.start.shortArea?.let { StringDesc.ResourceFormatted(MR.strings.map_past_drive_subtitle, started, it) }
                    ?: StringDesc.ResourceFormatted(MR.strings.map_past_drive_subtitle_no_start, started)
            }
        }

    /**
     * The saved route the route on screen is kept as, or null when it isn't saved: by its route ID for a plan, by the saved route's
     * own ID for a saved route's step, since saving it again makes a new one, and by its route ID for a past drive.
     */
    val savedRouteOnScreen: SavedRoute?
        get() {
            oneRouteStep?.let { step ->
                return when (val origin = step.origin) {
                    is OneRouteOrigin.Saved -> savedRoutes.firstOrNull { it.id == origin.savedRoute.id }
                    is OneRouteOrigin.Past -> savedRoutes.firstOrNull { it.routeId == origin.drive.routeId }
                }
            }
            val routeId = planning?.selectedRouteId ?: return null
            return savedRoutes.firstOrNull { it.routeId == routeId }
        }

    /** Whether the route on screen is saved; see [savedRouteOnScreen]. */
    val isSelectedRouteSaved: Boolean get() = savedRouteOnScreen != null

    /** The route with the ID among the routes on screen: a planned route or the one-route step's; null when neither has it. */
    fun routeOnScreen(routeId: String): Route? =
        planning?.listedRoutes?.firstOrNull { it.id == routeId } ?: driveRoute?.takeIf { it.id == routeId }

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

    /**
     * The routes' lines as the map draws them, each with its label; empty without a plan. The selected line is last, so it
     * draws on top of the others. The rating step's drive is its one line, without a label.
     */
    val routeLines: ImmutableList<RouteLine>
        get() {
            ratingStep?.let { step ->
                val question = step.question
                if (question.line.isEmpty()) return persistentListOf()
                return persistentListOf(
                    RouteLine(
                        id = question.driveId,
                        number = question.routeNumber,
                        points = question.line.toImmutableList(),
                        label = null,
                        labelPoint = midpointAlong(question.line),
                        isSelected = true,
                    ),
                )
            }
            oneRouteStep?.let { step ->
                val line = step.route?.line.orEmpty()
                if (line.isEmpty()) return persistentListOf()
                return persistentListOf(
                    RouteLine(
                        id = step.origin.id,
                        number = 1,
                        points = line.toImmutableList(),
                        label = null,
                        labelPoint = midpointAlong(line),
                        isSelected = true,
                    ),
                )
            }
            val planning = planning ?: return persistentListOf()
            return planning.listedRoutes.mapIndexedNotNull { index, route ->
                // A line without points has nothing to draw, and no midpoint
                if (route.line.isEmpty()) return@mapIndexedNotNull null
                // Same numbering as routeRows
                val number = (index + 1).takeIf { route.kind == RouteKind.Fun }
                val points = route.line.toImmutableList()
                RouteLine(
                    id = route.id,
                    number = number,
                    points = points,
                    label = if (number == null) {
                        StringDesc.ResourceFormatted(MR.strings.planning_fastest_label, durationText(route.duration))
                    } else {
                        StringDesc.ResourceFormatted(
                            MR.strings.planning_route_label,
                            number,
                            requireNotNull(extraText(route, planning.budget)) { "A fun route has an extra over the fastest" },
                        )
                    },
                    labelPoint = midpointAlong(route.line),
                    isSelected = route.id == planning.selectedRouteId,
                )
            }.sortedBy { it.isSelected }.toImmutableList()
        }

    /** The offer of the drive reminder above Done in the rating step; false without the step. */
    val showsReminderOffer: Boolean
        get() = ratingStep?.showsReminderOffer == true

    /** The reminder shows on the search step with the sheet collapsed, and never over the rating step or planning. */
    val showsReminder: Boolean
        get() = reminder != null &&
            ratingStep == null &&
            planning == null &&
            oneRouteStep == null &&
            startSearch == null &&
            sheetHeight == SheetHeight.Collapsed

    /** The reminder's second line: the destination's name, or the point on the map, and the day; null without a reminder. */
    val reminderText: StringDesc?
        get() {
            val reminder = reminder ?: return null
            val name = reminder.question.destination.name
                ?: MR.strings.search_point_on_map.desc()
            return StringDesc.ResourceFormatted(MR.strings.drive_feedback_reminder_text, name, reminder.day)
        }

    /** The rating step's title: the question about the destination; null without the step. */
    val ratingTitle: StringDesc?
        get() {
            val question = ratingStep?.question ?: return null
            val name = question.destination.name ?: return MR.strings.drive_feedback_question_unnamed.desc()
            return StringDesc.ResourceFormatted(MR.strings.drive_feedback_question, name)
        }

    /** The drive's time, planned duration and distance under the title; null without the rating step. */
    val ratingDriveText: StringDesc?
        get() {
            val step = ratingStep ?: return null
            return StringDesc.ResourceFormatted(
                MR.strings.drive_feedback_drive,
                step.driveTime,
                durationText(step.question.duration),
                distanceText(step.question.distance, distanceUnit, isApproximate = false),
            )
        }

    /** The thanks for the picked thumb; null before a thumb is picked. */
    val ratingThanks: StringDesc?
        get() = when (ratingStep?.rating?.thumb) {
            null -> null
            Thumb.Up -> MR.strings.drive_feedback_thanks_good.desc()
            Thumb.Down -> MR.strings.drive_feedback_thanks_not_good.desc()
        }

    /** The reason chips of the picked thumb, in order; empty before a thumb is picked. */
    val reasonChips: ImmutableList<ReasonChip>
        get() {
            val rating = ratingStep?.rating ?: return persistentListOf()
            return RatingReason.of(rating.thumb).map { reason ->
                ReasonChip(reason = reason, label = reason.label(), isSelected = reason in rating.reasons)
            }.toImmutableList()
        }

    private fun planningBounds(planning: RoutePlanningState): GeoBounds {
        val destination = planning.destination.location
        val origin = planning.start?.location ?: map.userLocation ?: return GeoBounds(destination, destination)
        return GeoBounds(origin, origin).including(destination)
    }

    // Grows around the centre, so a short or narrow area is framed with the streets around it
    private fun GeoBounds.withMinimumSpan(): GeoBounds {
        val latSpan = northEast.latitude - southWest.latitude
        val lonSpan = northEast.longitude - southWest.longitude
        if (latSpan >= Constants.Map.MIN_ROUTE_LAT_SPAN && lonSpan >= Constants.Map.MIN_ROUTE_LON_SPAN) return this
        val centreLat = (northEast.latitude + southWest.latitude) / 2
        val centreLon = (northEast.longitude + southWest.longitude) / 2
        val halfLat = maxOf(latSpan, Constants.Map.MIN_ROUTE_LAT_SPAN) / 2
        val halfLon = maxOf(lonSpan, Constants.Map.MIN_ROUTE_LON_SPAN) / 2
        return GeoBounds(
            southWest = GeoPoint(latitude = centreLat - halfLat, longitude = centreLon - halfLon),
            northEast = GeoPoint(latitude = centreLat + halfLat, longitude = centreLon + halfLon),
        )
    }

    private fun routeRow(route: Route, number: Int?, extra: StringDesc?, isSelected: Boolean): RouteRow = RouteRow(
        id = route.id,
        number = number,
        extra = extra,
        duration = durationText(route.duration),
        distance = distanceText(route.distance, distanceUnit, isApproximate = false),
        curvyDistance = distanceText(route.stats.curvyDistance, distanceUnit, isApproximate = false),
        ascent = heightText(route.stats.ascent, distanceUnit),
        funLevel = route.funLevel,
        isSelected = isSelected,
    )

    // The fastest route is the reference, so it has no extra over itself
    private fun extraText(route: Route, budget: Budget): StringDesc? {
        if (route.kind == RouteKind.Fastest) return null
        return when (budget) {
            is Budget.Time -> StringDesc.ResourceFormatted(MR.strings.planning_extra, durationText(route.extraDuration))

            is Budget.Distance -> StringDesc.ResourceFormatted(
                MR.strings.planning_extra,
                distanceText(route.extraDistance, distanceUnit, isApproximate = false),
            )
        }
    }

    // The closest fun route's budget picks the smallest chip that fits it. A budget in the other unit fits nothing, as with none.
    private fun noFunRouteMessageFor(planning: RoutePlanningState, plan: TripPlan): NoFunRouteMessage {
        val budget = planning.budget
        val options = budgetOptions(budget, distanceUnit)
        val closest = plan.closestFunRouteBudget
        val fitting = closest?.let { needed -> options.firstOrNull { it.allowsAtLeast(needed) } }
        return if (closest != null && fitting != null) {
            NoFunRouteMessage(
                text = StringDesc.ResourceFormatted(
                    MR.strings.planning_no_fun_route,
                    budgetText(budget, distanceUnit),
                    closestText(closest),
                ),
                useBudget = UseBudgetButton(
                    budget = fitting,
                    label = StringDesc.ResourceFormatted(MR.strings.planning_use_budget, budgetText(fitting, distanceUnit)),
                ),
            )
        } else {
            NoFunRouteMessage(
                text = StringDesc.ResourceFormatted(
                    MR.strings.planning_no_fun_route_beyond,
                    budgetText(budget, distanceUnit),
                    budgetText(options.last(), distanceUnit),
                ),
                useBudget = null,
            )
        }
    }

    private fun closestText(closest: Budget): StringDesc = when (closest) {
        is Budget.Time -> {
            // Rounded up to whole minutes, so the text never names a value the current chip already covers
            val wholeMinutes = ceil(closest.extra.toDouble(DurationUnit.MINUTES)).toInt().minutes
            StringDesc.ResourceFormatted(MR.strings.planning_extra, durationText(wholeMinutes))
        }

        is Budget.Distance -> StringDesc.ResourceFormatted(
            MR.strings.planning_extra,
            distanceText(closest.extra, distanceUnit, isApproximate = false),
        )
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

private fun boundsOf(points: List<GeoPoint>): GeoBounds =
    points.drop(1).fold(GeoBounds(points.first(), points.first())) { bounds, point -> bounds.including(point) }

// The chips of the budget's unit, the distance ones in [unit]
private fun budgetOptions(budget: Budget, unit: DistanceUnit): List<Budget> = when (budget) {
    is Budget.Time -> Constants.Budgets.TIME
    is Budget.Distance -> distanceChips(unit)
}

// Whether this budget allows at least [other]'s extra; never across units
private fun Budget.allowsAtLeast(other: Budget): Boolean = when (this) {
    is Budget.Time -> other is Budget.Time && extra >= other.extra
    is Budget.Distance -> other is Budget.Distance && extra >= other.extra
}
