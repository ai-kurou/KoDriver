package kurou.kodriver.feature.otherlist

import androidx.compose.runtime.Composable

/**
 * OSのTTS（音声合成）設定画面を開くアクションを取得する。
 * このプラットフォーム向けの実装を要求する expect 宣言。
 */
@Composable
expect fun rememberOpenTtsSettings(): () -> Unit
