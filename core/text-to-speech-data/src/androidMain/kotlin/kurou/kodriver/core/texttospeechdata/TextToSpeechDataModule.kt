package kurou.kodriver.core.texttospeechdata

import android.content.Context
import android.speech.tts.TextToSpeech
import kurou.kodriver.core.texttospeechdata.repository.AndroidSpeechSettingsSenderRepository
import kurou.kodriver.core.texttospeechdata.repository.AndroidTextToSpeechRepository
import kurou.kodriver.core.texttospeechdata.repository.AndroidVoiceListRepository
import kurou.kodriver.domain.repository.SpeechSettingsSenderRepository
import kurou.kodriver.domain.repository.TextToSpeechRepository
import kurou.kodriver.domain.repository.VoiceListRepository
import org.koin.dsl.bind
import org.koin.dsl.module

/**
 * OS標準のTTSのRepositoryバインドを行うKoinモジュール（:core:text-to-speech-data / androidMain）。
 *
 * 読み上げと音声一覧は [AndroidTextToSpeechRepository.engineOrNull] で同じ [TextToSpeech] を共有する。
 * 音声一覧の取得は、初期化に失敗していた場合に再初期化して後から導入された日本語データを反映する。
 */
val textToSpeechDataModule =
    module {
        single<SpeechSettingsSenderRepository> { AndroidSpeechSettingsSenderRepository() }
        single {
            val context = get<Context>()
            AndroidTextToSpeechRepository(
                textToSpeechFactory = { listener -> TextToSpeech(context, listener) },
            )
        } bind TextToSpeechRepository::class
        single<VoiceListRepository> {
            AndroidVoiceListRepository {
                get<AndroidTextToSpeechRepository>().engineOrNull(retryIfUnavailable = true)
            }
        }
    }
