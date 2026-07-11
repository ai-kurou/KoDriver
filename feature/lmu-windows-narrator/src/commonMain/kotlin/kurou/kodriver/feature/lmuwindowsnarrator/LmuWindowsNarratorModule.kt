package kurou.kodriver.feature.lmuwindowsnarrator

import kurou.kodriver.domain.engine.TextToSpeechEngine
import kurou.kodriver.domain.usecase.ObserveReadoutStartSoundTypeUseCase
import kurou.kodriver.domain.usecase.ObserveSoundVolumeUseCase
import kurou.kodriver.domain.usecase.PlaySpeechEventUseCase
import org.koin.core.module.Module
import org.koin.core.module.dsl.viewModel
import org.koin.core.qualifier.named
import org.koin.dsl.module

val lmuWindowsNarratorModule: Module = module {
    viewModel { LmuWindowsNarratorViewModel(get(), get(), get(), get(), get(), get(named("lmu_windows")), get()) }
    factory { NarratorUseCases(get(), get(), get()) }
    factory { FlagUseCases(get(), get()) }
    factory { VehicleApproachUseCases(get(), get(), get(), get(), get()) }
    factory { VehicleDamageUseCases(get(), get()) }
    factory { ReadoutListUseCases(get(), get(), get()) }
    factory { TyreTemperatureUseCases(get(), get(), get(), get()) }
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
