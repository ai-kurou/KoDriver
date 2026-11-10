package kurou.kodriver.core.designsystem

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback

/**
 * [SliderPreviewDetailPaneContent] に表示する文言。
 *
 * @property title 画面タイトル
 * @property description タイトル下の説明文
 * @property sliderLabel スライダー値のラベル書式（`%1$.1f倍` のような小数プレースホルダーを含む文字列。[formatSliderLabel] で整形される）
 * @property resetContentDescription リセットボタンの読み上げ文言
 * @property previewLabel 試聴ボタン（停止中）の文言
 * @property previewStopLabel 試聴ボタン（試聴中）の文言
 * @property previewSample 試聴ボタン押下時に通知する試聴用の文言
 */
data class SliderPreviewDetailTexts(
    val title: String,
    val description: String,
    val sliderLabel: String,
    val resetContentDescription: String,
    val previewLabel: String,
    val previewStopLabel: String,
    val previewSample: String,
)

/**
 * 1本のスライダーと試聴ボタンだけで構成される設定詳細画面の共通本体。
 *
 * 読み上げ速度・声の高さのように「値を1つ調整して試聴する」詳細画面が共通利用する。
 * 保存先やドメイン上の範囲は知らず、値・範囲・文言は呼び出し側から受け取る。
 *
 * @param texts 表示する文言
 * @param value 現在のスライダー値
 * @param valueRange スライダーの範囲
 * @param steps スライダーの分割数（[androidx.compose.material3.Slider] の `steps`）
 * @param defaultValue リセット時に通知するデフォルト値
 * @param isPreviewing 試聴中かどうか
 * @param onValueChangeFinished スライダー操作の完了・リセット時に変更後の値を通知する
 * @param onPreviewClicked 試聴ボタン押下時に [SliderPreviewDetailTexts.previewSample] を通知する
 */
@Composable
fun SliderPreviewDetailPaneContent(
    texts: SliderPreviewDetailTexts,
    value: Float,
    valueRange: ClosedFloatingPointRange<Float>,
    steps: Int,
    defaultValue: Float,
    isPreviewing: Boolean,
    onValueChangeFinished: (Float) -> Unit,
    onPreviewClicked: (String) -> Unit,
    canNavigateBack: Boolean,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val haptic = LocalHapticFeedback.current

    DetailPaneScaffold(
        title = texts.title,
        canNavigateBack = canNavigateBack,
        onBack = onBack,
        modifier = modifier,
    ) {
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState()),
        ) {
            DetailPaneDescription(text = texts.description)
            ThresholdSlider(
                value = value,
                valueRange = valueRange,
                labelFormatter = { texts.sliderLabel.formatSliderLabel(it) },
                onValueChangeFinished = { onValueChangeFinished(it) },
                modifier = Modifier.padding(horizontal = KoDriverSpacing.large),
                steps = steps,
                defaultValue = defaultValue,
                onResetToDefault = { onValueChangeFinished(defaultValue) },
                resetContentDescription = texts.resetContentDescription,
            )
            FilledTonalButton(
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.ContextClick)
                    onPreviewClicked(texts.previewSample)
                },
                modifier = Modifier.padding(horizontal = KoDriverSpacing.large),
            ) {
                Icon(
                    imageVector = if (isPreviewing) Icons.Default.Stop else Icons.Default.PlayArrow,
                    contentDescription = null,
                )
                Spacer(Modifier.width(KoDriverSpacing.small))
                Text(if (isPreviewing) texts.previewStopLabel else texts.previewLabel)
            }
        }
    }
}
