package kurou.kodriver.feature.otherconsoleipdetail

import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val otherConsoleIpDetailModule = module {
    viewModelOf(::OtherConsoleIpDetailViewModel)
}
