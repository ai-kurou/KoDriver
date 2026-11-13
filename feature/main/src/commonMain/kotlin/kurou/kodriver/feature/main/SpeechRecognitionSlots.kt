package kurou.kodriver.feature.main

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.koin.compose.viewmodel.koinViewModel

/**
 * 音声認識の画面部品の組。聞き取りの管理は一度だけ行い、描画だけを呼び出し側へ渡す。
 * [chip] は全画面の右下に重ねる小さなチップで、タップで聞き取りの ON/OFF を切り替える。
 * [card] は音声タブの一覧に置く状態カード（状態・入力デバイス・音量・認識結果）。
 */
class SpeechRecognitionSlots(
    val chip: @Composable (Modifier) -> Unit,
    val card: @Composable (Modifier) -> Unit,
) {
    companion object {
        /** 音声認識を提供しないとき（プレビュー・テスト・非対応のプラットフォーム）の、何も表示しない組。 */
        val None = SpeechRecognitionSlots(chip = { }, card = { })
    }
}

/**
 * 音声認識のチップとカードを返す。
 * 起動時の自動開始（[SpeechRecognitionViewModel.onAppLaunched]）、マイクの権限の要求と、アプリが見えなくなったとき（ON_STOP）の停止・前面に戻ったとき（ON_START）の再開をここで持つため、
 * `AppScreen` から一度だけ呼べば、タブを切り替えても聞き取りは止まらない。
 * 音声認識を提供しないプラットフォームでは [SpeechRecognitionSlots.None] を返す。
 */
@Composable
fun rememberSpeechRecognition(): SpeechRecognitionSlots {
    if (!isSpeechRecognitionSupportedOnPlatform()) return SpeechRecognitionSlots.None
    val viewModel: SpeechRecognitionViewModel = koinViewModel()
    val requestPermission = rememberRecordAudioPermissionRequester(viewModel::onPermissionResult)
    return rememberSpeechRecognition(viewModel, requestPermission)
}

@Composable
internal fun rememberSpeechRecognition(
    viewModel: SpeechRecognitionViewModel,
    requestPermission: () -> Unit,
): SpeechRecognitionSlots {
    // ON_START は前面に戻ったときにも届くため、起動時の自動開始は別に一度だけ呼ぶ
    LaunchedEffect(viewModel) { viewModel.onAppLaunched() }
    LifecycleEventEffect(Lifecycle.Event.ON_STOP) { viewModel.onAppBackgrounded() }
    LifecycleEventEffect(Lifecycle.Event.ON_START) { viewModel.onAppForegrounded() }
    return remember(viewModel, requestPermission) {
        SpeechRecognitionSlots(
            chip = { modifier -> SpeechRecognitionChipWithToggle(viewModel, requestPermission, modifier) },
            card = { modifier -> SpeechRecognitionCardWithState(viewModel, modifier) },
        )
    }
}

@Composable
private fun SpeechRecognitionChipWithToggle(
    viewModel: SpeechRecognitionViewModel,
    requestPermission: () -> Unit,
    modifier: Modifier,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    SpeechRecognitionChip(
        uiState = uiState,
        modifier = modifier,
        onClick = { viewModel.onEnabledChange(!uiState.enabled, requestPermission) },
    )
}

@Composable
private fun SpeechRecognitionCardWithState(
    viewModel: SpeechRecognitionViewModel,
    modifier: Modifier,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    // 停止中でも選択中の入力デバイスを表示できるよう、カードを出すときに一覧を取得する（Android では何もしない）
    LaunchedEffect(viewModel) { viewModel.refreshDevices() }
    SpeechRecognitionCard(
        uiState = uiState,
        onDeviceMenuOpen = viewModel::refreshDevices,
        onDeviceSelected = viewModel::onDeviceSelected,
        onResumeDelaySelected = viewModel::onResumeDelaySelected,
        modifier = modifier,
    )
}
