package kurou.kodriver.feature.main

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.stringResource
import kotlin.math.roundToInt

private val CHIP_SIZE = 44.dp
private val CHIP_ICON_SIZE = 24.dp
private val CHIP_SHADOW_ELEVATION = 4.dp
private val RING_INSET = 3.dp
private val RING_STROKE_WIDTH = 3.dp
private const val RING_TRACK_ALPHA = 0.2f
private const val RING_START_ANGLE = -90f
private const val FULL_CIRCLE_DEGREES = 360f

/** 音量リングの段階数。滑らかに伸ばさず段階表示にして、再コンポーズの負荷を抑える。 */
internal const val VOLUME_RING_STEPS = 5

/** 音量（0.0〜1.0）を、音量リングの段階（0〜[VOLUME_RING_STEPS]）へ変換する。範囲外は端に丸める。 */
internal fun volumeRingStep(level: Float): Int = (level.coerceIn(0f, 1f) * VOLUME_RING_STEPS).roundToInt()

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
 * 音声認識の聞き取り状態を、どの画面にも重ねて出せる小さな丸いチップで表す。表示だけで操作は受け付けず、聞き取り中は音量を縁のリングの段階で示す。操作は受け付けず、
 * 下の画面へのタップを妨げないよう `Surface` は使わない。
 * 状態は主に色で区別し（アイコンは有効=Mic・無効やエラー=MicOff）、読み上げ用の説明には [SpeechRecognitionStatus] の文言を使う。
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
    // OFF とエラーは聞き取れていないため、音量が残っていてもリングを出さない。
    val listening = chipState == SpeechRecognitionChipState.On || chipState == SpeechRecognitionChipState.CommandWaiting
    val ringStep = uiState.level?.takeIf { listening }?.let(::volumeRingStep)
    Box(
        modifier =
            modifier
                .size(CHIP_SIZE)
                .shadow(CHIP_SHADOW_ELEVATION, CircleShape)
                .clip(CircleShape)
                .background(containerColor)
                .semantics { contentDescription = description },
        contentAlignment = Alignment.Center,
    ) {
        if (ringStep != null) {
            VolumeRing(step = ringStep, color = contentColor)
        }
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = contentColor,
            modifier = Modifier.size(CHIP_ICON_SIZE),
        )
    }
}

/** チップの縁に沿って、[step] に応じて時計回りに伸びるリング。薄い軌道の上に、音量の段階分だけ弧を重ねる。 */
@Composable
private fun VolumeRing(
    step: Int,
    color: Color,
) {
    Canvas(modifier = Modifier.fillMaxSize().padding(RING_INSET)) {
        val strokeWidth = RING_STROKE_WIDTH.toPx()
        val arcSize = Size(size.width - strokeWidth, size.height - strokeWidth)
        val topLeft = Offset(strokeWidth / 2, strokeWidth / 2)
        drawArc(
            color = color.copy(alpha = RING_TRACK_ALPHA),
            startAngle = RING_START_ANGLE,
            sweepAngle = FULL_CIRCLE_DEGREES,
            useCenter = false,
            topLeft = topLeft,
            size = arcSize,
            style = Stroke(width = strokeWidth),
        )
        if (step > 0) {
            drawArc(
                color = color,
                startAngle = RING_START_ANGLE,
                sweepAngle = FULL_CIRCLE_DEGREES * step / VOLUME_RING_STEPS,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Butt),
            )
        }
    }
}
