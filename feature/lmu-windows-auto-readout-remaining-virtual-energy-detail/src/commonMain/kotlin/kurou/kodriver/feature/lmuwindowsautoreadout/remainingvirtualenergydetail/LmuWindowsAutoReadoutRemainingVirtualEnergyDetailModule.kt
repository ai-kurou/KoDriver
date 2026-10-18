package kurou.kodriver.feature.lmuwindowsautoreadout.remainingvirtualenergydetail

import kurou.kodriver.domain.lmuwindows.usecase.ObserveLmuWindowsRemainingVirtualEnergyReadoutTextUseCase
import kurou.kodriver.domain.lmuwindows.usecase.ObserveLmuWindowsRemainingVirtualEnergyThresholdPercentageUseCase
import kurou.kodriver.domain.lmuwindows.usecase.SaveLmuWindowsRemainingVirtualEnergyReadoutTextUseCase
import kurou.kodriver.domain.lmuwindows.usecase.SaveLmuWindowsRemainingVirtualEnergyThresholdPercentageUseCase
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
 * バーチャルエネルギー残量アナウンス詳細設定（lmu-windows-auto-readout-remaining-virtual-energy-detail feature）の Koin モジュール。
 *
 * 提供: LmuWindowsAutoReadoutRemainingVirtualEnergyDetailViewModel と、閾値・有効状態・文言の Observe/Save UseCase と読み上げUseCase集約。
 * 消費（get で解決）: LmuWindowsRemainingVirtualEnergyPreferencesRepository・AutoReadoutPreferencesRepository・TextToSpeechRepository・SoundVolumePreferencesRepository（:core:data）、
 *   試聴用の named(Simulator.LmuWindows.id) の PlayStartSoundForKeyUseCase・SpeakTextUseCase（:feature:lmu-windows-narrator で登録）。
 */
val lmuWindowsAutoReadoutRemainingVirtualEnergyDetailModule =
    module {
        viewModel {
            LmuWindowsAutoReadoutRemainingVirtualEnergyDetailViewModel(
                get(),
                get(),
            )
        }

        factory {
            RemainingVirtualEnergyReadoutUseCases(
                get(),
                get(),
                get(named(Simulator.LmuWindows.id)),
                get(named(Simulator.LmuWindows.id)),
                get(),
                get(),
            )
        }
        factoryOf(::ObserveLmuWindowsRemainingVirtualEnergyReadoutTextUseCase)
        factoryOf(::SaveLmuWindowsRemainingVirtualEnergyReadoutTextUseCase)
        factoryOf(::CheckTextToSpeechAvailableUseCase)
        factoryOf(::ObserveSoundVolumeUseCase)
        factoryOf(::ObserveLmuWindowsRemainingVirtualEnergyThresholdPercentageUseCase)
        factoryOf(::SaveLmuWindowsRemainingVirtualEnergyThresholdPercentageUseCase)
        factoryOf(::ObserveAutoReadoutEnabledStatesUseCase)
        factoryOf(::SaveAutoReadoutEnabledStateUseCase)
        factory { RemainingVirtualEnergyUseCases(get(), get(), get(), get()) }
    }
