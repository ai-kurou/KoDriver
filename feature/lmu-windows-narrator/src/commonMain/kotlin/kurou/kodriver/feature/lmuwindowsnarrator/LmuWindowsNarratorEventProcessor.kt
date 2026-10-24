package kurou.kodriver.feature.lmuwindowsnarrator

import kotlinx.serialization.encodeToString
import kurou.kodriver.core.narrator.SpeakOutcomes
import kurou.kodriver.core.narrator.TelemetryLogJson
import kurou.kodriver.core.narrator.TelemetryLogJsonCurrentField
import kurou.kodriver.core.narrator.TelemetryLogJsonPreviousField
import kurou.kodriver.core.narrator.buildTelemetryLogJson
import kurou.kodriver.core.narrator.runCatchingNarratorError
import kurou.kodriver.core.narrator.runCatchingTelemetryLogSave
import kurou.kodriver.core.narrator.speakWithOutcome
import kurou.kodriver.domain.engine.DebugReadoutText
import kurou.kodriver.domain.engine.ReadoutTextEvent
import kurou.kodriver.domain.engine.SpeechEvent
import kurou.kodriver.domain.engine.TextToSpeechEngine
import kurou.kodriver.domain.lmuwindows.model.LmuWindowsBrakeTemperatureData
import kurou.kodriver.domain.lmuwindows.model.LmuWindowsBrakeWearRemainingData
import kurou.kodriver.domain.lmuwindows.model.LmuWindowsRaceFlagsData
import kurou.kodriver.domain.lmuwindows.model.LmuWindowsRemainingFuelData
import kurou.kodriver.domain.lmuwindows.model.LmuWindowsTelemetryData
import kurou.kodriver.domain.lmuwindows.model.LmuWindowsTyreDetachedData
import kurou.kodriver.domain.lmuwindows.model.LmuWindowsTyreWearData
import kurou.kodriver.domain.lmuwindows.model.LmuWindowsVehicleApproachData
import kurou.kodriver.domain.lmuwindows.model.LmuWindowsVehicleDamageData
import kurou.kodriver.domain.lmuwindows.model.LmuWindowsVirtualEnergyData
import kurou.kodriver.domain.lmuwindows.usecase.LmuWindowsNarratorReadoutSettings
import kurou.kodriver.domain.lmuwindows.usecase.LmuWindowsNarratorState
import kurou.kodriver.domain.lmuwindows.usecase.TyreTemperatureReadoutInput
import kurou.kodriver.domain.model.DebugReadoutRequest
import kurou.kodriver.domain.model.NarrationOutcome
import kurou.kodriver.domain.model.ReadoutItemKey
import kurou.kodriver.domain.model.Simulator
import kurou.kodriver.domain.usecase.SaveTelemetryLogUseCase

internal data class LmuWindowsTelemetryLogContext(
    val state: LmuWindowsNarratorState,
    val settings: LmuWindowsNarratorReadoutSettings,
    val finalState: LmuWindowsNarratorState,
)

internal data class LmuWindowsTyreTemperatureLogContext(
    val state: LmuWindowsNarratorState,
    val settings: LmuWindowsNarratorReadoutSettings,
    val overheatState: LmuWindowsNarratorState,
    val finalState: LmuWindowsNarratorState,
)

internal data class LmuWindowsPitTimingLogContext(
    val state: LmuWindowsNarratorState,
    val settings: LmuWindowsNarratorReadoutSettings,
    val finalState: LmuWindowsNarratorState,
)

internal data class LmuWindowsPitTimingSnapshot(
    val telemetry: LmuWindowsTelemetryData,
    val virtualEnergy: LmuWindowsVirtualEnergyData,
    val tyreWear: LmuWindowsTyreWearData,
)

