package com.syntey.spiriteddriving.feature.profile.ui.accountScreen

import app.futured.arkitekt.annotation.GenerateFactory
import app.futured.arkitekt.crusecases.execute
import co.touchlab.kermit.Logger
import com.arkivanov.essenty.lifecycle.doOnCreate
import com.syntey.spiriteddriving.core.ui.base.AppComponentContext
import com.syntey.spiriteddriving.core.ui.base.ScreenComponent
import com.syntey.spiriteddriving.feature.profile.domain.ObserveAccountUseCase
import com.syntey.spiriteddriving.feature.profile.domain.SignOutUseCase
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import org.koin.core.annotation.Factory
import org.koin.core.annotation.InjectedParam

@Factory
@GenerateFactory
internal class AccountComponent(
    @InjectedParam componentContext: AppComponentContext,
    @InjectedParam override val navigation: AccountScreenNavigation,
    private val observeAccountUseCase: ObserveAccountUseCase,
    private val signOutUseCase: SignOutUseCase,
) : ScreenComponent<AccountViewState, Nothing, AccountScreenNavigation>(
    componentContext = componentContext,
    defaultState = AccountViewState(),
),
    AccountScreen,
    AccountScreenNavigation by navigation,
    AccountScreen.Actions {

    private val logger = Logger.withTag("AccountComponent")

    override val viewState: StateFlow<AccountViewState> = componentState
    override val actions: AccountScreen.Actions = this

    init {
        doOnCreate { observeAccount() }
    }

    override fun onSignOutClick() = signOut()

    // The observer pops the screen once the account is gone, so signing out here doesn't pop on its own
    private fun observeAccount() = observeAccountUseCase.execute {
        onNext { user ->
            if (user == null) {
                pop()
            } else {
                componentState.update { it.copy(name = user.name, email = user.email, provider = user.provider) }
            }
        }
        onError { error -> logger.e(error) { "Account not observed" } }
    }

    private fun signOut() = signOutUseCase.execute {
        onError { error -> logger.e(error) { "Sign-out failed" } }
    }
}
