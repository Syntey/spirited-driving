package com.syntey.spiriteddriving.shared.navigation.profile

import app.futured.arkitekt.decompose.ext.asStateFlow
import com.arkivanov.decompose.Child
import com.arkivanov.decompose.router.stack.ChildStack
import com.arkivanov.decompose.router.stack.childStack
import com.arkivanov.decompose.router.stack.navigate
import com.arkivanov.decompose.router.stack.pop
import com.syntey.spiriteddriving.core.ui.base.AppComponent
import com.syntey.spiriteddriving.core.ui.base.AppComponentContext
import com.syntey.spiriteddriving.feature.profile.ui.ProfileScreens
import kotlinx.coroutines.flow.StateFlow
import org.koin.core.annotation.Factory
import org.koin.core.annotation.InjectedParam

@Factory
internal class ProfileNavHostComponent(
    @InjectedParam componentContext: AppComponentContext,
    @InjectedParam private val onClosed: () -> Unit,
) : AppComponent<ProfileNavHostViewState, Nothing>(componentContext, ProfileNavHostViewState),
    ProfileNavHost {

    private val navigator: ProfileNavHostNavigation = ProfileNavHostNavigator()

    override val stack: StateFlow<ChildStack<ProfileConfig, ProfileChild>> = childStack(
        source = navigator.stackNavigator,
        serializer = ProfileConfig.serializer(),
        initialConfiguration = ProfileConfig.Profile,
        handleBackButton = true,
        childFactory = { config, childCtx ->
            when (config) {
                ProfileConfig.Profile -> ProfileChild.Profile(ProfileScreens.profile(childCtx, navigator))

                ProfileConfig.Settings -> ProfileChild.Settings(ProfileScreens.settings(childCtx, navigator))

                ProfileConfig.Feedback -> ProfileChild.Feedback(ProfileScreens.feedback(childCtx, navigator))

                ProfileConfig.About -> ProfileChild.About(ProfileScreens.about(childCtx, navigator))

                ProfileConfig.Licences -> ProfileChild.Licences(ProfileScreens.licences(childCtx, navigator))

                is ProfileConfig.LicenceText ->
                    ProfileChild.LicenceText(ProfileScreens.licenceText(childCtx, navigator, config.args))
            }
        },
    ).asStateFlow()

    override fun onBack() {
        if (stack.value.items.size > 1) {
            navigator.stackNavigator.pop()
        } else {
            onClosed()
        }
    }

    override fun onClose() = onClosed()

    override fun iosPopTo(newStack: List<Child<ProfileConfig, ProfileChild>>) {
        navigator.stackNavigator.navigate { _ -> newStack.map { child -> child.configuration } }
    }
}
