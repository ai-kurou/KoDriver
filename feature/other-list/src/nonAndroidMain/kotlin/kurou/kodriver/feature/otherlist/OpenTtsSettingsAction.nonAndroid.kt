package kurou.kodriver.feature.otherlist

import androidx.compose.runtime.Composable

/**
 * rememberOpenTtsSettings のこのプラットフォーム向け実装。
 * この項目はWindows専用のため、その他の非Android環境では何も行わない。
 */
@Composable
actual fun rememberOpenTtsSettings(): () -> Unit = {}
