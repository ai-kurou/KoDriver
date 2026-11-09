package kurou.kodriver.feature.main

import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.koin.compose.viewmodel.koinViewModel

/**
 * 音声認識の聞き取り状態を表す小さなチップを返す。タップすると、有効/無効スイッチ・音量・認識結果などの詳細を開く。
 * 詳細の見せ方は、チップを描くときに渡す [SpeechRecognitionDetailPresentation]（スマホはボトムシート、広い画面はポップオーバー）で決める。
 * マイクの権限の要求と、アプリが見えなくなったとき（ON_STOP）の停止・前面に戻ったとき（ON_START）の再開をここで持つため、
 * `AppScreen` から一度だけ呼べば、タブを切り替えても聞き取りは止まらない。
 * 音声認識を提供しないプラットフォームでは何も表示しない。
 */
@Composable
fun rememberSpeechRecognitionChip(): @Composable (Modifier, SpeechRecognitionDetailPresentation) -> Unit {
    if (!isSpeechRecognitionSupportedOnPlatform()) return NoSpeechRecognitionChip
    val viewModel: SpeechRecognitionViewModel = koinViewModel()
    val requestPermission = rememberRecordAudioPermissionRequester(viewModel::onPermissionResult)
    return rememberSpeechRecognitionChip(viewModel, requestPermission)
}

@Composable
internal fun rememberSpeechRecognitionChip(
    viewModel: SpeechRecognitionViewModel,
    requestPermission: () -> Unit,
): @Composable (Modifier, SpeechRecognitionDetailPresentation) -> Unit {
    LifecycleEventEffect(Lifecycle.Event.ON_STOP) { viewModel.onAppBackgrounded() }
    LifecycleEventEffect(Lifecycle.Event.ON_START) { viewModel.onAppForegrounded() }
    return remember(viewModel, requestPermission) {
        val chip: @Composable (Modifier, SpeechRecognitionDetailPresentation) -> Unit = { modifier, presentation ->
            SpeechRecognitionChipWithDetail(viewModel, requestPermission, presentation, modifier)
        }
        chip
    }
}

@Composable
private fun SpeechRecognitionChipWithDetail(
    viewModel: SpeechRecognitionViewModel,
    requestPermission: () -> Unit,
    presentation: SpeechRecognitionDetailPresentation,
    modifier: Modifier,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var detailVisible by remember { mutableStateOf(false) }
    // AppScreen が渡す余白はチップの外側に置き、ポップオーバーのアンカーはチップ自身の範囲にする
    Box(modifier = modifier) {
        Box {
            SpeechRecognitionChip(
                uiState = uiState,
                onClick = {
                    // 停止中でも選択中の入力デバイスを表示できるよう、開くときに一覧を取得する（Android では何もしない）
                    viewModel.refreshDevices()
                    detailVisible = true
                },
            )
            if (detailVisible) {
                SpeechRecognitionDetail(
                    presentation = presentation,
                    uiState = uiState,
                    onEnabledChange = { viewModel.onEnabledChange(it, requestPermission) },
                    onDeviceMenuOpen = viewModel::refreshDevices,
                    onDeviceSelected = viewModel::onDeviceSelected,
                    onDismissRequest = { detailVisible = false },
                )
            }
        }
    }
}

private val NoSpeechRecognitionChip: @Composable (Modifier, SpeechRecognitionDetailPresentation) -> Unit = { _, _ -> }
