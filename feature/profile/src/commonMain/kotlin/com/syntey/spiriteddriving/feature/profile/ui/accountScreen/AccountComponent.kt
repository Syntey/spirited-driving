package com.syntey.spiriteddriving.feature.profile.ui.accountScreen

import app.futured.arkitekt.annotation.GenerateFactory
import app.futured.arkitekt.crusecases.execute
import co.touchlab.kermit.Logger
import com.arkivanov.essenty.lifecycle.doOnCreate
import com.syntey.spiriteddriving.core.resources.*
import com.syntey.spiriteddriving.core.ui.base.AppComponentContext
import com.syntey.spiriteddriving.core.ui.base.ScreenComponent
import com.syntey.spiriteddriving.feature.profile.domain.DeleteAccountUseCase
import com.syntey.spiriteddriving.feature.profile.domain.ObserveAccountUseCase
import com.syntey.spiriteddriving.feature.profile.domain.SignOutUseCase
import dev.icerock.moko.resources.desc.StringDesc
import dev.icerock.moko.resources.desc.desc
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
    private val deleteAccountUseCase: DeleteAccountUseCase,
) : ScreenComponent<AccountViewState, AccountUiEvent, AccountScreenNavigation>(
    componentContext = componentContext,
    defaultState = AccountViewState(),
),
    AccountScreen,
    AccountScreenNavigation by navigation,
    AccountScreen.Actions {

    private val logger = Logger.withTag("AccountComponent")

    override val viewState: StateFlow<AccountViewState> = componentState
    override val title: StringDesc = MR.strings.account_screen_title.desc()
    override val actions: AccountScreen.Actions = this

    init {
        doOnCreate { observeAccount() }
    }

    override fun onBack() = pop()

    override fun onSignOutClick() {
        if (componentState.value.isDeleting) return
        signOut()
    }

    override fun onDeleteAccountClick() {
        if (componentState.value.isDeleting) return
        componentState.update { it.copy(isDeleteDialogShown = true) }
    }

    override fun onDeleteDialogDismiss() {
        componentState.update { it.copy(isDeleteDialogShown = false) }
    }

    override fun onDeleteConfirm() {
        if (componentState.value.isDeleting) return
        componentState.update { it.copy(isDeleteDialogShown = false, isDeleting = true) }
        deleteAccount()
    }

    // The observer pops the screen once the account is gone, so signing out here doesn't pop on its own. Deleting signs
    // out too, and then pops with the "Account deleted" message instead.
    private fun observeAccount() = observeAccountUseCase.execute {
        onNext { user ->
            if (user == null) {
                if (!componentState.value.isDeleting) pop()
            } else {
                componentState.update { it.copy(name = user.name, email = user.email, provider = user.provider) }
            }
        }
        onError { error -> logger.e(error) { "Account not observed" } }
    }

    private fun signOut() = signOutUseCase.execute {
        onError { error -> logger.e(error) { "Sign-out failed" } }
    }

    private fun deleteAccount() = deleteAccountUseCase.execute {
        // Stays in the deleting state: the screen is gone once it pops, so there's nothing to reset
        onSuccess { popAccountDeleted() }
        onError { error ->
            logger.e(error) { "Account deletion failed" }
            componentState.update { it.copy(isDeleting = false) }
            sendUiEvent(AccountUiEvent.DeleteFailed)
        }
    }
}
