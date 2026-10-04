package kurou.kodriver.feature.lmuwindowsnarrator

import kurou.kodriver.core.designsystem.readStartSoundBytes
import kurou.kodriver.core.narrator.WavNarratorEngine
import kurou.kodriver.core.narrator.WavResources
import kurou.kodriver.core.narrator.platformSoundModule
import kurou.kodriver.domain.engine.SpeechEvent
import kurou.kodriver.domain.engine.TextToSpeechEngine
import kurou.kodriver.domain.model.ReadoutStartSoundType
import kurou.kodriver.domain.model.Simulator
import kurou.kodriver.domain.usecase.DetermineLmuWindowsNarratorReadoutUseCase
import kurou.kodriver.domain.usecase.ObserveLmuWindowsBlueFlagReadoutTextUseCase
import kurou.kodriver.domain.usecase.ObserveLmuWindowsBrakeTemperatureReadoutTextUseCase
import kurou.kodriver.domain.usecase.ObserveLmuWindowsBrakeTemperatureUseCase
import kurou.kodriver.domain.usecase.ObserveLmuWindowsFlagEnabledStatesUseCase
import kurou.kodriver.domain.usecase.ObserveLmuWindowsFullCourseYellowFlagReadoutTextUseCase
import kurou.kodriver.domain.usecase.ObserveLmuWindowsMyBestLapVoiceTypeUseCase
import kurou.kodriver.domain.usecase.ObserveLmuWindowsPitTimingEnabledStatesUseCase
import kurou.kodriver.domain.usecase.ObserveLmuWindowsPitTimingTyreWearImminentReadoutTextUseCase
import kurou.kodriver.domain.usecase.ObserveLmuWindowsPitTimingTyreWearLapsUseCase
import kurou.kodriver.domain.usecase.ObserveLmuWindowsPitTimingTyreWearReadoutTextUseCase
import kurou.kodriver.domain.usecase.ObserveLmuWindowsPitTimingVirtualEnergyImminentReadoutTextUseCase
import kurou.kodriver.domain.usecase.ObserveLmuWindowsPitTimingVirtualEnergyLapsUseCase
import kurou.kodriver.domain.usecase.ObserveLmuWindowsPitTimingVirtualEnergyReadoutTextUseCase
import kurou.kodriver.domain.usecase.ObserveLmuWindowsRaceFlagsUseCase
import kurou.kodriver.domain.usecase.ObserveLmuWindowsRedFlagReadoutTextUseCase
import kurou.kodriver.domain.usecase.ObserveLmuWindowsRemainingVirtualEnergyReadoutTextUseCase
import kurou.kodriver.domain.usecase.ObserveLmuWindowsRemainingVirtualEnergyThresholdPercentageUseCase
import kurou.kodriver.domain.usecase.ObserveLmuWindowsSectorYellowFlagReadoutTextUseCase
import kurou.kodriver.domain.usecase.ObserveLmuWindowsTyreCarcassTemperatureUseCase
import kurou.kodriver.domain.usecase.ObserveLmuWindowsTyreDetachedUseCase
import kurou.kodriver.domain.usecase.ObserveLmuWindowsTyreTemperatureColdReadoutTextUseCase
import kurou.kodriver.domain.usecase.ObserveLmuWindowsTyreTemperatureEnabledStatesUseCase
import kurou.kodriver.domain.usecase.ObserveLmuWindowsTyreTemperatureOverheatReadoutTextUseCase
import kurou.kodriver.domain.usecase.ObserveLmuWindowsTyreWearReadoutTextUseCase
import kurou.kodriver.domain.usecase.ObserveLmuWindowsTyreWearThresholdPercentageUseCase
import kurou.kodriver.domain.usecase.ObserveLmuWindowsTyreWearUseCase
import kurou.kodriver.domain.usecase.ObserveLmuWindowsUseCase
import kurou.kodriver.domain.usecase.ObserveLmuWindowsVehicleApproachEnabledStatesUseCase
import kurou.kodriver.domain.usecase.ObserveLmuWindowsVehicleApproachSkipFirstLapUseCase
import kurou.kodriver.domain.usecase.ObserveLmuWindowsVehicleApproachStartLeftReadoutTextUseCase
import kurou.kodriver.domain.usecase.ObserveLmuWindowsVehicleApproachStartRightReadoutTextUseCase
import kurou.kodriver.domain.usecase.ObserveLmuWindowsVehicleApproachSustainedDurationUseCase
import kurou.kodriver.domain.usecase.ObserveLmuWindowsVehicleApproachSustainedLeftReadoutTextUseCase
import kurou.kodriver.domain.usecase.ObserveLmuWindowsVehicleApproachSustainedRightReadoutTextUseCase
import kurou.kodriver.domain.usecase.ObserveLmuWindowsVehicleApproachUseCase
import kurou.kodriver.domain.usecase.ObserveLmuWindowsVehicleClassBrakeTemperatureHighThresholdUseCase
import kurou.kodriver.domain.usecase.ObserveLmuWindowsVehicleClassTyreTemperatureHighThresholdUseCase
import kurou.kodriver.domain.usecase.ObserveLmuWindowsVehicleClassUseCase
import kurou.kodriver.domain.usecase.ObserveLmuWindowsVehicleDamageEnabledStatesUseCase
import kurou.kodriver.domain.usecase.ObserveLmuWindowsVehicleDamageOverheatReadoutTextUseCase
import kurou.kodriver.domain.usecase.ObserveLmuWindowsVehicleDamagePartDetachedReadoutTextUseCase
import kurou.kodriver.domain.usecase.ObserveLmuWindowsVehicleDamageTyreDetachedReadoutTextUseCase
import kurou.kodriver.domain.usecase.ObserveLmuWindowsVehicleDamageUseCase
import kurou.kodriver.domain.usecase.ObserveLmuWindowsVirtualEnergyUseCase
import kurou.kodriver.domain.usecase.ObserveQueueEnabledStatesUseCase
import kurou.kodriver.domain.usecase.ObserveReadoutEnabledStatesUseCase
import kurou.kodriver.domain.usecase.ObserveReadoutOrderUseCase
import kurou.kodriver.domain.usecase.ObserveReadoutStartSoundEnabledStatesUseCase
import kurou.kodriver.domain.usecase.ObserveReadoutStartSoundTypeUseCase
import kurou.kodriver.domain.usecase.ObserveResolvedReadoutOrderUseCase
import kurou.kodriver.domain.usecase.ObserveSelectedSimulatorUseCase
import kurou.kodriver.domain.usecase.ObserveSoundVolumeUseCase
import kurou.kodriver.domain.usecase.ObserveVoiceUseCase
import kurou.kodriver.domain.usecase.PlaySpeechEventUseCase
import kurou.kodriver.domain.usecase.PlayStartSoundForKeyUseCase
import kurou.kodriver.domain.usecase.ResolveReadoutOrderUseCase
import kurou.kodriver.domain.usecase.SaveTelemetryLogUseCase
import kurou.kodriver.domain.usecase.SpeakTextUseCase
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
 *   （NarratorUseCases / FlagUseCases / VehicleApproachUseCases / VehicleDamageUseCases / TyreDetachedUseCases /
 *   SimulatorUseCases / ReadoutListUseCases / TyreTemperatureUseCases / TyreWearUseCases /
 *   RemainingVirtualEnergyUseCases / PitTimingUseCases）、
 *   それらが束ねる各ドメイン UseCase、named(Simulator.LmuWindows.id) の音声再生系
 *   （PlaySpeechEventUseCase・PlayStartSoundForKeyUseCase・SpeakTextUseCase・
 *   各フラッグ・車両接近・VE残量警告・タイヤ過熱警告・VEピットタイミングの読み上げ文言の Observe UseCase・TextToSpeechEngine）、
 *   および LmuWindowsReadoutTextSpeaker
 *   （フラッグ・車両接近・VE残量警告・タイヤ過熱警告・VEピットタイミングの実際の読み上げ時に
 *   自由文字列をOS標準TTSで読み上げるフック。WavNarratorEngine の customSpeak に渡す）。
 * 音声設定監視用の ObserveVoiceUseCase を提供し、VoicePreferencesRepository（:core:data）を消費する。
 * 消費（get で解決）: 各 UseCase の依存 Repository（:core:lmu-windows-data / :core:data）、
 *   SoundPlayer（[platformSoundModule]）、unqualified の CheckTextToSpeechAvailableUseCase
 *   （:feature:lmu-windows-readout-flag-detail が登録。TTSが実際に利用可能かどうかは
 *   試聴用途と共有しても問題ないため named では区別しない）。
 * 音声系は GT7 と区別するため named(Simulator.LmuWindows.id) で登録している。
 */
