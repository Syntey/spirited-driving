package com.syntey.spiriteddriving.android.ui.navigation

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.arkivanov.decompose.ExperimentalDecomposeApi
import com.arkivanov.decompose.extensions.compose.stack.Children
import com.arkivanov.decompose.extensions.compose.stack.animation.predictiveback.predictiveBackAnimation
import com.arkivanov.decompose.router.stack.ChildStack
import com.arkivanov.essenty.backhandler.BackHandler
import com.syntey.spiriteddriving.core.resources.*
import com.syntey.spiriteddriving.core.ui.theme.Grid
import com.syntey.spiriteddriving.shared.navigation.signedIn.NavigationTab
import com.syntey.spiriteddriving.shared.navigation.signedIn.SignedInChild
import com.syntey.spiriteddriving.shared.navigation.signedIn.SignedInConfig
import com.syntey.spiriteddriving.shared.navigation.signedIn.SignedInNavHost
import com.syntey.spiriteddriving.shared.navigation.signedIn.SignedInNavHostViewState
import dev.icerock.moko.resources.ImageResource
import dev.icerock.moko.resources.compose.painterResource

@Composable
fun SignedInNavHostUi(
    navHost: SignedInNavHost,
    modifier: Modifier = Modifier,
) {
    val stack: ChildStack<SignedInConfig, SignedInChild> by navHost.stack.collectAsStateWithLifecycle()
    val viewState: SignedInNavHostViewState by navHost.viewState.collectAsStateWithLifecycle()
    val actions = navHost.actions

    Scaffold(
        modifier = modifier,
        bottomBar = {
            NavigationBar(modifier = Modifier.fillMaxWidth()) {
                for (tab in viewState.navigationTabs) {
                    NavigationBarItem(
                        selected = tab == viewState.selectedTab,
                        onClick = { actions.onTabSelected(tab) },
                        icon = {
                            Icon(
                                painter = painterResource(tab.icon),
                                contentDescription = null,
                                modifier = Modifier.size(Grid.d6),
                            )
                        },
                        label = { Text(text = tab.title.localized()) },
                    )
                }
            }
        },
        // Top inset is left to the top bar of each tab's navigation host
        contentWindowInsets = WindowInsets(0),
    ) { paddingValues ->
        TabsContent(
            stack = stack,
            backHandler = navHost.backHandler,
            onBack = actions::onBack,
            modifier = Modifier
                .padding(paddingValues)
                .consumeWindowInsets(paddingValues),
        )
    }
}

private val NavigationTab.icon: ImageResource
    get() = when (this) {
        NavigationTab.HOME -> MR.images.home
        NavigationTab.PROFILE -> MR.images.account
    }

@OptIn(ExperimentalDecomposeApi::class)
@Composable
private fun TabsContent(
    stack: ChildStack<SignedInConfig, SignedInChild>,
    backHandler: BackHandler,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Children(
        stack = stack,
        modifier = modifier,
        animation = predictiveBackAnimation(
            backHandler = backHandler,
            onBack = onBack,
        ),
    ) { child ->
        when (val childInstance = child.instance) {
            is SignedInChild.Home -> HomeNavHostUi(
                navHost = childInstance.navHost,
                modifier = Modifier.fillMaxSize(),
            )

            is SignedInChild.Profile -> ProfileNavHostUi(
                navHost = childInstance.navHost,
                modifier = Modifier.fillMaxSize(),
            )
        }
    }
}
