package kurou.kodriver.feature.othervoicepitchdetail

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import kurou.kodriver.core.designsystem.DetailPaneDescription
import kurou.kodriver.core.designsystem.DetailPaneScaffold
import kurou.kodriver.feature.othervoicepitchdetail.generated.resources.Res
import kurou.kodriver.feature.othervoicepitchdetail.generated.resources.navigate_back
import kurou.kodriver.feature.othervoicepitchdetail.generated.resources.voice_pitch_description
import kurou.kodriver.feature.othervoicepitchdetail.generated.resources.voice_pitch_title
import org.jetbrains.compose.resources.stringResource

/**
 * 声の高さのタイトルと説明を表示する Composable。
 */
@Composable
fun OtherVoicePitchDetailPane(
    canNavigateBack: Boolean,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    DetailPaneScaffold(
        title = stringResource(Res.string.voice_pitch_title),
        canNavigateBack = canNavigateBack,
        navigateBackContentDescription = stringResource(Res.string.navigate_back),
        onBack = onBack,
        modifier = modifier,
    ) {
        DetailPaneDescription(
            text = stringResource(Res.string.voice_pitch_description),
        )
    }
}
