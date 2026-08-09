package kurou.kodriver.feature.lmuwindowsnarrator

import kurou.kodriver.core.designsystem.readStartSoundBytes
import kurou.kodriver.core.model.ReadoutStartSoundType
import kurou.kodriver.core.narrator.WavNarratorEngine
import kurou.kodriver.core.narrator.WavResources
import kurou.kodriver.core.narrator.platformSoundModule
import kurou.kodriver.domain.engine.SpeechEvent
import kurou.kodriver.domain.engine.TextToSpeechEngine
import kurou.kodriver.domain.usecase.DetermineLmuWindowsNarratorReadoutUseCase
import kurou.kodriver.domain.usecase.ObserveLmuWindowsFlagEnabledStatesUseCase
import kurou.kodriver.domain.usecase.ObserveLmuWindowsMyBestLapVoiceTypeUseCase
import kurou.kodriver.domain.usecase.ObserveLmuWindowsPitTimingTyreWearLapsUseCase
import kurou.kodriver.domain.usecase.ObserveLmuWindowsPitTimingVirtualEnergyLapsUseCase
import kurou.kodriver.domain.usecase.ObserveLmuWindowsRaceFlagsUseCase
import kurou.kodriver.domain.usecase.ObserveLmuWindowsRedFlagVoiceTypeUseCase
import kurou.kodriver.domain.usecase.ObserveLmuWindowsRemainingVirtualEnergyThresholdPercentageUseCase
import kurou.kodriver.domain.usecase.ObserveLmuWindowsTyreCarcassTemperatureUseCase
import kurou.kodriver.domain.usecase.ObserveLmuWindowsTyreTemperatureEnabledStatesUseCase
import kurou.kodriver.domain.usecase.ObserveLmuWindowsTyreWearThresholdPercentageUseCase
import kurou.kodriver.domain.usecase.ObserveLmuWindowsTyreWearUseCase
import kurou.kodriver.domain.usecase.ObserveLmuWindowsUseCase
import kurou.kodriver.domain.usecase.ObserveLmuWindowsVehicleApproachEnabledStatesUseCase
import kurou.kodriver.domain.usecase.ObserveLmuWindowsVehicleApproachSkipFirstLapUseCase
import kurou.kodriver.domain.usecase.ObserveLmuWindowsVehicleApproachStartReadoutTypeUseCase
import kurou.kodriver.domain.usecase.ObserveLmuWindowsVehicleApproachSustainedDurationUseCase
import kurou.kodriver.domain.usecase.ObserveLmuWindowsVehicleApproachSustainedReadoutTypeUseCase
import kurou.kodriver.domain.usecase.ObserveLmuWindowsVehicleApproachUseCase
import kurou.kodriver.domain.usecase.ObserveLmuWindowsVehicleClassTyreTemperatureHighThresholdUseCase
import kurou.kodriver.domain.usecase.ObserveLmuWindowsVehicleClassUseCase
import kurou.kodriver.domain.usecase.ObserveLmuWindowsVehicleDamageEnabledStatesUseCase
import kurou.kodriver.domain.usecase.ObserveLmuWindowsVehicleDamageUseCase
import kurou.kodriver.domain.usecase.ObserveLmuWindowsVirtualEnergyUseCase
import kurou.kodriver.domain.usecase.ObserveQueueEnabledStatesUseCase
import kurou.kodriver.domain.usecase.ObserveReadoutEnabledStatesUseCase
import kurou.kodriver.domain.usecase.ObserveReadoutOrderUseCase
import kurou.kodriver.domain.usecase.ObserveReadoutStartSoundTypeUseCase
import kurou.kodriver.domain.usecase.ObserveSelectedSimulatorUseCase
import kurou.kodriver.domain.usecase.ObserveSoundVolumeUseCase
import kurou.kodriver.domain.usecase.PlaySpeechEventUseCase
import kurou.kodriver.domain.usecase.SaveTelemetryLogUseCase
import kurou.kodriver.feature.lmuwindowsnarrator.generated.resources.Res
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.koin.core.module.Module
import org.koin.core.module.dsl.viewModel
import org.koin.core.qualifier.named
import org.koin.dsl.module

