package kurou.kodriver.feature.gt7ps5readout.remainingfuellapsdetail

import kurou.kodriver.domain.usecase.ObserveGt7Ps5RemainingFuelLapsUseCase
import kurou.kodriver.domain.usecase.SaveGt7Ps5RemainingFuelLapsUseCase
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val gt7Ps5ReadoutRemainingFuelLapsDetailModule = module {
    viewModel { Gt7Ps5ReadoutRemainingFuelLapsDetailViewModel(get(), get()) }
    factory { ObserveGt7Ps5RemainingFuelLapsUseCase(get()) }
    factory { SaveGt7Ps5RemainingFuelLapsUseCase(get()) }
}
