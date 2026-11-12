package kurou.kodriver.presentation

import androidx.compose.runtime.staticCompositionLocalOf
import kurou.kodriver.feature.main.SpeechRecognitionSlots

/**
 * 音声認識のチップ（全画面の右下に重ねる ON/OFF トグル）と、音声タブに置く状態カード。聞き取りの管理（権限の要求・バックグラウンドでの停止と再開）は
 * [AppScreen] が一度だけ行い、タブを切り替えても止まらないよう、描画だけをここから渡す。
 * 提供されない場合（プレビューやテスト）は何も表示しない。
 */
internal val LocalSpeechRecognition = staticCompositionLocalOf { SpeechRecognitionSlots.None }
