package kurou.kodriver.feature.lmuwindowsnarrator

import kurou.kodriver.domain.engine.TextToSpeechEngine
import kurou.kodriver.domain.usecase.DetermineLmuWindowsNarratorReadoutUseCase
import kurou.kodriver.domain.usecase.ObserveFlagEnabledStatesUseCase
import kurou.kodriver.domain.usecase.ObserveLmuWindowsUseCase
import kurou.kodriver.domain.usecase.ObserveProximityUseCase
import kurou.kodriver.domain.usecase.ObserveRaceFlagsUseCase
import kurou.kodriver.domain.usecase.ObserveReadoutEnabledStatesUseCase
import kurou.kodriver.domain.usecase.ObserveReadoutOrderUseCase
import kurou.kodriver.domain.usecase.ObserveReadoutStartSoundTypeUseCase
import kurou.kodriver.domain.usecase.ObserveSelectedSimulatorUseCase
import kurou.kodriver.domain.usecase.ObserveSoundVolumeUseCase
import kurou.kodriver.domain.usecase.ObserveVehicleApproachSkipFirstLapUseCase
import kurou.kodriver.domain.usecase.ObserveVehicleApproachStartReadoutEnabledUseCase
import kurou.kodriver.domain.usecase.ObserveVehicleApproachStartReadoutTypeUseCase
import kurou.kodriver.domain.usecase.ObserveVehicleDamageEnabledStatesUseCase
import kurou.kodriver.domain.usecase.ObserveVehicleDamageUseCase
import kurou.kodriver.domain.usecase.PlaySpeechEventUseCase
import kurou.kodriver.domain.usecase.SaveTelemetryLogUseCase
import org.koin.core.module.Module
import org.koin.core.module.dsl.viewModel
import org.koin.core.qualifier.named
import org.koin.dsl.module

val lmuNarratorModule: Module = module {
    viewModel { LmuWindowsNarratorViewModel(get(), get(), get(), get(), get(named("lmu_windows")), get()) }
    factory { DetermineLmuWindowsNarratorReadoutUseCase() }
    factory { SaveTelemetryLogUseCase(get()) }
    factory { NarratorUseCases(get(), get()) }
    factory { ObserveFlagEnabledStatesUseCase(get()) }
    factory { ObserveLmuWindowsUseCase(get()) }
    factory { ObserveProximityUseCase(get()) }
    factory { ObserveRaceFlagsUseCase(get()) }
    factory { FlagUseCases(get(), get()) }
    factory { ObserveReadoutEnabledStatesUseCase(get()) }
    factory { ObserveReadoutOrderUseCase(get()) }
    factory { ObserveSelectedSimulatorUseCase(get()) }
    factory { ObserveVehicleApproachSkipFirstLapUseCase(get()) }
    factory { ObserveVehicleApproachStartReadoutEnabledUseCase(get()) }
    factory { ObserveVehicleApproachStartReadoutTypeUseCase(get()) }
    factory { ObserveVehicleDamageEnabledStatesUseCase(get()) }
    factory { ObserveVehicleDamageUseCase(get()) }
    factory { VehicleApproachUseCases(get(), get(), get(), get(), get()) }
    factory { VehicleDamageUseCases(get(), get()) }
    factory { ReadoutListUseCases(get(), get(), get()) }
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
