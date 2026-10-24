package kurou.kodriver.feature.lmuwindowsnarrator

import kurou.kodriver.core.designsystem.readStartSoundBytes
import kurou.kodriver.core.narrator.NarratorEngine
import kurou.kodriver.core.narrator.WavResources
import kurou.kodriver.core.narrator.platformSoundModule
import kurou.kodriver.domain.engine.TextToSpeechEngine
import kurou.kodriver.domain.lmuwindows.usecase.DetermineLmuWindowsBrakeReadoutUseCase
import kurou.kodriver.domain.lmuwindows.usecase.DetermineLmuWindowsManualReadoutUseCase
import kurou.kodriver.domain.lmuwindows.usecase.DetermineLmuWindowsMyBestLapReadoutUseCase
import kurou.kodriver.domain.lmuwindows.usecase.DetermineLmuWindowsPitTimingReadoutUseCase
import kurou.kodriver.domain.lmuwindows.usecase.DetermineLmuWindowsRaceFlagReadoutUseCase
import kurou.kodriver.domain.lmuwindows.usecase.DetermineLmuWindowsTyreReadoutUseCase
import kurou.kodriver.domain.lmuwindows.usecase.DetermineLmuWindowsVehicleReadoutUseCase
import kurou.kodriver.domain.lmuwindows.usecase.DetermineLmuWindowsVirtualEnergyReadoutUseCase
import kurou.kodriver.domain.lmuwindows.usecase.ObserveLmuWindowsBlueFlagReadoutTextUseCase
import kurou.kodriver.domain.lmuwindows.usecase.ObserveLmuWindowsBrakeTemperatureReadoutTextUseCase
import kurou.kodriver.domain.lmuwindows.usecase.ObserveLmuWindowsBrakeTemperatureUseCase
import kurou.kodriver.domain.lmuwindows.usecase.ObserveLmuWindowsBrakeWearReadoutTextUseCase
import kurou.kodriver.domain.lmuwindows.usecase.ObserveLmuWindowsBrakeWearRemainingUseCase
import kurou.kodriver.domain.lmuwindows.usecase.ObserveLmuWindowsFlagEnabledStatesUseCase
import kurou.kodriver.domain.lmuwindows.usecase.ObserveLmuWindowsFullCourseYellowFlagReadoutTextUseCase
import kurou.kodriver.domain.lmuwindows.usecase.ObserveLmuWindowsMyBestLapReadoutTextUseCase
import kurou.kodriver.domain.lmuwindows.usecase.ObserveLmuWindowsPitTimingEnabledStatesUseCase
import kurou.kodriver.domain.lmuwindows.usecase.ObserveLmuWindowsPitTimingTyreWearImminentReadoutTextUseCase
import kurou.kodriver.domain.lmuwindows.usecase.ObserveLmuWindowsPitTimingTyreWearLapsUseCase
import kurou.kodriver.domain.lmuwindows.usecase.ObserveLmuWindowsPitTimingTyreWearReadoutTextUseCase
import kurou.kodriver.domain.lmuwindows.usecase.ObserveLmuWindowsPitTimingVirtualEnergyImminentReadoutTextUseCase
import kurou.kodriver.domain.lmuwindows.usecase.ObserveLmuWindowsPitTimingVirtualEnergyLapsUseCase
import kurou.kodriver.domain.lmuwindows.usecase.ObserveLmuWindowsPitTimingVirtualEnergyReadoutTextUseCase
import kurou.kodriver.domain.lmuwindows.usecase.ObserveLmuWindowsRaceFlagsUseCase
import kurou.kodriver.domain.lmuwindows.usecase.ObserveLmuWindowsRedFlagReadoutTextUseCase
import kurou.kodriver.domain.lmuwindows.usecase.ObserveLmuWindowsRemainingVirtualEnergyReadoutTextUseCase
import kurou.kodriver.domain.lmuwindows.usecase.ObserveLmuWindowsRemainingVirtualEnergyThresholdPercentageUseCase
import kurou.kodriver.domain.lmuwindows.usecase.ObserveLmuWindowsSectorYellowFlagReadoutTextUseCase
import kurou.kodriver.domain.lmuwindows.usecase.ObserveLmuWindowsTyreCarcassTemperatureUseCase
import kurou.kodriver.domain.lmuwindows.usecase.ObserveLmuWindowsTyreDetachedUseCase
import kurou.kodriver.domain.lmuwindows.usecase.ObserveLmuWindowsTyreTemperatureColdReadoutTextUseCase
import kurou.kodriver.domain.lmuwindows.usecase.ObserveLmuWindowsTyreTemperatureEnabledStatesUseCase
import kurou.kodriver.domain.lmuwindows.usecase.ObserveLmuWindowsTyreTemperatureOverheatReadoutTextUseCase
import kurou.kodriver.domain.lmuwindows.usecase.ObserveLmuWindowsTyreWearReadoutTextUseCase
import kurou.kodriver.domain.lmuwindows.usecase.ObserveLmuWindowsTyreWearThresholdPercentageUseCase
import kurou.kodriver.domain.lmuwindows.usecase.ObserveLmuWindowsTyreWearUseCase
import kurou.kodriver.domain.lmuwindows.usecase.ObserveLmuWindowsUseCase
import kurou.kodriver.domain.lmuwindows.usecase.ObserveLmuWindowsVehicleApproachEnabledStatesUseCase
import kurou.kodriver.domain.lmuwindows.usecase.ObserveLmuWindowsVehicleApproachSkipFirstLapUseCase
import kurou.kodriver.domain.lmuwindows.usecase.ObserveLmuWindowsVehicleApproachStartLeftReadoutTextUseCase
import kurou.kodriver.domain.lmuwindows.usecase.ObserveLmuWindowsVehicleApproachStartRightReadoutTextUseCase
import kurou.kodriver.domain.lmuwindows.usecase.ObserveLmuWindowsVehicleApproachSustainedDurationUseCase
import kurou.kodriver.domain.lmuwindows.usecase.ObserveLmuWindowsVehicleApproachSustainedLeftReadoutTextUseCase
import kurou.kodriver.domain.lmuwindows.usecase.ObserveLmuWindowsVehicleApproachSustainedRightReadoutTextUseCase
import kurou.kodriver.domain.lmuwindows.usecase.ObserveLmuWindowsVehicleApproachUseCase
import kurou.kodriver.domain.lmuwindows.usecase.ObserveLmuWindowsVehicleClassBrakeTemperatureHighThresholdUseCase
import kurou.kodriver.domain.lmuwindows.usecase.ObserveLmuWindowsVehicleClassBrakeWearLowThresholdUseCase
import kurou.kodriver.domain.lmuwindows.usecase.ObserveLmuWindowsVehicleClassTyreTemperatureHighThresholdUseCase
import kurou.kodriver.domain.lmuwindows.usecase.ObserveLmuWindowsVehicleClassUseCase
import kurou.kodriver.domain.lmuwindows.usecase.ObserveLmuWindowsVehicleDamageEnabledStatesUseCase
import kurou.kodriver.domain.lmuwindows.usecase.ObserveLmuWindowsVehicleDamageOverheatReadoutTextUseCase
import kurou.kodriver.domain.lmuwindows.usecase.ObserveLmuWindowsVehicleDamagePartDetachedReadoutTextUseCase
import kurou.kodriver.domain.lmuwindows.usecase.ObserveLmuWindowsVehicleDamageTyreDetachedReadoutTextUseCase
import kurou.kodriver.domain.lmuwindows.usecase.ObserveLmuWindowsVehicleDamageUseCase
import kurou.kodriver.domain.lmuwindows.usecase.ObserveLmuWindowsVirtualEnergyUseCase
import kurou.kodriver.domain.model.ReadoutStartSoundType
import kurou.kodriver.domain.model.Simulator
import kurou.kodriver.domain.usecase.ObserveAutoReadoutEnabledStatesUseCase
import kurou.kodriver.domain.usecase.ObserveAutoReadoutOrderUseCase
import kurou.kodriver.domain.usecase.ObserveDebugReadoutRequestUseCase
import kurou.kodriver.domain.usecase.ObserveManualReadoutHotkeyPressesUseCase
import kurou.kodriver.domain.usecase.ObserveManualReadoutRequestsUseCase
import kurou.kodriver.domain.usecase.ObserveManualReadoutSetsUseCase
import kurou.kodriver.domain.usecase.ObserveQueueEnabledStatesUseCase
import kurou.kodriver.domain.usecase.ObserveReadoutHotkeyUseCase
import kurou.kodriver.domain.usecase.ObserveReadoutStartSoundEnabledStatesUseCase
import kurou.kodriver.domain.usecase.ObserveReadoutStartSoundTypeUseCase
import kurou.kodriver.domain.usecase.ObserveResolvedAutoReadoutOrderUseCase
import kurou.kodriver.domain.usecase.ObserveSelectedSimulatorUseCase
import kurou.kodriver.domain.usecase.ObserveSoundVolumeUseCase
import kurou.kodriver.domain.usecase.ObserveVoicePitchUseCase
import kurou.kodriver.domain.usecase.ObserveVoiceSpeedUseCase
import kurou.kodriver.domain.usecase.ObserveVoiceUseCase
import kurou.kodriver.domain.usecase.PlaySpeechEventUseCase
import kurou.kodriver.domain.usecase.PlayStartSoundForKeyUseCase
import kurou.kodriver.domain.usecase.ResolveAutoReadoutOrderUseCase
import kurou.kodriver.domain.usecase.SaveTelemetryLogUseCase
import kurou.kodriver.domain.usecase.SpeakTextUseCase
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
 *   RemainingVirtualEnergyUseCases / PitTimingUseCases / LmuWindowsReadoutTextUseCases）、
 *   それらが束ねる各ドメイン UseCase、named(Simulator.LmuWindows.id) の音声再生系
 *   （PlaySpeechEventUseCase・PlayStartSoundForKeyUseCase・SpeakTextUseCase・
 *   各フラッグ・車両接近・VE残量警告・タイヤ過熱警告・VEピットタイミング・自己ベストの読み上げ文言の Observe UseCase・TextToSpeechEngine）、
 *   および LmuWindowsReadoutTextSpeaker
 *   （フラッグ・車両接近・VE残量警告・タイヤ過熱警告・VEピットタイミング・自己ベストの読み上げ時に
 *   自由文字列をOS標準TTSで読み上げるフック。NarratorEngine の customSpeak に渡す）。
 * 音声・速度・高さ設定監視用の ObserveVoiceUseCase・ObserveVoiceSpeedUseCase・ObserveVoicePitchUseCase を提供し、
 * VoicePreferencesRepository・VoiceSpeedPreferencesRepository・VoicePitchPreferencesRepository（:core:data）を消費する。
 * 消費（get で解決）: 各 UseCase の依存 Repository（:core:lmu-windows-data / :core:data）、
 *   SoundPlayer（[platformSoundModule]）、unqualified の CheckTextToSpeechAvailableUseCase
 *   （:feature:lmu-windows-auto-readout-flag-detail が登録。TTSが実際に利用可能かどうかは
 *   試聴用途と共有しても問題ないため named では区別しない）。
 * 音声系は GT7 と区別するため named(Simulator.LmuWindows.id) で登録している。
 */
