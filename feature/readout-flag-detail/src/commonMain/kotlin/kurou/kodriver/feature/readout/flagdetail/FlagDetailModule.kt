package kurou.kodriver.feature.readout.flagdetail

import kurou.kodriver.domain.usecase.ObserveFlagEnabledStatesUseCase
import kurou.kodriver.domain.usecase.SaveFlagEnabledStateUseCase
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val flagDetailModule = module {
    viewModelOf(::FlagDetailViewModel)
    factory { ObserveFlagEnabledStatesUseCase(get()) }
    factory { SaveFlagEnabledStateUseCase(get()) }
}
