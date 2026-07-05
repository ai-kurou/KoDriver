package kurou.kodriver.feature.readoutlist

import kurou.kodriver.domain.usecase.ObserveLmuWindowsTyreTemperatureEnabledUseCase
import kurou.kodriver.domain.usecase.ObserveReadoutEnabledStatesUseCase
import kurou.kodriver.domain.usecase.ObserveReadoutOrderUseCase
import kurou.kodriver.domain.usecase.ObserveSelectedSimulatorUseCase
import kurou.kodriver.domain.usecase.SaveLmuWindowsTyreTemperatureEnabledUseCase
import kurou.kodriver.domain.usecase.SaveReadoutEnabledStateUseCase
import kurou.kodriver.domain.usecase.SaveReadoutOrderUseCase
import kurou.kodriver.domain.usecase.SaveSelectedSimulatorUseCase
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val readoutListModule = module {
    viewModelOf(::ReadoutListViewModel)
    factory { ReadoutListUseCases(get(), get(), get(), get(), get(), get(), get(), get()) }
    factory { ObserveSelectedSimulatorUseCase(get()) }
    factory { SaveSelectedSimulatorUseCase(get()) }
    factory { ObserveReadoutEnabledStatesUseCase(get()) }
    factory { SaveReadoutEnabledStateUseCase(get()) }
    factory { ObserveLmuWindowsTyreTemperatureEnabledUseCase(get()) }
    factory { SaveLmuWindowsTyreTemperatureEnabledUseCase(get()) }
    factory { ObserveReadoutOrderUseCase(get()) }
    factory { SaveReadoutOrderUseCase(get()) }
}
