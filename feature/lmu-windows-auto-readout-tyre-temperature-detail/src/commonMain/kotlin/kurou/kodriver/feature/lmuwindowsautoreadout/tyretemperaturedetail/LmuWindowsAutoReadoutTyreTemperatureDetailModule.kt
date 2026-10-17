package kurou.kodriver.feature.lmuwindowsautoreadout.tyretemperaturedetail

import kurou.kodriver.domain.lmuwindows.usecase.ObserveLmuWindowsTyreTemperatureColdReadoutTextUseCase
import kurou.kodriver.domain.lmuwindows.usecase.ObserveLmuWindowsTyreTemperatureEnabledStatesUseCase
import kurou.kodriver.domain.lmuwindows.usecase.ObserveLmuWindowsTyreTemperatureLowWarningPhasesUseCase
import kurou.kodriver.domain.lmuwindows.usecase.ObserveLmuWindowsTyreTemperatureOverheatReadoutTextUseCase
import kurou.kodriver.domain.lmuwindows.usecase.ObserveLmuWindowsVehicleClassTyreTemperatureHighThresholdUseCase
import kurou.kodriver.domain.lmuwindows.usecase.ObserveLmuWindowsVehicleClassTyreTemperatureSelectionUseCase
import kurou.kodriver.domain.lmuwindows.usecase.SaveLmuWindowsTyreTemperatureColdReadoutTextUseCase
import kurou.kodriver.domain.lmuwindows.usecase.SaveLmuWindowsTyreTemperatureEnabledStateUseCase
import kurou.kodriver.domain.lmuwindows.usecase.SaveLmuWindowsTyreTemperatureLowWarningPhasesUseCase
import kurou.kodriver.domain.lmuwindows.usecase.SaveLmuWindowsTyreTemperatureOverheatReadoutTextUseCase
import kurou.kodriver.domain.lmuwindows.usecase.SaveLmuWindowsVehicleClassTyreTemperatureHighThresholdUseCase
import kurou.kodriver.domain.lmuwindows.usecase.SaveLmuWindowsVehicleClassTyreTemperatureSelectionUseCase
import kurou.kodriver.domain.model.Simulator
import kurou.kodriver.domain.usecase.CheckTextToSpeechAvailableUseCase
import kurou.kodriver.domain.usecase.ObserveSoundVolumeUseCase
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.viewModel
import org.koin.core.qualifier.named
import org.koin.dsl.module

/**
 * タイヤ温度アナウンス詳細設定（lmu-windows-auto-readout-tyre-temperature-detail feature）の Koin モジュール。
 *
 * 提供: LmuWindowsAutoReadoutTyreTemperatureDetailViewModel、この feature 内で定義した
 *   UseCase 集約 data class（TyreTemperatureUseCases / TyreTemperatureReadoutUseCases）、それが束ねる各ドメイン UseCase。
 * 消費（get で解決）: LmuWindowsTyreTemperaturePreferencesRepository・LmuWindowsTyreTemperatureReadoutTextPreferencesRepository・TextToSpeechRepository・
 *   SoundVolumePreferencesRepository（:core:data）、試聴用の named(Simulator.LmuWindows.id) の
 *   SpeakTextUseCase・PlayStartSoundForKeyUseCase（:feature:lmu-windows-narrator で登録）。
 */
val lmuWindowsAutoReadoutTyreTemperatureDetailModule =
    module {
        // ViewModel（試聴用のTTS依存は TyreTemperatureReadoutUseCases 経由で解決）
        viewModel {
            LmuWindowsAutoReadoutTyreTemperatureDetailViewModel(
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
