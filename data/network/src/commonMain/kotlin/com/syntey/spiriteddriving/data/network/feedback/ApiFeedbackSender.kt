package com.syntey.spiriteddriving.data.network.feedback

import com.syntey.spiriteddriving.core.domain.feedback.FeedbackSender
import com.syntey.spiriteddriving.data.network.api.SpiritedDrivingApi
import com.syntey.spiriteddriving.data.network.dto.feedback.FeedbackRequestDto
import com.syntey.spiriteddriving.data.network.result.getOrThrowDomainError
import org.koin.core.annotation.Factory

@Factory
internal class ApiFeedbackSender(private val api: SpiritedDrivingApi) : FeedbackSender {

    override suspend fun send(message: String) {
        api.sendFeedback(FeedbackRequestDto(message = message)).getOrThrowDomainError()
    }
}
