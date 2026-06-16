package kurou.kodriver.feature.serverconnection

import kurou.kodriver.domain.usecase.CheckServerConnectionUseCase
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val serverConnectionModule = module {
    factory { CheckServerConnectionUseCase(get()) }
    viewModelOf(::ServerConnectionViewModel)
}
