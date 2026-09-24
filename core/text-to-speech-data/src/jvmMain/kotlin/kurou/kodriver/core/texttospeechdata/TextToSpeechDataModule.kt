package kurou.kodriver.core.texttospeechdata

import kurou.kodriver.core.texttospeechdata.repository.WindowsTextToSpeechRepository
import kurou.kodriver.domain.repository.TextToSpeechRepository
import org.koin.dsl.module

/**
 * OS標準のTTSのRepositoryバインドを行うKoinモジュール（:core:text-to-speech-data / jvmMain）。
 *
 * 非WindowsのJVM環境ではSAPIを利用できないが、[WindowsTextToSpeechRepository] 自体が
 * `isAvailable()` で `false` を返し読み上げも行わないため、分岐せずそのままバインドする。
 * Android版はandroidMainの同名モジュールを参照。
 */
val textToSpeechDataModule =
    module {
        single<TextToSpeechRepository> { WindowsTextToSpeechRepository() }
    }
