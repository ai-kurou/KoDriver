package kurou.kodriver.feature.lmuwindowsreadout.tyretemperaturedetail

import kurou.kodriver.domain.usecase.ObserveLmuWindowsTyreTemperatureEnabledStatesUseCase
import kurou.kodriver.domain.usecase.ObserveLmuWindowsTyreTemperatureHighThresholdUseCase
import kurou.kodriver.domain.usecase.SaveLmuWindowsTyreTemperatureEnabledStateUseCase
import kurou.kodriver.domain.usecase.SaveLmuWindowsTyreTemperatureHighThresholdUseCase
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.viewModel
import org.koin.core.qualifier.named
import org.koin.dsl.module

val lmuWindowsReadoutTyreTemperatureDetailModule = module {
    factoryOf(::ObserveLmuWindowsTyreTemperatureHighThresholdUseCase)
    factoryOf(::ObserveLmuWindowsTyreTemperatureEnabledStatesUseCase)
    factoryOf(::SaveLmuWindowsTyreTemperatureHighThresholdUseCase)
    factoryOf(::SaveLmuWindowsTyreTemperatureEnabledStateUseCase)
    viewModel { LmuWindowsReadoutTyreTemperatureDetailViewModel(get(), get(), get(), get(), get(named("lmu_windows"))) }
}
