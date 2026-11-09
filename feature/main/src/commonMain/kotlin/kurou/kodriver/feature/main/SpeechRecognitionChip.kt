package kurou.kodriver.feature.main

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.stringResource

private val CHIP_SIZE = 44.dp
private val CHIP_ICON_SIZE = 24.dp
private val CHIP_SHADOW_ELEVATION = 4.dp

/** 音声認識のチップの見た目を決める、聞き取りの状態の大分類。 */
internal enum class SpeechRecognitionChipState {
    /** 聞き取りを止めている。 */
    Off,

    /** 聞き取り中。ウェイクワードを待っている。 */
    On,

    /** ウェイクワードを検出し、コマンドを待っている。 */
    CommandWaiting,

    /** 権限なし・マイクを使えない・認識を利用できないなど、聞き取れない。 */
    Error,
}

/** 聞き取りの状態から、チップの見た目の分類を求める。 */
internal fun SpeechRecognitionUiState.toChipState(): SpeechRecognitionChipState =
    when (status) {
        SpeechRecognitionStatus.CommandWaiting -> SpeechRecognitionChipState.CommandWaiting

        SpeechRecognitionStatus.PermissionDenied,
        SpeechRecognitionStatus.Unavailable,
        SpeechRecognitionStatus.MicrophoneUnavailable,
        SpeechRecognitionStatus.NoSignal,
        SpeechRecognitionStatus.RecognitionFailed,
        -> SpeechRecognitionChipState.Error

        SpeechRecognitionStatus.Stopped,
        SpeechRecognitionStatus.Starting,
        SpeechRecognitionStatus.Listening,
        -> if (enabled) SpeechRecognitionChipState.On else SpeechRecognitionChipState.Off
    }

/**
 * 音声認識の聞き取り状態を、どの画面にも重ねて出せる小さな丸いチップで表す。表示だけで操作は受け付けない。
 * 状態は色とアイコンで区別し、読み上げ用の説明には [SpeechRecognitionStatus] の文言を使う。
 */
@Composable
internal fun SpeechRecognitionChip(
    uiState: SpeechRecognitionUiState,
    modifier: Modifier = Modifier,
) {
    val chipState = uiState.toChipState()
    val description = stringResource(uiState.status.label())
    val colors = MaterialTheme.colorScheme
    val containerColor: Color
    val contentColor: Color
    val icon: ImageVector
    when (chipState) {
        SpeechRecognitionChipState.Off -> {
            containerColor = colors.surfaceContainerHigh
            contentColor = colors.onSurfaceVariant
            icon = Icons.Filled.MicOff
        }

        SpeechRecognitionChipState.On -> {
            containerColor = colors.surfaceContainerHigh
            contentColor = colors.primary
            icon = Icons.Filled.Mic
        }

        SpeechRecognitionChipState.CommandWaiting -> {
            containerColor = colors.primaryContainer
            contentColor = colors.onPrimaryContainer
            icon = Icons.Filled.Mic
        }

        SpeechRecognitionChipState.Error -> {
            containerColor = colors.errorContainer
            contentColor = colors.onErrorContainer
            icon = Icons.Filled.MicOff
        }
    }
    Surface(
        modifier = modifier.size(CHIP_SIZE).semantics { contentDescription = description },
        shape = CircleShape,
        color = containerColor,
        contentColor = contentColor,
        shadowElevation = CHIP_SHADOW_ELEVATION,
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(CHIP_ICON_SIZE),
            )
        }
    }
}
