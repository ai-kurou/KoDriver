package kurou.kodriver.feature.main

import kurou.kodriver.domain.usecase.CheckAppUpdateAvailableUseCase
import kurou.kodriver.domain.usecase.ObserveExitConfirmationEnabledUseCase
import org.koin.core.module.Module
import org.koin.core.module.dsl.viewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val mainModule = module {
    factory { CheckAppUpdateAvailableUseCase(get()) }
    factory { ObserveExitConfirmationEnabledUseCase(get()) }
    viewModel { AppScreenViewModel(get(), currentAppVersion(), get(), get()) }
    viewModelOf(::ConnectionBannerViewModel)
}

expect val mainPlatformModule: Module
