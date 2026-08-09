package kurou.kodriver.feature.acewindowsnarrator

import kurou.kodriver.core.designsystem.readStartSoundBytes
import kurou.kodriver.core.model.ReadoutStartSoundType
import kurou.kodriver.core.narrator.WavNarratorEngine
import kurou.kodriver.core.narrator.WavResources
import kurou.kodriver.core.narrator.platformSoundModule
import kurou.kodriver.domain.engine.SpeechEvent
import kurou.kodriver.domain.engine.TextToSpeechEngine
import kurou.kodriver.domain.usecase.DetermineAceWindowsNarratorReadoutUseCase
import kurou.kodriver.domain.usecase.ObserveAceWindowsFlagEnabledStatesUseCase
import kurou.kodriver.domain.usecase.ObserveAceWindowsFlagUseCase
import kurou.kodriver.domain.usecase.ObserveAceWindowsFuelUseCase
import kurou.kodriver.domain.usecase.ObserveAceWindowsRemainingFuelThresholdPercentageUseCase
import kurou.kodriver.domain.usecase.ObserveAceWindowsStatusUseCase
import kurou.kodriver.domain.usecase.ObserveQueueEnabledStatesUseCase
import kurou.kodriver.domain.usecase.ObserveReadoutEnabledStatesUseCase
import kurou.kodriver.domain.usecase.ObserveReadoutOrderUseCase
import kurou.kodriver.domain.usecase.ObserveReadoutStartSoundTypeUseCase
import kurou.kodriver.domain.usecase.ObserveSelectedSimulatorUseCase
import kurou.kodriver.domain.usecase.ObserveSoundVolumeUseCase
import kurou.kodriver.domain.usecase.PlaySpeechEventUseCase
import kurou.kodriver.domain.usecase.SaveTelemetryLogUseCase
import kurou.kodriver.feature.acewindowsnarrator.generated.resources.Res
import org.jetbrains.compose.resources.ExperimentalResourceApi
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
@OptIn(ExperimentalResourceApi::class)
val aceWindowsNarratorModule: Module =
    module {
        // ViewModel（AceWindowsNarratorEventProcessor 経由で下記の TextToSpeechEngine を利用）
        viewModel { AceWindowsNarratorViewModel(get(), get(), get(), get(), get()) }

        // この feature 固有の UseCase 集約 data class（本モジュールで定義）
        factory { RemainingFuelUseCases(get(), get()) }
        factory { ReadoutListUseCases(get(), get(), get(), get()) }
        factory { FlagUseCases(get(), get()) }
        factory { AceWindowsNarratorEventProcessor(get(named("ace_windows")), get()) }

        // ドメイン UseCase（:core:domain。get() は :core:ace-windows-data / :core:data の Repository を解決）
        factory { DetermineAceWindowsNarratorReadoutUseCase() }
        factory { SaveTelemetryLogUseCase(get()) }
        factory { ObserveAceWindowsFuelUseCase(get()) }
        factory { ObserveAceWindowsRemainingFuelThresholdPercentageUseCase(get()) }
        factory { ObserveAceWindowsFlagUseCase(get()) }
        factory { ObserveAceWindowsFlagEnabledStatesUseCase(get()) }
        factory { ObserveAceWindowsStatusUseCase(get()) }
        factory { ObserveReadoutEnabledStatesUseCase(get()) }
        factory { ObserveReadoutOrderUseCase(get()) }
        factory { ObserveSelectedSimulatorUseCase(get()) }
        factory { ObserveQueueEnabledStatesUseCase(get()) }

        // 音声再生（named "ace_windows" で LMU/GT7 と分離。SoundPlayer は core:narrator の platformSoundModule が提供）
        includes(platformSoundModule(named("ace_windows")))
        single<TextToSpeechEngine>(named("ace_windows")) {
            AceWindowsWavNarratorEngine(
                WavNarratorEngine(
                    soundPlayer = get(named("ace_windows")),
                    resources =
                        WavResources(
                            eventToFile = aceWindowsEventToFile,
                            startSoundTypeToFile = aceWindowsStartSoundTypeToFile,
                            resourceLoader = Res::readBytes,
                            startSoundResourceLoader = ::readStartSoundBytes,
                        ),
                    eventToKey = { it.readoutItemKey },
                    defaultStartSoundType = ReadoutStartSoundType.FORMULA_RADIO,
                    volumeFlow = ObserveSoundVolumeUseCase(get())(),
                    startSoundTypeFlow = ObserveReadoutStartSoundTypeUseCase(get())(),
                ),
            )
        }
        factory(named("ace_windows")) { PlaySpeechEventUseCase(get(named("ace_windows"))) }
    }

private val aceWindowsEventToFile: Map<SpeechEvent, String> =
    mapOf(
        SpeechEvent.AceWindowsRemainingFuelWarning to "files/remaining_fuel_caution.wav",
        SpeechEvent.AceWindowsWhiteFlag to "files/white_flag.wav",
        SpeechEvent.AceWindowsGreenFlag to "files/green_flag.wav",
        SpeechEvent.AceWindowsRedFlag to "files/red_flag.wav",
        SpeechEvent.AceWindowsBlueFlag to "files/blue_flag.wav",
        SpeechEvent.AceWindowsYellowFlag to "files/yellow_flag.wav",
        SpeechEvent.AceWindowsBlackFlag to "files/black_flag.wav",
        SpeechEvent.AceWindowsBlackWhiteFlag to "files/black_white_flag.wav",
        SpeechEvent.AceWindowsCheckeredFlag to "files/checkered_flag.wav",
        SpeechEvent.AceWindowsOrangeCircleFlag to "files/orange_circle_flag.wav",
        SpeechEvent.AceWindowsRedYellowStripesFlag to "files/red_yellow_stripes_flag.wav",
    )

private val aceWindowsStartSoundTypeToFile: Map<ReadoutStartSoundType, String> =
    mapOf(
        ReadoutStartSoundType.FORMULA_RADIO to "files/formula_radio.wav",
        ReadoutStartSoundType.ELECTRONIC_NOISE to "files/electronic_noise.wav",
    )
