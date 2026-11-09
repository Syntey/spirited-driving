package com.syntey.spiriteddriving.core.domain.sample

/**
 * The template's sign-in, a flag kept on the device. Used by `:feature:sample` and the root navigation host.
 */
interface UserSession {
    suspend fun isUserLoggedIn(): Boolean

    suspend fun setUserLoggedIn(isLoggedIn: Boolean)
}
