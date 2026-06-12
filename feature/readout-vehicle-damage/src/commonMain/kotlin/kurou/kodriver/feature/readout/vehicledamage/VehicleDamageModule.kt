package kurou.kodriver.feature.readout.vehicledamage

import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val vehicleDamageModule = module {
    viewModelOf(::VehicleDamageViewModel)
}
