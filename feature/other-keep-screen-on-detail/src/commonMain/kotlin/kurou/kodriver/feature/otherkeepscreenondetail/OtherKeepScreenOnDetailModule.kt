package kurou.kodriver.feature.otherkeepscreenondetail

import kurou.kodriver.domain.usecase.ObserveKeepScreenOnUseCase
import kurou.kodriver.domain.usecase.SaveKeepScreenOnUseCase
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val otherKeepScreenOnDetailModule = module {
    viewModelOf(::OtherKeepScreenOnDetailViewModel)
    factory { ObserveKeepScreenOnUseCase(get()) }
    factory { SaveKeepScreenOnUseCase(get()) }
}
