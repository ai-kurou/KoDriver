package kurou.kodriver.feature.othervoicepitchdetail

import kurou.kodriver.domain.usecase.ObserveSoundVolumeUseCase
import kurou.kodriver.domain.usecase.ObserveVoicePitchUseCase
import kurou.kodriver.domain.usecase.ObserveVoiceSpeedUseCase
import kurou.kodriver.domain.usecase.ObserveVoiceUseCase
import kurou.kodriver.domain.usecase.SaveVoicePitchUseCase
import kurou.kodriver.domain.usecase.SpeakTextUseCase
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

/**
 * 声の高さ設定詳細（other-voice-pitch-detail feature）の Koin モジュール。
 *
 * ObserveVoiceSpeedUseCase で保存済みの読み上げ速度を監視する。
 * 提供: OtherVoicePitchDetailViewModel と、それが使うドメイン UseCase。
 * 消費（get で解決）: VoicePitchPreferencesRepository・VoiceSpeedPreferencesRepository・VoicePreferencesRepository・
 *   SoundVolumePreferencesRepository（:core:data で登録）、TextToSpeechRepository（:core:text-to-speech-data で登録）。
 */
val otherVoicePitchDetailModule =
    module {
        viewModelOf(::OtherVoicePitchDetailViewModel)

        factory { ObserveVoicePitchUseCase(get()) }
        factory { ObserveVoiceSpeedUseCase(get()) }
        factory { SaveVoicePitchUseCase(get()) }
        factory { ObserveVoiceUseCase(get()) }
        factory { ObserveSoundVolumeUseCase(get()) }
        factory { SpeakTextUseCase(get(), get(), get(), get()) }
    }
