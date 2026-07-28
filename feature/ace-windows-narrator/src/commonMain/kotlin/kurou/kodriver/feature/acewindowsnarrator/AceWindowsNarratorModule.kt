package kurou.kodriver.feature.acewindowsnarrator

import kurou.kodriver.domain.engine.TextToSpeechEngine
import kurou.kodriver.domain.usecase.DetermineAceWindowsNarratorReadoutUseCase
import kurou.kodriver.domain.usecase.ObserveAceWindowsFuelUseCase
import kurou.kodriver.domain.usecase.ObserveAceWindowsRemainingFuelThresholdPercentageUseCase
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
 * ACE (Assetto Corsa EVO) Windows版 アナウンス制御（ace-windows-narrator feature）の Koin モジュール。
 *
 * 提供: AceWindowsNarratorViewModel、AceWindowsNarratorEventProcessor、この feature 内で定義した
 *   UseCase 集約 data class（RemainingFuelUseCases / ReadoutListUseCases）、それらが束ねる
 *   各ドメイン UseCase、および named("ace_windows") の音声再生系
 *   （PlaySpeechEventUseCase・TextToSpeechEngine）。
 * 消費（get で解決）: 各 UseCase の依存 Repository（:core:ace-windows-data / :core:data）、
 *   SoundPlayer（[platformSoundModule]）。
 * 音声系は LMU/GT7 と区別するため named("ace_windows") で登録している。
 */
val aceWindowsNarratorModule: Module = module {
    // ViewModel（AceWindowsNarratorEventProcessor 経由で下記の TextToSpeechEngine を利用）
    viewModel { AceWindowsNarratorViewModel(get(), get(), get()) }

    // この feature 固有の UseCase 集約 data class（本モジュールで定義）
    factory { RemainingFuelUseCases(get(), get()) }
    factory { ReadoutListUseCases(get(), get(), get(), get()) }
    factory { AceWindowsNarratorEventProcessor(get(named("ace_windows")), get()) }

    // ドメイン UseCase（:core:domain。get() は :core:ace-windows-data / :core:data の Repository を解決）
    factory { DetermineAceWindowsNarratorReadoutUseCase() }
    factory { SaveTelemetryLogUseCase(get()) }
    factory { ObserveAceWindowsFuelUseCase(get()) }
    factory { ObserveAceWindowsRemainingFuelThresholdPercentageUseCase(get()) }
    factory { ObserveReadoutEnabledStatesUseCase(get()) }
    factory { ObserveReadoutOrderUseCase(get()) }
    factory { ObserveSelectedSimulatorUseCase(get()) }
    factory { ObserveQueueEnabledStatesUseCase(get()) }

    // 音声再生（named "ace_windows" で LMU/GT7 と分離。SoundPlayer は platformSoundModule が提供）
    includes(platformSoundModule)
    single<TextToSpeechEngine>(named("ace_windows")) {
        AceWindowsWavNarratorEngine(
            soundPlayer = get(),
            volumeFlow = ObserveSoundVolumeUseCase(get())(),
            startSoundTypeFlow = ObserveReadoutStartSoundTypeUseCase(get())(),
        )
    }
    factory(named("ace_windows")) { PlaySpeechEventUseCase(get(named("ace_windows"))) }
}

internal expect val platformSoundModule: Module
