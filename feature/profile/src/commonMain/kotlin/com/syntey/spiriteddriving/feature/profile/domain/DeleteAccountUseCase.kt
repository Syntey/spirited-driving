package com.syntey.spiriteddriving.feature.profile.domain

import app.futured.arkitekt.crusecases.UseCase
import com.syntey.spiriteddriving.core.domain.account.AccountDeleter
import com.syntey.spiriteddriving.core.platform.binding.Auth
import org.koin.core.annotation.Factory
import org.koin.core.annotation.Provided

/**
 * Deletes the account on the backend, then signs out, so the device keeps its settings and recent places as after signing
 * out. Throws what the deleter throws, and then stays signed in.
 */
@Factory
internal class DeleteAccountUseCase(private val accountDeleter: AccountDeleter, @Provided private val auth: Auth) : UseCase<Unit, Unit> {

    override suspend fun build(args: Unit) {
        accountDeleter.delete()
        auth.signOut()
    }
}
