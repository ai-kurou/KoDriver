package kurou.kodriver.core.designsystem

import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.text.TextStyle

/**
 * KoDriver アプリ全体のタイポグラフィ定義。
 *
 * 現時点では Material3 のデフォルトをそのまま採用しているが、[KoDriverTheme] を通じて
 * 全画面へ配布されるため、文字スケールの調整はこのファイルの変更だけで完結する。
 *
 * feature モジュール側では `fontSize` や `FontWeight` を直接指定せず、
 * `MaterialTheme.typography.*` のスタイルだけを参照すること。
 */
val KoDriverTypography = Typography()

private const val TABULAR_FIGURES_FONT_FEATURE_SETTINGS = "tnum"

/**
 * 高頻度で値が変化する数値表示（ラップタイム・タイヤ温度・燃料残量等）に適用するスタイル。
 *
 * `tnum`（tabular figures）を有効にすることで、数字の字幅が桁ごとに揺れず一定になり、
 * 走行中に一瞬で読み取る用途での視認性を確保する。フォントファミリー自体は変更せず、
 * 呼び出し時点の `LocalTextStyle`（通常は `MaterialTheme.typography.bodyLarge` 相当）に
 * OpenType 機能だけをマージするため、フォントサイズ等の見た目は変わらない。
 */
@Composable
@ReadOnlyComposable
fun koDriverNumericTextStyle(): TextStyle =
    LocalTextStyle.current.merge(TextStyle(fontFeatureSettings = TABULAR_FIGURES_FONT_FEATURE_SETTINGS))
