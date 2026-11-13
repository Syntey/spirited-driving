package com.syntey.spiriteddriving.feature.profile.domain

import app.futured.arkitekt.crusecases.UseCase
import com.syntey.spiriteddriving.core.domain.feedback.Feedback
import com.syntey.spiriteddriving.core.domain.feedback.FeedbackSender
import com.syntey.spiriteddriving.core.platform.binding.Platform
import org.koin.core.annotation.Factory
import org.koin.core.annotation.Provided

/** Sends the user's feedback, with the app and phone it came from. Throws what the sender throws. */
@Factory
internal class SendFeedbackUseCase(private val feedbackSender: FeedbackSender, @Provided private val platform: Platform) :
    UseCase<SendFeedbackUseCase.Args, Unit> {

    data class Args(val message: String)

    override suspend fun build(args: Args) = feedbackSender.send(
        Feedback(
            message = args.message.trim(),
            appVersion = platform.appVersionName,
            appBuild = platform.appBuildNumber,
            os = platform.osNameWithVersion,
            device = platform.deviceModel,
        ),
    )
}
