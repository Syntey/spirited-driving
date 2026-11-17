package com.syntey.spiriteddriving.feature.map.domain

import app.futured.arkitekt.crusecases.UseCase
import com.syntey.spiriteddriving.core.domain.settings.ClosedNotes
import com.syntey.spiriteddriving.core.domain.settings.Note
import com.syntey.spiriteddriving.core.platform.binding.DriveReminders
import org.koin.core.annotation.Factory
import org.koin.core.annotation.Provided

/**
 * Whether to offer the drive reminder after the first rating: the phone still lets the app ask for its permission, and the
 * offer hasn't been shown yet. An offer that is shown is closed at once, so it shows once whatever the answer is.
 */
@Factory
internal class ShouldOfferReminderUseCase(private val closedNotes: ClosedNotes, @Provided private val driveReminders: DriveReminders) :
    UseCase<Unit, Boolean> {

    override suspend fun build(args: Unit): Boolean {
        if (closedNotes.isClosed(Note.ReminderOffer) || !driveReminders.canOffer()) return false
        closedNotes.close(Note.ReminderOffer)
        return true
    }
}
