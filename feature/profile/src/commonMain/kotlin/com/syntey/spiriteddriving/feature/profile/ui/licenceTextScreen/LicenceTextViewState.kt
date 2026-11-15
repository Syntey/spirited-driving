package com.syntey.spiriteddriving.feature.profile.ui.licenceTextScreen

import com.syntey.spiriteddriving.feature.profile.model.Licence
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

data class LicenceTextViewState(val licences: ImmutableList<Licence> = persistentListOf())
