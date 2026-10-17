package kurou.kodriver.feature.acewindowsautoreadout.remainingfuellapsdetail

import kurou.kodriver.domain.acewindows.usecase.ObserveAceWindowsRemainingFuelLapsEmptyReadoutTextUseCase
import kurou.kodriver.domain.acewindows.usecase.ObserveAceWindowsRemainingFuelLapsReadoutTextUseCase
import kurou.kodriver.domain.acewindows.usecase.ObserveAceWindowsRemainingFuelLapsThresholdUseCase
import kurou.kodriver.domain.acewindows.usecase.SaveAceWindowsRemainingFuelLapsEmptyReadoutTextUseCase
import kurou.kodriver.domain.acewindows.usecase.SaveAceWindowsRemainingFuelLapsReadoutTextUseCase
import kurou.kodriver.domain.acewindows.usecase.SaveAceWindowsRemainingFuelLapsThresholdUseCase
import kurou.kodriver.domain.model.Simulator
import kurou.kodriver.domain.usecase.CheckTextToSpeechAvailableUseCase
import kurou.kodriver.domain.usecase.ObserveAutoReadoutEnabledStatesUseCase
import kurou.kodriver.domain.usecase.ObserveSoundVolumeUseCase
import kurou.kodriver.domain.usecase.SaveAutoReadoutEnabledStateUseCase
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.viewModel
import org.koin.core.qualifier.named
import org.koin.dsl.module

/**
 * ACE 燃料残り周回数アナウンス詳細設定（ace-windows-auto-readout-remaining-fuel-laps-detail feature）の Koin モジュール。
 *
 * 提供: AceWindowsAutoReadoutRemainingFuelLapsDetailViewModel と、閾値・有効状態・文言の Observe/Save UseCase と読み上げUseCase集約。
 * 消費（get で解決）: AceWindowsRemainingFuelLapsPreferencesRepository・AutoReadoutPreferencesRepository・
 *   TextToSpeechRepository・SoundVolumePreferencesRepository（:core:data）、
 *   試聴用の named(Simulator.AceWindows.id) の PlayStartSoundForKeyUseCase・SpeakTextUseCase
 *   （:feature:ace-windows-narrator で登録）。
 */
val aceWindowsAutoReadoutRemainingFuelLapsDetailModule =
    module {
        viewModel {
            AceWindowsAutoReadoutRemainingFuelLapsDetailViewModel(
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
        factoryOf(::ObserveAutoReadoutEnabledStatesUseCase)
        factoryOf(::SaveAutoReadoutEnabledStateUseCase)
        factory { RemainingFuelLapsUseCases(get(), get(), get(), get()) }
    }
