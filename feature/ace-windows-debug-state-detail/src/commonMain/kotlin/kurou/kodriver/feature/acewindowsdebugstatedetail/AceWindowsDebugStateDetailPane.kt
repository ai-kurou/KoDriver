package kurou.kodriver.feature.acewindowsdebugstatedetail

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import kurou.kodriver.core.designsystem.DetailPaneScaffold
import kurou.kodriver.core.designsystem.KoDriverTheme
import kurou.kodriver.feature.acewindowsdebugstatedetail.generated.resources.Res
import kurou.kodriver.feature.acewindowsdebugstatedetail.generated.resources.debug_state_title
import kurou.kodriver.feature.acewindowsdebugstatedetail.generated.resources.navigate_back
import org.jetbrains.compose.resources.stringResource

@Composable
fun AceWindowsDebugStateDetailPane(
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
private fun AceWindowsDebugStateDetailPanePreview() {
    KoDriverTheme {
        AceWindowsDebugStateDetailPane(
            canNavigateBack = true,
            onBack = {},
        )
    }
}
