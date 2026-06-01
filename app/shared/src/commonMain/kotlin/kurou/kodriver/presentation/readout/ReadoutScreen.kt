package kurou.kodriver.presentation.readout

import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.material3.adaptive.layout.ListDetailPaneScaffold
import androidx.compose.material3.adaptive.layout.ListDetailPaneScaffoldRole
import androidx.compose.material3.adaptive.layout.PaneExpansionAnchor
import androidx.compose.material3.adaptive.layout.PaneScaffoldDirective
import androidx.compose.material3.adaptive.layout.calculatePaneScaffoldDirective
import androidx.compose.material3.adaptive.layout.rememberPaneExpansionState
import androidx.compose.material3.adaptive.navigation.rememberListDetailPaneScaffoldNavigator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3AdaptiveApi::class)
@Composable
fun ReadoutContent(
    modifier: Modifier = Modifier,
    scaffoldDirective: PaneScaffoldDirective = calculatePaneScaffoldDirective(currentWindowAdaptiveInfo()),
    backHandler: @Composable (Boolean, () -> Unit) -> Unit = { _, _ -> },
) {
    val navigator = rememberListDetailPaneScaffoldNavigator(scaffoldDirective = scaffoldDirective)
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
        modifier = modifier,
        listPane = {
            ReadoutListPane(onItemClick = {
                scope.launch { navigator.navigateTo(ListDetailPaneScaffoldRole.Detail) }
            })
        },
        detailPane = {
            ReadoutDetailPane(
                canNavigateBack = navigator.canNavigateBack(),
                onBack = { navigateBack() },
            )
        },
    )
}

@Preview
@Composable
fun ReadoutContentPreview() {
    ReadoutContent()
}
