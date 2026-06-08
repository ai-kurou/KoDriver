package kurou.kodriver.feature.narrator

import kurou.kodriver.domain.engine.TextToSpeechEngine
import kurou.kodriver.domain.usecase.ObserveProximityUseCase
import kurou.kodriver.domain.usecase.ObserveReadoutEnabledStatesUseCase
import kurou.kodriver.domain.usecase.ObserveSelectedSimulatorUseCase
import org.koin.core.module.Module
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val narratorModule: Module = module {
    viewModelOf(::LmuNarratorViewModel)
    factory { ObserveProximityUseCase(get()) }
    factory { ObserveSelectedSimulatorUseCase(get()) }
    factory { ObserveReadoutEnabledStatesUseCase(get()) }
    single<TextToSpeechEngine> { WavNarratorEngine(get()) }
    includes(platformSoundModule)
}

internal expect val platformSoundModule: Module
