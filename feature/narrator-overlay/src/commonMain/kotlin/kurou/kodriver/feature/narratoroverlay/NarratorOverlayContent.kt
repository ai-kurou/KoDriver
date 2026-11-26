package kurou.kodriver.feature.narratoroverlay

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.updateTransition
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kurou.kodriver.core.designsystem.KoDriverSpacing
import kurou.kodriver.core.designsystem.KoDriverTheme
import kurou.kodriver.domain.overlay.model.OverlayTextSize
import kurou.kodriver.domain.speechrecognition.model.SpeechRecognitionTranscript
import kurou.kodriver.feature.narratoroverlay.generated.resources.Res
import kurou.kodriver.feature.narratoroverlay.generated.resources.speech_recognition_unknown_word
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

/**
 * オーバーレイHUDの背景色（薄い灰色）。ゲーム画面に重ねて表示する専用ウィンドウのための固定色で、
 * アプリ本体の[KoDriverTheme]のカラースキームとは独立して定義する。実際に描画する際は、
 * ユーザーが設定した[NarratorOverlayUiState.backgroundOpacity]に応じたアルファ値をこの色へ適用する。
 */
private val NarratorOverlayBackgroundColor = Color(0xFFEDEDED)

/**
 * 透明な背景でも文字を読みやすくする黒い影。ゲーム画面上の視認性を優先し、アプリテーマに依存しない固定色とする。
 */
private val NarratorOverlayTextShadow =
    Shadow(color = Color.Black.copy(alpha = 0.8f), offset = Offset(2f, 2f), blurRadius = 4f)

/**
 * 読み上げ内容の切り替えと、マイク文言の出現・消去にかける時間（ミリ秒）。ゲーム中の視認性を優先し、短く保つ。
 */
private const val NARRATOR_OVERLAY_TEXT_FADE_DURATION_MILLIS = 150

/** Voskが語彙に無い音を表す記号。そのまま見せると分かりにくいため、表示時に置き換える。 */
private const val VOSK_UNKNOWN_WORD = "[unk]"

/** マイク文言の文字サイズを、読み上げ文言（本文）のサイズに対して何倍にするか。 */
private const val NARRATOR_OVERLAY_TRANSCRIPT_TEXT_SCALE = 0.7f

/** 認識途中の文字列は確定した文言と見分けられるよう、文字色の不透明度を下げて表示する。 */
private const val NARRATOR_OVERLAY_PARTIAL_TEXT_ALPHA = 0.6f

/**
 * NarratorOverlayContent を提供する公開関数。
 */
@Composable
public fun NarratorOverlayContent(modifier: Modifier = Modifier) {
    val viewModel: NarratorOverlayViewModel = koinViewModel()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    NarratorOverlayContent(uiState = uiState, modifier = modifier)
}

@Composable
internal fun NarratorOverlayContent(
    uiState: NarratorOverlayUiState = NarratorOverlayUiState(),
    modifier: Modifier = Modifier,
) {
    // このオーバーレイはアプリ本体の Window とは別の独立した Window に単体でホストされるため、
    // 上位から KoDriverTheme が供給されない。MaterialTheme.typography を使うためにここで自前に適用する。
    KoDriverTheme {
        Box(
            modifier =
                modifier
                    .fillMaxSize()
                    .background(NarratorOverlayBackgroundColor.copy(alpha = uiState.backgroundOpacity / 100f))
                    .verticalScroll(rememberScrollState())
                    .padding(KoDriverSpacing.large),
            contentAlignment = Alignment.Center,
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                AnimatedContent(
                    targetState = uiState.latestTelemetryLog?.narratedText.orEmpty(),
                    transitionSpec = {
                        (
                            fadeIn(tween(NARRATOR_OVERLAY_TEXT_FADE_DURATION_MILLIS)) togetherWith
                                fadeOut(tween(NARRATOR_OVERLAY_TEXT_FADE_DURATION_MILLIS))
                        ).using(SizeTransform(clip = false) { _, _ -> snap() })
                    },
                    contentAlignment = Alignment.Center,
                    label = "narratorOverlayText",
                ) { narratedText ->
                    Text(
                        text = narratedText,
                        color = Color(uiState.overlayTextColor),
                        style = narratorOverlayTextStyle(uiState.overlayTextSize),
                        textAlign = TextAlign.Center,
                    )
                }
                NarratorOverlayTranscript(uiState = uiState)
            }
        }
    }
}

/**
 * 読み上げ文言の下段に表示するマイク文言。出現・消去はフェードと高さの変化を組み合わせ、
 * 中央寄せされた読み上げ文言の位置も滑らかに動かす。表示中の文言の更新はアニメーションしない
 * （認識途中の文字列は細かく書き換わるため）。
 */
