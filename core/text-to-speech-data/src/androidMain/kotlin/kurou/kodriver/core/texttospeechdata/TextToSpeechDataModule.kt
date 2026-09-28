package kurou.kodriver.core.texttospeechdata

import android.content.Context
import android.speech.tts.TextToSpeech
import kurou.kodriver.core.texttospeechdata.repository.AndroidTextToSpeechRepository
import kurou.kodriver.domain.repository.TextToSpeechRepository
import org.koin.dsl.module

/**
 * OS標準のTTSのRepositoryバインドを行うKoinモジュール（:core:text-to-speech-data / androidMain）。
 *
 * jvmMain版との違いは実装手段（[TextToSpeech] vs Windows SAPI）のみ。
 */
val textToSpeechDataModule =
    module {
        single<TextToSpeechRepository> {
            val context = get<Context>()
            AndroidTextToSpeechRepository(
                textToSpeechFactory = { listener -> TextToSpeech(context, listener) },
            )
        }
    }
