package com.syntey.spiriteddriving.feature.map.domain

import app.futured.arkitekt.crusecases.UseCase
import com.syntey.spiriteddriving.core.domain.settings.ClosedNotes
import com.syntey.spiriteddriving.core.domain.settings.Note
import org.koin.core.annotation.Factory

/**
 * Closes [Note] for good, so it doesn't show again.
 */
@Factory
internal class CloseNoteUseCase(private val closedNotes: ClosedNotes) : UseCase<Note, Unit> {

    override suspend fun build(args: Note) = closedNotes.close(args)
}
