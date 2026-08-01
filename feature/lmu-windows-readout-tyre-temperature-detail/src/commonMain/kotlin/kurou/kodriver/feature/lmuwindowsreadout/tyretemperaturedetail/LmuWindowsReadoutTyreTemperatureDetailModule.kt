package kurou.kodriver.feature.lmuwindowsreadout.tyretemperaturedetail

import kurou.kodriver.domain.usecase.ObserveLmuWindowsTyreTemperatureEnabledStatesUseCase
import kurou.kodriver.domain.usecase.ObserveLmuWindowsTyreTemperatureHighThresholdUseCase
import kurou.kodriver.domain.usecase.ObserveLmuWindowsTyreTemperatureLowWarningPhasesUseCase
import kurou.kodriver.domain.usecase.SaveLmuWindowsTyreTemperatureEnabledStateUseCase
import kurou.kodriver.domain.usecase.SaveLmuWindowsTyreTemperatureHighThresholdUseCase
import kurou.kodriver.domain.usecase.SaveLmuWindowsTyreTemperatureLowWarningPhasesUseCase
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.viewModel
import org.koin.core.qualifier.named
import org.koin.dsl.module

/**
 * タイヤ温度アナウンス詳細設定（lmu-windows-readout-tyre-temperature-detail feature）の Koin モジュール。
 *
 * 提供: LmuWindowsReadoutTyreTemperatureDetailViewModel と、それが使うドメイン UseCase。
 * 消費（get で解決）: LmuWindowsTyreTemperaturePreferencesRepository（:core:data）、試聴用の
 *   named("lmu_windows") の TextToSpeechEngine（:feature:lmu-windows-narrator で登録）。
 */
val lmuWindowsReadoutTyreTemperatureDetailModule =
    module {
    // ViewModel（get(named "lmu_windows") は narrator モジュールの TextToSpeechEngine を解決）
    viewModel {
        LmuWindowsReadoutTyreTemperatureDetailViewModel(
            get(),
            get(),
            get(),
            get(),
            get(),
            get(),
            get(named("lmu_windows")),
        )
    }

    // ドメイン UseCase（:core:domain。get() は :core:data の Preferences Repository を解決）
    factoryOf(::ObserveLmuWindowsTyreTemperatureHighThresholdUseCase)
    factoryOf(::ObserveLmuWindowsTyreTemperatureEnabledStatesUseCase)
    factoryOf(::ObserveLmuWindowsTyreTemperatureLowWarningPhasesUseCase)
    factoryOf(::SaveLmuWindowsTyreTemperatureHighThresholdUseCase)
    factoryOf(::SaveLmuWindowsTyreTemperatureEnabledStateUseCase)
    factoryOf(::SaveLmuWindowsTyreTemperatureLowWarningPhasesUseCase)
}
