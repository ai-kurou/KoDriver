package kurou.kodriver.feature.lmuwindowsreadout.braketemperaturedetail

import kurou.kodriver.domain.model.Simulator
import kurou.kodriver.domain.usecase.CheckTextToSpeechAvailableUseCase
import kurou.kodriver.domain.usecase.ObserveLmuWindowsBrakeTemperatureReadoutTextUseCase
import kurou.kodriver.domain.usecase.ObserveLmuWindowsVehicleClassBrakeTemperatureHighThresholdUseCase
import kurou.kodriver.domain.usecase.ObserveLmuWindowsVehicleClassBrakeTemperatureSelectionUseCase
import kurou.kodriver.domain.usecase.ObserveReadoutEnabledStatesUseCase
import kurou.kodriver.domain.usecase.ObserveSoundVolumeUseCase
import kurou.kodriver.domain.usecase.SaveLmuWindowsBrakeTemperatureReadoutTextUseCase
import kurou.kodriver.domain.usecase.SaveLmuWindowsVehicleClassBrakeTemperatureHighThresholdUseCase
import kurou.kodriver.domain.usecase.SaveLmuWindowsVehicleClassBrakeTemperatureSelectionUseCase
import kurou.kodriver.domain.usecase.SaveReadoutEnabledStateUseCase
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.viewModel
import org.koin.core.qualifier.named
import org.koin.dsl.module

/**
 * ブレーキ温度アナウンス詳細設定（lmu-windows-readout-brake-temperature-detail feature）の Koin モジュール。
 *
 * 提供: LmuWindowsReadoutBrakeTemperatureDetailViewModel、この feature 内で定義した
 *   UseCase 集約 data class（BrakeTemperatureUseCases）、それが束ねる各ドメイン UseCase。
 * 消費（get で解決）: LmuWindowsVehicleClassBrakeTemperaturePreferencesRepository・
 *   ReadoutPreferencesRepository・TextToSpeechRepository・SoundVolumePreferencesRepository（:core:data）、
 *   試聴用の named(Simulator.LmuWindows.id) の
 *   PlaySpeechEventUseCase（:feature:lmu-windows-narrator で登録）。
 */
val lmuWindowsReadoutBrakeTemperatureDetailModule =
    module {
        viewModel {
            LmuWindowsReadoutBrakeTemperatureDetailViewModel(
                get(),
                get(),
                get(),
                get(),
            )
        }

        factory {
            BrakeTemperatureUseCases(get(), get(), get(), get(), get(), get())
        }

        factory {
            BrakeTemperatureReadoutUseCases(get(named(Simulator.LmuWindows.id)), get(), get())
        }
        factoryOf(::ObserveLmuWindowsBrakeTemperatureReadoutTextUseCase)
        factoryOf(::SaveLmuWindowsBrakeTemperatureReadoutTextUseCase)
        factoryOf(::CheckTextToSpeechAvailableUseCase)
        factoryOf(::ObserveSoundVolumeUseCase)
        factoryOf(::ObserveLmuWindowsVehicleClassBrakeTemperatureHighThresholdUseCase)
        factoryOf(::ObserveLmuWindowsVehicleClassBrakeTemperatureSelectionUseCase)
        factoryOf(::SaveLmuWindowsVehicleClassBrakeTemperatureHighThresholdUseCase)
        factoryOf(::SaveLmuWindowsVehicleClassBrakeTemperatureSelectionUseCase)
        factoryOf(::ObserveReadoutEnabledStatesUseCase)
        factoryOf(::SaveReadoutEnabledStateUseCase)
    }
