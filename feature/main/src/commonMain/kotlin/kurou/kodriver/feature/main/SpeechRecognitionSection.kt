package kurou.kodriver.feature.main

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kurou.kodriver.core.designsystem.KoDriverSpacing
import org.koin.compose.viewmodel.koinViewModel

/**
 * 音声認識の聞き取りをアプリ全体で1つ管理し、設定画面に差し込むカードを返す。
 * マイクの権限の要求と、アプリが見えなくなったときの停止・前面に戻ったときの再開をここで持つため、
 * タブを切り替えても聞き取りは止まらない。音声認識もマイク入力の確認も提供しないプラットフォームでは何も表示しない。
 * 両方を提供するプラットフォーム（デスクトップ）では、音声認識のカードの下にマイク入力の確認用カードを並べる。
 */
@Composable
fun rememberSpeechRecognitionSection(): @Composable (Modifier) -> Unit {
    val speechRecognitionSection =
        if (isSpeechRecognitionSupportedOnPlatform()) {
            val viewModel: SpeechRecognitionViewModel = koinViewModel()
            val requestPermission = rememberRecordAudioPermissionRequester(viewModel::onPermissionResult)
            rememberSpeechRecognitionSection(viewModel, requestPermission)
        } else {
            null
        }
    val microphoneInputSection =
        if (isMicrophoneInputSupportedOnPlatform()) rememberMicrophoneInputSection(koinViewModel()) else null
    return remember(speechRecognitionSection, microphoneInputSection) {
        combineSections(speechRecognitionSection, microphoneInputSection)
    }
}

private val NoSpeechRecognitionSection: @Composable (Modifier) -> Unit = {}

private fun combineSections(
    first: (@Composable (Modifier) -> Unit)?,
    second: (@Composable (Modifier) -> Unit)?,
): @Composable (Modifier) -> Unit {
    if (first == null) return second ?: NoSpeechRecognitionSection
    if (second == null) return first
    return { modifier ->
        Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(KoDriverSpacing.small)) {
            first(Modifier)
            second(Modifier)
        }
    }
}

@Composable
internal fun rememberSpeechRecognitionSection(
    viewModel: SpeechRecognitionViewModel,
    requestPermission: () -> Unit,
): @Composable (Modifier) -> Unit {
    LifecycleEventEffect(Lifecycle.Event.ON_STOP) { viewModel.onAppBackgrounded() }
    LifecycleEventEffect(Lifecycle.Event.ON_START) { viewModel.onAppForegrounded() }
    return remember(viewModel, requestPermission) {
        val section: @Composable (Modifier) -> Unit = { modifier ->
            SpeechRecognitionSectionContent(viewModel, requestPermission, modifier)
        }
        section
    }
}

@Composable
private fun SpeechRecognitionSectionContent(
    viewModel: SpeechRecognitionViewModel,
    requestPermission: () -> Unit,
    modifier: Modifier,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    SpeechRecognitionCard(
        uiState = uiState,
        onEnabledChange = { viewModel.onEnabledChange(it, requestPermission) },
        modifier = modifier,
    )
}

@Composable
internal fun rememberMicrophoneInputSection(viewModel: MicrophoneInputViewModel): @Composable (Modifier) -> Unit {
    LifecycleEventEffect(Lifecycle.Event.ON_STOP) { viewModel.onAppBackgrounded() }
    LifecycleEventEffect(Lifecycle.Event.ON_START) { viewModel.onAppForegrounded() }
    LaunchedEffect(viewModel) { viewModel.refreshDevices() }
    return remember(viewModel) {
        val section: @Composable (Modifier) -> Unit = { modifier ->
            val uiState by viewModel.uiState.collectAsStateWithLifecycle()
            MicrophoneInputCard(
                uiState = uiState,
                onEnabledChange = viewModel::onEnabledChange,
                onDeviceMenuOpen = viewModel::refreshDevices,
                onDeviceSelected = viewModel::onDeviceSelected,
                modifier = modifier,
            )
        }
        section
    }
}
