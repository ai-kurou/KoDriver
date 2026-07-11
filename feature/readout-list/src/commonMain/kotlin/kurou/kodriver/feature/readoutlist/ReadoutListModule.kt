package kurou.kodriver.feature.readoutlist

import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val readoutListModule = module {
    viewModelOf(::ReadoutListViewModel)
}
