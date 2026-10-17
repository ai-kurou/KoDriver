package kurou.kodriver.feature.acewindowsnarrator

import kurou.kodriver.core.designsystem.readStartSoundBytes
import kurou.kodriver.core.narrator.NarratorEngine
import kurou.kodriver.core.narrator.WavResources
import kurou.kodriver.core.narrator.platformSoundModule
import kurou.kodriver.domain.acewindows.usecase.AceWindowsVehicleApproachThresholdsUseCases
import kurou.kodriver.domain.acewindows.usecase.DetermineAceWindowsNarratorReadoutUseCase
import kurou.kodriver.domain.acewindows.usecase.ObserveAceWindowsBestLapTimeUseCase
import kurou.kodriver.domain.acewindows.usecase.ObserveAceWindowsBlackFlagReadoutTextUseCase
import kurou.kodriver.domain.acewindows.usecase.ObserveAceWindowsBlackWhiteFlagReadoutTextUseCase
import kurou.kodriver.domain.acewindows.usecase.ObserveAceWindowsBlueFlagReadoutTextUseCase
import kurou.kodriver.domain.acewindows.usecase.ObserveAceWindowsCheckeredFlagReadoutTextUseCase
import kurou.kodriver.domain.acewindows.usecase.ObserveAceWindowsFlagEnabledStatesUseCase
import kurou.kodriver.domain.acewindows.usecase.ObserveAceWindowsFlagUseCase
import kurou.kodriver.domain.acewindows.usecase.ObserveAceWindowsFuelUseCase
import kurou.kodriver.domain.acewindows.usecase.ObserveAceWindowsGreenFlagReadoutTextUseCase
import kurou.kodriver.domain.acewindows.usecase.ObserveAceWindowsMyBestLapReadoutTextUseCase
import kurou.kodriver.domain.acewindows.usecase.ObserveAceWindowsOrangeCircleFlagReadoutTextUseCase
import kurou.kodriver.domain.acewindows.usecase.ObserveAceWindowsRedFlagReadoutTextUseCase
import kurou.kodriver.domain.acewindows.usecase.ObserveAceWindowsRedYellowStripesFlagReadoutTextUseCase
import kurou.kodriver.domain.acewindows.usecase.ObserveAceWindowsRemainingFuelLapsEmptyReadoutTextUseCase
import kurou.kodriver.domain.acewindows.usecase.ObserveAceWindowsRemainingFuelLapsReadoutTextUseCase
import kurou.kodriver.domain.acewindows.usecase.ObserveAceWindowsRemainingFuelLapsThresholdUseCase
import kurou.kodriver.domain.acewindows.usecase.ObserveAceWindowsRemainingFuelLapsUseCase
import kurou.kodriver.domain.acewindows.usecase.ObserveAceWindowsRemainingFuelReadoutTextUseCase
import kurou.kodriver.domain.acewindows.usecase.ObserveAceWindowsRemainingFuelThresholdPercentageUseCase
import kurou.kodriver.domain.acewindows.usecase.ObserveAceWindowsTyreCarcassTemperatureUseCase
import kurou.kodriver.domain.acewindows.usecase.ObserveAceWindowsTyreTemperatureEnabledStatesUseCase
import kurou.kodriver.domain.acewindows.usecase.ObserveAceWindowsTyreTemperatureHighThresholdUseCase
import kurou.kodriver.domain.acewindows.usecase.ObserveAceWindowsTyreTemperatureOverheatReadoutTextUseCase
import kurou.kodriver.domain.acewindows.usecase.ObserveAceWindowsVehicleApproachEnabledStatesUseCase
import kurou.kodriver.domain.acewindows.usecase.ObserveAceWindowsVehicleApproachReadoutTextUseCase
import kurou.kodriver.domain.acewindows.usecase.ObserveAceWindowsVehicleApproachUseCase
import kurou.kodriver.domain.acewindows.usecase.ObserveAceWindowsWhiteFlagReadoutTextUseCase
import kurou.kodriver.domain.acewindows.usecase.ObserveAceWindowsYellowFlagReadoutTextUseCase
import kurou.kodriver.domain.engine.TextToSpeechEngine
import kurou.kodriver.domain.model.ReadoutStartSoundType
import kurou.kodriver.domain.model.Simulator
import kurou.kodriver.domain.usecase.CheckTextToSpeechAvailableUseCase
import kurou.kodriver.domain.usecase.ObserveAutoReadoutEnabledStatesUseCase
import kurou.kodriver.domain.usecase.ObserveAutoReadoutOrderUseCase
import kurou.kodriver.domain.usecase.ObserveDebugReadoutRequestUseCase
import kurou.kodriver.domain.usecase.ObserveManualReadoutHotkeyPressesUseCase
import kurou.kodriver.domain.usecase.ObserveManualReadoutRequestsUseCase
import kurou.kodriver.domain.usecase.ObserveManualReadoutSetsUseCase
import kurou.kodriver.domain.usecase.ObserveQueueEnabledStatesUseCase
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
 * ACE (Assetto Corsa EVO) Windows版 アナウンス制御（ace-windows-narrator feature）の Koin モジュール。
 *
 * 提供: AceWindowsNarratorViewModel、AceWindowsNarratorEventProcessor、この feature 内で定義した
 *   UseCase 集約 data class（MyBestLapUseCases / RemainingFuelUseCases / RemainingFuelLapsUseCases /
 *   ManualReadoutUseCases / SimulatorUseCases / ReadoutListUseCases / FlagUseCases / TyreTemperatureUseCases / VehicleApproachUseCases /
 *   AceWindowsFlagReadoutTextUseCases / AceWindowsRemainingFuelReadoutTextUseCases / AceWindowsReadoutTextUseCases）、
 *   それらが束ねる各ドメイン UseCase、および
 *   named(Simulator.AceWindows.id) の音声再生系（PlaySpeechEventUseCase・PlayStartSoundForKeyUseCase・
 *   TextToSpeechEngine・SpeakTextUseCase・
 *   ObserveVoiceUseCase・ObserveVoiceSpeedUseCase・ObserveVoicePitchUseCase・CheckTextToSpeechAvailableUseCase・フラッグ・車両接近・タイヤ過熱・燃料残量・残り周回数・自己ベストラップ文言Observe UseCase）、
 *   および自由文言TTSの AceWindowsReadoutTextSpeaker。
 * 消費（get で解決）: 各 UseCase の依存 Repository（:core:ace-windows-data / :core:data）、
 *   SoundPlayer（[platformSoundModule]）・TextToSpeechRepository（:core:text-to-speech-data）。
 * 音声・速度・高さ設定の VoicePreferencesRepository・VoiceSpeedPreferencesRepository・VoicePitchPreferencesRepository（:core:data）を消費する。
 * 音声系は LMU/GT7 と区別するため named(Simulator.AceWindows.id) で登録している。
 */
