package kurou.kodriver.feature.readout.vehicledamagedetail

import kurou.kodriver.domain.usecase.ObserveVehicleDamageEnabledStatesUseCase
import kurou.kodriver.domain.usecase.SaveVehicleDamageEnabledStateUseCase
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val readoutVehicleDamageDetailModule = module {
    viewModelOf(::ReadoutVehicleDamageDetailViewModel)
    factory { ObserveVehicleDamageEnabledStatesUseCase(get()) }
    factory { SaveVehicleDamageEnabledStateUseCase(get()) }
}
