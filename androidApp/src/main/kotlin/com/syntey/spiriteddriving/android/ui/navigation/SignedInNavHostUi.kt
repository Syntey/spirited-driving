package com.syntey.spiriteddriving.android.ui.navigation

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.syntey.spiriteddriving.feature.navigation.signedIn.NavigationTab
import com.syntey.spiriteddriving.feature.navigation.signedIn.SignedInChild
import com.syntey.spiriteddriving.feature.navigation.signedIn.SignedInConfig
import com.syntey.spiriteddriving.feature.navigation.signedIn.SignedInNavHost
import com.syntey.spiriteddriving.feature.navigation.signedIn.SignedInNavHostViewState
import com.syntey.spiriteddriving.resources.localized
import com.arkivanov.decompose.ExperimentalDecomposeApi
import com.arkivanov.decompose.extensions.compose.stack.Children
import com.arkivanov.decompose.extensions.compose.stack.animation.predictiveback.predictiveBackAnimation
import com.arkivanov.decompose.router.stack.ChildStack
import com.arkivanov.essenty.backhandler.BackHandler

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
                        icon = { Icon(tab.icon, contentDescription = null) },
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

private val NavigationTab.icon: ImageVector
    get() = when (this) {
        NavigationTab.HOME -> Icons.Filled.Home
        NavigationTab.PROFILE -> Icons.Filled.AccountCircle
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