@OptIn(ExperimentalResourceApi::class)
val lmuWindowsNarratorModule: Module =
    module {
        factory { ObserveDebugReadoutRequestUseCase(get()) }
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
                get(),
                observeDebugReadoutRequest = get(),
            )
        }

        // この feature 固有の UseCase 集約 data class（本モジュールで定義）
        factory { ObserveVoiceUseCase(get()) }
        factory(named(Simulator.LmuWindows.id)) { ObserveVoiceSpeedUseCase(get()) }
        factory(named(Simulator.LmuWindows.id)) { ObserveVoicePitchUseCase(get()) }
        factory { NarratorUseCases(get(), get(), get(), get(), get(), get(), get(), get()) }
        factory { FlagUseCases(get(), get()) }
        factory { VehicleApproachUseCases(get(), get(), get(), get(), get()) }
        factory { VehicleDamageUseCases(get(), get()) }
        factory { TyreDetachedUseCases(get()) }
        factory { SimulatorUseCases(get()) }
        factory { ReadoutListUseCases(get(), get(), get()) }
        factory { TyreTemperatureUseCases(get(), get(), get(), get(), get()) }
        factory { TyreWearUseCases(get(), get()) }
        factory { BrakeTemperatureUseCases(get(), get()) }
        factory { BrakeWearUseCases(get(), get()) }
        factory {
            RemainingVirtualEnergyUseCases(
                get(),
                get(),
                get(),
                get(),
                ObserveManualReadoutSetsUseCase(get(named(Simulator.LmuWindows.id))),
                get(),
            )
        }
        factory { PitTimingUseCases(get(), get(), get()) }
        factory {
            LmuWindowsNarratorEventProcessor(
                get(named(Simulator.LmuWindows.id)),
                get(),
                get<LmuWindowsReadoutTextSpeaker>()::readoutText,
            )
        }

        // ドメイン UseCase（:core:domain。get() は :core:lmu-windows-data / :core:data の Repository を解決）
        factory { DetermineLmuWindowsMyBestLapReadoutUseCase() }
        factory { DetermineLmuWindowsVehicleReadoutUseCase() }
        factory { DetermineLmuWindowsTyreReadoutUseCase() }
        factory { DetermineLmuWindowsBrakeReadoutUseCase() }
        factory { DetermineLmuWindowsVirtualEnergyReadoutUseCase(get()) }
        factory { DetermineLmuWindowsManualReadoutUseCase() }
        factory { DetermineLmuWindowsPitTimingReadoutUseCase() }
        factory { DetermineLmuWindowsRaceFlagReadoutUseCase() }
        factory { ObserveReadoutHotkeyUseCase(get()) }
        factory { ObserveManualReadoutRequestsUseCase(get()) }
        factory { ObserveManualReadoutHotkeyPressesUseCase(get()) }
        factory { SaveTelemetryLogUseCase(get()) }
        factory { ObserveLmuWindowsFlagEnabledStatesUseCase(get()) }
        factory(named(Simulator.LmuWindows.id)) { ObserveLmuWindowsMyBestLapReadoutTextUseCase(get()) }
        factory { ObserveLmuWindowsUseCase(get()) }
        factory { ObserveLmuWindowsVehicleApproachUseCase(get()) }
        factory { ObserveLmuWindowsRaceFlagsUseCase(get()) }
        factory { ObserveAutoReadoutEnabledStatesUseCase(get()) }
        factory { ObserveAutoReadoutOrderUseCase(get()) }
        factory { ResolveAutoReadoutOrderUseCase() }
        factory { ObserveResolvedAutoReadoutOrderUseCase(get(), get()) }
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
        factory { ObserveLmuWindowsBrakeWearRemainingUseCase(get(), get()) }
        factory { ObserveLmuWindowsVehicleClassBrakeWearLowThresholdUseCase(get()) }
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
        // feature:lmu-windows-auto-readout-flag-detail が試聴用に別途定義しているため、
        // 同じ型を二重定義しないよう named(Simulator.LmuWindows.id) で区別する。
        factory(named(Simulator.LmuWindows.id)) {
            SpeakTextUseCase(
                get(),
                get(),
                get(named(Simulator.LmuWindows.id)),
                get(named(Simulator.LmuWindows.id)),
            )
        }
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
        factory(named(Simulator.LmuWindows.id)) { ObserveLmuWindowsBrakeWearReadoutTextUseCase(get()) }
        factory(named(Simulator.LmuWindows.id)) { ObserveLmuWindowsTyreTemperatureOverheatReadoutTextUseCase(get()) }
        factory(named(Simulator.LmuWindows.id)) { ObserveLmuWindowsTyreTemperatureColdReadoutTextUseCase(get()) }
        factory(named(Simulator.LmuWindows.id)) { ObserveLmuWindowsVehicleDamageOverheatReadoutTextUseCase(get()) }
        factory(named(Simulator.LmuWindows.id)) { ObserveLmuWindowsVehicleDamagePartDetachedReadoutTextUseCase(get()) }
        factory(named(Simulator.LmuWindows.id)) { ObserveLmuWindowsVehicleDamageTyreDetachedReadoutTextUseCase(get()) }
        factory {
            LmuWindowsReadoutTextSpeaker(
                readoutTexts =
                    LmuWindowsReadoutTextUseCases(
                        flag =
                            LmuWindowsFlagReadoutTextUseCases(
                                sectorYellow = get(named(Simulator.LmuWindows.id)),
                                blue = get(named(Simulator.LmuWindows.id)),
                                fullCourseYellow = get(named(Simulator.LmuWindows.id)),
                                red = get(named(Simulator.LmuWindows.id)),
                            ),
                        vehicleApproach =
                            LmuWindowsVehicleApproachReadoutTextUseCases(
                                startLeft = get(named(Simulator.LmuWindows.id)),
                                startRight = get(named(Simulator.LmuWindows.id)),
                                sustainedLeft = get(named(Simulator.LmuWindows.id)),
                                sustainedRight = get(named(Simulator.LmuWindows.id)),
                            ),
                        pitTiming =
                            LmuWindowsPitTimingReadoutTextUseCases(
                                virtualEnergy = get(named(Simulator.LmuWindows.id)),
                                virtualEnergyImminent = get(named(Simulator.LmuWindows.id)),
                                tyreWear = get(named(Simulator.LmuWindows.id)),
                                tyreWearImminent = get(named(Simulator.LmuWindows.id)),
                            ),
                        tyre =
                            LmuWindowsTyreReadoutTextUseCases(
                                wear = get(named(Simulator.LmuWindows.id)),
                                overheat = get(named(Simulator.LmuWindows.id)),
                                cold = get(named(Simulator.LmuWindows.id)),
                            ),
                        vehicleDamage =
                            LmuWindowsVehicleDamageReadoutTextUseCases(
                                overheat = get(named(Simulator.LmuWindows.id)),
                                partDetached = get(named(Simulator.LmuWindows.id)),
                                tyreDetached = get(named(Simulator.LmuWindows.id)),
                            ),
                        brake =
                            LmuWindowsBrakeReadoutTextUseCases(
                                temperature = get(named(Simulator.LmuWindows.id)),
                                wear = get(named(Simulator.LmuWindows.id)),
                            ),
                        remainingVirtualEnergy = get(named(Simulator.LmuWindows.id)),
                        myBestLap = get(named(Simulator.LmuWindows.id)),
                    ),
                checkTextToSpeechAvailable = get(),
                speakText = get(named(Simulator.LmuWindows.id)),
            )
        }

        single<TextToSpeechEngine>(named(Simulator.LmuWindows.id)) {
            LmuWindowsNarratorEngine(
                NarratorEngine(
                    soundPlayer = get(named(Simulator.LmuWindows.id)),
                    resources =
                        WavResources(
                            startSoundTypeToFile = lmuWindowsStartSoundTypeToFile,
                            startSoundResourceLoader = ::readStartSoundBytes,
                        ),
                    eventToKey = { it.readoutItemKey },
                    defaultStartSoundType = ReadoutStartSoundType.FORMULA_RADIO,
                    volumeFlow = ObserveSoundVolumeUseCase(get())(),
                    startSoundTypeFlow = ObserveReadoutStartSoundTypeUseCase(get())(),
                    startSoundEnabledStatesFlow = ObserveReadoutStartSoundEnabledStatesUseCase(get())(),
                    customSpeak = get<LmuWindowsReadoutTextSpeaker>()::invoke,
                    isCustomSpeakEvent = ::isLmuWindowsCustomSpeakEvent,
                ),
            )
        }
    }

private val lmuWindowsStartSoundTypeToFile: Map<ReadoutStartSoundType, String> =
    mapOf(
        ReadoutStartSoundType.FORMULA_RADIO to "files/formula_radio.wav",
        ReadoutStartSoundType.ELECTRONIC_NOISE to "files/electronic_noise.wav",
    )
