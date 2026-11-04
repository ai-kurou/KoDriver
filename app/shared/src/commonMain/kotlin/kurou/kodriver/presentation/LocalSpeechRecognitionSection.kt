package kurou.kodriver.presentation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier

/**
 * 「その他」の一覧に差し込む音声認識のカード。聞き取りの管理（権限の要求・バックグラウンドでの停止と再開）は
 * [AppScreen] が一度だけ行い、タブを切り替えても止まらないよう、描画だけをここから渡す。
 * 提供されない場合（プレビューやテスト）は何も表示しない。
 */
internal val LocalSpeechRecognitionSection =
    staticCompositionLocalOf<@Composable (Modifier) -> Unit> { {} }
