package kurou.kodriver.feature.lmuwindowsreadout.mybestlapdetail

import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val lmuWindowsReadoutMyBestLapDetailModule = module {
    viewModel { LmuWindowsReadoutMyBestLapDetailViewModel() }
}
