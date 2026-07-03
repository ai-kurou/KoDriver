package kurou.kodriver.feature.lmuwindowsreadout.mybestlapdetail

import kurou.kodriver.domain.usecase.ObserveLmuWindowsMyBestLapVoiceTypeUseCase
import kurou.kodriver.domain.usecase.SaveLmuWindowsMyBestLapVoiceTypeUseCase
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val lmuWindowsReadoutMyBestLapDetailModule = module {
    viewModel { LmuWindowsReadoutMyBestLapDetailViewModel(get(), get()) }
    factory { ObserveLmuWindowsMyBestLapVoiceTypeUseCase(get()) }
    factory { SaveLmuWindowsMyBestLapVoiceTypeUseCase(get()) }
}
