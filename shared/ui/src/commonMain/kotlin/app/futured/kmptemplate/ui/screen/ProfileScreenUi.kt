package app.futured.kmptemplate.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import app.futured.kmptemplate.feature.ui.profileScreen.ProfileScreen
import app.futured.kmptemplate.resources.*
import dev.icerock.moko.resources.compose.stringResource

@Composable
fun ProfileScreenUi(
    screen: ProfileScreen,
    modifier: Modifier = Modifier,
) {
    Content(modifier = modifier, actions = screen.actions)
}

@Composable
private fun Content(
    actions: ProfileScreen.Actions,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Button(onClick = actions::onLogout) {
            Text(stringResource(MR.strings.generic_sign_out))
        }
        Spacer(modifier = Modifier.height(8.dp))
        Button(onClick = actions::onThird) {
            Text(stringResource(MR.strings.profile_navigate_to_third))
        }
    }
}
