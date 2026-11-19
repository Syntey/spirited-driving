package com.syntey.spiriteddriving.feature.profile.ui.signInSheet

import app.futured.arkitekt.annotation.GenerateFactory
import app.futured.arkitekt.crusecases.execute
import co.touchlab.kermit.Logger
import com.syntey.spiriteddriving.core.platform.binding.SignInProvider
import com.syntey.spiriteddriving.core.platform.binding.SignInResult
import com.syntey.spiriteddriving.core.resources.*
import com.syntey.spiriteddriving.core.ui.base.AppComponentContext
import com.syntey.spiriteddriving.core.ui.base.ScreenComponent
import com.syntey.spiriteddriving.feature.profile.domain.SignInUseCase
import com.syntey.spiriteddriving.feature.profile.model.ui.SignInState
import com.syntey.spiriteddriving.feature.profile.model.ui.title
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import org.koin.core.annotation.Factory
import org.koin.core.annotation.InjectedParam

@Factory
@GenerateFactory
internal class SignInSheetComponent(
    @InjectedParam componentContext: AppComponentContext,
    @InjectedParam override val navigation: SignInSheetScreenNavigation,
    @InjectedParam private val args: SignInSheetArgs,
    private val signInUseCase: SignInUseCase,
) : ScreenComponent<SignInSheetViewState, Nothing, SignInSheetScreenNavigation>(
    componentContext = componentContext,
    defaultState = SignInSheetViewState(title = args.reason.title()),
),
    SignInSheetScreen,
    SignInSheetScreenNavigation by navigation,
    SignInSheetScreen.Actions {

    private val logger = Logger.withTag("SignInSheetComponent")

    override val viewState: StateFlow<SignInSheetViewState> = componentState
    override val actions: SignInSheetScreen.Actions = this

    override fun onGoogleClick() = signIn(SignInProvider.Google)
    override fun onClose() = finish(isSignedIn = false)

    // A second tap while one sign-in runs does nothing
    private fun signIn(provider: SignInProvider) {
        if (componentState.value.signIn.isSigningIn) return
        componentState.update { it.copy(signIn = SignInState.Running) }
        signInUseCase.execute(provider) {
            onSuccess { result ->
                componentState.update { it.copy(signIn = SignInState.after(result)) }
                if (result == SignInResult.SignedIn) finish(isSignedIn = true)
            }
            onError { error ->
                logger.e(error) { "Sign-in failed" }
                componentState.update { it.copy(signIn = SignInState.Failed) }
            }
        }
    }
}
