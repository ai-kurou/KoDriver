package kurou.kodriver.feature.acewindowsreadout.remainingfueldetail

import kurou.kodriver.domain.model.Simulator
import kurou.kodriver.domain.usecase.ObserveAceWindowsRemainingFuelThresholdPercentageUseCase
import kurou.kodriver.domain.usecase.ObserveReadoutEnabledStatesUseCase
import kurou.kodriver.domain.usecase.SaveAceWindowsRemainingFuelThresholdPercentageUseCase
import kurou.kodriver.domain.usecase.SaveReadoutEnabledStateUseCase
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.viewModel
import org.koin.core.qualifier.named
import org.koin.dsl.module

/**
 * 残り燃料アナウンス詳細設定（ace-windows-readout-remaining-fuel-detail feature）の Koin モジュール。
 *
 * 提供: AceWindowsReadoutRemainingFuelDetailViewModel と、それが使うドメイン UseCase。
 * 消費（get で解決）: AceWindowsRemainingFuelPreferencesRepository・ReadoutPreferencesRepository（:core:data）、
 *   試聴用の named(Simulator.AceWindows.id) の TextToSpeechEngine（:feature:ace-windows-narrator で登録）。
 */
val aceWindowsReadoutRemainingFuelDetailModule =
    module {
        // ViewModel（get(named(Simulator.AceWindows.id)) は narrator モジュールの TextToSpeechEngine を解決）
        viewModel {
            AceWindowsReadoutRemainingFuelDetailViewModel(
                get(),
                get(named(Simulator.AceWindows.id)),
            )
        }

        factory { RemainingFuelUseCases(get(), get(), get(), get()) }

        factoryOf(::ObserveAceWindowsRemainingFuelThresholdPercentageUseCase)
        factoryOf(::SaveAceWindowsRemainingFuelThresholdPercentageUseCase)
        factoryOf(::ObserveReadoutEnabledStatesUseCase)
        factoryOf(::SaveReadoutEnabledStateUseCase)
    }
