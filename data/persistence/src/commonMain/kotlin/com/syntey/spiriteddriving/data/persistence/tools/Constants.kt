package com.syntey.spiriteddriving.data.persistence.tools

internal const val SETTINGS_DATASTORE_FILENAME = "settings.preferences_pb"
internal const val DATABASE_FILENAME = "spirited_driving.db"

internal object Constants {

    object Drives {
        /** The most drives kept on the device; the oldest is dropped when a new one would exceed it. */
        const val MAX_KEPT = 100
    }
}
