package kurou.kodriver.feature.otherlist

import android.content.Intent
import android.speech.tts.TextToSpeech
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

/**
 * rememberOpenTtsSettings のこのプラットフォーム向け実装。
 *
 * `android.provider.Settings` にはTTS設定画面を直接開くアクションが存在しないため、
 * [TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA] でエンジン側の音声データインストール画面を開く。
 */
@Composable
actual fun rememberOpenTtsSettings(): () -> Unit {
    val context = LocalContext.current
    return {
        val intent = Intent(TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA)
        context.startActivity(intent)
    }
}
