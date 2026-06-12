package kurou.kodriver.feature.readout.vehicledamage

import kurou.kodriver.domain.usecase.ObserveVehicleDamageEnabledStatesUseCase
import kurou.kodriver.domain.usecase.SaveVehicleDamageEnabledStateUseCase
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val vehicleDamageModule = module {
    viewModelOf(::VehicleDamageViewModel)
    factory { ObserveVehicleDamageEnabledStatesUseCase(get()) }
    factory { SaveVehicleDamageEnabledStateUseCase(get()) }
}
