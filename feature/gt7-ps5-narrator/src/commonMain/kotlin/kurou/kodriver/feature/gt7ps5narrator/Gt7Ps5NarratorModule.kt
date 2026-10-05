package kurou.kodriver.feature.gt7ps5narrator

import kurou.kodriver.core.designsystem.readStartSoundBytes
import kurou.kodriver.core.narrator.WavNarratorEngine
import kurou.kodriver.core.narrator.WavResources
import kurou.kodriver.core.narrator.platformSoundModule
import kurou.kodriver.domain.engine.Gt7Ps5ReadoutTextEvent
import kurou.kodriver.domain.engine.SpeechEvent
import kurou.kodriver.domain.engine.TextToSpeechEngine
import kurou.kodriver.domain.model.ReadoutStartSoundType
import kurou.kodriver.domain.model.Simulator
import kurou.kodriver.domain.usecase.CheckTextToSpeechAvailableUseCase
import kurou.kodriver.domain.usecase.DetermineGt7Ps5NarratorReadoutUseCase
import kurou.kodriver.domain.usecase.ObserveGt7Ps5MyBestLapReadoutTextUseCase
import kurou.kodriver.domain.usecase.ObserveGt7Ps5RemainingFuelLapsEmptyReadoutTextUseCase
import kurou.kodriver.domain.usecase.ObserveGt7Ps5RemainingFuelLapsReadoutTextUseCase
import kurou.kodriver.domain.usecase.ObserveGt7Ps5RemainingFuelLapsUseCase
import kurou.kodriver.domain.usecase.ObserveGt7Ps5RemainingFuelReadoutTextUseCase
import kurou.kodriver.domain.usecase.ObserveGt7Ps5RemainingFuelThresholdPercentageUseCase
import kurou.kodriver.domain.usecase.ObserveGt7Ps5TyreTemperatureEnabledStatesUseCase
import kurou.kodriver.domain.usecase.ObserveGt7Ps5TyreTemperatureHighThresholdUseCase
import kurou.kodriver.domain.usecase.ObserveGt7Ps5TyreTemperatureOverheatReadoutTextUseCase
import kurou.kodriver.domain.usecase.ObserveGt7Ps5UseCase
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
 *   （MyBestLapUseCases / SimulatorUseCases / ReadoutListUseCases / RemainingFuelLapsUseCases / RemainingFuelUseCases /
 *   TyreTemperatureUseCases）、それらが束ねる
 *   各ドメイン UseCase、および named(Simulator.Gt7Ps5.id) の音声再生系
 *   （PlaySpeechEventUseCase・PlayStartSoundForKeyUseCase・TextToSpeechEngine・SpeakTextUseCase・
 *   自己ベストラップ更新・タイヤ過熱・燃料残量・残り周回数文言の Observe UseCase・
 *   CheckTextToSpeechAvailableUseCase・ObserveVoiceUseCase）、および Gt7Ps5ReadoutTextSpeaker。
 * 消費（get で解決）: 各 UseCase の依存 Repository（:core:gt7-ps5-data / :core:data）、
 *   TextToSpeechRepository・VoicePreferencesRepository、および SoundPlayer（[platformSoundModule]）。
 * 音声系は LMU と区別するため named(Simulator.Gt7Ps5.id) で登録している。
 */
