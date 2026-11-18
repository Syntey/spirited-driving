package com.syntey.spiriteddriving.shared.navigation.root

import app.futured.arkitekt.crusecases.execute
import app.futured.arkitekt.decompose.ext.asStateFlow
import co.touchlab.kermit.Logger
import com.syntey.spiriteddriving.feature.routes.ui.RoutesScreens
import com.syntey.spiriteddriving.shared.domain.ClaimDrivesOnSignInUseCase
import com.syntey.spiriteddriving.shared.domain.SyncAccountDataOnAccountChangeUseCase
import com.syntey.spiriteddriving.shared.domain.IsOnboardingDoneUseCase
import com.syntey.spiriteddriving.core.ui.base.AppComponent
import com.syntey.spiriteddriving.core.ui.base.AppComponentContext
import com.syntey.spiriteddriving.core.ui.base.AppComponentFactory
import com.syntey.spiriteddriving.feature.map.ui.MapScreens
import com.syntey.spiriteddriving.feature.onboarding.ui.OnboardingScreens
import com.syntey.spiriteddriving.feature.profile.model.SignInReason
import com.syntey.spiriteddriving.feature.profile.ui.ProfileScreens
import com.syntey.spiriteddriving.shared.navigation.profile.ProfileNavHostComponent
import com.syntey.spiriteddriving.shared.navigation.profile.ProfileResult
import com.arkivanov.decompose.router.slot.ChildSlot
import com.arkivanov.decompose.router.slot.activate
import com.arkivanov.decompose.router.slot.childSlot
import com.arkivanov.decompose.router.slot.dismiss
import com.arkivanov.essenty.lifecycle.doOnCreate
import kotlinx.coroutines.flow.StateFlow
import org.koin.core.annotation.Factory
import org.koin.core.annotation.InjectedParam

@Factory
internal class RootNavHostComponent(
    @InjectedParam componentContext: AppComponentContext,
    private val isOnboardingDoneUseCase: IsOnboardingDoneUseCase,
    private val claimDrivesOnSignInUseCase: ClaimDrivesOnSignInUseCase,
    private val syncAccountDataOnAccountChangeUseCase: SyncAccountDataOnAccountChangeUseCase,
) : AppComponent<RootNavHostViewState, Nothing>(componentContext, RootNavHostViewState),
    RootNavHost {

    private val logger = Logger.withTag("RootNavHostComponent")
    private val rootNavigator: RootNavHostNavigation = RootNavHostNavigator(::onSignInFinished, ::onSavedRouteChosen)

    override val slot: StateFlow<ChildSlot<RootConfig, RootChild>> = childSlot(
        source = rootNavigator.slotNavigator,
        serializer = RootConfig.serializer(),
        key = "slot",
        initialConfiguration = { null },
        handleBackButton = false,
        childFactory = { config, childCtx ->
            when (config) {
                RootConfig.Onboarding -> RootChild.Onboarding(OnboardingScreens.onboarding(childCtx, rootNavigator))
                RootConfig.Map -> RootChild.Map(MapScreens.tripPlan(childCtx, rootNavigator))
            }
        },
    ).asStateFlow()

    override val sheet: StateFlow<ChildSlot<RootSheetConfig, RootSheetChild>> = childSlot(
        source = rootNavigator.sheetNavigator,
        serializer = RootSheetConfig.serializer(),
        key = "sheet",
        initialConfiguration = { null },
        handleBackButton = true,
        childFactory = { config, childCtx ->
            when (config) {
                RootSheetConfig.Profile -> RootSheetChild.Profile(
                    AppComponentFactory.createAppComponent<ProfileNavHostComponent>(childCtx, ::onProfileFinished),
                )

                RootSheetConfig.SavedRoutes -> RootSheetChild.SavedRoutes(
                    RoutesScreens.savedRoutes(childCtx, rootNavigator),
                )

                is RootSheetConfig.SignIn -> RootSheetChild.SignIn(
                    ProfileScreens.signInSheet(childCtx, rootNavigator, config.args),
                )
            }
        },
    ).asStateFlow()

    init {
        doOnCreate {
            checkOnboardingDone()
            claimDrivesOnSignIn()
            syncAccountDataOnAccountChange()
        }
    }

    private fun onSignInFinished(reason: SignInReason, isSignedIn: Boolean) {
        when (reason) {
            SignInReason.SaveRoute -> (slot.value.child?.instance as? RootChild.Map)?.screen?.onSignInToSaveFinished(isSignedIn)

            // Saved routes opens once signed in, as the row asked for it; signed out, the sign-in sheet is all that's shown
            SignInReason.SavedRoutes -> if (isSignedIn) rootNavigator.sheetNavigator.activate(RootSheetConfig.SavedRoutes)

            // Trip history's sign-in sheet opens over Profile, which finishes it itself
            SignInReason.TripHistory -> Unit
        }
    }

    // Profile closes at once; a drive chosen in Trip history or planned from it then shows on the map
    private fun onProfileFinished(result: ProfileResult) {
        rootNavigator.sheetNavigator.dismiss()
        val map = (slot.value.child?.instance as? RootChild.Map)?.screen
        when (result) {
            ProfileResult.Closed -> Unit
            is ProfileResult.ShowPastDrive -> map?.showPastDrive(result.driveId)
            is ProfileResult.PlanNewRoutes -> map?.planNewRoutes(result.driveId)
        }
    }

    private fun onSavedRouteChosen(savedRouteId: String) {
        (slot.value.child?.instance as? RootChild.Map)?.screen?.showSavedRoute(savedRouteId)
    }

    private fun checkOnboardingDone() {
        isOnboardingDoneUseCase.execute {
            onSuccess { isDone ->
                if (isDone) {
                    rootNavigator.slotNavigator.activate(RootConfig.Map)
                } else {
                    rootNavigator.slotNavigator.activate(RootConfig.Onboarding)
                }
            }
            onError { error ->
                logger.e(error) { "First launch state not read" }
                rootNavigator.slotNavigator.activate(RootConfig.Map)
            }
        }
    }

    private fun claimDrivesOnSignIn() {
        claimDrivesOnSignInUseCase.execute {
            onError { error -> logger.e(error) { "Drives not claimed on sign-in" } }
        }
    }

    private fun syncAccountDataOnAccountChange() {
        syncAccountDataOnAccountChangeUseCase.execute {
            onError { error -> logger.e(error) { "Account data not cleared on sign-out" } }
        }
    }
}
