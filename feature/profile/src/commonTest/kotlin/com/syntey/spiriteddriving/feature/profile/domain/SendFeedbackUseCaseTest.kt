package com.syntey.spiriteddriving.feature.profile.domain

import com.syntey.spiriteddriving.core.domain.error.OfflineException
import com.syntey.spiriteddriving.core.domain.feedback.Feedback
import com.syntey.spiriteddriving.core.domain.feedback.FakeFeedbackSender
import com.syntey.spiriteddriving.core.platform.binding.Platform
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class SendFeedbackUseCaseTest {

    @Test
    fun `sends the message trimmed and the app and phone it came from`() = runTest {
        val sender = FakeFeedbackSender()
        val useCase = SendFeedbackUseCase(sender, PhonePlatform)

        useCase.build(SendFeedbackUseCase.Args(message = "  The lake road is great.\n "))

        assertEquals(
            listOf(
                Feedback(
                    message = "The lake road is great.",
                    appVersion = "1.0.0",
                    appBuild = "42",
                    os = "iOS 26.1",
                    device = "iPhone 17 Pro",
                ),
            ),
            sender.sent,
        )
    }

    @Test
    fun `an error from the sender reaches the caller`() = runTest {
        val sender = FakeFeedbackSender().apply { failure = OfflineException(null) }
        val useCase = SendFeedbackUseCase(sender, PhonePlatform)

        assertFailsWith<OfflineException> { useCase.build(SendFeedbackUseCase.Args(message = "Hello")) }
    }

    private object PhonePlatform : Platform {
        override val appName = "Spirited Driving"
        override val appVersionName = "1.0.0"
        override val appBuildNumber = "42"
        override val applicationId = "app.id"
        override val osNameWithVersion = "iOS 26.1"
        override val deviceModel = "iPhone 17 Pro"
        override val storeReviewUrl = "https://store.test/app"
        override val preferredLanguages = listOf("en")
        override val regionCode: String? = null

        override fun countryName(code: String): String? = null
    }
}
