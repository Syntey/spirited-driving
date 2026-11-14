package com.syntey.spiriteddriving.feature.map.domain

import app.futured.arkitekt.crusecases.UseCase
import com.syntey.spiriteddriving.core.domain.drive.Drive
import com.syntey.spiriteddriving.core.domain.drive.DriveNotRecordableException
import com.syntey.spiriteddriving.core.domain.drive.DriveRecorder
import com.syntey.spiriteddriving.core.domain.drive.StartedDrives
import com.syntey.spiriteddriving.core.domain.route.Route
import kotlin.time.Instant
import kotlin.uuid.Uuid
import org.koin.core.annotation.Factory

/**
 * Starts a drive on [Args.route] at [Args.at]. Starting the same route again before half its planned duration has passed
 * continues the drive it started, so tapping Drive twice doesn't record two drives. The drive is then recorded unless it
 * already is. Throws `OfflineException` without a connection, leaving the drive to be recorded later, and
 * `DriveNotRecordableException` after removing the drive, as the backend won't take it.
 */
@Factory
internal class StartDriveUseCase(private val startedDrives: StartedDrives, private val driveRecorder: DriveRecorder) :
    UseCase<StartDriveUseCase.Args, Drive> {

    data class Args(val route: Route, val at: Instant)

    override suspend fun build(args: Args): Drive {
        val drive = continuedDrive(args) ?: newDrive(args)
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
        startedDrives.add(drive)
        return drive
    }
}
