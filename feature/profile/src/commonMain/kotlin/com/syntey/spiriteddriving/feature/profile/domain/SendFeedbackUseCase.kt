package com.syntey.spiriteddriving.feature.profile.domain

import app.futured.arkitekt.crusecases.UseCase
import com.syntey.spiriteddriving.core.domain.feedback.FeedbackSender
import org.koin.core.annotation.Factory

/** Sends the user's feedback, trimmed. Throws what the sender throws. */
@Factory
internal class SendFeedbackUseCase(private val feedbackSender: FeedbackSender) : UseCase<SendFeedbackUseCase.Args, Unit> {

    data class Args(val message: String)

    override suspend fun build(args: Args) = feedbackSender.send(args.message.trim())
}
