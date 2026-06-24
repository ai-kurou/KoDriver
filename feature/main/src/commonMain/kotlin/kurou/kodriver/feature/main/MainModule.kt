package kurou.kodriver.feature.main

import kurou.kodriver.domain.usecase.CheckAppUpdateAvailableUseCase
import kurou.kodriver.domain.usecase.CheckGt7Ps5ConnectionUseCase
import kurou.kodriver.domain.usecase.ObserveConsoleAddressUseCase
import kurou.kodriver.domain.usecase.ObserveSelectedSimulatorUseCase
import org.koin.core.module.Module
import org.koin.core.module.dsl.viewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val mainModule = module {
    factory { CheckAppUpdateAvailableUseCase(get()) }
    factory { CheckGt7Ps5ConnectionUseCase(get()) }
    factory { ObserveConsoleAddressUseCase(get()) }
    factory { ObserveSelectedSimulatorUseCase(get()) }
    viewModel { AppScreenViewModel(get(), currentAppVersion()) }
    viewModelOf(::ConnectionBannerViewModel)
}

expect val mainPlatformModule: Module
