package kurou.kodriver.feature.gt7ps5connection

import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val gt7Ps5ConnectionModule = module {
    viewModelOf(::Gt7Ps5ConnectionViewModel)
}
