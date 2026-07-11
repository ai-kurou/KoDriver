package kurou.kodriver.feature.otherthemedetail

import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val otherThemeDetailModule = module {
    viewModelOf(::OtherThemeDetailViewModel)
}
