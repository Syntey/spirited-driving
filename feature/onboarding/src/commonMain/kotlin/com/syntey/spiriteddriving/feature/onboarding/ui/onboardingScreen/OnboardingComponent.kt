package com.syntey.spiriteddriving.feature.onboarding.ui.onboardingScreen

import com.syntey.spiriteddriving.feature.onboarding.model.ui.OnboardingStep
import app.futured.arkitekt.annotation.GenerateFactory
import app.futured.arkitekt.crusecases.execute
import co.touchlab.kermit.Logger
import com.arkivanov.essenty.backhandler.BackCallback
import com.syntey.spiriteddriving.core.ui.base.AppComponentContext
import com.syntey.spiriteddriving.core.ui.base.ScreenComponent
import com.syntey.spiriteddriving.feature.onboarding.domain.RequestLocationAccessUseCase
import com.syntey.spiriteddriving.feature.onboarding.domain.SaveVignetteUseCase
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import org.koin.core.annotation.Factory
import org.koin.core.annotation.InjectedParam

@Factory
@GenerateFactory
internal class OnboardingComponent(
    @InjectedParam componentContext: AppComponentContext,
    @InjectedParam override val navigation: OnboardingScreenNavigation,
    private val requestLocationAccessUseCase: RequestLocationAccessUseCase,
    private val saveVignetteUseCase: SaveVignetteUseCase,
) : ScreenComponent<OnboardingViewState, Nothing, OnboardingScreenNavigation>(
    componentContext = componentContext,
    defaultState = OnboardingViewState(),
),
    OnboardingScreen,
    OnboardingScreenNavigation by navigation,
    OnboardingScreen.Actions {

    private val logger = Logger.withTag("OnboardingComponent")

    // Back (Android) goes to the previous page. On the first page it's disabled, so back leaves the app.
    private val backCallback = BackCallback(isEnabled = false) { goTo(componentState.value.step.previous) }

    // While the system's location prompt shows or the answer is being saved, so a second tap doesn't ask or save twice.
    private var isWaiting = false

    override val viewState: StateFlow<OnboardingViewState> = componentState
    override val actions: OnboardingScreen.Actions = this

    init {
        backHandler.register(backCallback)
    }

    override fun onNext() = goTo(componentState.value.step.next)
    override fun onSkip() = goTo(OnboardingStep.Location)
    override fun onSwiped(step: OnboardingStep) = goTo(step)

    // On Location the prompt isn't answered yet, so swiping on asks, as Continue does.
    override fun onSwipedOn() {
        if (componentState.value.step == OnboardingStep.Location) requestLocationAccess()
    }

    override fun onContinue() = requestLocationAccess()
    override fun onVignetteAnswered(hasVignette: Boolean) = saveVignette(hasVignette)

    private fun goTo(step: OnboardingStep) {
        componentState.update { it.copy(step = step) }
        backCallback.isEnabled = step != OnboardingStep.entries.first()
    }

    private fun requestLocationAccess() {
        if (isWaiting) return
        requestLocationAccessUseCase.execute {
            onStart { isWaiting = true }
            onSuccess { access ->
                logger.d { "Location access: $access" }
                askAboutVignette()
            }
            // The app works without location, and the map asks again, so the vignette follows whatever happened.
            onError { error ->
                logger.e(error) { "Location prompt failed" }
                askAboutVignette()
            }
        }
    }

    private fun askAboutVignette() {
        isWaiting = false
        componentState.update { it.copy(lastSwipeable = OnboardingStep.Vignette) }
        goTo(OnboardingStep.Vignette)
    }

    private fun saveVignette(hasVignette: Boolean) {
        if (isWaiting) return
        saveVignetteUseCase.execute(SaveVignetteUseCase.Args(hasVignette)) {
            onStart { isWaiting = true }
            onSuccess { finish() }
            // Without the answer, First launch shows again on the next start. Until then the app is ready to use.
            onError { error ->
                logger.e(error) { "Vignette answer not saved" }
                finish()
            }
        }
    }
}
