package kurou.kodriver.feature.lmuwindowsreadout.braketemperaturedetail

import kurou.kodriver.domain.model.Simulator
import kurou.kodriver.domain.usecase.ObserveLmuWindowsBrakeTemperatureHighThresholdUseCase
import kurou.kodriver.domain.usecase.SaveLmuWindowsBrakeTemperatureHighThresholdUseCase
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.viewModel
import org.koin.core.qualifier.named
import org.koin.dsl.module

/**
 * ブレーキ温度アナウンス詳細設定（lmu-windows-readout-brake-temperature-detail feature）の Koin モジュール。
 *
 * 提供: LmuWindowsReadoutBrakeTemperatureDetailViewModel と、それが使うドメイン UseCase。
 * 消費（get で解決）: LmuWindowsBrakeTemperaturePreferencesRepository（:core:data）、試聴用の
 *   named(Simulator.LmuWindows.id) の TextToSpeechEngine（:feature:lmu-windows-narrator で登録）。
 */
val lmuWindowsReadoutBrakeTemperatureDetailModule =
    module {
        viewModel {
            LmuWindowsReadoutBrakeTemperatureDetailViewModel(get(), get(), get(named(Simulator.LmuWindows.id)))
        }

        factoryOf(::ObserveLmuWindowsBrakeTemperatureHighThresholdUseCase)
        factoryOf(::SaveLmuWindowsBrakeTemperatureHighThresholdUseCase)
    }
