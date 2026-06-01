package kurou.kodriver.presentation.readout

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.layout.ListDetailPaneScaffold
import androidx.compose.material3.adaptive.layout.ListDetailPaneScaffoldRole
import androidx.compose.material3.adaptive.layout.PaneExpansionAnchor
import androidx.compose.material3.adaptive.layout.rememberPaneExpansionState
import androidx.compose.material3.adaptive.navigation.rememberListDetailPaneScaffoldNavigator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import kurou.kodriver.presentation.component.PlaceholderContent

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

@Composable
internal fun ReadoutListPane(onItemClick: () -> Unit) {
    LazyColumn(modifier = Modifier.fillMaxSize()) {
        item { LmuListItem(onClick = onItemClick) }
    }
}

@Composable
internal fun ReadoutDetailPane(
    canNavigateBack: Boolean,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxSize()) {
        if (canNavigateBack) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "戻る")
            }
        }
        PlaceholderContent(title = "detailPane", modifier = Modifier.weight(1f))
    }
}

@Preview
@Composable
fun ReadoutContentPreview() {
    ReadoutContent()
}

@Preview
@Composable
fun ReadoutListPanePreview() {
    MaterialTheme {
        ReadoutListPane(onItemClick = {})
    }
}

@Preview
@Composable
fun ReadoutDetailPanePreview() {
    MaterialTheme {
        ReadoutDetailPane(canNavigateBack = true, onBack = {})
    }
}
