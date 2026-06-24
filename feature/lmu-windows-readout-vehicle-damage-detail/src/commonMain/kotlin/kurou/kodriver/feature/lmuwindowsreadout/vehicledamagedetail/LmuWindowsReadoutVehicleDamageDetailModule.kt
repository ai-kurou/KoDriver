package kurou.kodriver.feature.lmuwindowsreadout.vehicledamagedetail

import kurou.kodriver.domain.usecase.ObserveVehicleDamageEnabledStatesUseCase
import kurou.kodriver.domain.usecase.SaveVehicleDamageEnabledStateUseCase
import org.koin.core.module.dsl.viewModel
import org.koin.core.qualifier.named
import org.koin.dsl.module

val lmuWindowsReadoutVehicleDamageDetailModule = module {
    viewModel { LmuWindowsReadoutVehicleDamageDetailViewModel(get(), get(), get(named("lmu_windows"))) }
    factory { ObserveVehicleDamageEnabledStatesUseCase(get()) }
    factory { SaveVehicleDamageEnabledStateUseCase(get()) }
}
