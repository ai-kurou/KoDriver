package kurou.kodriver.feature.readout.vehicleapproach

import kurou.kodriver.domain.usecase.ObserveLateralThresholdUseCase
import kurou.kodriver.domain.usecase.ObserveLongitudinalThresholdUseCase
import kurou.kodriver.domain.usecase.ObserveSkipFirstLapUseCase
import kurou.kodriver.domain.usecase.SaveLateralThresholdUseCase
import kurou.kodriver.domain.usecase.SaveLongitudinalThresholdUseCase
import kurou.kodriver.domain.usecase.SaveSkipFirstLapUseCase
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val vehicleApproachModule = module {
    viewModelOf(::VehicleApproachViewModel)
    factory { ObserveLateralThresholdUseCase(get()) }
    factory { ObserveLongitudinalThresholdUseCase(get()) }
    factory { ObserveSkipFirstLapUseCase(get()) }
    factory { SaveLateralThresholdUseCase(get()) }
    factory { SaveLongitudinalThresholdUseCase(get()) }
    factory { SaveSkipFirstLapUseCase(get()) }
}
