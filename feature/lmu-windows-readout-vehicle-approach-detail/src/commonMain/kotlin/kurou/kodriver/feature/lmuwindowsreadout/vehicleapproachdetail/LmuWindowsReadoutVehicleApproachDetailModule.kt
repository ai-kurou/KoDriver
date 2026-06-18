package kurou.kodriver.feature.lmuwindowsreadout.vehicleapproachdetail

import kurou.kodriver.domain.usecase.ObserveLateralThresholdUseCase
import kurou.kodriver.domain.usecase.ObserveLongitudinalThresholdUseCase
import kurou.kodriver.domain.usecase.SaveLateralThresholdUseCase
import kurou.kodriver.domain.usecase.SaveLongitudinalThresholdUseCase
import kurou.kodriver.domain.usecase.VehicleApproachPreferencesUseCases
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val lmuReadoutVehicleApproachDetailModule = module {
    viewModelOf(::LmuWindowsReadoutVehicleApproachDetailViewModel)
    factory { ObserveLateralThresholdUseCase(get()) }
    factory { ObserveLongitudinalThresholdUseCase(get()) }
    factory { VehicleApproachPreferencesUseCases(get()) }
    factory { SaveLateralThresholdUseCase(get()) }
    factory { SaveLongitudinalThresholdUseCase(get()) }
}
