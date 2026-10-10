package kurou.kodriver.feature.lmuwindowsreadout.brakeweardetail

import kurou.kodriver.domain.model.Simulator
import kurou.kodriver.domain.usecase.CheckTextToSpeechAvailableUseCase
import kurou.kodriver.domain.usecase.ObserveLmuWindowsBrakeWearReadoutTextUseCase
import kurou.kodriver.domain.usecase.ObserveLmuWindowsBrakeWearRemainingUseCase
import kurou.kodriver.domain.usecase.ObserveLmuWindowsVehicleClassBrakeWearLowThresholdUseCase
import kurou.kodriver.domain.usecase.ObserveLmuWindowsVehicleClassBrakeWearSelectionUseCase
import kurou.kodriver.domain.usecase.ObserveReadoutEnabledStatesUseCase
import kurou.kodriver.domain.usecase.ObserveSoundVolumeUseCase
import kurou.kodriver.domain.usecase.SaveLmuWindowsBrakeWearReadoutTextUseCase
import kurou.kodriver.domain.usecase.SaveLmuWindowsVehicleClassBrakeWearLowThresholdUseCase
import kurou.kodriver.domain.usecase.SaveLmuWindowsVehicleClassBrakeWearSelectionUseCase
import kurou.kodriver.domain.usecase.SaveReadoutEnabledStateUseCase
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.viewModel
import org.koin.core.qualifier.named
import org.koin.dsl.module

/**
 * ブレーキ摩耗アナウンス詳細設定（lmu-windows-readout-brake-wear-detail feature）の Koin モジュール。
 *
 * 提供: LmuWindowsReadoutBrakeWearDetailViewModel、この feature 内で定義した
 *   UseCase 集約 data class（BrakeWearUseCases）、それが束ねる各ドメイン UseCase。
 * 消費（get で解決）: LmuWindowsVehicleClassBrakeWearPreferencesRepository・
 *   ReadoutPreferencesRepository・TextToSpeechRepository・SoundVolumePreferencesRepository（:core:data）、
 *   試聴用の named(Simulator.LmuWindows.id) の
 *   PlaySpeechEventUseCase（:feature:lmu-windows-narrator で登録）。
 */
val lmuWindowsReadoutBrakeWearDetailModule =
    module {
        viewModel {
            LmuWindowsReadoutBrakeWearDetailViewModel(
                get(),
                get(),
                get(),
                get(),
            )
        }

        factory {
            BrakeWearUseCases(get(), get(), get(), get(), get(), get(), get())
        }

        factory {
            BrakeWearReadoutUseCases(get(named(Simulator.LmuWindows.id)), get(), get())
        }
        factoryOf(::ObserveLmuWindowsBrakeWearRemainingUseCase)
        factoryOf(::ObserveLmuWindowsBrakeWearReadoutTextUseCase)
        factoryOf(::SaveLmuWindowsBrakeWearReadoutTextUseCase)
        factoryOf(::CheckTextToSpeechAvailableUseCase)
        factoryOf(::ObserveSoundVolumeUseCase)
        factoryOf(::ObserveLmuWindowsVehicleClassBrakeWearLowThresholdUseCase)
        factoryOf(::ObserveLmuWindowsVehicleClassBrakeWearSelectionUseCase)
        factoryOf(::SaveLmuWindowsVehicleClassBrakeWearLowThresholdUseCase)
        factoryOf(::SaveLmuWindowsVehicleClassBrakeWearSelectionUseCase)
        factoryOf(::ObserveReadoutEnabledStatesUseCase)
        factoryOf(::SaveReadoutEnabledStateUseCase)
    }
