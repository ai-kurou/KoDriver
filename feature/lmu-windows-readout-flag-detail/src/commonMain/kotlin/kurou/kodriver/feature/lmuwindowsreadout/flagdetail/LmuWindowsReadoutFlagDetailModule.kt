package kurou.kodriver.feature.lmuwindowsreadout.flagdetail

import kurou.kodriver.domain.usecase.ObserveFlagEnabledStatesUseCase
import kurou.kodriver.domain.usecase.SaveFlagEnabledStateUseCase
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val lmuWindowsReadoutFlagDetailModule = module {
    viewModelOf(::LmuWindowsReadoutFlagDetailViewModel)
    factory { ObserveFlagEnabledStatesUseCase(get()) }
    factory { SaveFlagEnabledStateUseCase(get()) }
}