internal class LmuWindowsNarratorEventProcessor(
    private val ttsEngine: TextToSpeechEngine,
    private val saveTelemetryLog: SaveTelemetryLogUseCase,
    private val readoutText: suspend (SpeechEvent) -> String?,
) {
    private var previousTelemetry: LmuWindowsTelemetryData? = null
    private var previousVehicleApproach: LmuWindowsVehicleApproachData? = null
    private var previousVehicleDamage: LmuWindowsVehicleDamageData? = null
    private var previousTyreDetached: LmuWindowsTyreDetachedData? = null
    private var previousRaceFlags: LmuWindowsRaceFlagsData? = null
    private var previousTyreWear: LmuWindowsTyreWearData? = null
    private var previousRemainingVirtualEnergy: LmuWindowsVirtualEnergyData? = null
    private var previousRemainingFuel: LmuWindowsRemainingFuelData? = null
    private var previousBrakeTemperature: LmuWindowsBrakeTemperatureData? = null
    private var previousBrakeWear: LmuWindowsBrakeWearRemainingData? = null

    /** 判定・テンプレート置換を通さず、通常の優先度・音声・ログ経路で処理する。 */
    suspend fun processDebugReadout(
        request: DebugReadoutRequest,
        autoReadoutOrder: List<ReadoutItemKey>,
        queueEnabledStates: Map<ReadoutItemKey, Boolean>,
        observedAtMs: Long,
        logContext: LmuWindowsTelemetryLogContext,
    ) {
        processEvents(
            events = listOf(DebugReadoutText(request.readoutItemKey, request.text)),
            autoReadoutOrder = autoReadoutOrder,
            queueEnabledStates = queueEnabledStates,
            observedAtMs = observedAtMs,
        ) {
            """{"state":${TelemetryLogJson.encodeToString(logContext.state)},""" +
                """"settings":${TelemetryLogJson.encodeToString(logContext.settings)},""" +
                """"observedAtMs":$observedAtMs,"debugReadout":true}"""
        }
    }

    suspend fun processMyBestLap(
        telemetry: LmuWindowsTelemetryData,
        events: List<ReadoutTextEvent>,
        autoReadoutOrder: List<ReadoutItemKey>,
        queueEnabledStates: Map<ReadoutItemKey, Boolean>,
        observedAtMs: Long,
        logContext: LmuWindowsTelemetryLogContext,
    ) {
        val previous = previousTelemetry
        processEvents(events, autoReadoutOrder, queueEnabledStates, observedAtMs) {
            buildTelemetryLogJson(
                state = logContext.state,
                previous = previous,
                current = telemetry,
                settings = logContext.settings,
                observedAtMs = observedAtMs,
                finalState = logContext.finalState,
            )
        }
        previousTelemetry = telemetry
    }

    suspend fun processVehicleApproach(
        vehicleApproach: LmuWindowsVehicleApproachData,
        events: List<ReadoutTextEvent>,
        autoReadoutOrder: List<ReadoutItemKey>,
        queueEnabledStates: Map<ReadoutItemKey, Boolean>,
        observedAtMs: Long,
        logContext: LmuWindowsTelemetryLogContext,
    ) {
        val previous = previousVehicleApproach
        processEvents(events, autoReadoutOrder, queueEnabledStates, observedAtMs) {
            buildTelemetryLogJson(
                state = logContext.state,
                previous = previous,
                current = vehicleApproach,
                settings = logContext.settings,
                observedAtMs = observedAtMs,
                finalState = logContext.finalState,
            )
        }
        previousVehicleApproach = vehicleApproach
    }

    suspend fun processVehicleDamage(
        vehicleDamage: LmuWindowsVehicleDamageData,
        events: List<ReadoutTextEvent>,
        autoReadoutOrder: List<ReadoutItemKey>,
        queueEnabledStates: Map<ReadoutItemKey, Boolean>,
        observedAtMs: Long,
        logContext: LmuWindowsTelemetryLogContext,
    ) {
        val previous = previousVehicleDamage
        processEvents(events, autoReadoutOrder, queueEnabledStates, observedAtMs) {
            buildTelemetryLogJson(
                state = logContext.state,
                previous = previous,
                current = vehicleDamage,
                settings = logContext.settings,
                observedAtMs = observedAtMs,
                finalState = logContext.finalState,
            )
        }
        previousVehicleDamage = vehicleDamage
    }

    suspend fun processTyreDetached(
        tyreDetached: LmuWindowsTyreDetachedData,
        events: List<ReadoutTextEvent>,
        autoReadoutOrder: List<ReadoutItemKey>,
        queueEnabledStates: Map<ReadoutItemKey, Boolean>,
        observedAtMs: Long,
        logContext: LmuWindowsTelemetryLogContext,
    ) {
        val previous = previousTyreDetached
        processEvents(events, autoReadoutOrder, queueEnabledStates, observedAtMs) {
            buildTelemetryLogJson(
                state = logContext.state,
                previous = previous,
                current = tyreDetached,
                settings = logContext.settings,
                observedAtMs = observedAtMs,
                finalState = logContext.finalState,
            )
        }
        previousTyreDetached = tyreDetached
    }

    suspend fun processRaceFlags(
        raceFlags: LmuWindowsRaceFlagsData,
        events: List<ReadoutTextEvent>,
        autoReadoutOrder: List<ReadoutItemKey>,
        queueEnabledStates: Map<ReadoutItemKey, Boolean>,
        observedAtMs: Long,
        logContext: LmuWindowsTelemetryLogContext,
    ) {
        val previous = previousRaceFlags
        processEvents(events, autoReadoutOrder, queueEnabledStates, observedAtMs) {
            buildTelemetryLogJson(
                state = logContext.state,
                previous = previous,
                current = raceFlags,
                settings = logContext.settings,
                observedAtMs = observedAtMs,
                finalState = logContext.finalState,
            )
        }
        previousRaceFlags = raceFlags
    }

    suspend fun processTyreWear(
        tyreWear: LmuWindowsTyreWearData,
        events: List<ReadoutTextEvent>,
        autoReadoutOrder: List<ReadoutItemKey>,
        queueEnabledStates: Map<ReadoutItemKey, Boolean>,
        observedAtMs: Long,
        logContext: LmuWindowsTelemetryLogContext,
    ) {
        val previous = previousTyreWear
        processEvents(events, autoReadoutOrder, queueEnabledStates, observedAtMs) {
            buildTelemetryLogJson(
                state = logContext.state,
                previous = previous,
                current = tyreWear,
                settings = logContext.settings,
                observedAtMs = observedAtMs,
                finalState = logContext.finalState,
            )
        }
        previousTyreWear = tyreWear
    }

    suspend fun processBrakeTemperature(
        brakeTemperature: LmuWindowsBrakeTemperatureData,
        events: List<ReadoutTextEvent>,
        autoReadoutOrder: List<ReadoutItemKey>,
        queueEnabledStates: Map<ReadoutItemKey, Boolean>,
        observedAtMs: Long,
        logContext: LmuWindowsTelemetryLogContext,
    ) {
        val previous = previousBrakeTemperature
        processEvents(events, autoReadoutOrder, queueEnabledStates, observedAtMs) {
            buildTelemetryLogJson(
                state = logContext.state,
                previous = previous,
                current = brakeTemperature,
                settings = logContext.settings,
                observedAtMs = observedAtMs,
                finalState = logContext.finalState,
            )
        }
        previousBrakeTemperature = brakeTemperature
    }

    suspend fun processBrakeWear(
        brakeWear: LmuWindowsBrakeWearRemainingData,
        events: List<ReadoutTextEvent>,
        autoReadoutOrder: List<ReadoutItemKey>,
        queueEnabledStates: Map<ReadoutItemKey, Boolean>,
        observedAtMs: Long,
        logContext: LmuWindowsTelemetryLogContext,
    ) {
        val previous = previousBrakeWear
        processEvents(events, autoReadoutOrder, queueEnabledStates, observedAtMs) {
            buildTelemetryLogJson(
                state = logContext.state,
                previous = previous,
                current = brakeWear,
                settings = logContext.settings,
                observedAtMs = observedAtMs,
                finalState = logContext.finalState,
            )
        }
        previousBrakeWear = brakeWear
    }

    suspend fun processRemainingVirtualEnergy(
        remainingVirtualEnergy: LmuWindowsVirtualEnergyData?,
        events: List<ReadoutTextEvent>,
        autoReadoutOrder: List<ReadoutItemKey>,
        queueEnabledStates: Map<ReadoutItemKey, Boolean>,
        observedAtMs: Long,
        logContext: LmuWindowsTelemetryLogContext,
    ) {
        val previous = previousRemainingVirtualEnergy
        processEvents(events, autoReadoutOrder, queueEnabledStates, observedAtMs) {
            if (remainingVirtualEnergy == null) {
                """{"state":${TelemetryLogJson.encodeToString(logContext.state)},""" +
                    """"settings":${TelemetryLogJson.encodeToString(logContext.settings)},""" +
                    """"observedAtMs":$observedAtMs}"""
            } else {
                buildTelemetryLogJson(
                    state = logContext.state,
                    previous = previous,
                    current = remainingVirtualEnergy,
                    settings = logContext.settings,
                    observedAtMs = observedAtMs,
                    finalState = logContext.finalState,
                )
            }
        }
        if (remainingVirtualEnergy != null) previousRemainingVirtualEnergy = remainingVirtualEnergy
    }

    suspend fun processRemainingFuel(
        fuel: LmuWindowsRemainingFuelData,
        events: List<ReadoutTextEvent>,
        autoReadoutOrder: List<ReadoutItemKey>,
        queueEnabledStates: Map<ReadoutItemKey, Boolean>,
        observedAtMs: Long,
        logContext: LmuWindowsTelemetryLogContext,
    ) {
        val previous = previousRemainingFuel
        processEvents(events, autoReadoutOrder, queueEnabledStates, observedAtMs) {
            buildTelemetryLogJson(
                state = logContext.state,
                previous = previous,
                current = fuel,
                settings = logContext.settings,
                observedAtMs = observedAtMs,
                finalState = logContext.finalState,
            )
        }
        previousRemainingFuel = fuel
    }

    suspend fun processBrakeWearCurrentValue(
        brakeWear: LmuWindowsBrakeWearRemainingData?,
        events: List<ReadoutTextEvent>,
        autoReadoutOrder: List<ReadoutItemKey>,
        queueEnabledStates: Map<ReadoutItemKey, Boolean>,
        observedAtMs: Long,
        logContext: LmuWindowsTelemetryLogContext,
    ) {
        val previous = previousBrakeWear
        processEvents(events, autoReadoutOrder, queueEnabledStates, observedAtMs) {
            if (brakeWear == null) {
                """{"state":${TelemetryLogJson.encodeToString(logContext.state)},""" +
                    """"settings":${TelemetryLogJson.encodeToString(logContext.settings)},""" +
                    """"observedAtMs":$observedAtMs}"""
            } else {
                buildTelemetryLogJson(
                    state = logContext.state,
                    previous = previous,
                    current = brakeWear,
                    settings = logContext.settings,
                    observedAtMs = observedAtMs,
                    finalState = logContext.finalState,
                )
            }
        }
    }

    suspend fun processManualReadout(
        events: List<ReadoutTextEvent>,
        autoReadoutOrder: List<ReadoutItemKey>,
        queueEnabledStates: Map<ReadoutItemKey, Boolean>,
        observedAtMs: Long,
        logContext: LmuWindowsTelemetryLogContext,
    ) {
        processEvents(events, autoReadoutOrder, queueEnabledStates, observedAtMs) {
            """{"state":${TelemetryLogJson.encodeToString(logContext.state)},""" +
                """"settings":${TelemetryLogJson.encodeToString(logContext.settings)},""" +
                """"observedAtMs":$observedAtMs}"""
        }
    }

    suspend fun processTyreTemperature(
        input: TyreTemperatureReadoutInput,
        events: List<ReadoutTextEvent>,
        autoReadoutOrder: List<ReadoutItemKey>,
        queueEnabledStates: Map<ReadoutItemKey, Boolean>,
        observedAtMs: Long,
        logContext: LmuWindowsTyreTemperatureLogContext,
    ) {
        processEvents(events, autoReadoutOrder, queueEnabledStates, observedAtMs) {
            buildTelemetryLogJson(
                state = logContext.state,
                input = input,
                settings = logContext.settings,
                observedAtMs = observedAtMs,
                overheatState = logContext.overheatState,
                finalState = logContext.finalState,
            )
        }
    }

    suspend fun processPitTiming(
        snapshot: LmuWindowsPitTimingSnapshot,
        events: List<ReadoutTextEvent>,
        autoReadoutOrder: List<ReadoutItemKey>,
        queueEnabledStates: Map<ReadoutItemKey, Boolean>,
        observedAtMs: Long,
        logContext: LmuWindowsPitTimingLogContext,
    ) {
        processEvents(events, autoReadoutOrder, queueEnabledStates, observedAtMs) {
            buildPitTimingTelemetryLogJson(
                state = logContext.state,
                telemetry = snapshot.telemetry,
                virtualEnergy = snapshot.virtualEnergy,
                tyreWear = snapshot.tyreWear,
                settings = logContext.settings,
                observedAtMs = observedAtMs,
                finalState = logContext.finalState,
            )
        }
    }

    /** 本文の確定・優先度つき再生・ログ保存を全LMUイベントで共有する。 */
    private suspend fun <E : ReadoutTextEvent> processEvents(
        events: List<E>,
        autoReadoutOrder: List<ReadoutItemKey>,
        queueEnabledStates: Map<ReadoutItemKey, Boolean>,
        observedAtMs: Long,
        telemetryJson: () -> String,
    ) {
        events.forEach { event ->
            val text = readoutTextSafely(event)?.takeIf { it.isNotBlank() }
            val narrationOutcome =
                if (text == null) {
                    NarrationOutcome.SKIPPED
                } else {
                    val resolvedEvent = event.withResolvedText(text)
                    speakWithPriority(resolvedEvent, autoReadoutOrder, queueEnabledStates)
                }
            saveTelemetryLogSafely(
                createdAt = observedAtMs,
                readoutItemKey = event.readoutItemKey,
                narratedText = text.orEmpty(),
                narrationOutcome = narrationOutcome,
                telemetryJson = telemetryJson(),
            )
        }
    }

    /**
     * 読み上げの処理結果を返す。キュー追加・通常再生・割り込み再生・優先度負けによる読み上げなしの4種を
     * 区別し、テレメトリログの narrationOutcome として保存される。
     *
     * 割り込み再生かどうかの判定は [speakWithOutcome] に委譲する。
     */
    private fun speakWithPriority(
        event: SpeechEvent,
        autoReadoutOrder: List<ReadoutItemKey>,
        queueEnabledStates: Map<ReadoutItemKey, Boolean>,
    ): NarrationOutcome =
        speakWithOutcome(
            eventKey = event.readoutItemKey,
            currentKey = { ttsEngine.currentReadoutItemKey },
            autoReadoutOrder = autoReadoutOrder,
            queueEnabled = queueEnabledStates[event.readoutItemKey] == true,
            speak = { queue -> ttsEngine.speak(event, queue) },
            stop = { ttsEngine.stop() },
            outcomes =
                SpeakOutcomes(
                    skipped = NarrationOutcome.SKIPPED,
                    queued = NarrationOutcome.QUEUED,
                    interrupted = NarrationOutcome.INTERRUPTED,
                    spoken = NarrationOutcome.SPOKEN,
                ),
        )

    /** 文言解決に失敗したイベントだけをスキップし、後続の読み上げとログ保存を継続する。 */
    private suspend fun readoutTextSafely(event: SpeechEvent): String? = runCatchingNarratorError { readoutText(event) }

    private suspend fun saveTelemetryLogSafely(
        createdAt: Long,
        readoutItemKey: ReadoutItemKey,
        narratedText: String,
        narrationOutcome: NarrationOutcome,
        telemetryJson: String,
    ) {
        runCatchingTelemetryLogSave {
            saveTelemetryLog(
                createdAt = createdAt,
                narrationOutcome = narrationOutcome,
                simulator = Simulator.LmuWindows,
                readoutItemKey = readoutItemKey,
                narratedText = narratedText,
                telemetryJson = telemetryJson,
            )
        }
    }
}

