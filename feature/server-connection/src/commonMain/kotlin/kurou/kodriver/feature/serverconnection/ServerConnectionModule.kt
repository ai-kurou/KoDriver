package kurou.kodriver.feature.serverconnection

import kurou.kodriver.domain.usecase.CheckServerConnectionUseCase
import kurou.kodriver.domain.usecase.ObserveSelectedSimulatorUseCase
import kurou.kodriver.domain.usecase.ObserveServerIpUseCase
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val serverConnectionModule = module {
    factory { CheckServerConnectionUseCase(get()) }
    factory { ObserveServerIpUseCase(get()) }
    factory { ObserveSelectedSimulatorUseCase(get()) }
    viewModelOf(::ServerConnectionViewModel)
}
