package com.syntey.spiriteddriving.feature.profile.ui.feedbackScreen

import app.futured.arkitekt.annotation.GenerateFactory
import app.futured.arkitekt.crusecases.execute
import com.arkivanov.essenty.lifecycle.doOnCreate
import com.syntey.spiriteddriving.core.domain.error.OfflineException
import com.syntey.spiriteddriving.core.platform.binding.Platform
import com.syntey.spiriteddriving.core.resources.*
import com.syntey.spiriteddriving.core.ui.base.AppComponentContext
import com.syntey.spiriteddriving.core.ui.base.ScreenComponent
import com.syntey.spiriteddriving.feature.profile.domain.SendFeedbackUseCase
import com.syntey.spiriteddriving.feature.profile.model.ui.FeedbackStatus
import com.syntey.spiriteddriving.feature.profile.tools.Constants
import dev.icerock.moko.resources.desc.ResourceFormatted
import dev.icerock.moko.resources.desc.StringDesc
import dev.icerock.moko.resources.desc.desc
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import org.koin.core.annotation.Factory
import org.koin.core.annotation.InjectedParam
import org.koin.core.annotation.Provided

@Factory
@GenerateFactory
internal class FeedbackComponent(
    @InjectedParam componentContext: AppComponentContext,
    @InjectedParam override val navigation: FeedbackScreenNavigation,
    @Provided private val platform: Platform,
    private val sendFeedbackUseCase: SendFeedbackUseCase,
) : ScreenComponent<FeedbackViewState, Nothing, FeedbackScreenNavigation>(
    componentContext = componentContext,
    defaultState = FeedbackViewState(),
),
    FeedbackScreen,
    FeedbackScreenNavigation by navigation,
    FeedbackScreen.Actions {

    override val viewState: StateFlow<FeedbackViewState> = componentState
    override val title: StringDesc = MR.strings.feedback_screen_title.desc()
    override val actions: FeedbackScreen.Actions = this

    init {
        doOnCreate { showSentWith() }
    }

    override fun onMessageChange(text: String) {
        componentState.update { state ->
            val status = if (state.status == FeedbackStatus.Offline || state.status == FeedbackStatus.Failed) {
                FeedbackStatus.Editing
            } else {
                state.status
            }
            state.copy(message = text.take(Constants.Feedback.MESSAGE_MAX_LENGTH), status = status)
        }
    }

    override fun onSendClick() {
        val state = componentState.value
        if (!state.canSend) return
        componentState.update { it.copy(status = FeedbackStatus.Sending) }
        sendFeedbackUseCase.execute(SendFeedbackUseCase.Args(message = state.message)) {
            onSuccess { componentState.update { current -> current.copy(status = FeedbackStatus.Sent) } }
            onError { error ->
                val status = if (error is OfflineException) FeedbackStatus.Offline else FeedbackStatus.Failed
                componentState.update { it.copy(status = status) }
            }
        }
    }

    override fun onBackClick() = pop()
    override fun onDoneClick() = pop()

    private fun showSentWith() {
        val sentWith = StringDesc.ResourceFormatted(
            MR.strings.feedback_sent_with,
            platform.appName,
            platform.appVersionName,
            platform.appBuildNumber,
            platform.osNameWithVersion,
            platform.deviceModel,
        )
        componentState.update { it.copy(sentWith = sentWith) }
    }
}
