package com.syntey.spiriteddriving.data.persistence.persistence.user

import androidx.datastore.preferences.core.booleanPreferencesKey
import com.syntey.spiriteddriving.core.domain.sample.UserSession
import com.syntey.spiriteddriving.data.persistence.persistence.PrimitivePersistence
import org.koin.core.annotation.Single

@Single
internal class UserPersistence(private val primitivePersistence: PrimitivePersistence) : UserSession {

    private companion object {
        val IS_USER_LOGGED_IN_KEY = booleanPreferencesKey("IS_USER_LOGGED_IN")
    }

    override suspend fun isUserLoggedIn(): Boolean = primitivePersistence.get(IS_USER_LOGGED_IN_KEY) ?: false

    override suspend fun setUserLoggedIn(isLoggedIn: Boolean) {
        primitivePersistence.save(IS_USER_LOGGED_IN_KEY, isLoggedIn)
    }
}
