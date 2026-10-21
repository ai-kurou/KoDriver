package kurou.kodriver.core.texttospeechdata

import android.content.Context
import android.speech.tts.TextToSpeech
import kotlinx.coroutines.flow.first
import kurou.kodriver.core.texttospeechdata.repository.AndroidSpeechSettingsSenderRepository
import kurou.kodriver.core.texttospeechdata.repository.AndroidTextToSpeechRepository
import kurou.kodriver.core.texttospeechdata.repository.AndroidVoiceListRepository
import kurou.kodriver.core.texttospeechdata.repository.androidAudioUsageToPlatformUsage
import kurou.kodriver.domain.repository.AndroidAudioUsagePreferencesRepository
import kurou.kodriver.domain.repository.SpeechSettingsSenderRepository
import kurou.kodriver.domain.repository.TextToSpeechRepository
import kurou.kodriver.domain.repository.VoiceListRepository
import org.koin.core.qualifier.named
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
        // :core:narrator は :core:domain に依存せず、この同名 qualifier の Int provider を利用する。
        single<suspend () -> Int>(named("android_sound_audio_usage")) {
            {
                androidAudioUsageToPlatformUsage(
                    get<AndroidAudioUsagePreferencesRepository>().observeAndroidAudioUsage().first(),
                )
            }
        }
        single<SpeechSettingsSenderRepository> { AndroidSpeechSettingsSenderRepository() }
        single {
            val context = get<Context>()
            AndroidTextToSpeechRepository(
                textToSpeechFactory = { listener -> TextToSpeech(context, listener) },
                audioUsageProvider = {
                    get<AndroidAudioUsagePreferencesRepository>().observeAndroidAudioUsage().first()
                },
            )
        } bind TextToSpeechRepository::class
        single<VoiceListRepository> {
            val repository = get<AndroidTextToSpeechRepository>()
            AndroidVoiceListRepository(
                engineProvider = { repository.engineOrNull(retryIfUnavailable = true) },
                defaultVoiceIdProvider = { repository.defaultVoiceId },
            )
        }
    }
