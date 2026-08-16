package kurou.kodriver.feature.othervolumedetail

import kurou.kodriver.domain.model.Simulator
import kurou.kodriver.domain.usecase.GetDeviceVolumeUseCase
import kurou.kodriver.domain.usecase.ObserveSoundVolumeUseCase
import kurou.kodriver.domain.usecase.PlaySpeechEventUseCase
import kurou.kodriver.domain.usecase.SaveSoundVolumeUseCase
import kurou.kodriver.domain.usecase.SetDeviceVolumeUseCase
import org.koin.core.module.dsl.viewModelOf
import org.koin.core.qualifier.named
import org.koin.dsl.module

/**
 * 音量設定詳細（other-volume-detail feature）の Koin モジュール。
 *
 * 提供: OtherVolumeDetailViewModel と、それが使うドメイン UseCase。
 * 消費（get で解決）: SoundVolumePreferencesRepository（:core:data で登録）、
 * DeviceVolumeRepository（:core:device-volume-data で登録）、および試聴用の
 * named(Simulator.LmuWindows.id) の TextToSpeechEngine（:feature:lmu-windows-narrator で登録）。
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

        // 試聴再生（named(Simulator.LmuWindows.id) の TextToSpeechEngine に依存）
        factory { PlaySpeechEventUseCase(get(named(Simulator.LmuWindows.id))) }
    }