private fun buildTelemetryLogJson(
    state: LmuWindowsNarratorState,
    previous: LmuWindowsVehicleApproachData?,
    current: LmuWindowsVehicleApproachData,
    settings: LmuWindowsNarratorReadoutSettings,
    observedAtMs: Long,
    finalState: LmuWindowsNarratorState,
): String =
    buildTelemetryLogJson(
        stateJson = TelemetryLogJson.encodeToString(state),
        previous =
            TelemetryLogJsonPreviousField(
                name = "previousVehicleApproach",
                json =
                    previous?.let {
                        TelemetryLogJson.encodeToString(it)
                    },
            ),
        current =
            TelemetryLogJsonCurrentField(
                name = "vehicleApproach",
                json = TelemetryLogJson.encodeToString(current),
            ),
        settingsJson = TelemetryLogJson.encodeToString(settings),
        observedAtMs = observedAtMs,
        finalStateJson = TelemetryLogJson.encodeToString(finalState),
    )

private fun buildTelemetryLogJson(
    state: LmuWindowsNarratorState,
    previous: LmuWindowsTelemetryData?,
    current: LmuWindowsTelemetryData,
    settings: LmuWindowsNarratorReadoutSettings,
    observedAtMs: Long,
    finalState: LmuWindowsNarratorState,
): String =
    buildTelemetryLogJson(
        stateJson = TelemetryLogJson.encodeToString(state),
        previous = TelemetryLogJsonPreviousField(name = "previousTelemetry", json = previous?.toJson()),
        current = TelemetryLogJsonCurrentField(name = "telemetry", json = current.toJson()),
        settingsJson = TelemetryLogJson.encodeToString(settings),
        observedAtMs = observedAtMs,
        finalStateJson = TelemetryLogJson.encodeToString(finalState),
    )

