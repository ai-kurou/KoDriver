package kurou.kodriver.feature.otherlist

import androidx.compose.runtime.Composable

/**
 * rememberOpenTtsSettings のこのプラットフォーム向け実装。
 * この項目自体がAndroid専用のため何も行わない。
 */
@Composable
actual fun rememberOpenTtsSettings(): () -> Unit = {}
