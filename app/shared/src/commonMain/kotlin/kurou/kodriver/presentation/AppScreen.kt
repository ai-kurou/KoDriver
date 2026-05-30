package kurou.kodriver.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.HeadsetMic
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.PictureInPicture
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.sp
import kotlin.math.roundToInt

enum class AppDestination(
    val label: String,
    val icon: ImageVector,
) {
    Dashboard("読み上げ", Icons.Default.HeadsetMic),
    Overlay("オーバーレイ", Icons.Default.PictureInPicture),
    Settings("その他", Icons.Default.MoreHoriz),
}

@Composable
fun AppScreen(
    dashboardContent: @Composable () -> Unit = { PlaceholderContent("読み上げ") },
) {
    var currentDestination by rememberSaveable { mutableStateOf(AppDestination.Dashboard) }

    MaterialTheme {
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
            }
        ) {
            when (currentDestination) {
                AppDestination.Dashboard -> dashboardContent()
                AppDestination.Overlay -> PlaceholderContent("オーバーレイ")
                AppDestination.Settings -> PlaceholderContent("その他")
            }
        }
    }
}

@Composable
fun DashboardContent(uiState: TelemetryUiState, onReconnect: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center,
    ) {
        when (uiState) {
            is TelemetryUiState.Connecting -> Text("接続中...", fontSize = 24.sp)
            is TelemetryUiState.Error -> Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("エラー: ${uiState.message}", fontSize = 18.sp, color = MaterialTheme.colorScheme.error)
                Button(onClick = onReconnect) { Text("再接続") }
            }
            is TelemetryUiState.Connected -> Text(
                text = "${uiState.data.vehicle.speedKmh.roundToInt()} km/h",
                fontSize = 96.sp,
                color = MaterialTheme.colorScheme.onBackground,
            )
        }
    }
}

@Composable
private fun PlaceholderContent(title: String) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center,
    ) {
        Text(title, fontSize = 24.sp, color = MaterialTheme.colorScheme.onBackground)
    }
}
