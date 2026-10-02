package kurou.kodriver.feature.othervoicedetail

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kurou.kodriver.core.designsystem.DetailPaneBodyText
import kurou.kodriver.core.designsystem.DetailPaneDescription
import kurou.kodriver.core.designsystem.DetailPaneScaffold
import kurou.kodriver.core.designsystem.DetailPaneSubtitle
import kurou.kodriver.core.designsystem.KoDriverSpacing
import kurou.kodriver.core.designsystem.KoDriverTheme
import kurou.kodriver.domain.model.VOICE_ID_UNSPECIFIED
import kurou.kodriver.feature.othervoicedetail.generated.resources.Res
import kurou.kodriver.feature.othervoicedetail.generated.resources.navigate_back
import kurou.kodriver.feature.othervoicedetail.generated.resources.voice_description
import kurou.kodriver.feature.othervoicedetail.generated.resources.voice_empty
import kurou.kodriver.feature.othervoicedetail.generated.resources.voice_loading
import kurou.kodriver.feature.othervoicedetail.generated.resources.voice_retry
import kurou.kodriver.feature.othervoicedetail.generated.resources.voice_saved_missing
import kurou.kodriver.feature.othervoicedetail.generated.resources.voice_subtitle
import kurou.kodriver.feature.othervoicedetail.generated.resources.voice_system_default
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
        onVoiceSelected = viewModel::onVoiceSelected,
        onRetryClicked = viewModel::onRetryClicked,
        canNavigateBack = canNavigateBack,
        onBack = onBack,
        modifier = modifier,
    )
}

/**
 * OtherVoiceDetail の画面本体を表示する Composable。
 */
@Composable
fun OtherVoiceDetailPaneContent(
    uiState: OtherVoiceDetailUiState,
    modifier: Modifier = Modifier,
    onVoiceSelected: (String) -> Unit = {},
    onRetryClicked: () -> Unit = {},
    canNavigateBack: Boolean = true,
    onBack: () -> Unit = {},
) {
    val haptic = LocalHapticFeedback.current
    val selectedId = if (uiState.savedVoiceMissing) VOICE_ID_UNSPECIFIED else uiState.selectedVoiceId
    DetailPaneScaffold(
        title = stringResource(Res.string.voice_title),
        canNavigateBack = canNavigateBack,
        navigateBackContentDescription = stringResource(Res.string.navigate_back),
        onBack = onBack,
        modifier = modifier,
    ) {
        Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
            DetailPaneDescription(text = stringResource(Res.string.voice_description))
            DetailPaneSubtitle(
                text = stringResource(Res.string.voice_subtitle),
                modifier = Modifier.padding(horizontal = KoDriverSpacing.large),
            )
            Column(modifier = Modifier.selectableGroup().padding(horizontal = KoDriverSpacing.large)) {
                val options =
                    listOf(VOICE_ID_UNSPECIFIED to stringResource(Res.string.voice_system_default)) +
                        uiState.voices.map { it.id to it.displayName }
                options.forEach { (id, label) ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .selectable(
                                    selected = selectedId == id,
                                    role = Role.RadioButton,
                                    onClick = {
                                        haptic.performHapticFeedback(HapticFeedbackType.ContextClick)
                                        onVoiceSelected(id)
                                    },
                                ).padding(vertical = KoDriverSpacing.small),
                    ) {
                        RadioButton(selected = selectedId == id, onClick = null)
                        Text(label)
                    }
                }
                if (uiState.isLoading) {
                    DetailPaneBodyText(text = stringResource(Res.string.voice_loading))
                } else if (uiState.voices.isEmpty()) {
                    DetailPaneBodyText(text = stringResource(Res.string.voice_empty))
                    TextButton(onClick = onRetryClicked) {
                        Text(stringResource(Res.string.voice_retry))
                    }
                }
                if (uiState.savedVoiceMissing) {
                    DetailPaneBodyText(text = stringResource(Res.string.voice_saved_missing))
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun OtherVoiceDetailPanePreview() {
    KoDriverTheme {
        OtherVoiceDetailPaneContent(uiState = OtherVoiceDetailUiState())
    }
}
