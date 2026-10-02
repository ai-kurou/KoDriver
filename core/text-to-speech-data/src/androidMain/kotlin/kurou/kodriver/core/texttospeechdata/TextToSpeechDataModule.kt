package kurou.kodriver.core.texttospeechdata

import android.content.Context
import android.speech.tts.TextToSpeech
import kurou.kodriver.core.texttospeechdata.repository.AndroidTextToSpeechRepository
import kurou.kodriver.core.texttospeechdata.repository.AndroidVoiceListRepository
import kurou.kodriver.domain.repository.TextToSpeechRepository
import kurou.kodriver.domain.repository.VoiceListRepository
import org.koin.dsl.bind
import org.koin.dsl.module

/**
 * OS標準のTTSのRepositoryバインドを行うKoinモジュール（:core:text-to-speech-data / androidMain）。
 *
 * 読み上げと音声一覧は [AndroidTextToSpeechRepository.engineOrNull] で同じ [TextToSpeech] を共有する。
 */
val textToSpeechDataModule =
    module {
        single {
            val context = get<Context>()
            AndroidTextToSpeechRepository(
                textToSpeechFactory = { listener -> TextToSpeech(context, listener) },
            )
        } bind TextToSpeechRepository::class
        single<VoiceListRepository> {
            AndroidVoiceListRepository(get<AndroidTextToSpeechRepository>()::engineOrNull)
        }
    }
