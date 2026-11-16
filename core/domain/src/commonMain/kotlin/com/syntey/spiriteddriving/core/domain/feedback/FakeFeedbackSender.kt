package com.syntey.spiriteddriving.core.domain.feedback

/**
 * [FeedbackSender] that records the messages it got. Throws [failure] instead of recording, when it's set.
 */
class FakeFeedbackSender : FeedbackSender {

    var failure: Throwable? = null

    val sent: List<String>
        field = mutableListOf<String>()

    override suspend fun send(message: String) {
        failure?.let { throw it }
        sent += message
    }
}
