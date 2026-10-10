package kurou.kodriver.feature.othervoicespeeddetail

import kurou.kodriver.domain.usecase.ObserveSoundVolumeUseCase
import kurou.kodriver.domain.usecase.ObserveVoicePitchUseCase
import kurou.kodriver.domain.usecase.ObserveVoiceSpeedUseCase
import kurou.kodriver.domain.usecase.ObserveVoiceUseCase
import kurou.kodriver.domain.usecase.SaveVoiceSpeedUseCase
import kurou.kodriver.domain.usecase.SpeakTextUseCase
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

/**
 * 読み上げ速度設定詳細（other-voice-speed-detail feature）の Koin モジュール。
 *
 * ObserveVoicePitchUseCase で保存済みの声の高さを監視する。
 * 提供: OtherVoiceSpeedDetailViewModel と、それが使うドメイン UseCase。
 * 消費（get で解決）: VoiceSpeedPreferencesRepository・VoicePitchPreferencesRepository・VoicePreferencesRepository・
 *   SoundVolumePreferencesRepository（:core:data で登録）、TextToSpeechRepository（:core:text-to-speech-data で登録）。
 */
val otherVoiceSpeedDetailModule =
    module {
        viewModelOf(::OtherVoiceSpeedDetailViewModel)

        factory { ObserveVoiceSpeedUseCase(get()) }
        factory { ObserveVoicePitchUseCase(get()) }
        factory { SaveVoiceSpeedUseCase(get()) }
        factory { ObserveVoiceUseCase(get()) }
        factory { ObserveSoundVolumeUseCase(get()) }
        factory { SpeakTextUseCase(get(), get(), get(), get()) }
    }
