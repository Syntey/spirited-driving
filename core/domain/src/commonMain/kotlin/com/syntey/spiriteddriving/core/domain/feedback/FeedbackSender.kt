package com.syntey.spiriteddriving.core.domain.feedback

import com.syntey.spiriteddriving.core.domain.error.OfflineException

/**
 * Sends the user's feedback to the backend. It's anonymous: the backend never links it to an account, so there's nothing to
 * reply to. The app and phone it came from go along in the User-Agent header of every call. Throws [OfflineException] without a
 * connection.
 */
interface FeedbackSender {

    /**
     * @param message What the user wrote: 1 to 2,000 characters after trimming.
     */
    suspend fun send(message: String)
}
