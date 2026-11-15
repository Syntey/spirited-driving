package com.syntey.spiriteddriving.feature.profile.ui.licenceTextScreen

import kotlinx.serialization.Serializable

/** [libraryId] finds the licences, [name] is the screen's title. */
@Serializable
data class LicenceTextArgs(val libraryId: String, val name: String)
