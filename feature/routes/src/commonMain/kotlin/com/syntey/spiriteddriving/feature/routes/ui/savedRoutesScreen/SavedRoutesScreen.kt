package com.syntey.spiriteddriving.feature.routes.ui.savedRoutesScreen

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
        fun onRouteClick(id: String)

        /** Chose Rename in a row's menu. */
        fun onRenameClick(id: String)

        /** Confirmed the rename dialog with [name]. */
        fun onRenameSave(name: String)

        /** Dismissed the rename dialog with Cancel or outside it. */
        fun onRenameDismiss()

        /** Chose Delete in a row's menu, or swiped the row away. */
        fun onDeleteClick(id: String)

        /** Tapped Undo on a deleted route's message. */
        fun onDeleteUndo(id: String)

        /** The deleted route's message ended without Undo, so the delete goes through. */
        fun onDeleteMessageGone(id: String)

        /** Tapped Try again after the list failed to load. */
        fun onRetry()

        /** Tapped the back button in the bar. */
        fun onClose()

        companion object {
            fun noOpActions(): Actions = object : Actions {
                override fun onRouteClick(id: String) = Unit
                override fun onRenameClick(id: String) = Unit
                override fun onRenameSave(name: String) = Unit
                override fun onRenameDismiss() = Unit
                override fun onDeleteClick(id: String) = Unit
                override fun onDeleteUndo(id: String) = Unit
                override fun onDeleteMessageGone(id: String) = Unit
                override fun onRetry() = Unit
                override fun onClose() = Unit
            }
        }
    }
}
