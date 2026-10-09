package kurou.kodriver.feature.othervoicespeeddetail

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import kurou.kodriver.core.designsystem.DetailPaneDescription
import kurou.kodriver.core.designsystem.DetailPaneScaffold
import kurou.kodriver.core.designsystem.KoDriverTheme
import kurou.kodriver.feature.othervoicespeeddetail.generated.resources.Res
import kurou.kodriver.feature.othervoicespeeddetail.generated.resources.navigate_back
import kurou.kodriver.feature.othervoicespeeddetail.generated.resources.voice_speed_description
import kurou.kodriver.feature.othervoicespeeddetail.generated.resources.voice_speed_title
import org.jetbrains.compose.resources.stringResource

/**
 * 読み上げ速度の詳細画面を表示する Composable。
 */
@Composable
fun OtherVoiceSpeedDetailPane(
    canNavigateBack: Boolean,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    DetailPaneScaffold(
        title = stringResource(Res.string.voice_speed_title),
        canNavigateBack = canNavigateBack,
        navigateBackContentDescription = stringResource(Res.string.navigate_back),
        onBack = onBack,
        modifier = modifier,
    ) {
        DetailPaneDescription(text = stringResource(Res.string.voice_speed_description))
    }
}

@Preview(showBackground = true)
@Composable
private fun OtherVoiceSpeedDetailPanePreview() {
    KoDriverTheme {
        OtherVoiceSpeedDetailPane(canNavigateBack = true, onBack = {})
    }
}