@OptIn(ExperimentalResourceApi::class)
val gt7Ps5NarratorModule: Module =
    module {
        // ViewModel（Gt7Ps5NarratorEventProcessor 経由で下記の TextToSpeechEngine を利用）
        viewModel { Gt7Ps5NarratorViewModel(get(), get(), get(), get(), get(), get(), get(), get()) }

        // この feature 固有の UseCase 集約 data class（本モジュールで定義）
        factory { MyBestLapUseCases(get()) }
        factory { SimulatorUseCases(get()) }
        factory { ReadoutListUseCases(get(), get(), get()) }
        factory { RemainingFuelLapsUseCases(get()) }
        factory { RemainingFuelUseCases(get()) }
        factory { TyreTemperatureUseCases(get(), get()) }
        factory {
            Gt7Ps5NarratorEventProcessor(
                get(named(Simulator.Gt7Ps5.id)),
                get(),
                get<Gt7Ps5ReadoutTextSpeaker>()::readoutText,
            )
        }

        // ドメイン UseCase（:core:domain。get() は :core:gt7-ps5-data / :core:data の Repository を解決）
        factory { DetermineGt7Ps5NarratorReadoutUseCase() }
        factory { SaveTelemetryLogUseCase(get()) }
        factory { ObserveGt7Ps5UseCase(get()) }
        factory { ObserveReadoutEnabledStatesUseCase(get()) }
        factory { ObserveReadoutOrderUseCase(get()) }
        factory { ResolveReadoutOrderUseCase() }
        factory { ObserveResolvedReadoutOrderUseCase(get(), get()) }
        factory { ObserveSelectedSimulatorUseCase(get()) }
        factory { ObserveGt7Ps5RemainingFuelLapsUseCase(get()) }
        factory { ObserveGt7Ps5RemainingFuelThresholdPercentageUseCase(get()) }
        factory { ObserveGt7Ps5TyreTemperatureHighThresholdUseCase(get()) }
        factory { ObserveGt7Ps5TyreTemperatureEnabledStatesUseCase(get()) }
        factory { ObserveQueueEnabledStatesUseCase(get()) }

        // PR2の詳細ペイン用UseCaseや他シミュレーターと区別し、GT7のTTS依存を本モジュールで登録する。
        factory(named(Simulator.Gt7Ps5.id)) { ObserveGt7Ps5RemainingFuelReadoutTextUseCase(get()) }
        factory(named(Simulator.Gt7Ps5.id)) { ObserveGt7Ps5RemainingFuelLapsReadoutTextUseCase(get()) }
        factory(named(Simulator.Gt7Ps5.id)) { ObserveGt7Ps5RemainingFuelLapsEmptyReadoutTextUseCase(get()) }
        factory(named(Simulator.Gt7Ps5.id)) { ObserveGt7Ps5TyreTemperatureOverheatReadoutTextUseCase(get()) }
        factory(named(Simulator.Gt7Ps5.id)) { ObserveGt7Ps5MyBestLapReadoutTextUseCase(get()) }
        factory(named(Simulator.Gt7Ps5.id)) { ObserveVoiceUseCase(get()) }
        factory(named(Simulator.Gt7Ps5.id)) { CheckTextToSpeechAvailableUseCase(get()) }
        factory(named(Simulator.Gt7Ps5.id)) { SpeakTextUseCase(get(), get(named(Simulator.Gt7Ps5.id))) }
        factory {
            Gt7Ps5ReadoutTextSpeaker(
                observeRemainingFuelLapsReadoutText = get(named(Simulator.Gt7Ps5.id)),
                observeRemainingFuelLapsEmptyReadoutText = get(named(Simulator.Gt7Ps5.id)),
                observeRemainingFuelReadoutText = get(named(Simulator.Gt7Ps5.id)),
                observeMyBestLapReadoutText = get(named(Simulator.Gt7Ps5.id)),
                observeTyreOverheatReadoutText = get(named(Simulator.Gt7Ps5.id)),
                checkTextToSpeechAvailable = get(named(Simulator.Gt7Ps5.id)),
                speakText = get(named(Simulator.Gt7Ps5.id)),
            )
        }

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
                    customSpeak = get<Gt7Ps5ReadoutTextSpeaker>()::invoke,
                    isCustomSpeakEvent = { it is Gt7Ps5ReadoutTextEvent },
                    eventToKey = { it.readoutItemKey },
                    defaultStartSoundType = ReadoutStartSoundType.FORMULA_RADIO,
                    volumeFlow = ObserveSoundVolumeUseCase(get())(),
                    startSoundTypeFlow = ObserveReadoutStartSoundTypeUseCase(get())(),
                    startSoundEnabledStatesFlow = ObserveReadoutStartSoundEnabledStatesUseCase(get())(),
                ),
            )
        }
        factory(named(Simulator.Gt7Ps5.id)) { PlayStartSoundForKeyUseCase(get(named(Simulator.Gt7Ps5.id))) }
        factory(named(Simulator.Gt7Ps5.id)) { PlaySpeechEventUseCase(get(named(Simulator.Gt7Ps5.id))) }
    }

private val gt7Ps5EventToFile: Map<SpeechEvent, String> = emptyMap()

private val gt7Ps5StartSoundTypeToFile: Map<ReadoutStartSoundType, String> =
    mapOf(
        ReadoutStartSoundType.FORMULA_RADIO to "files/formula_radio.wav",
        ReadoutStartSoundType.ELECTRONIC_NOISE to "files/electronic_noise.wav",
    )