private fun LmuWindowsTelemetryData.toJson(): String =
    "{" +
        """"currentLapTimeMs":${timing.currentLapTimeMs},""" +
        """"lastLapTimeMs":${timing.lastLapTimeMs},""" +
        """"bestLapTimeMs":${timing.bestLapTimeMs},""" +
        """"currentLap":${timing.currentLap},""" +
        """"maxLaps":${timing.maxLaps}""" +
        "}"

private fun buildTelemetryLogJson(
    state: LmuWindowsNarratorState,
    previous: LmuWindowsVehicleDamageData?,
    current: LmuWindowsVehicleDamageData,
    settings: LmuWindowsNarratorReadoutSettings,
    observedAtMs: Long,
    finalState: LmuWindowsNarratorState,
): String =
    buildTelemetryLogJson(
        stateJson = TelemetryLogJson.encodeToString(state),
        previous =
            TelemetryLogJsonPreviousField(
                name = "previousVehicleDamage",
                json =
                    previous?.let {
                        TelemetryLogJson.encodeToString(it)
                    },
            ),
        current = TelemetryLogJsonCurrentField(name = "vehicleDamage", json = TelemetryLogJson.encodeToString(current)),
        settingsJson = TelemetryLogJson.encodeToString(settings),
        observedAtMs = observedAtMs,
        finalStateJson = TelemetryLogJson.encodeToString(finalState),
    )

