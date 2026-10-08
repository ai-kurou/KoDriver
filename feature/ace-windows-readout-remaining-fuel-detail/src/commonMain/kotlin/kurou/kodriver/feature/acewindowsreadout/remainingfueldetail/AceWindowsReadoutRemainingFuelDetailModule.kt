package kurou.kodriver.feature.acewindowsreadout.remainingfueldetail

import kurou.kodriver.domain.model.Simulator
import kurou.kodriver.domain.usecase.CheckTextToSpeechAvailableUseCase
import kurou.kodriver.domain.usecase.ObserveAceWindowsRemainingFuelReadoutTextUseCase
import kurou.kodriver.domain.usecase.ObserveAceWindowsRemainingFuelThresholdPercentageUseCase
import kurou.kodriver.domain.usecase.ObserveReadoutEnabledStatesUseCase
import kurou.kodriver.domain.usecase.ObserveSoundVolumeUseCase
import kurou.kodriver.domain.usecase.SaveAceWindowsRemainingFuelReadoutTextUseCase
import kurou.kodriver.domain.usecase.SaveAceWindowsRemainingFuelThresholdPercentageUseCase
import kurou.kodriver.domain.usecase.SaveReadoutEnabledStateUseCase
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.viewModel
import org.koin.core.qualifier.named
import org.koin.dsl.module

/**
 * ACE 燃料残量アナウンス詳細設定（ace-windows-readout-remaining-fuel-detail feature）の Koin モジュール。
 *
 * 提供: AceWindowsReadoutRemainingFuelDetailViewModel と、閾値・有効状態・文言の Observe/Save UseCase と読み上げUseCase集約。
 * 消費（get で解決）: AceWindowsRemainingFuelPreferencesRepository・ReadoutPreferencesRepository・
 *   TextToSpeechRepository・SoundVolumePreferencesRepository（:core:data）、
 *   試聴用の named(Simulator.AceWindows.id) の PlayStartSoundForKeyUseCase・SpeakTextUseCase（:feature:ace-windows-narrator で登録）。
 */
val aceWindowsReadoutRemainingFuelDetailModule =
    module {
        viewModel {
            AceWindowsReadoutRemainingFuelDetailViewModel(
                get(),
                get(),
            )
        }

        factory {
            RemainingFuelReadoutUseCases(
                get(),
                get(),
                get(named(Simulator.AceWindows.id)),
                get(named(Simulator.AceWindows.id)),
                get(),
                get(),
            )
        }
        factoryOf(::ObserveAceWindowsRemainingFuelReadoutTextUseCase)
        factoryOf(::SaveAceWindowsRemainingFuelReadoutTextUseCase)
        factoryOf(::CheckTextToSpeechAvailableUseCase)
        factoryOf(::ObserveSoundVolumeUseCase)
        factoryOf(::ObserveAceWindowsRemainingFuelThresholdPercentageUseCase)
        factoryOf(::SaveAceWindowsRemainingFuelThresholdPercentageUseCase)
        factoryOf(::ObserveReadoutEnabledStatesUseCase)
        factoryOf(::SaveReadoutEnabledStateUseCase)
        factory { RemainingFuelUseCases(get(), get(), get(), get()) }
    }
