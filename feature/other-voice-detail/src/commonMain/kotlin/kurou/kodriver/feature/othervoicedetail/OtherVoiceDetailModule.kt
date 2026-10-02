package kurou.kodriver.feature.othervoicedetail

import kurou.kodriver.domain.usecase.GetAvailableVoicesUseCase
import kurou.kodriver.domain.usecase.ObserveSoundVolumeUseCase
import kurou.kodriver.domain.usecase.ObserveVoiceUseCase
import kurou.kodriver.domain.usecase.SaveVoiceUseCase
import kurou.kodriver.domain.usecase.SpeakTextUseCase
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

/**
 * 読み上げ音声設定詳細（other-voice-detail feature）の Koin モジュール。
 *
 * 提供: OtherVoiceDetailViewModel と音声一覧取得・設定監視・保存・試聴・音量監視の UseCase。
 * 消費: TextToSpeechRepository（:core:text-to-speech-data）、SoundVolumePreferencesRepository（:core:data）、
 *   VoicePreferencesRepository（:core:data）、VoiceListRepository（:core:text-to-speech-data の JVM 実装）。
 */
val otherVoiceDetailModule =
    module {
        viewModelOf(::OtherVoiceDetailViewModel)
        factory { GetAvailableVoicesUseCase(get()) }
        factory { ObserveVoiceUseCase(get()) }
        factory { SaveVoiceUseCase(get()) }
        factory { SpeakTextUseCase(get(), get()) }
        factory { ObserveSoundVolumeUseCase(get()) }
    }
