package kurou.kodriver.feature.lmuconnection

import kurou.kodriver.domain.usecase.CheckLmuConnectionUseCase
import kurou.kodriver.domain.usecase.ObserveSelectedSimulatorUseCase
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val lmuConnectionModule = module {
    factory { CheckLmuConnectionUseCase(get()) }
    factory { ObserveSelectedSimulatorUseCase(get()) }
    viewModelOf(::LmuConnectionViewModel)
}
