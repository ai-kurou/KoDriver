package kurou.kodriver.feature.main

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.outlined.Mic
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.ProvidedValue
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kurou.kodriver.core.designsystem.KoDriverSpacing
import kurou.kodriver.core.designsystem.LocalBottomEndOverlayInset
import org.jetbrains.compose.resources.stringResource
import kotlin.math.roundToInt

private val CHIP_SIZE = 44.dp

/**
 * 右下の音声認識のチップが占める幅・高さ（チップ本体と、他の要素との間の余白を含む）。
 * 右下に置く他の要素（FAB・Snackbar など）はこの分だけ避ける。
 */
val SPEECH_RECOGNITION_CHIP_OVERLAY_INSET = CHIP_SIZE + KoDriverSpacing.small

/**
 * [SPEECH_RECOGNITION_CHIP_OVERLAY_INSET] を [LocalBottomEndOverlayInset] として配下の画面へ渡す。
 * チップを重ねるアプリ共通シェルが `CompositionLocalProvider` に渡す。
 */
val speechRecognitionChipOverlayInset: ProvidedValue<Dp> =
    LocalBottomEndOverlayInset provides SPEECH_RECOGNITION_CHIP_OVERLAY_INSET
private val CHIP_ICON_SIZE = 24.dp
private val CHIP_SHADOW_ELEVATION = 4.dp
private val RING_INSET = 3.dp
private val RING_STROKE_WIDTH = 3.dp
private const val RING_TRACK_ALPHA = 0.2f
private const val RING_START_ANGLE = -90f
private const val FULL_CIRCLE_DEGREES = 360f
private const val CHIP_COLOR_ANIMATION_MILLIS = 200
private const val RING_RISE_ANIMATION_MILLIS = 80
private const val RING_FALL_ANIMATION_MILLIS = 250
private const val COMMAND_PULSE_COUNT = 3
private const val COMMAND_PULSE_MILLIS = 700
private const val COMMAND_PULSE_START_RADIUS_FRACTION = 0.4f
private const val COMMAND_PULSE_START_ALPHA = 0.5f

/** コマンド待ちに入った直後の波紋の、進み具合（0〜1）に対応する半径（チップの半径に対する割合）。中心付近から縁へ広がる。 */
internal fun commandPulseRadiusFraction(progress: Float): Float =
    COMMAND_PULSE_START_RADIUS_FRACTION + (1f - COMMAND_PULSE_START_RADIUS_FRACTION) * progress.coerceIn(0f, 1f)

/** コマンド待ちに入った直後の波紋の、進み具合（0〜1）に対応する不透明度。広がるほど薄くなり、最後は消える。 */
internal fun commandPulseAlpha(progress: Float): Float = COMMAND_PULSE_START_ALPHA * (1f - progress.coerceIn(0f, 1f))

/** 音量リングの段階数。滑らかに伸ばさず段階表示にして、再コンポーズの負荷を抑える。 */
internal const val VOLUME_RING_STEPS = 5

/** 音量リングの [step]（0〜[VOLUME_RING_STEPS]）に対応する、弧の角度（度）。 */
internal fun volumeRingSweepAngle(step: Int): Float = FULL_CIRCLE_DEGREES * step / VOLUME_RING_STEPS

/** 音量リングの弧を動かす長さ（ミリ秒）。伸びるときは素早く、縮むときはゆっくりにして、メーターのように見せる。 */
internal fun volumeRingAnimationMillis(rising: Boolean): Int =
    if (rising) RING_RISE_ANIMATION_MILLIS else RING_FALL_ANIMATION_MILLIS

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
 * チップに出す音量リングの段階を求める。聞き取れていない OFF とエラーでは、音量が残っていても出さない（null）。
 * 音量を通知しないプラットフォームなど [SpeechRecognitionUiState.level] が null のときも null。
 */
internal fun SpeechRecognitionUiState.volumeRingStepOrNull(): Int? {
    val chipState = toChipState()
    val listening = chipState == SpeechRecognitionChipState.On || chipState == SpeechRecognitionChipState.CommandWaiting
    return level?.takeIf { listening }?.let(::volumeRingStep)
}

