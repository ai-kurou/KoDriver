package kurou.kodriver.feature.otherconsoleipdetail

import kurou.kodriver.domain.usecase.ObserveConsoleAddressUseCase
import kurou.kodriver.domain.usecase.SaveConsoleAddressUseCase
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val otherConsoleIpDetailModule = module {
    viewModelOf(::OtherConsoleIpDetailViewModel)
    factory { ObserveConsoleAddressUseCase(get()) }
    factory { SaveConsoleAddressUseCase(get()) }
}