private fun buildTelemetryLogJson(
    state: LmuWindowsNarratorState,
    previous: LmuWindowsTyreDetachedData?,
    current: LmuWindowsTyreDetachedData,
    settings: LmuWindowsNarratorReadoutSettings,
    observedAtMs: Long,
    finalState: LmuWindowsNarratorState,
): String =
    buildTelemetryLogJson(
        stateJson = TelemetryLogJson.encodeToString(state),
        previous =
            TelemetryLogJsonPreviousField(
                name = "previousTyreDetached",
                json =
                    previous?.let {
                        TelemetryLogJson.encodeToString(it)
                    },
            ),
        current = TelemetryLogJsonCurrentField(name = "tyreDetached", json = TelemetryLogJson.encodeToString(current)),
        settingsJson = TelemetryLogJson.encodeToString(settings),
        observedAtMs = observedAtMs,
        finalStateJson = TelemetryLogJson.encodeToString(finalState),
    )

/**
 * タイヤ摩耗判定入力（[LmuWindowsTyreWearData]）は判定ロジック（
 * [kurou.kodriver.domain.lmuwindows.usecase.DetermineLmuWindowsTyreReadoutUseCase.determineTyreWear]）と
 * 共有しているため、フィールドを手動で選ばず [TelemetryLogJson] でシリアライズしてそのまま記録する。
 * これにより判定に使う入力が増えても記録側の更新漏れが構造的に起こらない。
 */