/**
 * 音声認識の聞き取り状態を、どの画面にも重ねて出せる小さな丸いチップで表す。聞き取り中は音量を縁のリングの段階で示す。
 * タップで詳細を開く（[onClick]）。タップできるのはチップ自身だけで、下の画面へのタップは妨げない（`Surface` は使わない）。
 * 状態は色とアイコンの形で区別する（ウェイクワード待ち=輪郭だけの Mic・コマンド待ち=塗りつぶしの Mic と、入った直後の波紋・無効やエラー=MicOff）。
 * 読み上げ用の説明には [SpeechRecognitionStatus] の文言を使う。
 */
@Composable
internal fun SpeechRecognitionChip(
    uiState: SpeechRecognitionUiState,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
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
            icon = Icons.Outlined.Mic
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
    // 状態が切り替わったときに色が急に変わらないよう、短く補間する
    val colorSpec = tween<Color>(durationMillis = CHIP_COLOR_ANIMATION_MILLIS)
    val animatedContainerColor by animateColorAsState(containerColor, colorSpec)
    val animatedContentColor by animateColorAsState(contentColor, colorSpec)
    val ringStep = uiState.volumeRingStepOrNull()
    val commandPulse = remember { Animatable(0f) }
    val commandWaiting = chipState == SpeechRecognitionChipState.CommandWaiting
    // 無限に繰り返すと画面を見ていなくても描画が続くため、コマンド待ちに入った直後だけ数回広げる
    LaunchedEffect(commandWaiting) {
        commandPulse.snapTo(0f)
        if (commandWaiting) {
            repeat(COMMAND_PULSE_COUNT) {
                commandPulse.snapTo(0f)
                commandPulse.animateTo(1f, tween(COMMAND_PULSE_MILLIS, easing = LinearOutSlowInEasing))
            }
        }
    }
    Box(
        modifier =
            modifier
                .size(CHIP_SIZE)
                .shadow(CHIP_SHADOW_ELEVATION, CircleShape)
                .clip(CircleShape)
                .background(animatedContainerColor)
                .then(if (onClick != null) Modifier.clickable(role = Role.Button, onClick = onClick) else Modifier)
                .semantics { contentDescription = description }
                .drawBehind {
                    val progress = commandPulse.value
                    if (progress > 0f && progress < 1f) {
                        drawCircle(
                            color = animatedContentColor.copy(alpha = commandPulseAlpha(progress)),
                            radius = size.minDimension / 2 * commandPulseRadiusFraction(progress),
                        )
                    }
                },
        contentAlignment = Alignment.Center,
    ) {
        if (ringStep != null) {
            VolumeRing(step = ringStep, color = animatedContentColor)
        }
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = animatedContentColor,
            modifier = Modifier.size(CHIP_ICON_SIZE),
        )
    }
}

/**
 * チップの縁に沿って、[step] に応じて時計回りに伸びるリング。薄い軌道の上に、音量の段階分だけ弧を重ねる。
 * 段階が変わったときは弧の角度を補間し、伸びるときは素早く、縮むときはゆっくり動かす。
 * 再コンポーズは段階が変わるときだけで、補間中は描画だけが走る。
 */
@Composable
private fun VolumeRing(
    step: Int,
    color: Color,
) {
    // 前回の段階は再コンポーズを起こさないよう、スナップショット状態ではなく単なる配列で持つ
    val previousStep = remember { intArrayOf(step) }
    val sweepAngle by animateFloatAsState(
        targetValue = volumeRingSweepAngle(step),
        animationSpec = tween(durationMillis = volumeRingAnimationMillis(rising = step > previousStep[0])),
    )
    SideEffect { previousStep[0] = step }
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
        if (sweepAngle > 0f) {
            drawArc(
                color = color,
                startAngle = RING_START_ANGLE,
                sweepAngle = sweepAngle,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Butt),
            )
        }
    }
}
