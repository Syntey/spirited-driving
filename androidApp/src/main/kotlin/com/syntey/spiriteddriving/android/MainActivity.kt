package com.syntey.spiriteddriving.android

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.syntey.spiriteddriving.android.ui.navigation.RootNavHostUi
import com.syntey.spiriteddriving.shared.navigation.root.RootNavHost
import com.syntey.spiriteddriving.shared.navigation.root.RootNavHostFactory
import com.syntey.spiriteddriving.core.ui.base.DefaultAppComponentContext
import com.syntey.spiriteddriving.core.ui.theme.AppTheme
import com.arkivanov.decompose.retainedComponent

class MainActivity : ComponentActivity() {

    private lateinit var rootNavHost: RootNavHost

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        rootNavHost = retainedComponent { retainedContext ->
            RootNavHostFactory.create(DefaultAppComponentContext(retainedContext))
        }

        enableEdgeToEdge()
        setContent {
            AppTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background,
                ) {
                    RootNavHostUi(navHost = rootNavHost)
                }
            }
        }
    }
}

@Preview
@Composable
private fun DefaultPreview() {
    AppTheme {}
}