private fun buildTelemetryLogJson(
    state: LmuWindowsNarratorState,
    previous: LmuWindowsTyreWearData?,
    current: LmuWindowsTyreWearData,
    settings: LmuWindowsNarratorReadoutSettings,
    observedAtMs: Long,
    finalState: LmuWindowsNarratorState,
): String =
    buildTelemetryLogJson(
        stateJson = TelemetryLogJson.encodeToString(state),
        previous =
            TelemetryLogJsonPreviousField(
                name = "previousTyreWear",
                json =
                    previous?.let {
                        TelemetryLogJson.encodeToString(it)
                    },
            ),
        current = TelemetryLogJsonCurrentField(name = "tyreWear", json = TelemetryLogJson.encodeToString(current)),
        settingsJson = TelemetryLogJson.encodeToString(settings),
        observedAtMs = observedAtMs,
        finalStateJson = TelemetryLogJson.encodeToString(finalState),
    )

/**
 * ブレーキ温度判定入力（[LmuWindowsBrakeTemperatureData]）は判定ロジック（
 * [kurou.kodriver.domain.lmuwindows.usecase.DetermineLmuWindowsBrakeReadoutUseCase.determineBrakeTemperatureOverheat]）と
 * 共有しているため、フィールドを手動で選ばず [TelemetryLogJson] でシリアライズしてそのまま記録する。
 * これにより判定に使う入力が増えても記録側の更新漏れが構造的に起こらない。
 */
