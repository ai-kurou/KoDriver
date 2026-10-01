package kurou.kodriver.feature.othervoicedetail

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kurou.kodriver.core.designsystem.DetailPaneScaffold
import kurou.kodriver.core.designsystem.KoDriverTheme
import kurou.kodriver.feature.othervoicedetail.generated.resources.Res
import kurou.kodriver.feature.othervoicedetail.generated.resources.navigate_back
import kurou.kodriver.feature.othervoicedetail.generated.resources.voice_title
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

/**
 * OtherVoiceDetail の画面を表示する Composable。
 */
@Composable
fun OtherVoiceDetailPane(
    canNavigateBack: Boolean,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val viewModel: OtherVoiceDetailViewModel = koinViewModel()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    OtherVoiceDetailPaneContent(
        uiState = uiState,
        canNavigateBack = canNavigateBack,
        onBack = onBack,
        modifier = modifier,
    )
}

/**
 * OtherVoiceDetail の画面本体を表示する Composable。
 */
@Suppress("UNUSED_PARAMETER")
@Composable
fun OtherVoiceDetailPaneContent(
    uiState: OtherVoiceDetailUiState,
    canNavigateBack: Boolean = true,
    onBack: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    DetailPaneScaffold(
        title = stringResource(Res.string.voice_title),
        canNavigateBack = canNavigateBack,
        navigateBackContentDescription = stringResource(Res.string.navigate_back),
        onBack = onBack,
        modifier = modifier,
    ) {}
}

@Preview(showBackground = true)
@Composable
private fun OtherVoiceDetailPanePreview() {
    KoDriverTheme {
        OtherVoiceDetailPaneContent(uiState = OtherVoiceDetailUiState)
    }
}
