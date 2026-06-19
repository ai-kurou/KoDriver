package kurou.kodriver.feature.otherlist

import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val otherListModule = module {
    viewModel { OtherListViewModel(get(), currentAppVersion(), currentAppVersionLabel()) }
}
