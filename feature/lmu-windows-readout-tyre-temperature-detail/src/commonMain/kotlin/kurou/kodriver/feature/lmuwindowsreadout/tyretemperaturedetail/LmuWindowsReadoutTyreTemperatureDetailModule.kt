package kurou.kodriver.feature.lmuwindowsreadout.tyretemperaturedetail

import org.koin.core.module.dsl.viewModel
import org.koin.core.qualifier.named
import org.koin.dsl.module

val lmuWindowsReadoutTyreTemperatureDetailModule = module {
    viewModel {
        LmuWindowsReadoutTyreTemperatureDetailViewModel(
            get(),
            get(),
            get(),
            get(),
            get(),
            get(),
            get(named("lmu_windows")),
        )
    }
}
