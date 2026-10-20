package kurou.kodriver.feature.otherandroidaudiousagedetail

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import kurou.kodriver.core.designsystem.DetailPaneDescription
import kurou.kodriver.core.designsystem.DetailPaneScaffold
import kurou.kodriver.feature.otherandroidaudiousagedetail.generated.resources.Res
import kurou.kodriver.feature.otherandroidaudiousagedetail.generated.resources.audio_usage_description
import kurou.kodriver.feature.otherandroidaudiousagedetail.generated.resources.audio_usage_title
import org.jetbrains.compose.resources.stringResource

/** Android の音声用途に関する説明を表示する詳細ペイン。 */
@Composable
fun OtherAndroidAudioUsageDetailPane(
    canNavigateBack: Boolean,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    DetailPaneScaffold(
        title = stringResource(Res.string.audio_usage_title),
        canNavigateBack = canNavigateBack,
        onBack = onBack,
        modifier = modifier,
    ) {
        DetailPaneDescription(text = stringResource(Res.string.audio_usage_description))
    }
}
