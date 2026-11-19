package com.syntey.spiriteddriving.feature.routes.model.ui

import com.syntey.spiriteddriving.core.domain.savedroute.FakeSavedRoutesSource
import com.syntey.spiriteddriving.core.domain.savedroute.SavedRoute
import com.syntey.spiriteddriving.core.domain.settings.DistanceUnit
import com.syntey.spiriteddriving.feature.routes.ui.savedRoutesScreen.SavedRoutesViewState
import kotlinx.collections.immutable.toImmutableList

internal object SavedRoutesPreviewStates {

    val routes: List<SavedRoute> = listOf(
        FakeSavedRoutesSource.Recorded,
        FakeSavedRoutesSource.Recorded.copy(id = "saved-2", name = "Křivoklát loop", funLevel = 3),
        FakeSavedRoutesSource.Recorded.copy(id = "saved-3", name = "Sázava valley", funLevel = 4),
    )

    fun viewState(
        status: SavedRoutesStatus = SavedRoutesStatus.Ready,
        savedRoutes: List<SavedRoute>? = routes,
        distanceUnit: DistanceUnit = DistanceUnit.Kilometres,
    ) = SavedRoutesViewState(
        savedRoutes = savedRoutes?.toImmutableList(),
        distanceUnit = distanceUnit,
        status = status,
    )
}
