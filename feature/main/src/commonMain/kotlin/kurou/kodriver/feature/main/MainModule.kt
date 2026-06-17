package kurou.kodriver.feature.main

import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val mainModule = module {
    viewModel { AppScreenViewModel(get(), currentAppVersion()) }
}
