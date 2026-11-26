package com.syntey.spiriteddriving.feature.routes.ui.savedRoutesScreen

import com.syntey.spiriteddriving.core.domain.savedroute.SavedRouteId
import dev.icerock.moko.resources.desc.StringDesc
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

interface SavedRoutesScreen {
    /** The title the navigation hosts show in the bar. */
    val title: StringDesc
    val viewState: StateFlow<SavedRoutesViewState>
    val actions: Actions

    /** One-time events for the screen, such as a message after a change. */
    val events: Flow<SavedRoutesUiEvent>

    interface Actions {
        /** Tapped a row: shows the route on the map. */
        fun onRouteClick(id: SavedRouteId)

        /** Chose Rename in a row's menu. */
        fun onRenameClick(id: SavedRouteId)

        /** Confirmed the rename dialog with [name]. */
        fun onRenameSave(name: String)

        /** Dismissed the rename dialog with Cancel or outside it. */
        fun onRenameDismiss()

        /** Chose Delete in a row's menu, or swiped the row away: asks first. */
        fun onDeleteClick(id: SavedRouteId)

        /** Confirmed the delete confirmation. */
        fun onDeleteConfirm()

        /** Dismissed the delete confirmation with Cancel or outside it. */
        fun onDeleteDismiss()

        /** Tapped Try again after the list failed to load. */
        fun onRetry()

        /** Tapped the back button in the bar. */
        fun onClose()

        companion object {
            fun noOpActions(): Actions = object : Actions {
                override fun onRouteClick(id: SavedRouteId) = Unit
                override fun onRenameClick(id: SavedRouteId) = Unit
                override fun onRenameSave(name: String) = Unit
                override fun onRenameDismiss() = Unit
                override fun onDeleteClick(id: SavedRouteId) = Unit
                override fun onDeleteConfirm() = Unit
                override fun onDeleteDismiss() = Unit
                override fun onRetry() = Unit
                override fun onClose() = Unit
            }
        }
    }
}