@OptIn(ExperimentalResourceApi::class)
val lmuWindowsNarratorModule: Module =
    module {
        // ViewModel（LmuWindowsNarratorEventProcessor 経由で下記の TextToSpeechEngine を利用）
        viewModel {
            LmuWindowsNarratorViewModel(
                get(),
                get(),
                get(),
                get(),
                get(),
                get(),
                get(),
                get(),
                get(),
                get(),
                get(),
                get(),
                get(),
            )
        }

        // この feature 固有の UseCase 集約 data class（本モジュールで定義）
        factory { ObserveVoiceUseCase(get()) }
        factory { NarratorUseCases(get(), get()) }
        factory { FlagUseCases(get(), get()) }
        factory { VehicleApproachUseCases(get(), get(), get(), get(), get()) }
        factory { VehicleDamageUseCases(get(), get()) }
        factory { TyreDetachedUseCases(get()) }
        factory { SimulatorUseCases(get()) }
        factory { ReadoutListUseCases(get(), get(), get()) }
        factory { TyreTemperatureUseCases(get(), get(), get(), get(), get()) }
        factory { TyreWearUseCases(get(), get()) }
        factory { BrakeTemperatureUseCases(get(), get()) }
        factory { RemainingVirtualEnergyUseCases(get(), get()) }
        factory { PitTimingUseCases(get(), get(), get()) }
        factory {
            LmuWindowsNarratorEventProcessor(
                get(named(Simulator.LmuWindows.id)),
                get(),
                get<LmuWindowsReadoutTextSpeaker>()::readoutText,
            )
        }

        // ドメイン UseCase（:core:domain。get() は :core:lmu-windows-data / :core:data の Repository を解決）
        factory { DetermineLmuWindowsNarratorReadoutUseCase() }
        factory { SaveTelemetryLogUseCase(get()) }
        factory { ObserveLmuWindowsFlagEnabledStatesUseCase(get()) }
        factory { ObserveLmuWindowsMyBestLapVoiceTypeUseCase(get()) }
        factory { ObserveLmuWindowsUseCase(get()) }
        factory { ObserveLmuWindowsVehicleApproachUseCase(get()) }
        factory { ObserveLmuWindowsRaceFlagsUseCase(get()) }
        factory { ObserveReadoutEnabledStatesUseCase(get()) }
        factory { ObserveReadoutOrderUseCase(get()) }
        factory { ResolveReadoutOrderUseCase() }
        factory { ObserveResolvedReadoutOrderUseCase(get(), get()) }
        factory { ObserveSelectedSimulatorUseCase(get()) }
        factory { ObserveLmuWindowsVehicleApproachSkipFirstLapUseCase(get()) }
        factory { ObserveLmuWindowsVehicleApproachEnabledStatesUseCase(get()) }
        factory { ObserveLmuWindowsVehicleApproachSustainedDurationUseCase(get()) }
        factory { ObserveLmuWindowsVehicleDamageEnabledStatesUseCase(get()) }
        factory { ObserveLmuWindowsVehicleDamageUseCase(get()) }
        factory { ObserveLmuWindowsTyreDetachedUseCase(get()) }
        factory { ObserveLmuWindowsTyreCarcassTemperatureUseCase(get()) }
        factory { ObserveLmuWindowsVehicleClassTyreTemperatureHighThresholdUseCase(get()) }
        factory { ObserveLmuWindowsVehicleClassUseCase(get()) }
        factory { ObserveLmuWindowsTyreTemperatureEnabledStatesUseCase(get()) }
        factory { ObserveLmuWindowsTyreWearUseCase(get()) }
        factory { ObserveLmuWindowsTyreWearThresholdPercentageUseCase(get()) }
        factory { ObserveLmuWindowsBrakeTemperatureUseCase(get()) }
        factory { ObserveLmuWindowsVehicleClassBrakeTemperatureHighThresholdUseCase(get()) }
        factory { ObserveLmuWindowsVirtualEnergyUseCase(get()) }
        factory { ObserveLmuWindowsRemainingVirtualEnergyThresholdPercentageUseCase(get()) }
        factory { ObserveLmuWindowsPitTimingVirtualEnergyLapsUseCase(get()) }
        factory { ObserveLmuWindowsPitTimingTyreWearLapsUseCase(get()) }
        factory { ObserveLmuWindowsPitTimingEnabledStatesUseCase(get()) }
        factory { ObserveQueueEnabledStatesUseCase(get()) }

        // 音声再生（named "lmu_windows" で GT7/ACE と分離。SoundPlayer は core:narrator の platformSoundModule が提供）
        factory(named(Simulator.LmuWindows.id)) { PlaySpeechEventUseCase(get(named(Simulator.LmuWindows.id))) }
        factory(named(Simulator.LmuWindows.id)) { PlayStartSoundForKeyUseCase(get(named(Simulator.LmuWindows.id))) }
        includes(platformSoundModule(named(Simulator.LmuWindows.id)))

        // フラッグ・車両接近開始時の本文をOS標準TTSのみで読み上げるフック。unqualified の SpeakTextUseCase / 各フラッグの Observe…ReadoutTextUseCase は
        // feature:lmu-windows-readout-flag-detail が試聴用に別途定義しているため、
        // 同じ型を二重定義しないよう named(Simulator.LmuWindows.id) で区別する。
        factory(named(Simulator.LmuWindows.id)) { SpeakTextUseCase(get(), get()) }
        factory(named(Simulator.LmuWindows.id)) { ObserveLmuWindowsSectorYellowFlagReadoutTextUseCase(get()) }
        factory(named(Simulator.LmuWindows.id)) { ObserveLmuWindowsBlueFlagReadoutTextUseCase(get()) }
        factory(named(Simulator.LmuWindows.id)) { ObserveLmuWindowsFullCourseYellowFlagReadoutTextUseCase(get()) }
        factory(named(Simulator.LmuWindows.id)) { ObserveLmuWindowsRedFlagReadoutTextUseCase(get()) }
        factory(named(Simulator.LmuWindows.id)) { ObserveLmuWindowsVehicleApproachStartLeftReadoutTextUseCase(get()) }
        factory(named(Simulator.LmuWindows.id)) {
            ObserveLmuWindowsVehicleApproachSustainedLeftReadoutTextUseCase(get())
        }
        factory(named(Simulator.LmuWindows.id)) { ObserveLmuWindowsVehicleApproachStartRightReadoutTextUseCase(get()) }
        factory(named(Simulator.LmuWindows.id)) {
            ObserveLmuWindowsVehicleApproachSustainedRightReadoutTextUseCase(get())
        }
        factory(named(Simulator.LmuWindows.id)) { ObserveLmuWindowsPitTimingTyreWearReadoutTextUseCase(get()) }
        factory(named(Simulator.LmuWindows.id)) { ObserveLmuWindowsPitTimingTyreWearImminentReadoutTextUseCase(get()) }
        factory(named(Simulator.LmuWindows.id)) { ObserveLmuWindowsPitTimingVirtualEnergyReadoutTextUseCase(get()) }
        factory(named(Simulator.LmuWindows.id)) {
            ObserveLmuWindowsPitTimingVirtualEnergyImminentReadoutTextUseCase(get())
        }
        factory(named(Simulator.LmuWindows.id)) { ObserveLmuWindowsRemainingVirtualEnergyReadoutTextUseCase(get()) }
        factory(named(Simulator.LmuWindows.id)) { ObserveLmuWindowsTyreWearReadoutTextUseCase(get()) }
        factory(named(Simulator.LmuWindows.id)) { ObserveLmuWindowsBrakeTemperatureReadoutTextUseCase(get()) }
        factory(named(Simulator.LmuWindows.id)) { ObserveLmuWindowsTyreTemperatureOverheatReadoutTextUseCase(get()) }
        factory(named(Simulator.LmuWindows.id)) { ObserveLmuWindowsTyreTemperatureColdReadoutTextUseCase(get()) }
        factory(named(Simulator.LmuWindows.id)) { ObserveLmuWindowsVehicleDamageOverheatReadoutTextUseCase(get()) }
        factory(named(Simulator.LmuWindows.id)) { ObserveLmuWindowsVehicleDamagePartDetachedReadoutTextUseCase(get()) }
        factory(named(Simulator.LmuWindows.id)) { ObserveLmuWindowsVehicleDamageTyreDetachedReadoutTextUseCase(get()) }
        factory {
            LmuWindowsReadoutTextSpeaker(
                observeSectorYellowFlagReadoutText = get(named(Simulator.LmuWindows.id)),
                observeBlueFlagReadoutText = get(named(Simulator.LmuWindows.id)),
                observeFullCourseYellowFlagReadoutText = get(named(Simulator.LmuWindows.id)),
                observeRedFlagReadoutText = get(named(Simulator.LmuWindows.id)),
                observeStartLeftReadoutText = get(named(Simulator.LmuWindows.id)),
                observeSustainedLeftReadoutText = get(named(Simulator.LmuWindows.id)),
                observeStartRightReadoutText = get(named(Simulator.LmuWindows.id)),
                observeSustainedRightReadoutText = get(named(Simulator.LmuWindows.id)),
                observePitTimingTyreWearReadoutText = get(named(Simulator.LmuWindows.id)),
                observePitTimingTyreWearImminentReadoutText = get(named(Simulator.LmuWindows.id)),
                observePitTimingVirtualEnergyReadoutText = get(named(Simulator.LmuWindows.id)),
                observePitTimingVirtualEnergyImminentReadoutText = get(named(Simulator.LmuWindows.id)),
                observeRemainingVirtualEnergyReadoutText = get(named(Simulator.LmuWindows.id)),
                observeBrakeTemperatureReadoutText = get(named(Simulator.LmuWindows.id)),
                observeTyreWearReadoutText = get(named(Simulator.LmuWindows.id)),
                observeTyreOverheatReadoutText = get(named(Simulator.LmuWindows.id)),
                observeTyreColdReadoutText = get(named(Simulator.LmuWindows.id)),
                observeOverheatReadoutText = get(named(Simulator.LmuWindows.id)),
                observePartDetachedReadoutText = get(named(Simulator.LmuWindows.id)),
                observeTyreDetachedReadoutText = get(named(Simulator.LmuWindows.id)),
                checkTextToSpeechAvailable = get(),
                speakText = get(named(Simulator.LmuWindows.id)),
            )
        }

        single<TextToSpeechEngine>(named(Simulator.LmuWindows.id)) {
            LmuWindowsWavNarratorEngine(
                WavNarratorEngine(
                    soundPlayer = get(named(Simulator.LmuWindows.id)),
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
                    startSoundEnabledStatesFlow = ObserveReadoutStartSoundEnabledStatesUseCase(get())(),
                    customSpeak = get<LmuWindowsReadoutTextSpeaker>()::invoke,
                    isCustomSpeakEvent = {
                        it is SpeechEvent.PitTimingWarning || it is SpeechEvent.RemainingVirtualEnergyWarning ||
                            it is SpeechEvent.TyreWearWarning || it is SpeechEvent.BrakeOverheat ||
                            it is SpeechEvent.TyreOverheat || it is SpeechEvent.TyreCold ||
                            it is SpeechEvent.Overheating || it is SpeechEvent.PartDetached ||
                            it is SpeechEvent.TyreDetached
                    },
                    customSpeakEvents =
                        setOf(
                            SpeechEvent.CarLeft,
                            SpeechEvent.CarLeftSustained,
                            SpeechEvent.CarRight,
                            SpeechEvent.CarRightSustained,
                            SpeechEvent.BlueFlag,
                            SpeechEvent.YellowFlag,
                            SpeechEvent.FullCourseYellow,
                            SpeechEvent.RedFlag,
                        ),
                ),
            )
        }
    }

private val lmuWindowsEventToFile: Map<SpeechEvent, String> =
    buildMap {
        put(SpeechEvent.LmuWindowsMyBestLapFormal, "files/my_best_lap_formal.wav")
        put(SpeechEvent.LmuWindowsMyBestLapCasual, "files/my_best_lap_casual.wav")
    }

private val lmuWindowsStartSoundTypeToFile: Map<ReadoutStartSoundType, String> =
    mapOf(
        ReadoutStartSoundType.FORMULA_RADIO to "files/formula_radio.wav",
        ReadoutStartSoundType.ELECTRONIC_NOISE to "files/electronic_noise.wav",
    )
