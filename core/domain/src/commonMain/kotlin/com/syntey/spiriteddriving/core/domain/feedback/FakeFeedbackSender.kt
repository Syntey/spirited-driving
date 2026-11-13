package com.syntey.spiriteddriving.core.domain.feedback

/**
 * [FeedbackSender] that records the feedback it got. Throws [failure] instead of recording, when it's set.
 */
class FakeFeedbackSender : FeedbackSender {

    var failure: Throwable? = null

    val sent: List<Feedback>
        field = mutableListOf<Feedback>()

    override suspend fun send(feedback: Feedback) {
        failure?.let { throw it }
        sent += feedback
    }
}
