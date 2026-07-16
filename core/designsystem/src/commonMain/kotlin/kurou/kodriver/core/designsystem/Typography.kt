package kurou.kodriver.core.designsystem

import androidx.compose.material3.Typography

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
