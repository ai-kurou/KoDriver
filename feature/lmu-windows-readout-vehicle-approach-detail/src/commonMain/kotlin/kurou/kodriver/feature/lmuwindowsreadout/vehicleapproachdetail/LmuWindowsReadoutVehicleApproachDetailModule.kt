package kurou.kodriver.feature.lmuwindowsreadout.vehicleapproachdetail

import kurou.kodriver.domain.usecase.LmuWindowsVehicleApproachPreferencesUseCases
import kurou.kodriver.domain.usecase.ObserveLmuWindowsVehicleApproachLateralThresholdUseCase
import kurou.kodriver.domain.usecase.ObserveLmuWindowsVehicleApproachLongitudinalThresholdUseCase
import kurou.kodriver.domain.usecase.SaveLmuWindowsVehicleApproachLateralThresholdUseCase
import kurou.kodriver.domain.usecase.SaveLmuWindowsVehicleApproachLongitudinalThresholdUseCase
import org.koin.core.module.dsl.viewModel
import org.koin.core.qualifier.named
import org.koin.dsl.module

/**
 * 車両接近アナウンス詳細設定（lmu-windows-readout-vehicle-approach-detail feature）の Koin モジュール。
 *
 * 提供: LmuWindowsReadoutVehicleApproachDetailViewModel と、それが使うドメイン UseCase
 *   （閾値の Observe/Save と、開始読み上げ設定をまとめた LmuWindowsVehicleApproachPreferencesUseCases）。
 * 消費（get で解決）: LmuWindowsVehicleApproachThresholdsPreferencesRepository・
 *   LmuWindowsVehicleApproachPreferencesRepository（:core:data）、試聴用の named("lmu_windows") の
 *   TextToSpeechEngine（:feature:lmu-windows-narrator で登録）。
 */
val lmuWindowsReadoutVehicleApproachDetailModule = module {
    // ViewModel（get(named "lmu_windows") は narrator モジュールの TextToSpeechEngine を解決）
    viewModel {
        LmuWindowsReadoutVehicleApproachDetailViewModel(get(), get(), get(), get(), get(), get(named("lmu_windows")))
    }

    // ドメイン UseCase（:core:domain。get() は :core:data の Preferences Repository を解決）
    factory { ObserveLmuWindowsVehicleApproachLateralThresholdUseCase(get()) }
    factory { ObserveLmuWindowsVehicleApproachLongitudinalThresholdUseCase(get()) }
    factory { LmuWindowsVehicleApproachPreferencesUseCases(get()) }
    factory { SaveLmuWindowsVehicleApproachLateralThresholdUseCase(get()) }
    factory { SaveLmuWindowsVehicleApproachLongitudinalThresholdUseCase(get()) }
}
