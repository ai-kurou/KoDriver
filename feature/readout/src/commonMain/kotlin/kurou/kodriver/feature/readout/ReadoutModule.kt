package kurou.kodriver.feature.readout

import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val readoutModule = module {
    viewModelOf(::ReadoutViewModel)
}
