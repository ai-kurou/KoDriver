package kurou.kodriver.feature.othergt7ps5ipdetail

import kurou.kodriver.domain.usecase.ObserveGt7Ps5AddressUseCase
import kurou.kodriver.domain.usecase.SaveGt7Ps5AddressUseCase
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val otherGt7Ps5IpDetailModule = module {
    viewModelOf(::OtherGt7Ps5IpDetailViewModel)
    factory { ObserveGt7Ps5AddressUseCase(get()) }
    factory { SaveGt7Ps5AddressUseCase(get()) }
}
