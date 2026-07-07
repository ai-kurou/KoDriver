package kurou.kodriver.feature.lmuwindowsnarrator

import kurou.kodriver.domain.engine.TextToSpeechEngine
import kurou.kodriver.domain.usecase.DetermineLmuWindowsNarratorReadoutUseCase
import kurou.kodriver.domain.usecase.ObserveLmuWindowsFlagEnabledStatesUseCase
import kurou.kodriver.domain.usecase.ObserveLmuWindowsMyBestLapVoiceTypeUseCase
import kurou.kodriver.domain.usecase.ObserveLmuWindowsProximityUseCase
import kurou.kodriver.domain.usecase.ObserveLmuWindowsRaceFlagsUseCase
import kurou.kodriver.domain.usecase.ObserveLmuWindowsTyreCarcassTemperatureUseCase
import kurou.kodriver.domain.usecase.ObserveLmuWindowsTyreTemperatureHighThresholdUseCase
import kurou.kodriver.domain.usecase.ObserveLmuWindowsUseCase
import kurou.kodriver.domain.usecase.ObserveLmuWindowsVehicleApproachSkipFirstLapUseCase
import kurou.kodriver.domain.usecase.ObserveLmuWindowsVehicleApproachStartReadoutEnabledUseCase
import kurou.kodriver.domain.usecase.ObserveLmuWindowsVehicleApproachStartReadoutTypeUseCase
import kurou.kodriver.domain.usecase.ObserveLmuWindowsVehicleDamageEnabledStatesUseCase
import kurou.kodriver.domain.usecase.ObserveLmuWindowsVehicleDamageUseCase
import kurou.kodriver.domain.usecase.ObserveReadoutEnabledStatesUseCase
import kurou.kodriver.domain.usecase.ObserveReadoutOrderUseCase
import kurou.kodriver.domain.usecase.ObserveReadoutStartSoundTypeUseCase
import kurou.kodriver.domain.usecase.ObserveSelectedSimulatorUseCase
import kurou.kodriver.domain.usecase.ObserveSoundVolumeUseCase
import kurou.kodriver.domain.usecase.PlaySpeechEventUseCase
import kurou.kodriver.domain.usecase.SaveTelemetryLogUseCase
import org.koin.core.module.Module
import org.koin.core.module.dsl.viewModel
import org.koin.core.qualifier.named
import org.koin.dsl.module

val lmuNarratorModule: Module = module {
    viewModel { LmuWindowsNarratorViewModel(get(), get(), get(), get(), get(), get(named("lmu_windows")), get()) }
    factory { DetermineLmuWindowsNarratorReadoutUseCase() }
    factory { SaveTelemetryLogUseCase(get()) }
    factory { NarratorUseCases(get(), get(), get()) }
    factory { ObserveLmuWindowsFlagEnabledStatesUseCase(get()) }
    factory { ObserveLmuWindowsMyBestLapVoiceTypeUseCase(get()) }
    factory { ObserveLmuWindowsUseCase(get()) }
    factory { ObserveLmuWindowsProximityUseCase(get()) }
    factory { ObserveLmuWindowsRaceFlagsUseCase(get()) }
    factory { FlagUseCases(get(), get()) }
    factory { ObserveReadoutEnabledStatesUseCase(get()) }
    factory { ObserveReadoutOrderUseCase(get()) }
    factory { ObserveSelectedSimulatorUseCase(get()) }
    factory { ObserveLmuWindowsVehicleApproachSkipFirstLapUseCase(get()) }
    factory { ObserveLmuWindowsVehicleApproachStartReadoutEnabledUseCase(get()) }
    factory { ObserveLmuWindowsVehicleApproachStartReadoutTypeUseCase(get()) }
    factory { ObserveLmuWindowsVehicleDamageEnabledStatesUseCase(get()) }
    factory { ObserveLmuWindowsVehicleDamageUseCase(get()) }
    factory { VehicleApproachUseCases(get(), get(), get(), get(), get()) }
    factory { VehicleDamageUseCases(get(), get()) }
    factory { ReadoutListUseCases(get(), get(), get()) }
    factory { ObserveLmuWindowsTyreCarcassTemperatureUseCase(get()) }
    factory { ObserveLmuWindowsTyreTemperatureHighThresholdUseCase(get()) }
    factory { TyreTemperatureUseCases(get(), get()) }
    factory(named("lmu_windows")) { PlaySpeechEventUseCase(get(named("lmu_windows"))) }
    includes(platformSoundModule)
    single<TextToSpeechEngine>(named("lmu_windows")) {
        LmuWindowsWavNarratorEngine(
            soundPlayer = get(),
            volumeFlow = ObserveSoundVolumeUseCase(get())(),
            startSoundTypeFlow = ObserveReadoutStartSoundTypeUseCase(get())(),
        )
    }
}

internal expect val platformSoundModule: Module
