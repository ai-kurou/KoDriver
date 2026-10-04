package kurou.kodriver.feature.lmuwindowsreadout.tyretemperaturedetail

import kurou.kodriver.domain.model.Simulator
import kurou.kodriver.domain.usecase.CheckTextToSpeechAvailableUseCase
import kurou.kodriver.domain.usecase.ObserveLmuWindowsTyreTemperatureColdReadoutTextUseCase
import kurou.kodriver.domain.usecase.ObserveLmuWindowsTyreTemperatureEnabledStatesUseCase
import kurou.kodriver.domain.usecase.ObserveLmuWindowsTyreTemperatureLowWarningPhasesUseCase
import kurou.kodriver.domain.usecase.ObserveLmuWindowsTyreTemperatureOverheatReadoutTextUseCase
import kurou.kodriver.domain.usecase.ObserveLmuWindowsVehicleClassTyreTemperatureHighThresholdUseCase
import kurou.kodriver.domain.usecase.ObserveLmuWindowsVehicleClassTyreTemperatureSelectionUseCase
import kurou.kodriver.domain.usecase.ObserveSoundVolumeUseCase
import kurou.kodriver.domain.usecase.SaveLmuWindowsTyreTemperatureColdReadoutTextUseCase
import kurou.kodriver.domain.usecase.SaveLmuWindowsTyreTemperatureEnabledStateUseCase
import kurou.kodriver.domain.usecase.SaveLmuWindowsTyreTemperatureLowWarningPhasesUseCase
import kurou.kodriver.domain.usecase.SaveLmuWindowsTyreTemperatureOverheatReadoutTextUseCase
import kurou.kodriver.domain.usecase.SaveLmuWindowsVehicleClassTyreTemperatureHighThresholdUseCase
import kurou.kodriver.domain.usecase.SaveLmuWindowsVehicleClassTyreTemperatureSelectionUseCase
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.viewModel
import org.koin.core.qualifier.named
import org.koin.dsl.module

/**
 * タイヤ温度アナウンス詳細設定（lmu-windows-readout-tyre-temperature-detail feature）の Koin モジュール。
 *
 * 提供: LmuWindowsReadoutTyreTemperatureDetailViewModel、この feature 内で定義した
 *   UseCase 集約 data class（TyreTemperatureUseCases / TyreTemperatureReadoutUseCases）、それが束ねる各ドメイン UseCase。
 * 消費（get で解決）: LmuWindowsTyreTemperaturePreferencesRepository・TextToSpeechRepository・
 *   SoundVolumePreferencesRepository（:core:data）、試聴用の named(Simulator.LmuWindows.id) の
 *   SpeakTextUseCase・PlayStartSoundForKeyUseCase（:feature:lmu-windows-narrator で登録）。
 */
val lmuWindowsReadoutTyreTemperatureDetailModule =
    module {
        // ViewModel（試聴用のTTS依存は TyreTemperatureReadoutUseCases 経由で解決）
        viewModel {
            LmuWindowsReadoutTyreTemperatureDetailViewModel(
                get(),
                get(),
            )
        }

        // この feature 固有の UseCase 集約 data class（本モジュールで定義）
        factory {
            TyreTemperatureUseCases(get(), get(), get(), get(), get(), get(), get(), get())
        }

        // ドメイン UseCase（:core:domain。get() は :core:data の Preferences Repository を解決）
        factory {
            TyreTemperatureReadoutUseCases(
                get(),
                get(),
                get(),
                get(),
                get(named(Simulator.LmuWindows.id)),
                get(named(Simulator.LmuWindows.id)),
                get(),
                get(),
            )
        }
        factoryOf(::ObserveLmuWindowsTyreTemperatureOverheatReadoutTextUseCase)
        factoryOf(::ObserveLmuWindowsTyreTemperatureColdReadoutTextUseCase)
        factoryOf(::SaveLmuWindowsTyreTemperatureOverheatReadoutTextUseCase)
        factoryOf(::SaveLmuWindowsTyreTemperatureColdReadoutTextUseCase)
        factoryOf(::CheckTextToSpeechAvailableUseCase)
        factoryOf(::ObserveSoundVolumeUseCase)
        factoryOf(::ObserveLmuWindowsTyreTemperatureEnabledStatesUseCase)
        factoryOf(::ObserveLmuWindowsTyreTemperatureLowWarningPhasesUseCase)
        factoryOf(::ObserveLmuWindowsVehicleClassTyreTemperatureHighThresholdUseCase)
        factoryOf(::ObserveLmuWindowsVehicleClassTyreTemperatureSelectionUseCase)
        factoryOf(::SaveLmuWindowsTyreTemperatureEnabledStateUseCase)
        factoryOf(::SaveLmuWindowsTyreTemperatureLowWarningPhasesUseCase)
        factoryOf(::SaveLmuWindowsVehicleClassTyreTemperatureHighThresholdUseCase)
        factoryOf(::SaveLmuWindowsVehicleClassTyreTemperatureSelectionUseCase)
    }
