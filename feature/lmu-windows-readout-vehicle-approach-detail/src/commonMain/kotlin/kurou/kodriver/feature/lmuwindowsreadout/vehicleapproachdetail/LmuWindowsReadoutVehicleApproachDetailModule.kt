package kurou.kodriver.feature.lmuwindowsreadout.vehicleapproachdetail

import kurou.kodriver.domain.usecase.ObserveLateralThresholdUseCase
import kurou.kodriver.domain.usecase.ObserveLongitudinalThresholdUseCase
import kurou.kodriver.domain.usecase.SaveLateralThresholdUseCase
import kurou.kodriver.domain.usecase.SaveLongitudinalThresholdUseCase
import kurou.kodriver.domain.usecase.VehicleApproachPreferencesUseCases
import org.koin.core.module.dsl.viewModel
import org.koin.core.qualifier.named
import org.koin.dsl.module

val lmuReadoutVehicleApproachDetailModule = module {
    viewModel {
        LmuWindowsReadoutVehicleApproachDetailViewModel(get(), get(), get(), get(), get(), get(named("lmu_windows")))
    }
    factory { ObserveLateralThresholdUseCase(get()) }
    factory { ObserveLongitudinalThresholdUseCase(get()) }
    factory { VehicleApproachPreferencesUseCases(get()) }
    factory { SaveLateralThresholdUseCase(get()) }
    factory { SaveLongitudinalThresholdUseCase(get()) }
}
