package kurou.kodriver.feature.otherwakewordhotkeydetail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kurou.kodriver.core.designsystem.DetailPaneDescription
import kurou.kodriver.core.designsystem.DetailPaneScaffold
import kurou.kodriver.core.designsystem.KoDriverSpacing
import kurou.kodriver.core.designsystem.KoDriverTheme
import kurou.kodriver.domain.manualreadout.util.isManualReadoutHotkeySupported
import kurou.kodriver.domain.model.ManualReadoutHotkeyModifiers
import kurou.kodriver.feature.otherwakewordhotkeydetail.generated.resources.Res
import kurou.kodriver.feature.otherwakewordhotkeydetail.generated.resources.wake_word_hotkey_description
import kurou.kodriver.feature.otherwakewordhotkeydetail.generated.resources.wake_word_hotkey_speech_recognition_off
import kurou.kodriver.feature.otherwakewordhotkeydetail.generated.resources.wake_word_hotkey_title
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

private const val SIMHUB_HOTKEY_SETUP_URL =
    "https://github.com/ai-kurou/KoDriver/blob/main/docs/simhub-hotkey-setup.md"

/**
 * OtherWakeWordHotkeyDetail の画面を表示する Composable。
 */
@Composable
public fun OtherWakeWordHotkeyDetailPane(
    canNavigateBack: Boolean,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    showHotkey: Boolean = isManualReadoutHotkeySupported(),
) {
    // ホットキー非対応の環境では ViewModel を作らず、OS への登録も始めない。
    if (!showHotkey) {
        OtherWakeWordHotkeyDetailContent(
            uiState = OtherWakeWordHotkeyDetailUiState(),
            canNavigateBack = canNavigateBack,
            onBack = onBack,
            modifier = modifier,
            showHotkey = false,
        )
        return
    }
    val viewModel: OtherWakeWordHotkeyDetailViewModel = koinViewModel()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    OtherWakeWordHotkeyDetailContent(
        uiState = uiState,
        canNavigateBack = canNavigateBack,
        onBack = onBack,
        onAssignHotkey = viewModel::assignHotkey,
        onSelectModifiers = viewModel::selectHotkeyModifiers,
        onClearHotkey = viewModel::clearHotkey,
        onRetryHotkeyRegistration = viewModel::onRetryHotkeyRegistration,
        modifier = modifier,
    )
}

@Composable
internal fun OtherWakeWordHotkeyDetailContent(
    uiState: OtherWakeWordHotkeyDetailUiState,
    canNavigateBack: Boolean,
    onBack: () -> Unit,
    onAssignHotkey: (Int) -> Unit = {},
    onSelectModifiers: (ManualReadoutHotkeyModifiers) -> Unit = {},
    onClearHotkey: () -> Unit = {},
    onRetryHotkeyRegistration: () -> Unit = {},
    modifier: Modifier = Modifier,
    showHotkey: Boolean = true,
) {
    val uriHandler = LocalUriHandler.current
    DetailPaneScaffold(
        title = stringResource(Res.string.wake_word_hotkey_title),
        canNavigateBack = canNavigateBack,
        onBack = onBack,
        modifier = modifier,
    ) {
        Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
            DetailPaneDescription(text = stringResource(Res.string.wake_word_hotkey_description))
            if (showHotkey) {
                Column(
                    modifier = Modifier.padding(KoDriverSpacing.large),
                    verticalArrangement = Arrangement.spacedBy(KoDriverSpacing.medium),
                ) {
                    OtherWakeWordHotkeyCard(
                        state = uiState,
                        onAssign = onAssignHotkey,
                        onClear = onClearHotkey,
                        onRetry = onRetryHotkeyRegistration,
                        onSelectModifiers = onSelectModifiers,
                        onOpenGuide = { uriHandler.openUri(SIMHUB_HOTKEY_SETUP_URL) },
                    )
                    if (uiState.showSpeechRecognitionOffNotice) {
                        Text(
                            text = stringResource(Res.string.wake_word_hotkey_speech_recognition_off),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun OtherWakeWordHotkeyDetailPanePreview() {
    KoDriverTheme {
        OtherWakeWordHotkeyDetailContent(
            uiState = OtherWakeWordHotkeyDetailUiState(),
            canNavigateBack = true,
            onBack = {},
        )
    }
}
