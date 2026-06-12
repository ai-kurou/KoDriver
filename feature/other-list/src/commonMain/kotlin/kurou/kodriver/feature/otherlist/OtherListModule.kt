package kurou.kodriver.feature.otherlist

import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val otherListModule = module {
    viewModelOf(::OtherListViewModel)
}
