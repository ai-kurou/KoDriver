package kurou.kodriver.feature.lmureadout.flagdetail

import kurou.kodriver.domain.usecase.ObserveFlagEnabledStatesUseCase
import kurou.kodriver.domain.usecase.SaveFlagEnabledStateUseCase
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val lmuReadoutFlagDetailModule = module {
    viewModelOf(::LmuReadoutFlagDetailViewModel)
    factory { ObserveFlagEnabledStatesUseCase(get()) }
    factory { SaveFlagEnabledStateUseCase(get()) }
}
