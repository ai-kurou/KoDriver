package kurou.kodriver.feature.main

import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
 * 音声認識の聞き取りをアプリ全体で1つ管理し、設定画面に差し込むカードを返す。
 * マイクの権限の要求と、アプリが見えなくなったときの停止・前面に戻ったときの再開をここで持つため、
 * タブを切り替えても聞き取りは止まらない。音声認識を提供しないプラットフォームでは何も表示しない。
 */
@Composable
fun rememberSpeechRecognitionSection(): @Composable (Modifier) -> Unit {
    if (!isSpeechRecognitionSupportedOnPlatform()) return NoSpeechRecognitionSection
    val viewModel: SpeechRecognitionViewModel = koinViewModel()
    val requestPermission = rememberRecordAudioPermissionRequester(viewModel::onPermissionResult)
    return rememberSpeechRecognitionSection(viewModel, requestPermission)
}

/**
 * 音声認識の聞き取り状態を表す小さなチップを返す。タップすると、有効/無効スイッチ・音量・認識結果などの詳細を開く。
 * 詳細の見せ方は、チップを描くときに渡す [SpeechRecognitionDetailPresentation]（スマホはボトムシート、広い画面はポップオーバー）で決める。
 * [rememberSpeechRecognitionSection] と同じ ViewModel を共有し、聞き取りの管理（停止と再開）はそちらが行う。
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
): @Composable (Modifier, SpeechRecognitionDetailPresentation) -> Unit =
    remember(viewModel, requestPermission) {
        val chip: @Composable (Modifier, SpeechRecognitionDetailPresentation) -> Unit = { modifier, presentation ->
            SpeechRecognitionChipWithDetail(viewModel, requestPermission, presentation, modifier)
        }
        chip
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
    // ポップオーバーをチップの位置に合わせて出すため、チップと詳細を同じ Box に置く
    Box(modifier = modifier) {
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

private val NoSpeechRecognitionChip: @Composable (Modifier, SpeechRecognitionDetailPresentation) -> Unit = { _, _ -> }

private val NoSpeechRecognitionSection: @Composable (Modifier) -> Unit = {}

@Composable
internal fun rememberSpeechRecognitionSection(
    viewModel: SpeechRecognitionViewModel,
    requestPermission: () -> Unit,
): @Composable (Modifier) -> Unit {
    LifecycleEventEffect(Lifecycle.Event.ON_STOP) { viewModel.onAppBackgrounded() }
    LifecycleEventEffect(Lifecycle.Event.ON_START) { viewModel.onAppForegrounded() }
    // 停止中でも選択中の入力デバイスを表示できるよう、カードを出すときに一覧を取得する（Android では何もしない）
    LaunchedEffect(viewModel) { viewModel.refreshDevices() }
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
        onDeviceMenuOpen = viewModel::refreshDevices,
        onDeviceSelected = viewModel::onDeviceSelected,
        modifier = modifier,
    )
}
