package com.syntey.spiriteddriving.data.network.feedback

import com.syntey.spiriteddriving.core.domain.feedback.Feedback
import com.syntey.spiriteddriving.data.network.dto.feedback.FeedbackRequestDto
import com.syntey.spiriteddriving.data.network.injection.NetworkModule
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class FeedbackMappingTest {

    @Test
    fun `every field of the feedback maps to its request field`() {
        val dto = feedback().toDto()

        assertEquals(
            FeedbackRequestDto(
                message = "The map froze on the Karlštejn route",
                appVersion = "1.0.0",
                appBuild = "42",
                os = "iOS 26.1",
                device = "iPhone 17 Pro",
            ),
            dto,
        )
    }

    @Test
    fun `request is encoded with the field names of the API`() {
        val json = NetworkModule().restApiJson().encodeToString(FeedbackRequestDto.serializer(), feedback().toDto())

        assertTrue(
            json.contains(
                "\"message\":\"The map froze on the Karlštejn route\"," +
                    "\"appVersion\":\"1.0.0\",\"appBuild\":\"42\",\"os\":\"iOS 26.1\",\"device\":\"iPhone 17 Pro\"",
            ),
            json,
        )
    }

    private fun feedback() = Feedback(
        message = "The map froze on the Karlštejn route",
        appVersion = "1.0.0",
        appBuild = "42",
        os = "iOS 26.1",
        device = "iPhone 17 Pro",
    )
}
