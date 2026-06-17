package kurou.kodriver.feature.lmuwindowsreadout.vehicledamagedetail

import kurou.kodriver.domain.usecase.ObserveVehicleDamageEnabledStatesUseCase
import kurou.kodriver.domain.usecase.SaveVehicleDamageEnabledStateUseCase
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val lmuWindowsReadoutVehicleDamageDetailModule = module {
    viewModelOf(::LmuWindowsReadoutVehicleDamageDetailViewModel)
    factory { ObserveVehicleDamageEnabledStatesUseCase(get()) }
    factory { SaveVehicleDamageEnabledStateUseCase(get()) }
}
