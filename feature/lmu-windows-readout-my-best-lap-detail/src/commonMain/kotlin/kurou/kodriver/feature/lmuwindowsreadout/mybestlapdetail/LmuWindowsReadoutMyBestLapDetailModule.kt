package kurou.kodriver.feature.lmuwindowsreadout.mybestlapdetail

import org.koin.core.module.dsl.viewModel
import org.koin.core.qualifier.named
import org.koin.dsl.module

val lmuWindowsReadoutMyBestLapDetailModule = module {
    viewModel { LmuWindowsReadoutMyBestLapDetailViewModel(get(), get(), get(named("lmu_windows"))) }
}
