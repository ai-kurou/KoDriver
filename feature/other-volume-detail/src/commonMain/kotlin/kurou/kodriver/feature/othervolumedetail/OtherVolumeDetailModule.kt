package kurou.kodriver.feature.othervolumedetail

import kurou.kodriver.domain.usecase.GetDeviceVolumeUseCase
import kurou.kodriver.domain.usecase.ObserveSoundVolumeUseCase
import kurou.kodriver.domain.usecase.ObserveVoicePitchUseCase
import kurou.kodriver.domain.usecase.ObserveVoiceSpeedUseCase
import kurou.kodriver.domain.usecase.ObserveVoiceUseCase
import kurou.kodriver.domain.usecase.SaveSoundVolumeUseCase
import kurou.kodriver.domain.usecase.SetDeviceVolumeUseCase
import kurou.kodriver.domain.usecase.SpeakTextUseCase
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

/**
 * 音量設定詳細（other-volume-detail feature）の Koin モジュール。
 *
 * ObserveVoicePitchUseCase を提供し、VoicePitchPreferencesRepository（:core:data）を消費する。
 * 提供: OtherVolumeDetailViewModel と、それが使うドメイン UseCase。
 * 消費（get で解決）: SoundVolumePreferencesRepository（:core:data で登録）、
 * DeviceVolumeRepository（:core:device-volume-data で登録）、および試聴用の
 * TextToSpeechRepository（:core:text-to-speech-data で登録）と音声・速度・高さの設定Repository（:core:data で登録）。
 */
val otherVolumeDetailModule =
    module {
        // ViewModel
        viewModelOf(::OtherVolumeDetailViewModel)

        // ドメイン UseCase（:core:domain。get() は :core:data / :core:device-volume-data の Repository を解決）
        factory { ObserveSoundVolumeUseCase(get()) }
        factory { SaveSoundVolumeUseCase(get()) }
        factory { GetDeviceVolumeUseCase(get()) }
        factory { SetDeviceVolumeUseCase(get()) }
        factory { SoundVolumeUseCases(get(), get()) }
        factory { DeviceVolumeUseCases(get(), get()) }

        // 試聴再生（読み上げ速度の試聴と同じ、キャンセル可能なTTS）
        factory { ObserveVoiceUseCase(get()) }
        factory { ObserveVoiceSpeedUseCase(get()) }
        factory { ObserveVoicePitchUseCase(get()) }
        factory { SpeakTextUseCase(get(), get(), get(), get()) }
    }
