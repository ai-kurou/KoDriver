package kurou.kodriver.feature.announcer

import kurou.kodriver.domain.engine.TextToSpeechEngine
import kurou.kodriver.domain.usecase.ObserveProximityUseCase
import kurou.kodriver.domain.usecase.ObserveReadoutEnabledStatesUseCase
import kurou.kodriver.domain.usecase.ObserveSelectedSimulatorUseCase
import org.koin.core.module.Module
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val announcerModule: Module = module {
    viewModelOf(::AnnouncerViewModel)
    factory { ObserveProximityUseCase(get()) }
    factory { ObserveSelectedSimulatorUseCase(get()) }
    factory { ObserveReadoutEnabledStatesUseCase(get()) }
    single<TextToSpeechEngine> { WavAnnouncerEngine(get()) }
    includes(platformSoundModule)
}

internal expect val platformSoundModule: Module
