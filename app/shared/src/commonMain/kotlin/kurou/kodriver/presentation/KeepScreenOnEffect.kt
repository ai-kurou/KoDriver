package kurou.kodriver.presentation

import androidx.compose.runtime.Composable

/**
 * KeepScreenOnEffect のプラットフォーム別実装を要求する expect 宣言。
 */
@Composable
expect fun KeepScreenOnEffect(enabled: Boolean)