@Composable
private fun NarratorOverlayTranscript(uiState: NarratorOverlayUiState) {
    val transcriptText =
        narratorOverlayTranscriptText(
            transcript = uiState.speechRecognitionTranscript,
            unknownWord = stringResource(Res.string.speech_recognition_unknown_word),
        )
    val content =
        if (transcriptText.isEmpty()) {
            null
        } else {
            NarratorOverlayTranscriptContent(
                text = transcriptText,
                isPartial = uiState.speechRecognitionTranscript.partialText.isNotEmpty(),
            )
        }
    val transition = updateTransition(targetState = content, label = "narratorOverlayTranscript")
    val animationSpec = tween<IntSize>(NARRATOR_OVERLAY_TEXT_FADE_DURATION_MILLIS)
    // 影が切れないように、高さの変化中も切り抜かない（clip = false）。
    transition.AnimatedVisibility(
        visible = { it != null },
        enter =
            fadeIn(tween(NARRATOR_OVERLAY_TEXT_FADE_DURATION_MILLIS)) +
                expandVertically(animationSpec = animationSpec, expandFrom = Alignment.Top, clip = false),
        exit =
            fadeOut(tween(NARRATOR_OVERLAY_TEXT_FADE_DURATION_MILLIS)) +
                shrinkVertically(animationSpec = animationSpec, shrinkTowards = Alignment.Top, clip = false),
    ) {
        // 消去中は新しい状態が null になるため、直前まで表示していた文言を描き続ける。
        val shown = transition.targetState ?: transition.currentState ?: return@AnimatedVisibility
        val textColor = Color(uiState.overlayTextColor)
        Text(
            text = shown.text,
            color =
                if (shown.isPartial) {
                    textColor.copy(alpha = textColor.alpha * NARRATOR_OVERLAY_PARTIAL_TEXT_ALPHA)
                } else {
                    textColor
                },
            style = narratorOverlayTranscriptTextStyle(uiState.overlayTextSize),
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = KoDriverSpacing.small),
        )
    }
}

/** 下段に表示中のマイク文言。 */
private data class NarratorOverlayTranscriptContent(
    val text: String,
    val isPartial: Boolean,
)

/**
 * マイク文言として表示する文字列を返す。認識途中の文字列があればそれを、なければ直近に確定した発話を返す
 * （確定した発話は、次の認識途中の文字列が来るまで残る）。どちらもなければ空文字を返す。
 * Vosk の語彙外記号 `[unk]` は [unknownWord] に置き換える。
 */
internal fun narratorOverlayTranscriptText(
    transcript: SpeechRecognitionTranscript,
    unknownWord: String,
): String =
    transcript.partialText
        .ifEmpty { transcript.recognizedText.orEmpty() }
        .replace(VOSK_UNKNOWN_WORD, unknownWord)

@Composable
internal fun narratorOverlayTextStyle(overlayTextSize: OverlayTextSize): TextStyle =
    when (overlayTextSize) {
        OverlayTextSize.EXTRA_SMALL -> MaterialTheme.typography.titleLarge.copy(fontSize = 14.sp, lineHeight = 18.2.sp)
        OverlayTextSize.SMALL -> MaterialTheme.typography.titleLarge.copy(fontSize = 18.sp, lineHeight = 23.4.sp)
        OverlayTextSize.MEDIUM -> MaterialTheme.typography.titleLarge.copy(fontSize = 22.sp, lineHeight = 28.6.sp)
        OverlayTextSize.LARGE -> MaterialTheme.typography.titleLarge.copy(fontSize = 28.sp, lineHeight = 36.4.sp)
        OverlayTextSize.EXTRA_LARGE -> MaterialTheme.typography.titleLarge.copy(fontSize = 36.sp, lineHeight = 46.8.sp)
        OverlayTextSize.HUGE -> MaterialTheme.typography.titleLarge.copy(fontSize = 48.sp, lineHeight = 62.4.sp)
        OverlayTextSize.MAXIMUM -> MaterialTheme.typography.titleLarge.copy(fontSize = 64.sp, lineHeight = 83.2.sp)
    }.copy(shadow = NarratorOverlayTextShadow)

/** マイク文言のスタイル。読み上げ文言（[narratorOverlayTextStyle]）より小さくし、影・色は本文と同じ扱いにする。 */
@Composable
internal fun narratorOverlayTranscriptTextStyle(overlayTextSize: OverlayTextSize): TextStyle {
    val bodyStyle = narratorOverlayTextStyle(overlayTextSize)
    return bodyStyle.copy(
        fontSize = bodyStyle.fontSize * NARRATOR_OVERLAY_TRANSCRIPT_TEXT_SCALE,
        lineHeight = bodyStyle.lineHeight * NARRATOR_OVERLAY_TRANSCRIPT_TEXT_SCALE,
    )
}

@Preview(showBackground = true)
@Composable
private fun NarratorOverlayContentPreview() {
    NarratorOverlayContent(
        uiState = NarratorOverlayUiState(latestTelemetryLog = null),
    )
}
