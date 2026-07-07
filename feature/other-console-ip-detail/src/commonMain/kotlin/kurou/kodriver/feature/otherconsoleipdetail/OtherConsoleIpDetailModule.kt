package kurou.kodriver.feature.otherconsoleipdetail

import kurou.kodriver.domain.usecase.ObserveConsoleAddressUseCase
import kurou.kodriver.domain.usecase.ObserveGt7Ps5UdpPortUseCase
import kurou.kodriver.domain.usecase.SaveConsoleAddressUseCase
import kurou.kodriver.domain.usecase.SaveGt7Ps5UdpPortUseCase
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val otherConsoleIpDetailModule = module {
    viewModelOf(::OtherConsoleIpDetailViewModel)
    factory { ObserveConsoleAddressUseCase(get()) }
    factory { SaveConsoleAddressUseCase(get()) }
    factory { ObserveGt7Ps5UdpPortUseCase(get()) }
    factory { SaveGt7Ps5UdpPortUseCase(get()) }
}
