package kurou.kodriver.feature.lmureadout.vehicledamagedetail

import kurou.kodriver.domain.usecase.ObserveVehicleDamageEnabledStatesUseCase
import kurou.kodriver.domain.usecase.SaveVehicleDamageEnabledStateUseCase
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val lmuReadoutVehicleDamageDetailModule = module {
    viewModelOf(::LmuReadoutVehicleDamageDetailViewModel)
    factory { ObserveVehicleDamageEnabledStatesUseCase(get()) }
    factory { SaveVehicleDamageEnabledStateUseCase(get()) }
}
