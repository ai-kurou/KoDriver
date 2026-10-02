package kurou.kodriver.core.texttospeechdata.repository

import android.speech.tts.TextToSpeech
import kotlinx.coroutines.CancellationException
import kurou.kodriver.domain.model.TTS_CULTURE_NAME
import kurou.kodriver.domain.model.TextToSpeechVoice
import kurou.kodriver.domain.repository.VoiceListRepository

/**
 * 日本語のうちオフラインで使えるインストール済み音声を取得する。
 * 詳細画面の再読み込みで音声の導入を反映できるよう、一覧はキャッシュせず毎回取得する。
 */
internal class AndroidVoiceListRepository(
    private val engineProvider: suspend () -> TextToSpeech?,
) : VoiceListRepository {
    override suspend fun availableVoices(): List<TextToSpeechVoice> {
        val engine = engineProvider() ?: return emptyList()
        val voices =
            try {
                engine.voices
            } catch (e: CancellationException) {
                throw e
            } catch (_: Exception) {
                null
            }
        return voices
            .orEmpty()
            .filter {
                it.locale.language == "ja" &&
                    !it.isNetworkConnectionRequired &&
                    TextToSpeech.Engine.KEY_FEATURE_NOT_INSTALLED !in it.features.orEmpty()
            }.map {
                TextToSpeechVoice(
                    id = it.name,
                    displayName = formatVoiceDisplayName(it.name),
                    cultureName = TTS_CULTURE_NAME,
                )
            }
    }
}
