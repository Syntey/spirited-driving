package com.syntey.spiriteddriving.feature.profile.ui.feedbackScreen

import dev.icerock.moko.resources.desc.StringDesc
import kotlinx.coroutines.flow.StateFlow

interface FeedbackScreen {
    /** The title the navigation hosts show in the bar. */
    val title: StringDesc
    val viewState: StateFlow<FeedbackViewState>
    val actions: Actions

    interface Actions {
        /** Typed in the message field: [text] replaces the message. */
        fun onMessageChange(text: String)

        /** Tapped Send. */
        fun onSendClick()

        /** Tapped Done, once the feedback is sent. */
        fun onDoneClick()

        /** Tapped the back button in the bar. */
        fun onBack()

        companion object {
            fun noOpActions(): Actions = object : Actions {
                override fun onMessageChange(text: String) = Unit
                override fun onSendClick() = Unit
                override fun onDoneClick() = Unit
                override fun onBack() = Unit
            }
        }
    }
}
