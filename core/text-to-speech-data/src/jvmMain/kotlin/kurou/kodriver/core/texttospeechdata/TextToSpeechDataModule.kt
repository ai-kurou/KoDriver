package kurou.kodriver.core.texttospeechdata

import kurou.kodriver.core.texttospeechdata.repository.DesktopTextToSpeechRepository
import kurou.kodriver.core.texttospeechdata.repository.DesktopVoiceListRepository
import kurou.kodriver.core.texttospeechdata.repository.WindowsSpeechSettingsSenderRepository
import kurou.kodriver.core.texttospeechdata.windows.MacSpeechSynthesizer
import kurou.kodriver.core.texttospeechdata.windows.SapiSpeechSynthesizer
import kurou.kodriver.core.texttospeechdata.windows.SpeechSynthesizer
import kurou.kodriver.domain.repository.SpeechSettingsSenderRepository
import kurou.kodriver.domain.repository.TextToSpeechRepository
import kurou.kodriver.domain.repository.VoiceListRepository
import org.koin.dsl.module

/**
 * OS標準のTTSのRepositoryバインドを行うKoinモジュール（:core:text-to-speech-data / jvmMain）。
 *
 * macOSでは動作確認用に`say`コマンドの [MacSpeechSynthesizer]、それ以外は [SapiSpeechSynthesizer] を
 * 1つ作り、読み上げと音声一覧の両Repositoryで共有する（macでは音量・声の高さは非対応）。
 * Windows・macOS以外のJVM環境ではSAPIを利用できないが、[DesktopTextToSpeechRepository] 自体が
 * `isAvailable()` で `false` を返し読み上げも行わない。
 * Android版はandroidMainの同名モジュールを参照。
 */
val textToSpeechDataModule =
    module {
        val synthesizer: SpeechSynthesizer = createSpeechSynthesizer()
        single<SpeechSettingsSenderRepository> { WindowsSpeechSettingsSenderRepository() }
        single<TextToSpeechRepository> { DesktopTextToSpeechRepository(synthesizer) }
        single<VoiceListRepository> { DesktopVoiceListRepository(synthesizer) }
    }

private fun createSpeechSynthesizer(): SpeechSynthesizer =
    if (System.getProperty("os.name").lowercase().startsWith("mac")) {
        MacSpeechSynthesizer()
    } else {
        SapiSpeechSynthesizer()
    }
