package com.syntey.spiriteddriving.feature.ui.profileScreen

import app.futured.arkitekt.annotation.GenerateFactory
import app.futured.arkitekt.crusecases.execute
import com.syntey.spiriteddriving.feature.domain.SetUserLoggedInUseCase
import com.syntey.spiriteddriving.feature.ui.base.AppComponentContext
import com.syntey.spiriteddriving.feature.ui.base.ScreenComponent
import kotlinx.coroutines.flow.StateFlow
import org.koin.core.annotation.Factory
import org.koin.core.annotation.InjectedParam

@GenerateFactory
@Factory
internal class ProfileComponent(
    @InjectedParam componentContext: AppComponentContext,
    @InjectedParam override val navigation: ProfileScreenNavigation,
    private val setUserLoggedInUseCase: SetUserLoggedInUseCase,
) : ScreenComponent<ProfileViewState, Nothing, ProfileScreenNavigation>(
    componentContext,
    ProfileViewState,
),
    ProfileScreen,
    ProfileScreenNavigation by navigation,
    ProfileScreen.Actions {

    override val actions: ProfileScreen.Actions = this
    override val viewState: StateFlow<ProfileViewState> = componentState
    override fun onLogout() {
        setUserLoggedInUseCase.execute(SetUserLoggedInUseCase.Args(false)) {
            onSuccess {
                navigateToLogin()
            }
        }
    }
    override fun onThird() = navigateToThird("hello third from profile")
}
