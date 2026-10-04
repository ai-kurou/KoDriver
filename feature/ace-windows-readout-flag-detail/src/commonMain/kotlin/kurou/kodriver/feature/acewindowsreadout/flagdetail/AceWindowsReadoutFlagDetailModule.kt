package kurou.kodriver.feature.acewindowsreadout.flagdetail

import kurou.kodriver.domain.model.Simulator
import kurou.kodriver.domain.usecase.ObserveAceWindowsCheckeredFlagReadoutTextUseCase
import kurou.kodriver.domain.usecase.ObserveAceWindowsFlagEnabledStatesUseCase
import kurou.kodriver.domain.usecase.ObserveSoundVolumeUseCase
import kurou.kodriver.domain.usecase.SaveAceWindowsCheckeredFlagReadoutTextUseCase
import kurou.kodriver.domain.usecase.SaveAceWindowsFlagEnabledStateUseCase
import org.koin.core.module.dsl.viewModel
import org.koin.core.qualifier.named
import org.koin.dsl.module

/**
 * ACEフラッグ詳細設定の Koin モジュール。
 * 提供: ViewModel・有効状態と文言のObserve/Save UseCase・設定UseCase集約・ObserveSoundVolumeUseCase。
 * 消費: AceWindowsFlagPreferencesRepository・AceWindowsFlagReadoutTextPreferencesRepository・
 *   SoundVolumePreferencesRepository（:core:data）、named(Simulator.AceWindows.id) の
 *   PlaySpeechEventUseCase・PlayStartSoundForKeyUseCase・SpeakTextUseCase・CheckTextToSpeechAvailableUseCase
 *   （:feature:ace-windows-narrator。SpeakTextUseCaseは同名のObserveVoiceUseCaseを利用する）。
 */
val aceWindowsReadoutFlagDetailModule =
    module {
        viewModel {
            AceWindowsReadoutFlagDetailViewModel(
                get(),
                get(named(Simulator.AceWindows.id)),
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
        factory { ObserveAceWindowsCheckeredFlagReadoutTextUseCase(get()) }
        factory { SaveAceWindowsCheckeredFlagReadoutTextUseCase(get()) }
        factory { ObserveSoundVolumeUseCase(get()) }
    }
