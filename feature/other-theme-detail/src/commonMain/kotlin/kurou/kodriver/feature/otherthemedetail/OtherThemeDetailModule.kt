package kurou.kodriver.feature.otherthemedetail

import kurou.kodriver.domain.usecase.ObserveThemeModeUseCase
import kurou.kodriver.domain.usecase.SaveThemeModeUseCase
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val otherThemeDetailModule = module {
    viewModelOf(::OtherThemeDetailViewModel)
    factory { ObserveThemeModeUseCase(get()) }
    factory { SaveThemeModeUseCase(get()) }
}
