package kurou.kodriver.feature.acewindowsreadout.tyretemperaturedetail

import kurou.kodriver.domain.model.Simulator
import kurou.kodriver.domain.usecase.ObserveAceWindowsTyreTemperatureEnabledStatesUseCase
import kurou.kodriver.domain.usecase.ObserveAceWindowsTyreTemperatureHighThresholdUseCase
import kurou.kodriver.domain.usecase.SaveAceWindowsTyreTemperatureEnabledStateUseCase
import kurou.kodriver.domain.usecase.SaveAceWindowsTyreTemperatureHighThresholdUseCase
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.viewModel
import org.koin.core.qualifier.named
import org.koin.dsl.module

/**
 * タイヤ温度アナウンス詳細設定（ace-windows-readout-tyre-temperature-detail feature）の Koin モジュール。
 *
 * 提供: AceWindowsReadoutTyreTemperatureDetailViewModel と、それが使うドメイン UseCase。
 * 消費（get で解決）: AceWindowsTyreTemperaturePreferencesRepository（:core:data）、試聴用の
 *   named(Simulator.AceWindows.id) の TextToSpeechEngine（:feature:ace-windows-narrator で登録）。
 */
val aceWindowsReadoutTyreTemperatureDetailModule =
    module {
        // ViewModel（get(named(Simulator.AceWindows.id)) は narrator モジュールの TextToSpeechEngine を解決）
        viewModel {
            AceWindowsReadoutTyreTemperatureDetailViewModel(
                get(),
                get(),
                get(),
                get(),
                get(named(Simulator.AceWindows.id)),
            )
        }

        factoryOf(::ObserveAceWindowsTyreTemperatureEnabledStatesUseCase)
        factoryOf(::ObserveAceWindowsTyreTemperatureHighThresholdUseCase)
        factoryOf(::SaveAceWindowsTyreTemperatureEnabledStateUseCase)
        factoryOf(::SaveAceWindowsTyreTemperatureHighThresholdUseCase)
    }
