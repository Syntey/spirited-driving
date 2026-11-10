package com.syntey.spiriteddriving.feature.sample.loginScreen

import app.futured.arkitekt.annotation.GenerateFactory
import app.futured.arkitekt.crusecases.execute
import com.syntey.spiriteddriving.feature.sample.domain.SetUserLoggedInUseCase
import com.syntey.spiriteddriving.core.ui.base.AppComponentContext
import com.syntey.spiriteddriving.core.ui.base.ScreenComponent
import kotlinx.coroutines.flow.StateFlow
import org.koin.core.annotation.Factory
import org.koin.core.annotation.InjectedParam

@GenerateFactory
@Factory
internal class LoginComponent(
    @InjectedParam componentContext: AppComponentContext,
    @InjectedParam override val navigation: LoginScreenNavigation,
    private val setUserLoggedInUseCase: SetUserLoggedInUseCase,
) : ScreenComponent<LoginViewState, Nothing, LoginScreenNavigation>(
    componentContext = componentContext,
    defaultState = LoginViewState,
),
    LoginScreen,
    LoginScreenNavigation by navigation,
    LoginScreen.Actions {

    override val actions: LoginScreen.Actions = this
    override val viewState: StateFlow<LoginViewState> = componentState

    override fun onLoginClick() {
        setUserLoggedInUseCase.execute(SetUserLoggedInUseCase.Args(true)) {
            onSuccess {
                navigateToSignedIn()
            }
        }
    }
}
