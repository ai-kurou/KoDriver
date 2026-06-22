package kurou.kodriver.feature.gt7ps5connection

import kurou.kodriver.domain.usecase.CheckGt7Ps5ConnectionUseCase
import kurou.kodriver.domain.usecase.ObserveSelectedSimulatorUseCase
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val gt7Ps5ConnectionModule = module {
    factory { CheckGt7Ps5ConnectionUseCase(get()) }
    factory { ObserveSelectedSimulatorUseCase(get()) }
    viewModelOf(::Gt7Ps5ConnectionViewModel)
}
