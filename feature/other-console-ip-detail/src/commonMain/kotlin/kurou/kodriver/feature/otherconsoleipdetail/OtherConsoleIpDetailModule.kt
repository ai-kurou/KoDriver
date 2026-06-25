package kurou.kodriver.feature.otherconsoleipdetail

import kurou.kodriver.domain.usecase.ObserveConsoleAddressUseCase
import kurou.kodriver.domain.usecase.ObserveGt7UdpPortUseCase
import kurou.kodriver.domain.usecase.SaveConsoleAddressUseCase
import kurou.kodriver.domain.usecase.SaveGt7UdpPortUseCase
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val otherConsoleIpDetailModule = module {
    viewModelOf(::OtherConsoleIpDetailViewModel)
    factory { ObserveConsoleAddressUseCase(get()) }
    factory { SaveConsoleAddressUseCase(get()) }
    factory { ObserveGt7UdpPortUseCase(get()) }
    factory { SaveGt7UdpPortUseCase(get()) }
}