private fun buildTelemetryLogJson(
    state: LmuWindowsNarratorState,
    previous: LmuWindowsBrakeTemperatureData?,
    current: LmuWindowsBrakeTemperatureData,
    settings: LmuWindowsNarratorReadoutSettings,
    observedAtMs: Long,
    finalState: LmuWindowsNarratorState,
): String =
    buildTelemetryLogJson(
        stateJson = TelemetryLogJson.encodeToString(state),
        previous =
            TelemetryLogJsonPreviousField(
                name = "previousBrakeTemperature",
                json =
                    previous?.let {
                        TelemetryLogJson.encodeToString(it)
                    },
            ),
        current =
            TelemetryLogJsonCurrentField(
                name = "brakeTemperature",
                json = TelemetryLogJson.encodeToString(current),
            ),
        settingsJson = TelemetryLogJson.encodeToString(settings),
        observedAtMs = observedAtMs,
        finalStateJson = TelemetryLogJson.encodeToString(finalState),
    )

/**
 * ブレーキ残量判定入力（[LmuWindowsBrakeWearRemainingData]）は判定ロジック（
 * [kurou.kodriver.domain.lmuwindows.usecase.DetermineLmuWindowsBrakeReadoutUseCase.determineBrakeWearLow]）と
 * 共有しているため、フィールドを手動で選ばず [TelemetryLogJson] でシリアライズしてそのまま記録する。
 */
private fun buildTelemetryLogJson(
    state: LmuWindowsNarratorState,
    previous: LmuWindowsBrakeWearRemainingData?,
    current: LmuWindowsBrakeWearRemainingData,
    settings: LmuWindowsNarratorReadoutSettings,
    observedAtMs: Long,
    finalState: LmuWindowsNarratorState,
): String =
    buildTelemetryLogJson(
        stateJson = TelemetryLogJson.encodeToString(state),
        previous =
            TelemetryLogJsonPreviousField(
                name = "previousBrakeWear",
                json =
                    previous?.let {
                        TelemetryLogJson.encodeToString(it)
                    },
            ),
        current =
            TelemetryLogJsonCurrentField(
                name = "brakeWear",
                json = TelemetryLogJson.encodeToString(current),
            ),
        settingsJson = TelemetryLogJson.encodeToString(settings),
        observedAtMs = observedAtMs,
        finalStateJson = TelemetryLogJson.encodeToString(finalState),
    )

/**
 * バーチャルエネルギー残量判定入力（[LmuWindowsVirtualEnergyData]）は判定ロジック（
 * [kurou.kodriver.domain.lmuwindows.usecase.DetermineLmuWindowsVirtualEnergyReadoutUseCase.determineRemainingVirtualEnergy]）と
 * 共有しているため、フィールドを手動で選ばず [TelemetryLogJson] でシリアライズしてそのまま記録する。
 * これにより判定に使う入力が増えても記録側の更新漏れが構造的に起こらない。
 */
private fun buildTelemetryLogJson(
    state: LmuWindowsNarratorState,
    previous: LmuWindowsVirtualEnergyData?,
    current: LmuWindowsVirtualEnergyData,
    settings: LmuWindowsNarratorReadoutSettings,
    observedAtMs: Long,
    finalState: LmuWindowsNarratorState,
): String =
    buildTelemetryLogJson(
        stateJson = TelemetryLogJson.encodeToString(state),
        previous =
            TelemetryLogJsonPreviousField(
                name = "previousRemainingVirtualEnergy",
                json =
                    previous?.let {
                        TelemetryLogJson.encodeToString(it)
                    },
            ),
        current =
            TelemetryLogJsonCurrentField(
                name = "remainingVirtualEnergy",
                json = TelemetryLogJson.encodeToString(current),
            ),
        settingsJson = TelemetryLogJson.encodeToString(settings),
        observedAtMs = observedAtMs,
        finalStateJson = TelemetryLogJson.encodeToString(finalState),
    )

