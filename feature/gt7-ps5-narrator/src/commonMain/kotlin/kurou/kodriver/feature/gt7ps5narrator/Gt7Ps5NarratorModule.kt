package kurou.kodriver.feature.gt7ps5narrator

import kurou.kodriver.domain.engine.TextToSpeechEngine
import kurou.kodriver.domain.usecase.DetermineGt7Ps5NarratorReadoutUseCase
import kurou.kodriver.domain.usecase.ObserveGt7Ps5RemainingFuelLapsEnabledUseCase
import kurou.kodriver.domain.usecase.ObserveGt7Ps5RemainingFuelLapsUseCase
import kurou.kodriver.domain.usecase.ObserveGt7Ps5UseCase
import kurou.kodriver.domain.usecase.ObserveMyBestLapVoiceTypeUseCase
import kurou.kodriver.domain.usecase.ObserveReadoutEnabledStatesUseCase
import kurou.kodriver.domain.usecase.ObserveReadoutOrderUseCase
import kurou.kodriver.domain.usecase.ObserveReadoutStartSoundTypeUseCase
import kurou.kodriver.domain.usecase.ObserveSelectedSimulatorUseCase
import kurou.kodriver.domain.usecase.ObserveSoundVolumeUseCase
import kurou.kodriver.domain.usecase.PlaySpeechEventUseCase
import org.koin.core.module.Module
import org.koin.core.module.dsl.viewModel
import org.koin.core.qualifier.named
import org.koin.dsl.module

val gt7Ps5NarratorModule: Module = module {
    viewModel { Gt7Ps5NarratorViewModel(get(), get(), get(), get(named("gt7_ps5")), get()) }
    includes(platformSoundModule)
    factory { DetermineGt7Ps5NarratorReadoutUseCase() }
    factory { ObserveGt7Ps5UseCase(get()) }
    factory { ObserveMyBestLapVoiceTypeUseCase(get()) }
    factory { MyBestLapUseCases(get(), get()) }
    factory { ObserveReadoutEnabledStatesUseCase(get()) }
    factory { ObserveReadoutOrderUseCase(get()) }
    factory { ObserveSelectedSimulatorUseCase(get()) }
    factory { ReadoutListUseCases(get(), get(), get()) }
    factory { ObserveGt7Ps5RemainingFuelLapsUseCase(get()) }
    factory { ObserveGt7Ps5RemainingFuelLapsEnabledUseCase(get()) }
    factory { RemainingFuelLapsUseCases(get(), get()) }
    single<TextToSpeechEngine>(named("gt7_ps5")) {
        Gt7Ps5WavNarratorEngine(
            soundPlayer = get(),
            volumeFlow = ObserveSoundVolumeUseCase(get())(),
            startSoundTypeFlow = ObserveReadoutStartSoundTypeUseCase(get())(),
        )
    }
    factory(named("gt7_ps5")) { PlaySpeechEventUseCase(get(named("gt7_ps5"))) }
}

internal expect val platformSoundModule: Module
