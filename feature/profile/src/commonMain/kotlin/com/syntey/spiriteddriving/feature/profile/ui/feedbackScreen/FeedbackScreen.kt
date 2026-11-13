package com.syntey.spiriteddriving.feature.profile.ui.feedbackScreen

import kotlinx.coroutines.flow.StateFlow

interface FeedbackScreen {
    val viewState: StateFlow<FeedbackViewState>
    val actions: Actions

    interface Actions {
        /** Typed in the message field: [text] replaces the message. */
        fun onMessageChange(text: String)

        /** Tapped Send. */
        fun onSendClick()

        /** Tapped Done, once the feedback is sent. */
        fun onDoneClick()

        companion object {
            fun noOpActions(): Actions = object : Actions {
                override fun onMessageChange(text: String) = Unit
                override fun onSendClick() = Unit
                override fun onDoneClick() = Unit
            }
        }
    }
}
