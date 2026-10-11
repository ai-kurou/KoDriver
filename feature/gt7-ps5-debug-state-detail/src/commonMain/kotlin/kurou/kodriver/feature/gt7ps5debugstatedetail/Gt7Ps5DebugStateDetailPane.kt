package kurou.kodriver.feature.gt7ps5debugstatedetail

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import kurou.kodriver.core.designsystem.DetailPaneScaffold
import kurou.kodriver.core.designsystem.KoDriverTheme
import kurou.kodriver.feature.gt7ps5debugstatedetail.generated.resources.Res
import kurou.kodriver.feature.gt7ps5debugstatedetail.generated.resources.debug_state_title
import kurou.kodriver.feature.gt7ps5debugstatedetail.generated.resources.navigate_back
import org.jetbrains.compose.resources.stringResource

@Composable
fun Gt7Ps5DebugStateDetailPane(
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
private fun Gt7Ps5DebugStateDetailPanePreview() {
    KoDriverTheme {
        Gt7Ps5DebugStateDetailPane(
            canNavigateBack = true,
            onBack = {},
        )
    }
}
