package kurou.kodriver.feature.serverconnection

import kurou.kodriver.domain.usecase.FetchServerVersionUseCase
import kurou.kodriver.domain.usecase.ObserveSelectedSimulatorUseCase
import kurou.kodriver.domain.usecase.ObserveServerIpUseCase
import org.koin.core.module.dsl.viewModel
import org.koin.core.qualifier.named
import org.koin.dsl.module

val serverConnectionModule = module {
    factory { FetchServerVersionUseCase(get()) }
    factory { ObserveServerIpUseCase(get()) }
    factory { ObserveSelectedSimulatorUseCase(get()) }
    viewModel { ServerConnectionViewModel(get(), get(), get(), get(named("appVersion"))) }
}
