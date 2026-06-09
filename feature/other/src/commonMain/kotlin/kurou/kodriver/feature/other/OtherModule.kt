package kurou.kodriver.feature.other

import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val otherModule = module {
    viewModelOf(::OtherViewModel)
}
