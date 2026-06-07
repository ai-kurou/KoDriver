package kurou.kodriver.feature.readout.vehicleapproach

import kurou.kodriver.domain.usecase.ObserveProximityUseCase
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val vehicleApproachModule = module {
    viewModelOf(::VehicleApproachViewModel)
    factory { ObserveProximityUseCase(get()) }
}
