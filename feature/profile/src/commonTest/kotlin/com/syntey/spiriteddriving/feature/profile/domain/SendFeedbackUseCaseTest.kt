package com.syntey.spiriteddriving.feature.profile.domain

import com.syntey.spiriteddriving.core.domain.error.OfflineException
import com.syntey.spiriteddriving.core.domain.feedback.FakeFeedbackSender
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class SendFeedbackUseCaseTest {

    @Test
    fun `sends the message trimmed`() = runTest {
        val sender = FakeFeedbackSender()
        val useCase = SendFeedbackUseCase(sender)

        useCase.build(SendFeedbackUseCase.Args(message = "  The lake road is great.\n "))

        assertEquals(listOf("The lake road is great."), sender.sent)
    }

    @Test
    fun `an error from the sender reaches the caller`() = runTest {
        val sender = FakeFeedbackSender().apply { failure = OfflineException(null) }
        val useCase = SendFeedbackUseCase(sender)

        assertFailsWith<OfflineException> { useCase.build(SendFeedbackUseCase.Args(message = "Hello")) }
    }
}
