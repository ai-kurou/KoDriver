package kurou.kodriver.feature.debugstatedetail

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import kodriver.feature.debugstatedetail.generated.resources.Res
import kodriver.feature.debugstatedetail.generated.resources.debug_state_title
import kodriver.feature.debugstatedetail.generated.resources.navigate_back
import kurou.kodriver.core.designsystem.DetailPaneScaffold
import org.jetbrains.compose.resources.stringResource

@Composable
fun DebugStateDetailPane(
    canNavigateBack: Boolean,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    DetailPaneScaffold(
        title = stringResource(Res.string.debug_state_title),
        canNavigateBack = canNavigateBack,
        navigateBackContentDescription = stringResource(Res.string.navigate_back),
        onBack = onBack,
        modifier = modifier,
    ) {}
}

@Preview(showBackground = true)
@Composable
private fun DebugStateDetailPanePreview() {
    DebugStateDetailPane(
        canNavigateBack = true,
        onBack = {},
    )
}
