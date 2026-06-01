package kurou.kodriver.presentation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.HeadsetMic
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import kurou.kodriver.presentation.component.PlaceholderContent
import kurou.kodriver.presentation.readout.ReadoutContent

enum class AppDestination(
    val label: String,
    val icon: ImageVector,
) {
    Readout("読み上げ", Icons.Default.HeadsetMic),
    More("その他", Icons.Default.MoreHoriz),
}

@Composable
fun AppScreen(
    readoutContent: @Composable () -> Unit = { ReadoutContent() },
) {
    var currentDestination by rememberSaveable { mutableStateOf(AppDestination.Readout) }

    MaterialTheme {
        Box(modifier = Modifier.navigationBarsPadding()) {
            NavigationSuiteScaffold(
                navigationSuiteItems = {
                    AppDestination.entries.forEach { dest ->
                        item(
                            icon = { Icon(dest.icon, contentDescription = dest.label) },
                            label = { Text(dest.label) },
                            selected = currentDestination == dest,
                            onClick = { currentDestination = dest },
                        )
                    }
                },
            ) {
                Box(modifier = Modifier.statusBarsPadding()) {
                    when (currentDestination) {
                        AppDestination.Readout -> readoutContent()
                        AppDestination.More -> PlaceholderContent("その他")
                    }
                }
            }
        }
    }
}
