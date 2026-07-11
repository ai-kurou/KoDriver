package kurou.kodriver.feature.lmuwindowsreadout.vehicledamagedetail

import org.koin.core.module.dsl.viewModel
import org.koin.core.qualifier.named
import org.koin.dsl.module

val lmuWindowsReadoutVehicleDamageDetailModule = module {
    viewModel { LmuWindowsReadoutVehicleDamageDetailViewModel(get(), get(), get(named("lmu_windows"))) }
}
