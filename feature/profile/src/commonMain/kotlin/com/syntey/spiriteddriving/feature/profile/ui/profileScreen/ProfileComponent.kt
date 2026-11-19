package com.syntey.spiriteddriving.feature.profile.ui.profileScreen

import app.futured.arkitekt.annotation.GenerateFactory
import app.futured.arkitekt.crusecases.execute
import co.touchlab.kermit.Logger
import com.arkivanov.essenty.lifecycle.doOnCreate
import com.syntey.spiriteddriving.core.platform.binding.Platform
import com.syntey.spiriteddriving.core.platform.binding.SignInProvider
import com.syntey.spiriteddriving.core.resources.*
import com.syntey.spiriteddriving.core.ui.base.AppComponentContext
import com.syntey.spiriteddriving.core.ui.base.ScreenComponent
import com.syntey.spiriteddriving.feature.profile.domain.ObserveAccountUseCase
import com.syntey.spiriteddriving.feature.profile.domain.SignInUseCase
import com.syntey.spiriteddriving.feature.profile.model.ui.SignInState
import com.syntey.spiriteddriving.feature.profile.model.ui.toAccountRow
import dev.icerock.moko.resources.desc.StringDesc
import dev.icerock.moko.resources.desc.desc
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import org.koin.core.annotation.Factory
import org.koin.core.annotation.InjectedParam
import org.koin.core.annotation.Provided

@Factory
@GenerateFactory
internal class ProfileComponent(
    @InjectedParam componentContext: AppComponentContext,
    @InjectedParam override val navigation: ProfileScreenNavigation,
    @Provided private val platform: Platform,
    private val observeAccountUseCase: ObserveAccountUseCase,
    private val signInUseCase: SignInUseCase,
) : ScreenComponent<ProfileViewState, ProfileUiEvent, ProfileScreenNavigation>(
    componentContext = componentContext,
    defaultState = ProfileViewState(),
),
    ProfileScreen,
    ProfileScreenNavigation by navigation,
    ProfileScreen.Actions {

    private val logger = Logger.withTag("ProfileComponent")

    override val viewState: StateFlow<ProfileViewState> = componentState
    override val title: StringDesc = MR.strings.profile_screen_title.desc()
    override val actions: ProfileScreen.Actions = this

    init {
        doOnCreate { observeAccount() }
    }

    override fun onBack() = close()
    override fun onSettingsClick() = openSettings()
    override fun onRateClick() = sendUiEvent(ProfileUiEvent.OpenWebPage(platform.storeReviewUrl))
    override fun onSendFeedbackClick() = openFeedback()
    override fun onAboutClick() = openAbout()
    override fun onSignInWithGoogleClick() = signIn(SignInProvider.Google)
    override fun onAccountClick() = openAccount()
    override fun onTripHistoryClick() {
        if (componentState.value.account != null) openTripHistory() else openSignInForTripHistory()
    }
    override fun onAccountDeleted() = sendUiEvent(ProfileUiEvent.AccountDeleted)

    private fun observeAccount() = observeAccountUseCase.execute {
        onNext { user ->
            componentState.update { state ->
                if (user == null) {
                    state.copy(account = null)
                } else {
                    state.copy(account = user.toAccountRow(), signIn = SignInState())
                }
            }
        }
        onError { error -> logger.e(error) { "Account not observed" } }
    }

    private fun signIn(provider: SignInProvider) {
        // A second tap while one sign-in runs does nothing
        if (componentState.value.signIn.isSigningIn) return
        componentState.update { it.copy(signIn = SignInState.Running) }
        signInUseCase.execute(provider) {
            onSuccess { result ->
                componentState.update { it.copy(signIn = SignInState.after(result)) }
            }
            onError { error ->
                logger.e(error) { "Sign-in failed" }
                componentState.update { it.copy(signIn = SignInState.Failed) }
            }
        }
    }
}
