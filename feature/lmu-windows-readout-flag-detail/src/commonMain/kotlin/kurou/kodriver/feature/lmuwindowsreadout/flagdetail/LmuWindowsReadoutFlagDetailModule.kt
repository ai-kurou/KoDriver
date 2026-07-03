package kurou.kodriver.feature.lmuwindowsreadout.flagdetail

import kurou.kodriver.domain.usecase.ObserveLmuWindowsFlagEnabledStatesUseCase
import kurou.kodriver.domain.usecase.SaveLmuWindowsFlagEnabledStateUseCase
import org.koin.core.module.dsl.viewModel
import org.koin.core.qualifier.named
import org.koin.dsl.module

val lmuWindowsReadoutFlagDetailModule = module {
    viewModel { LmuWindowsReadoutFlagDetailViewModel(get(), get(), get(named("lmu_windows"))) }
    factory { ObserveLmuWindowsFlagEnabledStatesUseCase(get()) }
    factory { SaveLmuWindowsFlagEnabledStateUseCase(get()) }
}
