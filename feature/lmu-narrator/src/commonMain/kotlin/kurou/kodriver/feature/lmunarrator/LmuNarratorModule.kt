package kurou.kodriver.feature.lmunarrator

import kurou.kodriver.domain.engine.TextToSpeechEngine
import kurou.kodriver.domain.usecase.ObserveFlagEnabledStatesUseCase
import kurou.kodriver.domain.usecase.ObserveLmuUseCase
import kurou.kodriver.domain.usecase.ObserveProximityUseCase
import kurou.kodriver.domain.usecase.ObserveRaceFlagsUseCase
import kurou.kodriver.domain.usecase.ObserveReadoutEnabledStatesUseCase
import kurou.kodriver.domain.usecase.ObserveReadoutOrderUseCase
import kurou.kodriver.domain.usecase.ObserveSelectedSimulatorUseCase
import kurou.kodriver.domain.usecase.ObserveSkipFirstLapUseCase
import kurou.kodriver.domain.usecase.ObserveSoundVolumeUseCase
import kurou.kodriver.domain.usecase.ObserveVehicleDamageEnabledStatesUseCase
import kurou.kodriver.domain.usecase.ObserveVehicleDamageUseCase
import kurou.kodriver.domain.usecase.PlaySpeechEventUseCase
import org.koin.core.module.Module
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val lmuNarratorModule: Module = module {
    viewModelOf(::LmuNarratorViewModel)
    factory { ObserveFlagEnabledStatesUseCase(get()) }
    factory { ObserveLmuUseCase(get()) }
    factory { ObserveProximityUseCase(get()) }
    factory { ObserveRaceFlagsUseCase(get()) }
    factory { FlagUseCases(get(), get()) }
    factory { ObserveReadoutEnabledStatesUseCase(get()) }
    factory { ObserveReadoutOrderUseCase(get()) }
    factory { ObserveSelectedSimulatorUseCase(get()) }
    factory { ObserveSkipFirstLapUseCase(get()) }
    factory { ObserveVehicleDamageEnabledStatesUseCase(get()) }
    factory { ObserveVehicleDamageUseCase(get()) }
    factory { VehicleApproachUseCases(get(), get(), get()) }
    factory { VehicleDamageUseCases(get(), get()) }
    factory { ReadoutListUseCases(get(), get(), get()) }
    factory { ObserveSoundVolumeUseCase(get()) }
    single<TextToSpeechEngine> {
        LmuWavNarratorEngine(
            soundPlayer = get(),
            volumeFlow = get<ObserveSoundVolumeUseCase>()(),
        )
    }
    factory { PlaySpeechEventUseCase(get()) }
    includes(platformSoundModule)
}

internal expect val platformSoundModule: Module
