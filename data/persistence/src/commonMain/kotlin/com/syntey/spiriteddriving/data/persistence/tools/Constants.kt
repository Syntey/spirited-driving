package com.syntey.spiriteddriving.data.persistence.tools

internal object Constants {

    object Drives {
        /** The most drives kept on the device; the oldest is dropped when a new one would exceed it. */
        const val MAX_KEPT = 100
    }

    object Files {
        const val SETTINGS_DATASTORE = "settings.preferences_pb"
        const val DATABASE = "spirited_driving.db"
    }
}
