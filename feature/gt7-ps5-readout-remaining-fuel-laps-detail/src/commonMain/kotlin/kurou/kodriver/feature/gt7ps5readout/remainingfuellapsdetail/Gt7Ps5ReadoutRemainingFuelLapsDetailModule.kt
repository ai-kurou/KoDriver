package kurou.kodriver.feature.gt7ps5readout.remainingfuellapsdetail

import org.koin.core.module.dsl.viewModel
import org.koin.core.qualifier.named
import org.koin.dsl.module

val gt7Ps5ReadoutRemainingFuelLapsDetailModule = module {
    viewModel { Gt7Ps5ReadoutRemainingFuelLapsDetailViewModel(get(), get(), get(named("gt7_ps5"))) }
}
