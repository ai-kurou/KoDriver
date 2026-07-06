package kurou.kodriver.feature.lmuwindowsreadout.tyretemperaturedetail

import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val lmuWindowsReadoutTyreTemperatureDetailModule = module {
    viewModelOf(::LmuWindowsReadoutTyreTemperatureDetailViewModel)
}
