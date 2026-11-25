package com.syntey.spiriteddriving.feature.map.domain

import com.syntey.spiriteddriving.core.platform.binding.AuthListenerRegistration
import com.syntey.spiriteddriving.core.platform.binding.AuthUser
import com.syntey.spiriteddriving.core.platform.binding.AuthUserListener
import com.syntey.spiriteddriving.core.platform.binding.PlatformAuth
import com.syntey.spiriteddriving.core.platform.binding.SignInListener
import com.syntey.spiriteddriving.core.platform.binding.SignInProvider
import com.syntey.spiriteddriving.core.platform.binding.TokenListener

internal class FakePlatformAuth(private val initialUser: AuthUser?) : PlatformAuth {

    private var userListener: AuthUserListener? = null

    override fun currentUser(): AuthUser? = initialUser

    override fun addUserListener(listener: AuthUserListener): AuthListenerRegistration {
        userListener = listener
        listener.onUser(initialUser)
        return AuthListenerRegistration { userListener = null }
    }

    override fun signIn(provider: SignInProvider, listener: SignInListener) = Unit

    override fun signOut() = Unit

    override fun idToken(listener: TokenListener) = listener.onToken(null)

    fun emitUser(user: AuthUser?) {
        checkNotNull(userListener).onUser(user)
    }
}
