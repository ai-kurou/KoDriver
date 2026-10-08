package kurou.kodriver.feature.acewindowsreadout.remainingfuellapsdetail

import kurou.kodriver.domain.model.Simulator
import kurou.kodriver.domain.usecase.CheckTextToSpeechAvailableUseCase
import kurou.kodriver.domain.usecase.ObserveAceWindowsRemainingFuelLapsEmptyReadoutTextUseCase
import kurou.kodriver.domain.usecase.ObserveAceWindowsRemainingFuelLapsReadoutTextUseCase
import kurou.kodriver.domain.usecase.ObserveAceWindowsRemainingFuelLapsThresholdUseCase
import kurou.kodriver.domain.usecase.ObserveReadoutEnabledStatesUseCase
import kurou.kodriver.domain.usecase.ObserveSoundVolumeUseCase
import kurou.kodriver.domain.usecase.SaveAceWindowsRemainingFuelLapsEmptyReadoutTextUseCase
import kurou.kodriver.domain.usecase.SaveAceWindowsRemainingFuelLapsReadoutTextUseCase
import kurou.kodriver.domain.usecase.SaveAceWindowsRemainingFuelLapsThresholdUseCase
import kurou.kodriver.domain.usecase.SaveReadoutEnabledStateUseCase
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.viewModel
import org.koin.core.qualifier.named
import org.koin.dsl.module

/**
 * ACE 燃料残り周回数アナウンス詳細設定（ace-windows-readout-remaining-fuel-laps-detail feature）の Koin モジュール。
 *
 * 提供: AceWindowsReadoutRemainingFuelLapsDetailViewModel と、閾値・有効状態・文言の Observe/Save UseCase と読み上げUseCase集約。
 * 消費（get で解決）: AceWindowsRemainingFuelLapsPreferencesRepository・ReadoutPreferencesRepository・
 *   TextToSpeechRepository・SoundVolumePreferencesRepository（:core:data）、
 *   試聴用の named(Simulator.AceWindows.id) の PlayStartSoundForKeyUseCase・SpeakTextUseCase
 *   （:feature:ace-windows-narrator で登録）。
 */
val aceWindowsReadoutRemainingFuelLapsDetailModule =
    module {
        viewModel {
            AceWindowsReadoutRemainingFuelLapsDetailViewModel(
                get(),
                get(),
            )
        }

        factory {
            RemainingFuelLapsReadoutUseCases(
                get(),
                get(),
                get(),
                get(),
                get(named(Simulator.AceWindows.id)),
                get(named(Simulator.AceWindows.id)),
                get(),
                get(),
            )
        }
        factoryOf(::ObserveAceWindowsRemainingFuelLapsReadoutTextUseCase)
        factoryOf(::SaveAceWindowsRemainingFuelLapsReadoutTextUseCase)
        factoryOf(::ObserveAceWindowsRemainingFuelLapsEmptyReadoutTextUseCase)
        factoryOf(::SaveAceWindowsRemainingFuelLapsEmptyReadoutTextUseCase)
        factoryOf(::CheckTextToSpeechAvailableUseCase)
        factoryOf(::ObserveSoundVolumeUseCase)
        factoryOf(::ObserveAceWindowsRemainingFuelLapsThresholdUseCase)
        factoryOf(::SaveAceWindowsRemainingFuelLapsThresholdUseCase)
        factoryOf(::ObserveReadoutEnabledStatesUseCase)
        factoryOf(::SaveReadoutEnabledStateUseCase)
        factory { RemainingFuelLapsUseCases(get(), get(), get(), get()) }
    }