private fun buildTelemetryLogJson(
    state: LmuWindowsNarratorState,
    previous: LmuWindowsRemainingFuelData?,
    current: LmuWindowsRemainingFuelData,
    settings: LmuWindowsNarratorReadoutSettings,
    observedAtMs: Long,
    finalState: LmuWindowsNarratorState,
): String =
    buildTelemetryLogJson(
        stateJson = TelemetryLogJson.encodeToString(state),
        previous =
            TelemetryLogJsonPreviousField(
                name = "previousRemainingFuel",
                json = previous?.let { TelemetryLogJson.encodeToString(it) },
            ),
        current =
            TelemetryLogJsonCurrentField(
                name = "remainingFuel",
                json = TelemetryLogJson.encodeToString(current),
            ),
        settingsJson = TelemetryLogJson.encodeToString(settings),
        observedAtMs = observedAtMs,
        finalStateJson = TelemetryLogJson.encodeToString(finalState),
    )

private fun buildPitTimingTelemetryLogJson(
    state: LmuWindowsNarratorState,
    telemetry: LmuWindowsTelemetryData,
    virtualEnergy: LmuWindowsVirtualEnergyData,
    tyreWear: LmuWindowsTyreWearData,
    settings: LmuWindowsNarratorReadoutSettings,
    observedAtMs: Long,
    finalState: LmuWindowsNarratorState,
): String =
    "{" +
        """"state":${TelemetryLogJson.encodeToString(state)},""" +
        """"telemetry":${telemetry.toJson()},""" +
        """"virtualEnergy":${TelemetryLogJson.encodeToString(virtualEnergy)},""" +
        """"tyreWear":${TelemetryLogJson.encodeToString(tyreWear)},""" +
        """"settings":${TelemetryLogJson.encodeToString(settings)},""" +
        """"observedAtMs":$observedAtMs,""" +
        """"finalState":${TelemetryLogJson.encodeToString(finalState)}""" +
        "}"

/**
 * フラッグ判定入力（[LmuWindowsRaceFlagsData]）は判定ロジック（
 * [kurou.kodriver.domain.lmuwindows.usecase.DetermineLmuWindowsRaceFlagReadoutUseCase.determineRaceFlags]）と
 * 共有しているため、フィールドを手動で選ばず [TelemetryLogJson] でシリアライズしてそのまま記録する。
 * これにより判定に使う入力が増えても記録側の更新漏れが構造的に起こらない。
 */
private fun buildTelemetryLogJson(
    state: LmuWindowsNarratorState,
    previous: LmuWindowsRaceFlagsData?,
    current: LmuWindowsRaceFlagsData,
    settings: LmuWindowsNarratorReadoutSettings,
    observedAtMs: Long,
    finalState: LmuWindowsNarratorState,
): String =
    buildTelemetryLogJson(
        stateJson = TelemetryLogJson.encodeToString(state),
        previous =
            TelemetryLogJsonPreviousField(
                name = "previousRaceFlags",
                json =
                    previous?.let {
                        TelemetryLogJson.encodeToString(it)
                    },
            ),
        current = TelemetryLogJsonCurrentField(name = "raceFlags", json = TelemetryLogJson.encodeToString(current)),
        settingsJson = TelemetryLogJson.encodeToString(settings),
        observedAtMs = observedAtMs,
        finalStateJson = TelemetryLogJson.encodeToString(finalState),
    )

/**
 * タイヤ温度読み上げの入力は [TyreTemperatureReadoutInput] で判定ロジック（
 * [kurou.kodriver.domain.lmuwindows.usecase.DetermineLmuWindowsTyreReadoutUseCase.determineTyreTemperatureOverheat] /
 * [kurou.kodriver.domain.lmuwindows.usecase.DetermineLmuWindowsTyreReadoutUseCase.determineTyreTemperatureLow]）と
 * 共有しているため、フィールドを手動で選ばず [TelemetryLogJson] でシリアライズしてそのまま記録する。
 * これにより判定に使う入力が増えても記録側の更新漏れが構造的に起こらない。
 */
private fun buildTelemetryLogJson(
    state: LmuWindowsNarratorState,
    input: TyreTemperatureReadoutInput,
    settings: LmuWindowsNarratorReadoutSettings,
    observedAtMs: Long,
    overheatState: LmuWindowsNarratorState,
    finalState: LmuWindowsNarratorState,
): String =
    "{" +
        """"state":${TelemetryLogJson.encodeToString(state)},""" +
        """"input":${TelemetryLogJson.encodeToString(input)},""" +
        """"settings":${TelemetryLogJson.encodeToString(settings)},""" +
        """"observedAtMs":$observedAtMs,""" +
        """"overheatState":${TelemetryLogJson.encodeToString(overheatState)},""" +
        """"finalState":${TelemetryLogJson.encodeToString(finalState)}""" +
        "}"
