package kurou.kodriver.presentation

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.HeadsetMic
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.layout.ListDetailPaneScaffold
import androidx.compose.material3.adaptive.layout.ListDetailPaneScaffoldRole
import androidx.compose.material3.adaptive.layout.PaneExpansionAnchor
import androidx.compose.material3.adaptive.layout.rememberPaneExpansionState
import androidx.compose.material3.adaptive.navigation.rememberListDetailPaneScaffoldNavigator
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kodriver.app.shared.generated.resources.Res
import kodriver.app.shared.generated.resources.lmu
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.painterResource

enum class AppDestination(
    val label: String,
    val icon: ImageVector,
) {
    Readout("読み上げ", Icons.Default.HeadsetMic),
    More("その他", Icons.Default.MoreHoriz),
}

@Composable
fun AppScreen(
    readoutContent: @Composable () -> Unit = { PlaceholderContent("読み上げ") },
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

@OptIn(ExperimentalMaterial3AdaptiveApi::class)
@Composable
fun ReadoutContent(
    backHandler: @Composable (Boolean, () -> Unit) -> Unit = { _, _ -> },
) {
    val navigator = rememberListDetailPaneScaffoldNavigator()
    val scope = rememberCoroutineScope()
    val navigateBack = { scope.launch { navigator.navigateBack() } }
    val paneExpansionState = rememberPaneExpansionState(
        anchors = listOf(PaneExpansionAnchor.Offset.fromStart(300.dp)),
        initialAnchoredIndex = 0,
    )

    backHandler(navigator.canNavigateBack()) { navigateBack() }

    ListDetailPaneScaffold(
        directive = navigator.scaffoldDirective,
        scaffoldState = navigator.scaffoldState,
        paneExpansionState = paneExpansionState,
        listPane = {
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                item {
                    ListItem(
                        headlineContent = { Text("Le Mans Ultimate") },
                        leadingContent = {
                            Image(
                                painter = painterResource(Res.drawable.lmu),
                                contentDescription = "Le Mans Ultimate",
                                modifier = Modifier.size(40.dp),
                            )
                        },
                        modifier = Modifier.clickable {
                            scope.launch {
                                navigator.navigateTo(ListDetailPaneScaffoldRole.Detail)
                            }
                        },
                    )
                }
            }
        },
        detailPane = {
            Column(modifier = Modifier.fillMaxSize()) {
                if (navigator.canNavigateBack()) {
                    IconButton(onClick = { navigateBack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "戻る")
                    }
                }
                PlaceholderContent(title = "detailPane", modifier = Modifier.weight(1f))
            }
        },
    )
}

@Composable
private fun PlaceholderContent(title: String, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center,
    ) {
        Text(title, fontSize = 24.sp, color = MaterialTheme.colorScheme.onBackground)
    }
}
