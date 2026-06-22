package kurou.kodriver.feature.main

import kurou.kodriver.domain.usecase.CheckAppUpdateAvailableUseCase
import kurou.kodriver.domain.usecase.CheckGt7Ps5ConnectionUseCase
import org.koin.core.module.dsl.viewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val mainModule = module {
    factory { CheckAppUpdateAvailableUseCase(get()) }
    factory { CheckGt7Ps5ConnectionUseCase(get()) }
    viewModel { AppScreenViewModel(get(), currentAppVersion()) }
    viewModelOf(::ConnectionBannerViewModel)
}
