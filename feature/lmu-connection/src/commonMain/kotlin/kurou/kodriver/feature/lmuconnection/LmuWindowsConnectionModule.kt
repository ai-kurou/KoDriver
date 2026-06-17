package kurou.kodriver.feature.lmuconnection

import kurou.kodriver.domain.usecase.CheckLmuWindowsConnectionUseCase
import kurou.kodriver.domain.usecase.ObserveSelectedSimulatorUseCase
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val lmuConnectionModule = module {
    factory { CheckLmuWindowsConnectionUseCase(get()) }
    factory { ObserveSelectedSimulatorUseCase(get()) }
    viewModelOf(::LmuWindowsConnectionViewModel)
}
