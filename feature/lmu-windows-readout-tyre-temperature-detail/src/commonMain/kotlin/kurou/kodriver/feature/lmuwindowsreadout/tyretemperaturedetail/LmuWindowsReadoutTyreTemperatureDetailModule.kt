package kurou.kodriver.feature.lmuwindowsreadout.tyretemperaturedetail

import kurou.kodriver.domain.usecase.ObserveLmuWindowsTyreTemperatureHighThresholdUseCase
import kurou.kodriver.domain.usecase.SaveLmuWindowsTyreTemperatureHighThresholdUseCase
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val lmuWindowsReadoutTyreTemperatureDetailModule = module {
    factoryOf(::ObserveLmuWindowsTyreTemperatureHighThresholdUseCase)
    factoryOf(::SaveLmuWindowsTyreTemperatureHighThresholdUseCase)
    viewModelOf(::LmuWindowsReadoutTyreTemperatureDetailViewModel)
}
