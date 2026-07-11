package kurou.kodriver.feature.gt7ps5readout.mybestlapdetail

import org.koin.core.module.dsl.viewModel
import org.koin.core.qualifier.named
import org.koin.dsl.module

val gt7Ps5ReadoutMyBestLapDetailModule = module {
    viewModel { Gt7Ps5ReadoutMyBestLapDetailViewModel(get(), get(), get(named("gt7_ps5"))) }
}
