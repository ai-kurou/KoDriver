package kurou.kodriver.feature.otherandroidaudiousagedetail

import kurou.kodriver.domain.usecase.ObserveAndroidAudioUsageUseCase
import kurou.kodriver.domain.usecase.ObserveSoundVolumeUseCase
import kurou.kodriver.domain.usecase.ObserveVoicePitchUseCase
import kurou.kodriver.domain.usecase.ObserveVoiceSpeedUseCase
import kurou.kodriver.domain.usecase.ObserveVoiceUseCase
import kurou.kodriver.domain.usecase.SaveAndroidAudioUsageUseCase
import kurou.kodriver.domain.usecase.SpeakTextUseCase
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

/**
 * 音声の用途設定詳細の Koin モジュール。
 * 提供: OtherAndroidAudioUsageDetailViewModel と用途・音量監視、保存、試聴 UseCase。
 * 消費（get で解決）: AndroidAudioUsagePreferencesRepository・SoundVolumePreferencesRepository、
 * TextToSpeechRepository・VoicePreferencesRepository・VoiceSpeedPreferencesRepository・VoicePitchPreferencesRepository。
 */
val otherAndroidAudioUsageDetailModule =
    module {
        viewModelOf(::OtherAndroidAudioUsageDetailViewModel)

        factory { ObserveAndroidAudioUsageUseCase(get()) }
        factory { SaveAndroidAudioUsageUseCase(get()) }
        factory { SpeakTextUseCase(get(), get(), get(), get()) }
        factory { ObserveSoundVolumeUseCase(get()) }
        factory { ObserveVoiceUseCase(get()) }
        factory { ObserveVoiceSpeedUseCase(get()) }
        factory { ObserveVoicePitchUseCase(get()) }
    }
