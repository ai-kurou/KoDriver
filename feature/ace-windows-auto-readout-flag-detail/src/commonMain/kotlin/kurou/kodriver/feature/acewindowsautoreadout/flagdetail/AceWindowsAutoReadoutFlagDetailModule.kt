package kurou.kodriver.feature.acewindowsautoreadout.flagdetail

import kurou.kodriver.domain.acewindows.usecase.ObserveAceWindowsFlagEnabledStatesUseCase
import kurou.kodriver.domain.acewindows.usecase.ObserveAceWindowsFlagReadoutTextUseCase
import kurou.kodriver.domain.acewindows.usecase.SaveAceWindowsFlagEnabledStateUseCase
import kurou.kodriver.domain.acewindows.usecase.SaveAceWindowsFlagReadoutTextUseCase
import kurou.kodriver.domain.model.Simulator
import kurou.kodriver.domain.usecase.ObserveSoundVolumeUseCase
import org.koin.core.module.dsl.viewModel
import org.koin.core.qualifier.named
import org.koin.dsl.module

/**
 * ACEフラッグ詳細設定の Koin モジュール。
 * 提供: ViewModel・有効状態とフラッグ文言のObserve/Save UseCase・設定UseCase集約・ObserveSoundVolumeUseCase。
 * 消費: AceWindowsFlagPreferencesRepository・AceWindowsFlagReadoutTextPreferencesRepository・
 *   SoundVolumePreferencesRepository（:core:data）、named(Simulator.AceWindows.id) の
 *   PlayStartSoundForKeyUseCase・SpeakTextUseCase・CheckTextToSpeechAvailableUseCase
 *   （:feature:ace-windows-narrator。SpeakTextUseCaseは同名のObserveVoiceUseCaseを利用する）。
 */
val aceWindowsAutoReadoutFlagDetailModule =
    module {
        viewModel {
            AceWindowsAutoReadoutFlagDetailViewModel(
                get(),
                get(named(Simulator.AceWindows.id)),
                get(named(Simulator.AceWindows.id)),
                get(named(Simulator.AceWindows.id)),
                get(),
            )
        }
        factory { FlagSettingsUseCases(get(), get(), get()) }
        factory { FlagReadoutTextUseCases(get(), get()) }
        factory { ObserveAceWindowsFlagEnabledStatesUseCase(get()) }
        factory { SaveAceWindowsFlagEnabledStateUseCase(get()) }
        factory { ObserveAceWindowsFlagReadoutTextUseCase(get()) }
        factory { SaveAceWindowsFlagReadoutTextUseCase(get()) }
        factory { ObserveSoundVolumeUseCase(get()) }
    }
