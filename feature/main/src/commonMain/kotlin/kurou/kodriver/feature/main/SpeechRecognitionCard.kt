package kurou.kodriver.feature.main

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import kurou.kodriver.core.designsystem.KoDriverPhraseChip
import kurou.kodriver.core.designsystem.KoDriverSpacing
import kurou.kodriver.feature.main.generated.resources.Res
import kurou.kodriver.feature.main.generated.resources.speech_recognition_level_description
import kurou.kodriver.feature.main.generated.resources.speech_recognition_status_command_waiting
import kurou.kodriver.feature.main.generated.resources.speech_recognition_status_listening
import kurou.kodriver.feature.main.generated.resources.speech_recognition_status_microphone_unavailable
import kurou.kodriver.feature.main.generated.resources.speech_recognition_status_no_signal
import kurou.kodriver.feature.main.generated.resources.speech_recognition_status_permission_denied
import kurou.kodriver.feature.main.generated.resources.speech_recognition_status_recognition_failed
import kurou.kodriver.feature.main.generated.resources.speech_recognition_status_starting
import kurou.kodriver.feature.main.generated.resources.speech_recognition_status_stopped
import kurou.kodriver.feature.main.generated.resources.speech_recognition_status_unavailable
import kurou.kodriver.feature.main.generated.resources.speech_recognition_title
import kurou.kodriver.feature.main.generated.resources.speech_recognition_unknown_word
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource

/** 認識途中の文字列と履歴を表示する領域の最大の高さ。超えた分はスクロールする。 */
private val HISTORY_MAX_HEIGHT = 120.dp

/** 入力音量ゲージの高さ。 */
private val LEVEL_GAUGE_HEIGHT = 12.dp

/** 入力音量ゲージの値変化を滑らかに見せるためのアニメーション時間(ミリ秒)。 */
private const val LEVEL_ANIMATION_MILLIS = 100

/**
 * 音声タブの一覧に置く、音声認識の状態カード。聞き取りの状態・入力デバイス選択（デスクトップのみ）・入力音量・認識結果を並べる。
 * 有効/無効の切り替えは右下のチップ（[SpeechRecognitionChip]）のタップで行うため、このカードにはスイッチを置かない。
 */
@Composable
internal fun SpeechRecognitionCard(
    uiState: SpeechRecognitionUiState,
    onDeviceMenuOpen: () -> Unit,
    onDeviceSelected: (String) -> Unit,
    onResumeDelaySelected: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val levelDescription = stringResource(Res.string.speech_recognition_level_description)
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.surfaceContainerLow,
    ) {
        SpeechRecognitionCardContent(
            uiState,
            levelDescription,
            onDeviceMenuOpen,
            onDeviceSelected,
            onResumeDelaySelected,
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun SpeechRecognitionCardContent(
    uiState: SpeechRecognitionUiState,
    levelDescription: String,
    onDeviceMenuOpen: () -> Unit,
    onDeviceSelected: (String) -> Unit,
    onResumeDelaySelected: (Int) -> Unit,
) {
    Column(
        modifier = Modifier.padding(KoDriverSpacing.medium),
        verticalArrangement = Arrangement.spacedBy(KoDriverSpacing.small),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(KoDriverSpacing.medium),
        ) {
            Icon(
                imageVector = Icons.Filled.Mic,
                contentDescription = null,
                tint =
                    if (uiState.enabled) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    },
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(Res.string.speech_recognition_title),
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    text = stringResource(uiState.status.label()),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        // 入力デバイスを選べるのはデスクトップだけ。Android では一覧が空のままなので表示しない
        if (uiState.devices.isNotEmpty() && uiState.deviceName != null) {
            AudioInputDeviceDropdown(
                devices = uiState.devices,
                selected = uiState.deviceName,
                onMenuOpen = onDeviceMenuOpen,
                onSelected = onDeviceSelected,
            )
        }
        SpeechRecognitionResumeDelayDropdown(selectedMs = uiState.resumeDelayMs, onSelected = onResumeDelaySelected)
        uiState.level?.let { level ->
            val animatedLevel by animateFloatAsState(
                targetValue = level,
                animationSpec = tween(durationMillis = LEVEL_ANIMATION_MILLIS),
            )
            LinearProgressIndicator(
                progress = { animatedLevel },
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .height(LEVEL_GAUGE_HEIGHT)
                        .semantics { contentDescription = levelDescription },
            )
        }
        if (uiState.partialText.isNotEmpty() || uiState.recognized.isNotEmpty()) {
            // 履歴が長くなっても下のセット一覧の領域を奪わないよう、高さを抑えてスクロールさせる。
            Column(
                modifier =
                    Modifier
                        .heightIn(max = HISTORY_MAX_HEIGHT)
                        .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(KoDriverSpacing.small),
            ) {
                val unknownWord = stringResource(Res.string.speech_recognition_unknown_word)
                if (uiState.partialText.isNotEmpty()) {
                    Text(
                        text = uiState.partialText.replace(VOSK_UNKNOWN_WORD, unknownWord),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                uiState.recognized.forEach { candidates ->
                    // 1行が1回の発話。候補はチップで並べ、幅を超える分は折り返す
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(KoDriverSpacing.extraSmall),
                        verticalArrangement = Arrangement.spacedBy(KoDriverSpacing.extraSmall),
                    ) {
                        candidates.forEach {
                            KoDriverPhraseChip(
                                text = it.replace(VOSK_UNKNOWN_WORD, unknownWord),
                                muted = it.contains(VOSK_UNKNOWN_WORD),
                            )
                        }
                    }
                }
            }
        }
    }
}

/** Voskが語彙に無い音を表す記号。そのまま見せると分かりにくいため、表示時に置き換える。 */
private const val VOSK_UNKNOWN_WORD = "[unk]"

internal fun SpeechRecognitionStatus.label(): StringResource =
    when (this) {
        SpeechRecognitionStatus.Stopped -> Res.string.speech_recognition_status_stopped
        SpeechRecognitionStatus.Starting -> Res.string.speech_recognition_status_starting
        SpeechRecognitionStatus.Listening -> Res.string.speech_recognition_status_listening
        SpeechRecognitionStatus.CommandWaiting -> Res.string.speech_recognition_status_command_waiting
        SpeechRecognitionStatus.PermissionDenied -> Res.string.speech_recognition_status_permission_denied
        SpeechRecognitionStatus.Unavailable -> Res.string.speech_recognition_status_unavailable
        SpeechRecognitionStatus.MicrophoneUnavailable -> Res.string.speech_recognition_status_microphone_unavailable
        SpeechRecognitionStatus.NoSignal -> Res.string.speech_recognition_status_no_signal
        SpeechRecognitionStatus.RecognitionFailed -> Res.string.speech_recognition_status_recognition_failed
    }
