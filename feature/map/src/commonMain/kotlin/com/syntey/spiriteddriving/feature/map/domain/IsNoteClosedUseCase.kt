package com.syntey.spiriteddriving.feature.map.domain

import app.futured.arkitekt.crusecases.UseCase
import com.syntey.spiriteddriving.core.domain.settings.ClosedNotes
import com.syntey.spiriteddriving.core.domain.settings.Note
import org.koin.core.annotation.Factory

/**
 * Whether the user closed [Note] for good.
 */
@Factory
internal class IsNoteClosedUseCase(private val closedNotes: ClosedNotes) : UseCase<Note, Boolean> {

    override suspend fun build(args: Note): Boolean = closedNotes.isClosed(args)
}
