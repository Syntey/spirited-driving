package com.syntey.spiriteddriving.core.domain.feedback

import com.syntey.spiriteddriving.core.domain.error.OfflineException

/**
 * Sends the user's feedback to the backend. It's anonymous: the backend never links it to an account, so there's nothing to
 * reply to. Throws [OfflineException] without a connection.
 */
interface FeedbackSender {

    suspend fun send(feedback: Feedback)
}

/**
 * What the user wrote, with what the app sends along to help read it.
 *
 * @property message What the user wrote: 1 to 2,000 characters after trimming.
 * @property appVersion The app's version name, such as "1.0.0".
 * @property appBuild The app's build number, such as "42".
 * @property os The operating system and its version, such as "iOS 26.1" or "Android 16".
 * @property device The device's model, such as "iPhone 17 Pro" or "Pixel 9".
 */
data class Feedback(val message: String, val appVersion: String, val appBuild: String, val os: String, val device: String)
