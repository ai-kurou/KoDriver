package kurou.kodriver.feature.main

import kurou.kodriver.domain.usecase.CheckAppUpdateAvailableUseCase
import kurou.kodriver.domain.usecase.ObserveExitConfirmationEnabledUseCase
import kurou.kodriver.domain.usecase.ObserveKeepScreenOnUseCase
import kurou.kodriver.domain.usecase.SaveExitConfirmationEnabledUseCase
import kurou.kodriver.domain.usecase.SaveKeepScreenOnUseCase
import org.koin.core.module.Module
import org.koin.core.module.dsl.viewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val mainModule = module {
    factory { CheckAppUpdateAvailableUseCase(get()) }
    factory { ObserveExitConfirmationEnabledUseCase(get()) }
    factory { ObserveKeepScreenOnUseCase(get()) }
    factory { SaveExitConfirmationEnabledUseCase(get()) }
    factory { SaveKeepScreenOnUseCase(get()) }
    viewModel { AppScreenViewModel(get(), currentAppVersion(), get(), get(), get()) }
    viewModelOf(::ConnectionBannerViewModel)
}

expect val mainPlatformModule: Module
