package kurou.kodriver.feature.readoutlist

import kurou.kodriver.domain.usecase.ObserveGt7Ps5RemainingFuelLapsEnabledUseCase
import kurou.kodriver.domain.usecase.ObserveReadoutEnabledStatesUseCase
import kurou.kodriver.domain.usecase.ObserveReadoutOrderUseCase
import kurou.kodriver.domain.usecase.ObserveSelectedSimulatorUseCase
import kurou.kodriver.domain.usecase.SaveGt7Ps5RemainingFuelLapsEnabledUseCase
import kurou.kodriver.domain.usecase.SaveReadoutEnabledStateUseCase
import kurou.kodriver.domain.usecase.SaveReadoutOrderUseCase
import kurou.kodriver.domain.usecase.SaveSelectedSimulatorUseCase
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val readoutListModule = module {
    viewModelOf(::ReadoutListViewModel)
    factory { ObserveSelectedSimulatorUseCase(get()) }
    factory { SaveSelectedSimulatorUseCase(get()) }
    factory { ObserveReadoutEnabledStatesUseCase(get()) }
    factory { SaveReadoutEnabledStateUseCase(get()) }
    factory { ObserveGt7Ps5RemainingFuelLapsEnabledUseCase(get()) }
    factory { SaveGt7Ps5RemainingFuelLapsEnabledUseCase(get()) }
    factory { ObserveReadoutOrderUseCase(get()) }
    factory { SaveReadoutOrderUseCase(get()) }
}
