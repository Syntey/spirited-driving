package com.syntey.spiriteddriving.feature.map.domain

import app.futured.arkitekt.crusecases.UseCase
import com.syntey.spiriteddriving.core.domain.drive.Drive
import com.syntey.spiriteddriving.core.domain.drive.DriveNotRecordableException
import com.syntey.spiriteddriving.core.domain.drive.DriveQuestion
import com.syntey.spiriteddriving.core.domain.drive.DriveRecorder
import com.syntey.spiriteddriving.core.domain.drive.OpenDriveQuestion
import com.syntey.spiriteddriving.core.domain.drive.StartedDrives
import com.syntey.spiriteddriving.core.domain.drive.TripHistoryCache
import com.syntey.spiriteddriving.core.domain.route.Route
import com.syntey.spiriteddriving.core.domain.route.RouteKind
import com.syntey.spiriteddriving.core.platform.binding.DriveReminders
import com.syntey.spiriteddriving.feature.map.tools.Constants
import kotlin.time.Instant
import kotlin.uuid.Uuid
import org.koin.core.annotation.Factory

/**
 * Starts a drive on [Args.route] at [Args.at]. Starting the same route again before half its planned duration has passed
 * continues the drive it started, so tapping Drive twice doesn't record two drives. A new drive replaces the open question:
 * on a fun route that no kept drive has been rated on, it opens its own question, otherwise the open one is dropped. The
 * drive is then recorded unless it already is. Throws `OfflineException` without a connection, leaving the drive to be
 * recorded later, and `DriveNotRecordableException` after removing the drive, as the backend won't take it.
 *
 * A new question also schedules the drive's notification for the planned arrival plus a share of the duration, and a dropped
 * question's notification is cancelled. A continued drive keeps the notification it has.
 */
@Factory
internal class StartDriveUseCase(
    private val startedDrives: StartedDrives,
    private val driveRecorder: DriveRecorder,
    private val openDriveQuestion: OpenDriveQuestion,
    private val tripHistoryCache: TripHistoryCache,
    private val driveReminders: DriveReminders,
) : UseCase<StartDriveUseCase.Args, Drive> {

    data class Args(
        val route: Route,
        /** The route's number in the list it was picked from; null for the fastest route. */
        val routeNumber: Int?,
        val at: Instant,
    )

    override suspend fun build(args: Args): Drive {
        val drive = continuedDrive(args) ?: newDrive(args)
        // Kept for Trip history, so the drive can be opened and driven offline, also before it is recorded
        tripHistoryCache.putRoute(drive.id, args.route)
        if (drive.isRecorded) return drive
        try {
            driveRecorder.record(drive)
        } catch (error: DriveNotRecordableException) {
            startedDrives.remove(drive.id)
            throw error
        }
        startedDrives.markRecorded(drive.id)
        return drive.copy(isRecorded = true)
    }

    // Newest first, so the newest drive still in its half wins
    private suspend fun continuedDrive(args: Args): Drive? = startedDrives.all().firstOrNull { drive ->
        drive.routeId == args.route.id && args.at < drive.startedAt + drive.plannedDuration / 2
    }

    private suspend fun newDrive(args: Args): Drive {
        val drive = Drive(
            id = Uuid.random().toString(),
            routeId = args.route.id,
            startedAt = args.at,
            plannedDuration = args.route.duration,
        )
        val isRouteRated = startedDrives.all().any { it.routeId == args.route.id && it.rating != null }
        startedDrives.add(drive)
        updateQuestion(args, drive, isRouteRated)
        return drive
    }

    private suspend fun updateQuestion(args: Args, drive: Drive, isRouteRated: Boolean) {
        val route = args.route
        val routeNumber = args.routeNumber
        if (route.kind == RouteKind.Fun && routeNumber != null && !isRouteRated) {
            openDriveQuestion.set(
                DriveQuestion(
                    driveId = drive.id,
                    startedAt = drive.startedAt,
                    routeNumber = routeNumber,
                    start = route.start.location,
                    destination = route.destination,
                    duration = route.duration,
                    distance = route.distance,
                    line = route.line,
                    attribution = route.attribution,
                ),
            )
            driveReminders.schedule(
                drive.id,
                route.destination.name,
                args.at + route.duration * Constants.DriveFeedback.REMINDER_AFTER_DURATION,
            )
        } else {
            openDriveQuestion.get()?.let { dropped ->
                openDriveQuestion.clear(dropped.driveId)
                driveReminders.cancel(dropped.driveId)
            }
        }
    }
}
