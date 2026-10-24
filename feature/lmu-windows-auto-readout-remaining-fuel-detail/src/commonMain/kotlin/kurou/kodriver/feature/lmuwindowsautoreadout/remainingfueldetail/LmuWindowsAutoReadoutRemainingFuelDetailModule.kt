package kurou.kodriver.feature.lmuwindowsautoreadout.remainingfueldetail

import kurou.kodriver.domain.lmuwindows.usecase.ObserveLmuWindowsRemainingFuelReadoutTextUseCase
import kurou.kodriver.domain.lmuwindows.usecase.ObserveLmuWindowsRemainingFuelThresholdPercentageUseCase
import kurou.kodriver.domain.lmuwindows.usecase.SaveLmuWindowsRemainingFuelReadoutTextUseCase
import kurou.kodriver.domain.lmuwindows.usecase.SaveLmuWindowsRemainingFuelThresholdPercentageUseCase
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
 * 燃料残量アナウンス詳細設定（lmu-windows-auto-readout-remaining-virtual-energy-detail feature）の Koin モジュール。
 *
 * 提供: LmuWindowsAutoReadoutRemainingFuelDetailViewModel と、閾値・有効状態・文言の Observe/Save UseCase と読み上げUseCase集約。
 * 消費（get で解決）: LmuWindowsRemainingFuelPreferencesRepository・AutoReadoutPreferencesRepository・TextToSpeechRepository・SoundVolumePreferencesRepository（:core:data）、
 *   試聴用の named(Simulator.LmuWindows.id) の PlayStartSoundForKeyUseCase・SpeakTextUseCase（:feature:lmu-windows-narrator で登録）。
 */
val lmuWindowsAutoReadoutRemainingFuelDetailModule =
    module {
        viewModel {
            LmuWindowsAutoReadoutRemainingFuelDetailViewModel(
                get(),
                get(),
            )
        }

        factory {
            RemainingFuelReadoutUseCases(
                get(),
                get(),
                get(named(Simulator.LmuWindows.id)),
                get(named(Simulator.LmuWindows.id)),
                get(),
                get(),
            )
        }
        factoryOf(::ObserveLmuWindowsRemainingFuelReadoutTextUseCase)
        factoryOf(::SaveLmuWindowsRemainingFuelReadoutTextUseCase)
        factoryOf(::CheckTextToSpeechAvailableUseCase)
        factoryOf(::ObserveSoundVolumeUseCase)
        factoryOf(::ObserveLmuWindowsRemainingFuelThresholdPercentageUseCase)
        factoryOf(::SaveLmuWindowsRemainingFuelThresholdPercentageUseCase)
        factoryOf(::ObserveAutoReadoutEnabledStatesUseCase)
        factoryOf(::SaveAutoReadoutEnabledStateUseCase)
        factory { RemainingFuelUseCases(get(), get(), get(), get()) }
    }
