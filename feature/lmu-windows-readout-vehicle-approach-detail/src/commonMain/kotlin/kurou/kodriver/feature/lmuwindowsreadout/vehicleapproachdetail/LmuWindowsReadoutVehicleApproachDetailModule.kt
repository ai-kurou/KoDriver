package kurou.kodriver.feature.lmuwindowsreadout.vehicleapproachdetail

import org.koin.core.module.dsl.viewModel
import org.koin.core.qualifier.named
import org.koin.dsl.module

val lmuWindowsReadoutVehicleApproachDetailModule = module {
    viewModel {
        LmuWindowsReadoutVehicleApproachDetailViewModel(get(), get(), get(), get(), get(), get(named("lmu_windows")))
    }
}
