package kurou.kodriver.feature.manualreadoutlist

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.koin.compose.viewmodel.koinViewModel

/** 音声認識の確認用カード。音声認識を提供しないプラットフォームでは何も表示しない。 */
@Composable
internal fun ManualReadoutSpeechRecognitionSection(modifier: Modifier = Modifier) {
    if (!isSpeechRecognitionSupportedOnPlatform()) return
    val viewModel: SpeechRecognitionViewModel = koinViewModel()
    val requestPermission = rememberRecordAudioPermissionRequester(viewModel::onPermissionResult)
    SpeechRecognitionSection(viewModel = viewModel, requestPermission = requestPermission, modifier = modifier)
}

/** 画面から離れたとき、またはアプリが見えなくなったときに聞き取りを止める。 */
@Composable
internal fun SpeechRecognitionSection(
    viewModel: SpeechRecognitionViewModel,
    requestPermission: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    LifecycleEventEffect(Lifecycle.Event.ON_STOP) { viewModel.stop() }
    DisposableEffect(viewModel) { onDispose { viewModel.stop() } }
    SpeechRecognitionCard(
        uiState = uiState,
        onEnabledChange = { viewModel.onEnabledChange(it, requestPermission) },
        modifier = modifier,
    )
}