/**
 * LMU アナウンス制御（lmu-windows-narrator feature）の Koin モジュール。
 *
 * 提供: LmuWindowsNarratorViewModel、LmuWindowsNarratorEventProcessor、この feature 内で定義した UseCase 集約 data class
 *   （NarratorUseCases / FlagUseCases / VehicleApproachUseCases / VehicleDamageUseCases /
 *   ReadoutListUseCases / TyreTemperatureUseCases / TyreWearUseCases / RemainingVirtualEnergyUseCases /
 *   PitTimingUseCases）、
 *   それらが束ねる各ドメイン UseCase、および named("lmu_windows") の音声再生系
 *   （PlaySpeechEventUseCase・TextToSpeechEngine）。
 * 消費（get で解決）: 各 UseCase の依存 Repository（:core:lmu-windows-data / :core:data）、
 *   SoundPlayer（[platformSoundModule]）。
 * 音声系は GT7 と区別するため named("lmu_windows") で登録している。
 */
@OptIn(ExperimentalResourceApi::class)
val lmuWindowsNarratorModule: Module =
    module {
        // ViewModel（LmuWindowsNarratorEventProcessor 経由で下記の TextToSpeechEngine を利用）
        viewModel { LmuWindowsNarratorViewModel(get(), get(), get(), get(), get(), get(), get(), get(), get(), get()) }

        // この feature 固有の UseCase 集約 data class（本モジュールで定義）
        factory { NarratorUseCases(get(), get(), get()) }
        factory { FlagUseCases(get(), get()) }
        factory { VehicleApproachUseCases(get(), get(), get(), get(), get(), get(), get()) }
        factory { VehicleDamageUseCases(get(), get()) }
        factory { ReadoutListUseCases(get(), get(), get(), get()) }
        factory { TyreTemperatureUseCases(get(), get(), get(), get(), get()) }
        factory { TyreWearUseCases(get(), get()) }
        factory { RemainingVirtualEnergyUseCases(get(), get()) }
        factory { PitTimingUseCases(get(), get()) }
        factory { LmuWindowsNarratorEventProcessor(get(named("lmu_windows")), get()) }

        // ドメイン UseCase（:core:domain。get() は :core:lmu-windows-data / :core:data の Repository を解決）
        factory { DetermineLmuWindowsNarratorReadoutUseCase() }
        factory { SaveTelemetryLogUseCase(get()) }
        factory { ObserveLmuWindowsFlagEnabledStatesUseCase(get()) }
        factory { ObserveLmuWindowsMyBestLapVoiceTypeUseCase(get()) }
        factory { ObserveLmuWindowsRedFlagVoiceTypeUseCase(get()) }
        factory { ObserveLmuWindowsUseCase(get()) }
        factory { ObserveLmuWindowsVehicleApproachUseCase(get()) }
        factory { ObserveLmuWindowsRaceFlagsUseCase(get()) }
        factory { ObserveReadoutEnabledStatesUseCase(get()) }
        factory { ObserveReadoutOrderUseCase(get()) }
        factory { ObserveSelectedSimulatorUseCase(get()) }
        factory { ObserveLmuWindowsVehicleApproachSkipFirstLapUseCase(get()) }
        factory { ObserveLmuWindowsVehicleApproachEnabledStatesUseCase(get()) }
        factory { ObserveLmuWindowsVehicleApproachStartReadoutTypeUseCase(get()) }
        factory { ObserveLmuWindowsVehicleApproachSustainedDurationUseCase(get()) }
        factory { ObserveLmuWindowsVehicleApproachSustainedReadoutTypeUseCase(get()) }
        factory { ObserveLmuWindowsVehicleDamageEnabledStatesUseCase(get()) }
        factory { ObserveLmuWindowsVehicleDamageUseCase(get()) }
        factory { ObserveLmuWindowsTyreCarcassTemperatureUseCase(get()) }
        factory { ObserveLmuWindowsVehicleClassTyreTemperatureHighThresholdUseCase(get()) }
        factory { ObserveLmuWindowsVehicleClassUseCase(get()) }
        factory { ObserveLmuWindowsTyreTemperatureEnabledStatesUseCase(get()) }
        factory { ObserveLmuWindowsTyreWearUseCase(get()) }
        factory { ObserveLmuWindowsTyreWearThresholdPercentageUseCase(get()) }
        factory { ObserveLmuWindowsVirtualEnergyUseCase(get()) }
        factory { ObserveLmuWindowsRemainingVirtualEnergyThresholdPercentageUseCase(get()) }
        factory { ObserveLmuWindowsPitTimingVirtualEnergyLapsUseCase(get()) }
        factory { ObserveLmuWindowsPitTimingTyreWearLapsUseCase(get()) }
        factory { ObserveQueueEnabledStatesUseCase(get()) }

        // 音声再生（named "lmu_windows" で GT7/ACE と分離。SoundPlayer は core:narrator の platformSoundModule が提供）
        factory(named("lmu_windows")) { PlaySpeechEventUseCase(get(named("lmu_windows"))) }
        includes(platformSoundModule(named("lmu_windows")))
        single<TextToSpeechEngine>(named("lmu_windows")) {
            LmuWindowsWavNarratorEngine(
                WavNarratorEngine(
                    soundPlayer = get(named("lmu_windows")),
                    resources =
                        WavResources(
                            eventToFile = lmuWindowsEventToFile,
                            startSoundTypeToFile = lmuWindowsStartSoundTypeToFile,
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
    }

private val lmuWindowsEventToFile: Map<SpeechEvent, String> =
    buildMap {
        put(SpeechEvent.CarLeft, "files/car_left.wav")
        put(SpeechEvent.CarRight, "files/car_right.wav")
        put(SpeechEvent.LeftApproach, "files/left_approach.wav")
        put(SpeechEvent.RightApproach, "files/right_approach.wav")
        put(SpeechEvent.KeepLeft, "files/keep_left.wav")
        put(SpeechEvent.KeepRight, "files/keep_right.wav")
        put(SpeechEvent.LeftSustained, "files/left_sustained.wav")
        put(SpeechEvent.RightSustained, "files/right_sustained.wav")
        put(SpeechEvent.BlueFlag, "files/blue_flag.wav")
        put(SpeechEvent.YellowFlag, "files/yellow_flag.wav")
        put(SpeechEvent.FullCourseYellow, "files/full_course_yellow.wav")
        put(SpeechEvent.SessionStop, "files/session_stopped.wav")
        put(SpeechEvent.RedFlag, "files/red_flag.wav")
        put(SpeechEvent.Overheating, "files/gp2_gp2.wav")
        put(SpeechEvent.LmuWindowsMyBestLapFormal, "files/my_best_lap_formal.wav")
        put(SpeechEvent.LmuWindowsMyBestLapCasual, "files/my_best_lap_casual.wav")
        put(SpeechEvent.TyreOverheat, "files/tyre_overheat.wav")
        put(SpeechEvent.TyreCold, "files/tyre_cold.wav")
        put(SpeechEvent.TyreWearWarning, "files/tyre_wear_caution.wav")
        put(SpeechEvent.RemainingVirtualEnergyWarning, "files/remaining_virtual_energy_caution.wav")
        for (laps in 0..MAX_PIT_TIMING_LAPS) {
            put(SpeechEvent.PitTimingWarning(laps), "files/pit_timing_laps_$laps.wav")
        }
    }

private val lmuWindowsStartSoundTypeToFile: Map<ReadoutStartSoundType, String> =
    mapOf(
        ReadoutStartSoundType.FORMULA_RADIO to "files/formula_radio.wav",
        ReadoutStartSoundType.ELECTRONIC_NOISE to "files/electronic_noise.wav",
    )

private const val MAX_PIT_TIMING_LAPS = 5
