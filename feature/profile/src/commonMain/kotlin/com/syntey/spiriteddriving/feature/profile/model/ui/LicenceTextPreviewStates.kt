package com.syntey.spiriteddriving.feature.profile.model.ui

import com.syntey.spiriteddriving.feature.profile.model.Licence
import com.syntey.spiriteddriving.feature.profile.tools.Constants
import com.syntey.spiriteddriving.feature.profile.ui.licenceTextScreen.LicenceTextViewState
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

internal object LicenceTextPreviewStates {

    /** A library with one licence, which has its text. */
    val oneLicence = persistentListOf(
        Licence(
            name = "MIT License",
            text = Constants.Licences.PHOSPHOR_LICENCE_TEXT,
            url = Constants.Licences.PHOSPHOR_LICENCE_URL,
        ),
    )

    /** A library with two licences, of which the second has no text, only a page. */
    val twoLicences = persistentListOf(
        Licence(
            name = "MIT License",
            text = Constants.Licences.PHOSPHOR_LICENCE_TEXT,
            url = Constants.Licences.PHOSPHOR_LICENCE_URL,
        ),
        Licence(name = "Apache License 2.0", text = null, url = "https://spdx.org/licenses/Apache-2.0.html"),
    )

    fun viewState(licences: ImmutableList<Licence> = persistentListOf()) = LicenceTextViewState(licences = licences)
}
