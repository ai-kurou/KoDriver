package kurou.kodriver.feature.otherserveripdetail

import kurou.kodriver.domain.usecase.ObserveServerIpUseCase
import kurou.kodriver.domain.usecase.SaveServerIpUseCase
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val otherServerIpDetailModule = module {
    viewModelOf(::OtherServerIpDetailViewModel)
    factory { ObserveServerIpUseCase(get()) }
    factory { SaveServerIpUseCase(get()) }
    factory<ServerConnectivityChecker> { createServerConnectivityChecker() }
}
