package com.syntey.spiriteddriving.core.domain.settings

/** Notes the user closed for good, kept on the device. */
interface ClosedNotes {
    suspend fun isClosed(note: Note): Boolean
    suspend fun close(note: Note)
}

/** A note the user can close for good. */
enum class Note {
    /** "Precise location is off" on the map. */
    PreciseLocation,
}
