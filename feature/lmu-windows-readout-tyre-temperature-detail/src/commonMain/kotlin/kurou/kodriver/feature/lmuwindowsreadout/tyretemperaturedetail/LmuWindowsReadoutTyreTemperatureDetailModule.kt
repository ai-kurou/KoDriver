package kurou.kodriver.feature.lmuwindowsreadout.tyretemperaturedetail

import kurou.kodriver.domain.usecase.ObserveLmuWindowsTyreTemperatureEnabledStatesUseCase
import kurou.kodriver.domain.usecase.ObserveLmuWindowsTyreTemperatureHighThresholdUseCase
import kurou.kodriver.domain.usecase.ObserveLmuWindowsTyreTemperatureLowWarningPhasesUseCase
import kurou.kodriver.domain.usecase.SaveLmuWindowsTyreTemperatureEnabledStateUseCase
import kurou.kodriver.domain.usecase.SaveLmuWindowsTyreTemperatureHighThresholdUseCase
import kurou.kodriver.domain.usecase.SaveLmuWindowsTyreTemperatureLowWarningPhasesUseCase
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.viewModel
import org.koin.core.qualifier.named
import org.koin.dsl.module

val lmuWindowsReadoutTyreTemperatureDetailModule = module {
    factoryOf(::ObserveLmuWindowsTyreTemperatureHighThresholdUseCase)
    factoryOf(::ObserveLmuWindowsTyreTemperatureEnabledStatesUseCase)
    factoryOf(::ObserveLmuWindowsTyreTemperatureLowWarningPhasesUseCase)
    factoryOf(::SaveLmuWindowsTyreTemperatureHighThresholdUseCase)
    factoryOf(::SaveLmuWindowsTyreTemperatureEnabledStateUseCase)
    factoryOf(::SaveLmuWindowsTyreTemperatureLowWarningPhasesUseCase)
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
