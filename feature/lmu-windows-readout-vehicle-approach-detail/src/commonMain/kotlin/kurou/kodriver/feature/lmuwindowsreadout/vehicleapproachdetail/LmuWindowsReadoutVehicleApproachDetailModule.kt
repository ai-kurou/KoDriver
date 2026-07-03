package kurou.kodriver.feature.lmuwindowsreadout.vehicleapproachdetail

import kurou.kodriver.domain.usecase.LmuWindowsVehicleApproachPreferencesUseCases
import kurou.kodriver.domain.usecase.ObserveLmuWindowsVehicleApproachLateralThresholdUseCase
import kurou.kodriver.domain.usecase.ObserveLmuWindowsVehicleApproachLongitudinalThresholdUseCase
import kurou.kodriver.domain.usecase.SaveLmuWindowsVehicleApproachLateralThresholdUseCase
import kurou.kodriver.domain.usecase.SaveLmuWindowsVehicleApproachLongitudinalThresholdUseCase
import org.koin.core.module.dsl.viewModel
import org.koin.core.qualifier.named
import org.koin.dsl.module

val lmuReadoutVehicleApproachDetailModule = module {
    viewModel {
        LmuWindowsReadoutVehicleApproachDetailViewModel(get(), get(), get(), get(), get(), get(named("lmu_windows")))
    }
    factory { ObserveLmuWindowsVehicleApproachLateralThresholdUseCase(get()) }
    factory { ObserveLmuWindowsVehicleApproachLongitudinalThresholdUseCase(get()) }
    factory { LmuWindowsVehicleApproachPreferencesUseCases(get()) }
    factory { SaveLmuWindowsVehicleApproachLateralThresholdUseCase(get()) }
    factory { SaveLmuWindowsVehicleApproachLongitudinalThresholdUseCase(get()) }
}
