package kurou.kodriver.feature.gt7ps5narrator

import kurou.kodriver.domain.engine.TextToSpeechEngine
import kurou.kodriver.domain.usecase.ObserveReadoutStartSoundTypeUseCase
import kurou.kodriver.domain.usecase.PlaySpeechEventUseCase
import org.koin.core.module.Module
import org.koin.core.qualifier.named
import org.koin.dsl.module

val gt7Ps5NarratorModule: Module = module {
    includes(platformSoundModule)
    factory { ObserveReadoutStartSoundTypeUseCase(get()) }
    single<TextToSpeechEngine>(named("gt7_ps5")) {
        Gt7Ps5WavNarratorEngine(
            soundPlayer = get(),
            startSoundTypeFlow = get<ObserveReadoutStartSoundTypeUseCase>()(),
        )
    }
    factory(named("gt7_ps5")) { PlaySpeechEventUseCase(get(named("gt7_ps5"))) }
}

internal expect val platformSoundModule: Module
