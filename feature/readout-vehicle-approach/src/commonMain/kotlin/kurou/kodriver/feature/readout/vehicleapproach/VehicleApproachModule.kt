package kurou.kodriver.feature.readout.vehicleapproach

import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val vehicleApproachModule = module {
    viewModelOf(::VehicleApproachViewModel)
}
