package kurou.kodriver.feature.gt7ps5narrator

import kurou.kodriver.core.designsystem.readStartSoundBytes
import kurou.kodriver.core.narrator.WavNarratorEngine
import kurou.kodriver.core.narrator.WavResources
import kurou.kodriver.core.narrator.platformSoundModule
import kurou.kodriver.domain.engine.SpeechEvent
import kurou.kodriver.domain.engine.TextToSpeechEngine
import kurou.kodriver.domain.model.ReadoutStartSoundType
import kurou.kodriver.domain.model.Simulator
import kurou.kodriver.domain.usecase.DetermineGt7Ps5NarratorReadoutUseCase
import kurou.kodriver.domain.usecase.ObserveGt7Ps5MyBestLapVoiceTypeUseCase
import kurou.kodriver.domain.usecase.ObserveGt7Ps5RemainingFuelLapsUseCase
import kurou.kodriver.domain.usecase.ObserveGt7Ps5RemainingFuelThresholdPercentageUseCase
import kurou.kodriver.domain.usecase.ObserveGt7Ps5TyreTemperatureHighThresholdUseCase
import kurou.kodriver.domain.usecase.ObserveGt7Ps5UseCase
import kurou.kodriver.domain.usecase.ObserveQueueEnabledStatesUseCase
import kurou.kodriver.domain.usecase.ObserveReadoutEnabledStatesUseCase
import kurou.kodriver.domain.usecase.ObserveReadoutOrderUseCase
import kurou.kodriver.domain.usecase.ObserveReadoutStartSoundTypeUseCase
import kurou.kodriver.domain.usecase.ObserveSelectedSimulatorUseCase
import kurou.kodriver.domain.usecase.ObserveSoundVolumeUseCase
import kurou.kodriver.domain.usecase.PlaySpeechEventUseCase
import kurou.kodriver.domain.usecase.SaveTelemetryLogUseCase
import kurou.kodriver.feature.gt7ps5narrator.generated.resources.Res
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.koin.core.module.Module
import org.koin.core.module.dsl.viewModel
import org.koin.core.qualifier.named
import org.koin.dsl.module

/**
 * GT7 PS5 アナウンス制御（gt7-ps5-narrator feature）の Koin モジュール。
 *
 * 提供: Gt7Ps5NarratorViewModel、Gt7Ps5NarratorEventProcessor、この feature 内で定義した UseCase 集約 data class
 *   （MyBestLapUseCases / ReadoutListUseCases / RemainingFuelLapsUseCases / RemainingFuelUseCases /
 *   TyreTemperatureUseCases）、それらが束ねる
 *   各ドメイン UseCase、および named(Simulator.Gt7Ps5.id) の音声再生系
 *   （PlaySpeechEventUseCase・TextToSpeechEngine）。
 * 消費（get で解決）: 各 UseCase の依存 Repository（:core:gt7-ps5-data / :core:data）、
 *   SoundPlayer（[platformSoundModule]）。
 * 音声系は LMU と区別するため named(Simulator.Gt7Ps5.id) で登録している。
 */
@OptIn(ExperimentalResourceApi::class)
val gt7Ps5NarratorModule: Module =
    module {
        // ViewModel（Gt7Ps5NarratorEventProcessor 経由で下記の TextToSpeechEngine を利用）
        viewModel { Gt7Ps5NarratorViewModel(get(), get(), get(), get(), get(), get(), get()) }

        // この feature 固有の UseCase 集約 data class（本モジュールで定義）
        factory { MyBestLapUseCases(get(), get()) }
        factory { ReadoutListUseCases(get(), get(), get(), get()) }
        factory { RemainingFuelLapsUseCases(get()) }
        factory { RemainingFuelUseCases(get()) }
        factory { TyreTemperatureUseCases(get()) }
        factory { Gt7Ps5NarratorEventProcessor(get(named(Simulator.Gt7Ps5.id)), get()) }

        // ドメイン UseCase（:core:domain。get() は :core:gt7-ps5-data / :core:data の Repository を解決）
        factory { DetermineGt7Ps5NarratorReadoutUseCase() }
        factory { SaveTelemetryLogUseCase(get()) }
        factory { ObserveGt7Ps5UseCase(get()) }
        factory { ObserveGt7Ps5MyBestLapVoiceTypeUseCase(get()) }
        factory { ObserveReadoutEnabledStatesUseCase(get()) }
        factory { ObserveReadoutOrderUseCase(get()) }
        factory { ObserveSelectedSimulatorUseCase(get()) }
        factory { ObserveGt7Ps5RemainingFuelLapsUseCase(get()) }
        factory { ObserveGt7Ps5RemainingFuelThresholdPercentageUseCase(get()) }
        factory { ObserveGt7Ps5TyreTemperatureHighThresholdUseCase(get()) }
        factory { ObserveQueueEnabledStatesUseCase(get()) }

        // 音声再生（named "gt7_ps5" で LMU/ACE と分離。SoundPlayer は core:narrator の platformSoundModule が提供）
        includes(platformSoundModule(named(Simulator.Gt7Ps5.id)))
        single<TextToSpeechEngine>(named(Simulator.Gt7Ps5.id)) {
            Gt7Ps5WavNarratorEngine(
                WavNarratorEngine(
                    soundPlayer = get(named(Simulator.Gt7Ps5.id)),
                    resources =
                        WavResources(
                            eventToFile = gt7Ps5EventToFile,
                            startSoundTypeToFile = gt7Ps5StartSoundTypeToFile,
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
        factory(named(Simulator.Gt7Ps5.id)) { PlaySpeechEventUseCase(get(named(Simulator.Gt7Ps5.id))) }
    }

private val gt7Ps5EventToFile: Map<SpeechEvent, String> =
    buildMap {
        put(SpeechEvent.Gt7Ps5MyBestLapFormal, "files/my_best_lap_formal.wav")
        put(SpeechEvent.Gt7Ps5MyBestLapCasual, "files/my_best_lap_casual.wav")
        put(SpeechEvent.Gt7Ps5RemainingFuelWarning, "files/remaining_fuel_caution.wav")
        put(SpeechEvent.Gt7Ps5TyreOverheat, "files/tyre_overheat.wav")
        for (laps in 0..MAX_REMAINING_FUEL_LAPS) {
            put(SpeechEvent.RemainingFuelLapsWarning(laps), "files/remaining_fuel_laps_$laps.wav")
        }
    }

private val gt7Ps5StartSoundTypeToFile: Map<ReadoutStartSoundType, String> =
    mapOf(
        ReadoutStartSoundType.FORMULA_RADIO to "files/formula_radio.wav",
        ReadoutStartSoundType.ELECTRONIC_NOISE to "files/electronic_noise.wav",
    )

private const val MAX_REMAINING_FUEL_LAPS = 5