@OptIn(ExperimentalResourceApi::class)
val aceWindowsNarratorModule: Module =
    module {
        factory { ObserveDebugReadoutRequestUseCase(get()) }
        factory { ObserveManualReadoutRequestsUseCase(get()) }
        factory { ObserveManualReadoutHotkeyPressesUseCase(get()) }
        // ViewModel（AceWindowsNarratorEventProcessor 経由で下記の TextToSpeechEngine を利用）
        viewModel {
            AceWindowsNarratorViewModel(
                get(),
                get(),
                get(),
                get(),
                get(),
                get(),
                get(),
                get(),
                get(),
                manualReadoutUseCases = get(),
                observeDebugReadoutRequest = get(),
            )
        }

        // この feature 固有の UseCase 集約 data class（本モジュールで定義）
        factory { MyBestLapUseCases(get()) }
        factory { RemainingFuelUseCases(get(), get()) }
        factory { RemainingFuelLapsUseCases(get(), get()) }
        factory {
            ManualReadoutUseCases(
                get(),
                get(),
                ObserveManualReadoutSetsUseCase(get(named(Simulator.AceWindows.id))),
            )
        }
        factory { SimulatorUseCases(get()) }
        factory { ReadoutListUseCases(get(), get(), get()) }
        factory { FlagUseCases(get(), get()) }
        factory { TyreTemperatureUseCases(get(), get(), get()) }
        factory { VehicleApproachUseCases(get(), get(), get()) }
        factory {
            AceWindowsNarratorEventProcessor(
                get(named(Simulator.AceWindows.id)),
                get(),
                get<AceWindowsReadoutTextSpeaker>()::readoutText,
            )
        }

        // ドメイン UseCase（:core:domain。get() は :core:ace-windows-data / :core:data の Repository を解決）
        factory { DetermineAceWindowsNarratorReadoutUseCase() }
        factory { SaveTelemetryLogUseCase(get()) }
        factory { ObserveAceWindowsBestLapTimeUseCase(get()) }
        factory { ObserveAceWindowsFuelUseCase(get()) }
        factory { ObserveAceWindowsRemainingFuelThresholdPercentageUseCase(get()) }
        factory { ObserveAceWindowsRemainingFuelLapsUseCase(get()) }
        factory { ObserveAceWindowsRemainingFuelLapsThresholdUseCase(get()) }
        factory { ObserveAceWindowsFlagUseCase(get()) }
        factory { ObserveAceWindowsFlagEnabledStatesUseCase(get()) }
        factory { ObserveAceWindowsTyreCarcassTemperatureUseCase(get()) }
        factory { ObserveAceWindowsTyreTemperatureHighThresholdUseCase(get()) }
        factory { ObserveAceWindowsTyreTemperatureEnabledStatesUseCase(get()) }
        factory { ObserveAceWindowsVehicleApproachUseCase(get()) }
        factory { ObserveAceWindowsVehicleApproachEnabledStatesUseCase(get()) }
        factory { AceWindowsVehicleApproachThresholdsUseCases(get()) }
        factory { ObserveAutoReadoutEnabledStatesUseCase(get()) }
        factory { ObserveAutoReadoutOrderUseCase(get()) }
        factory { ResolveAutoReadoutOrderUseCase() }
        factory { ObserveResolvedAutoReadoutOrderUseCase(get(), get()) }
        factory { ObserveSelectedSimulatorUseCase(get()) }
        factory { ObserveQueueEnabledStatesUseCase(get()) }

        // TTS依存は他シミュレーターのunqualified登録と区別する。
        factory(named(Simulator.AceWindows.id)) { ObserveAceWindowsCheckeredFlagReadoutTextUseCase(get()) }
        factory(named(Simulator.AceWindows.id)) { ObserveAceWindowsWhiteFlagReadoutTextUseCase(get()) }
        factory(named(Simulator.AceWindows.id)) { ObserveAceWindowsGreenFlagReadoutTextUseCase(get()) }
        factory(named(Simulator.AceWindows.id)) { ObserveAceWindowsRedFlagReadoutTextUseCase(get()) }
        factory(named(Simulator.AceWindows.id)) { ObserveAceWindowsBlueFlagReadoutTextUseCase(get()) }
        factory(named(Simulator.AceWindows.id)) { ObserveAceWindowsYellowFlagReadoutTextUseCase(get()) }
        factory(named(Simulator.AceWindows.id)) { ObserveAceWindowsBlackFlagReadoutTextUseCase(get()) }
        factory(named(Simulator.AceWindows.id)) { ObserveAceWindowsBlackWhiteFlagReadoutTextUseCase(get()) }
        factory(named(Simulator.AceWindows.id)) { ObserveAceWindowsOrangeCircleFlagReadoutTextUseCase(get()) }
        factory(named(Simulator.AceWindows.id)) { ObserveAceWindowsRedYellowStripesFlagReadoutTextUseCase(get()) }
        factory(named(Simulator.AceWindows.id)) { ObserveAceWindowsVehicleApproachReadoutTextUseCase(get()) }
        factory(named(Simulator.AceWindows.id)) { ObserveAceWindowsTyreTemperatureOverheatReadoutTextUseCase(get()) }
        factory(named(Simulator.AceWindows.id)) { ObserveAceWindowsRemainingFuelReadoutTextUseCase(get()) }
        factory(named(Simulator.AceWindows.id)) { ObserveAceWindowsRemainingFuelLapsReadoutTextUseCase(get()) }
        factory(named(Simulator.AceWindows.id)) { ObserveAceWindowsRemainingFuelLapsEmptyReadoutTextUseCase(get()) }
        factory(named(Simulator.AceWindows.id)) { ObserveAceWindowsMyBestLapReadoutTextUseCase(get()) }
        factory(named(Simulator.AceWindows.id)) { ObserveVoiceUseCase(get()) }
        factory(named(Simulator.AceWindows.id)) { ObserveVoiceSpeedUseCase(get()) }
        factory(named(Simulator.AceWindows.id)) { ObserveVoicePitchUseCase(get()) }
        factory(named(Simulator.AceWindows.id)) { CheckTextToSpeechAvailableUseCase(get()) }
        factory(named(Simulator.AceWindows.id)) {
            SpeakTextUseCase(
                get(),
                get(named(Simulator.AceWindows.id)),
                get(named(Simulator.AceWindows.id)),
                get(named(Simulator.AceWindows.id)),
            )
        }
        factory {
            AceWindowsReadoutTextSpeaker(
                readoutTexts =
                    AceWindowsReadoutTextUseCases(
                        flag =
                            AceWindowsFlagReadoutTextUseCases(
                                checkered = get(named(Simulator.AceWindows.id)),
                                white = get(named(Simulator.AceWindows.id)),
                                green = get(named(Simulator.AceWindows.id)),
                                red = get(named(Simulator.AceWindows.id)),
                                blue = get(named(Simulator.AceWindows.id)),
                                yellow = get(named(Simulator.AceWindows.id)),
                                black = get(named(Simulator.AceWindows.id)),
                                blackWhite = get(named(Simulator.AceWindows.id)),
                                orangeCircle = get(named(Simulator.AceWindows.id)),
                                redYellowStripes = get(named(Simulator.AceWindows.id)),
                            ),
                        remainingFuel =
                            AceWindowsRemainingFuelReadoutTextUseCases(
                                fuel = get(named(Simulator.AceWindows.id)),
                                fuelLaps = get(named(Simulator.AceWindows.id)),
                                fuelLapsEmpty = get(named(Simulator.AceWindows.id)),
                            ),
                        vehicleApproach = get(named(Simulator.AceWindows.id)),
                        tyreOverheat = get(named(Simulator.AceWindows.id)),
                        myBestLap = get(named(Simulator.AceWindows.id)),
                    ),
                checkTextToSpeechAvailable = get(named(Simulator.AceWindows.id)),
                speakText = get(named(Simulator.AceWindows.id)),
            )
        }

        // 音声再生（named "ace_windows" で LMU/GT7 と分離。SoundPlayer は core:narrator の platformSoundModule が提供）
        includes(platformSoundModule(named(Simulator.AceWindows.id)))
        single<TextToSpeechEngine>(named(Simulator.AceWindows.id)) {
            AceWindowsNarratorEngine(
                NarratorEngine(
                    soundPlayer = get(named(Simulator.AceWindows.id)),
                    resources =
                        WavResources(
                            startSoundTypeToFile = aceWindowsStartSoundTypeToFile,
                            startSoundResourceLoader = ::readStartSoundBytes,
                        ),
                    customSpeak = get<AceWindowsReadoutTextSpeaker>()::invoke,
                    isCustomSpeakEvent = ::isAceWindowsCustomSpeakEvent,
                    eventToKey = { it.readoutItemKey },
                    defaultStartSoundType = ReadoutStartSoundType.FORMULA_RADIO,
                    volumeFlow = ObserveSoundVolumeUseCase(get())(),
                    startSoundTypeFlow = ObserveReadoutStartSoundTypeUseCase(get())(),
                    startSoundEnabledStatesFlow = ObserveReadoutStartSoundEnabledStatesUseCase(get())(),
                ),
            )
        }
        factory(named(Simulator.AceWindows.id)) { PlayStartSoundForKeyUseCase(get(named(Simulator.AceWindows.id))) }
        factory(named(Simulator.AceWindows.id)) { PlaySpeechEventUseCase(get(named(Simulator.AceWindows.id))) }
    }

private val aceWindowsStartSoundTypeToFile: Map<ReadoutStartSoundType, String> =
    mapOf(
        ReadoutStartSoundType.FORMULA_RADIO to "files/formula_radio.wav",
        ReadoutStartSoundType.ELECTRONIC_NOISE to "files/electronic_noise.wav",
    )
