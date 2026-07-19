package kurou.kodriver.feature.gt7ps5narrator

import kurou.kodriver.domain.engine.TextToSpeechEngine
import kurou.kodriver.domain.usecase.DetermineGt7Ps5NarratorReadoutUseCase
import kurou.kodriver.domain.usecase.ObserveGt7Ps5MyBestLapVoiceTypeUseCase
import kurou.kodriver.domain.usecase.ObserveGt7Ps5RemainingFuelLapsUseCase
import kurou.kodriver.domain.usecase.ObserveGt7Ps5UseCase
import kurou.kodriver.domain.usecase.ObserveQueueEnabledStatesUseCase
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

/**
 * GT7 PS5 アナウンス制御（gt7-ps5-narrator feature）の Koin モジュール。
 *
 * 提供: Gt7Ps5NarratorViewModel、Gt7Ps5NarratorEventProcessor、この feature 内で定義した UseCase 集約 data class
 *   （MyBestLapUseCases / ReadoutListUseCases / RemainingFuelLapsUseCases）、それらが束ねる
 *   各ドメイン UseCase、および named("gt7_ps5") の音声再生系
 *   （PlaySpeechEventUseCase・TextToSpeechEngine）。
 * 消費（get で解決）: 各 UseCase の依存 Repository（:core:gt7-ps5-data / :core:data）、
 *   SoundPlayer（[platformSoundModule]）。
 * 音声系は LMU と区別するため named("gt7_ps5") で登録している。
 */
val gt7Ps5NarratorModule: Module = module {
    // ViewModel（Gt7Ps5NarratorEventProcessor 経由で下記の TextToSpeechEngine を利用）
    viewModel { Gt7Ps5NarratorViewModel(get(), get(), get(), get(), get()) }

    // この feature 固有の UseCase 集約 data class（本モジュールで定義）
    factory { MyBestLapUseCases(get(), get()) }
    factory { ReadoutListUseCases(get(), get(), get(), get()) }
    factory { RemainingFuelLapsUseCases(get()) }
    factory { Gt7Ps5NarratorEventProcessor(get(named("gt7_ps5")), get()) }

    // ドメイン UseCase（:core:domain。get() は :core:gt7-ps5-data / :core:data の Repository を解決）
    factory { DetermineGt7Ps5NarratorReadoutUseCase() }
    factory { SaveTelemetryLogUseCase(get()) }
    factory { ObserveGt7Ps5UseCase(get()) }
    factory { ObserveGt7Ps5MyBestLapVoiceTypeUseCase(get()) }
    factory { ObserveReadoutEnabledStatesUseCase(get()) }
    factory { ObserveReadoutOrderUseCase(get()) }
    factory { ObserveSelectedSimulatorUseCase(get()) }
    factory { ObserveGt7Ps5RemainingFuelLapsUseCase(get()) }
    factory { ObserveQueueEnabledStatesUseCase(get()) }

    // 音声再生（named "gt7_ps5" で LMU と分離。SoundPlayer は platformSoundModule が提供）
    includes(platformSoundModule)
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
