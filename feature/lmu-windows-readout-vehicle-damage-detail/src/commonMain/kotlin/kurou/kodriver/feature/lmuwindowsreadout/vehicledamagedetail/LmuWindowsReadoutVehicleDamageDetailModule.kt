package kurou.kodriver.feature.lmuwindowsreadout.vehicledamagedetail

import kurou.kodriver.domain.usecase.ObserveLmuWindowsVehicleDamageEnabledStatesUseCase
import kurou.kodriver.domain.usecase.SaveLmuWindowsVehicleDamageEnabledStateUseCase
import org.koin.core.module.dsl.viewModel
import org.koin.core.qualifier.named
import org.koin.dsl.module

val lmuWindowsReadoutVehicleDamageDetailModule = module {
    viewModel { LmuWindowsReadoutVehicleDamageDetailViewModel(get(), get(), get(named("lmu_windows"))) }
    factory { ObserveLmuWindowsVehicleDamageEnabledStatesUseCase(get()) }
    factory { SaveLmuWindowsVehicleDamageEnabledStateUseCase(get()) }
}
