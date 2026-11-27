package kurou.kodriver.feature.otherwakewordhotkeydetail

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import kurou.kodriver.core.designsystem.DetailPaneDescription
import kurou.kodriver.core.designsystem.DetailPaneScaffold
import kurou.kodriver.core.designsystem.KoDriverTheme
import kurou.kodriver.feature.otherwakewordhotkeydetail.generated.resources.Res
import kurou.kodriver.feature.otherwakewordhotkeydetail.generated.resources.wake_word_hotkey_description
import kurou.kodriver.feature.otherwakewordhotkeydetail.generated.resources.wake_word_hotkey_title
import org.jetbrains.compose.resources.stringResource

/**
 * OtherWakeWordHotkeyDetail の画面を表示する Composable。
 */
@Composable
public fun OtherWakeWordHotkeyDetailPane(
    canNavigateBack: Boolean,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    DetailPaneScaffold(
        title = stringResource(Res.string.wake_word_hotkey_title),
        canNavigateBack = canNavigateBack,
        onBack = onBack,
        modifier = modifier,
    ) {
        DetailPaneDescription(text = stringResource(Res.string.wake_word_hotkey_description))
    }
}

@Preview(showBackground = true)
@Composable
private fun OtherWakeWordHotkeyDetailPanePreview() {
    KoDriverTheme {
        OtherWakeWordHotkeyDetailPane(canNavigateBack = true, onBack = {})
    }
}
