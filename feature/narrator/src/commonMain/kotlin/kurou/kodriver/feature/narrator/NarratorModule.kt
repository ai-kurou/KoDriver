package kurou.kodriver.feature.narrator

import kurou.kodriver.domain.engine.TextToSpeechEngine
import kurou.kodriver.domain.usecase.ObserveFlagEnabledStatesUseCase
import kurou.kodriver.domain.usecase.ObserveLmuUseCase
import kurou.kodriver.domain.usecase.ObserveProximityUseCase
import kurou.kodriver.domain.usecase.ObserveRaceFlagsUseCase
import kurou.kodriver.domain.usecase.ObserveReadoutEnabledStatesUseCase
import kurou.kodriver.domain.usecase.ObserveReadoutOrderUseCase
import kurou.kodriver.domain.usecase.ObserveSelectedSimulatorUseCase
import kurou.kodriver.domain.usecase.ObserveSkipFirstLapUseCase
import org.koin.core.module.Module
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val narratorModule: Module = module {
    viewModelOf(::LmuNarratorViewModel)
    factory { ObserveFlagEnabledStatesUseCase(get()) }
    factory { ObserveLmuUseCase(get()) }
    factory { ObserveProximityUseCase(get()) }
    factory { ObserveRaceFlagsUseCase(get()) }
    factory { ObserveReadoutEnabledStatesUseCase(get()) }
    factory { ObserveReadoutOrderUseCase(get()) }
    factory { ObserveSelectedSimulatorUseCase(get()) }
    factory { ObserveSkipFirstLapUseCase(get()) }
    factory { VehicleApproachUseCases(get(), get(), get()) }
    single<TextToSpeechEngine> { WavNarratorEngine(get()) }
    includes(platformSoundModule)
}

internal expect val platformSoundModule: Module
