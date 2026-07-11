package kurou.kodriver.feature.main

import org.koin.core.module.Module
import org.koin.core.module.dsl.viewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val mainModule = module {
    viewModel { AppScreenViewModel(get(), currentAppVersion(), get(), get(), get()) }
    viewModelOf(::ConnectionBannerViewModel)
}

expect val mainPlatformModule: Module
