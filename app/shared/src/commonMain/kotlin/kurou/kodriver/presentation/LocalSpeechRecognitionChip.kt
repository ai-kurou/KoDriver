package kurou.kodriver.presentation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import kurou.kodriver.feature.main.SpeechRecognitionDetailPresentation

/**
 * 全画面の右下に重ねる音声認識のチップ。聞き取りの管理（権限の要求・バックグラウンドでの停止と再開）は [AppScreen] が一度だけ行い、
 * タブを切り替えても止まらないよう、描画だけをここから渡す。タップで開く詳細の見せ方は、第2引数で指定する。
 * 提供されない場合（プレビューやテスト）は何も表示しない。
 */
internal val LocalSpeechRecognitionChip =
    staticCompositionLocalOf<@Composable (Modifier, SpeechRecognitionDetailPresentation) -> Unit> { { _, _ -> } }
