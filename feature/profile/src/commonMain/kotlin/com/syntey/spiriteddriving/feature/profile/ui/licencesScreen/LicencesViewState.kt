package com.syntey.spiriteddriving.feature.profile.ui.licencesScreen

import com.syntey.spiriteddriving.feature.profile.model.ui.LibraryItem
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

data class LicencesViewState(val libraries: ImmutableList<LibraryItem> = persistentListOf())
