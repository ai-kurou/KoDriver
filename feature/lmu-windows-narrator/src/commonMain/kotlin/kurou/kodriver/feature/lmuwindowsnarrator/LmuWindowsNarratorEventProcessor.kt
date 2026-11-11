package kurou.kodriver.feature.lmuwindowsnarrator

import kotlinx.serialization.encodeToString
import kurou.kodriver.core.narrator.SpeakOutcomes
import kurou.kodriver.core.narrator.TelemetryLogJson
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
import kurou.kodriver.domain.model.NarrationSource
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
    val virtualEnergy: LmuWindowsVirtualEnergyData?,
    val tyreWear: LmuWindowsTyreWearData?,
    val fuel: LmuWindowsRemainingFuelData? = null,
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
    ): Boolean {
        val previous = previousTyreWear
        val dropped =
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
        return dropped
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
    ): Boolean {
        val previous = previousBrakeWear
        val dropped =
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
        return dropped
    }

    suspend fun processRemainingVirtualEnergy(
        remainingVirtualEnergy: LmuWindowsVirtualEnergyData?,
        events: List<ReadoutTextEvent>,
        autoReadoutOrder: List<ReadoutItemKey>,
        queueEnabledStates: Map<ReadoutItemKey, Boolean>,
        observedAtMs: Long,
        logContext: LmuWindowsTelemetryLogContext,
    ): Boolean {
        val previous = previousRemainingVirtualEnergy
        val dropped =
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
        return dropped
    }

    suspend fun processRemainingFuel(
        fuel: LmuWindowsRemainingFuelData,
        events: List<ReadoutTextEvent>,
        autoReadoutOrder: List<ReadoutItemKey>,
        queueEnabledStates: Map<ReadoutItemKey, Boolean>,
        observedAtMs: Long,
        logContext: LmuWindowsTelemetryLogContext,
    ): Boolean {
        val previous = previousRemainingFuel
        val dropped =
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
        return dropped
    }

    suspend fun processManualReadout(
        events: List<ReadoutTextEvent>,
        observedAtMs: Long,
        logContext: LmuWindowsTelemetryLogContext,
        playStartSound: Boolean = true,
    ): Boolean {
        var spoken = false
        processEvents(
            events,
            emptyList(),
            emptyMap(),
            observedAtMs,
            NarrationSource.MANUAL,
            playStartSound,
            onOutcome = { if (it != NarrationOutcome.SKIPPED) spoken = true },
        ) {
            """{"state":${TelemetryLogJson.encodeToString(logContext.state)},""" +
                """"settings":${TelemetryLogJson.encodeToString(logContext.settings)},""" +
                """"observedAtMs":$observedAtMs}"""
        }
        return spoken
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
                fuel = snapshot.fuel,
                settings = logContext.settings,
                observedAtMs = observedAtMs,
                finalState = logContext.finalState,
            )
        }
    }

    /**
     * 本文の確定・優先度つき再生・ログ保存を全LMUイベントで共有する。
     *
     * 本文を解決できたのに優先度判定で読み上げられなかったイベントが1件でもあれば true を返す
     * （本文が空でスキップされた場合は含めない）。一度きりの警告フラグを呼び出し側が戻して再試行するために使う。
     */
    @Suppress("LongParameterList")
    private suspend fun <E : ReadoutTextEvent> processEvents(
        events: List<E>,
        autoReadoutOrder: List<ReadoutItemKey>,
        queueEnabledStates: Map<ReadoutItemKey, Boolean>,
        observedAtMs: Long,
        narrationSource: NarrationSource = NarrationSource.AUTO,
        playStartSound: Boolean = true,
        onOutcome: (NarrationOutcome) -> Unit = {},
        telemetryJson: () -> String,
    ): Boolean {
        var droppedByPriority = false
        events.forEach { event ->
            val text = readoutTextSafely(event)?.takeIf { it.isNotBlank() }
            val narrationOutcome =
                if (text == null) {
                    NarrationOutcome.SKIPPED
                } else {
                    val resolvedEvent = event.withResolvedText(text)
                    speakWithPriority(
                        resolvedEvent,
                        autoReadoutOrder,
                        queueEnabledStates,
                        isManual = narrationSource == NarrationSource.MANUAL,
                        playStartSound = playStartSound,
                    )
                }
            saveTelemetryLogSafely(
                createdAt = observedAtMs,
                readoutItemKey = event.readoutItemKey,
                narratedText = text.orEmpty(),
                narrationOutcome = narrationOutcome,
                narrationSource = narrationSource,
                telemetryJson = telemetryJson(),
            )
            onOutcome(narrationOutcome)
            if (text != null && narrationOutcome == NarrationOutcome.SKIPPED) droppedByPriority = true
        }
        return droppedByPriority
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
        isManual: Boolean = false,
        playStartSound: Boolean = true,
    ): NarrationOutcome =
        speakWithOutcome(
            eventKey = event.readoutItemKey,
            currentKey = { ttsEngine.currentReadoutItemKey },
            autoReadoutOrder = autoReadoutOrder,
            queueEnabled = queueEnabledStates[event.readoutItemKey] == true,
            speak = { queue -> ttsEngine.speak(event, queue, isManual, playStartSound) },
            stop = { ttsEngine.stop() },
            outcomes =
                SpeakOutcomes(
                    skipped = NarrationOutcome.SKIPPED,
                    queued = NarrationOutcome.QUEUED,
                    interrupted = NarrationOutcome.INTERRUPTED,
                    spoken = NarrationOutcome.SPOKEN,
                ),
            isManual = isManual,
            currentIsManual = { ttsEngine.currentIsManual },
        )

    /** 文言解決に失敗したイベントだけをスキップし、後続の読み上げとログ保存を継続する。 */
    private suspend fun readoutTextSafely(event: SpeechEvent): String? = runCatchingNarratorError { readoutText(event) }

    private suspend fun saveTelemetryLogSafely(
        createdAt: Long,
        readoutItemKey: ReadoutItemKey,
        narratedText: String,
        narrationOutcome: NarrationOutcome,
        narrationSource: NarrationSource,
        telemetryJson: String,
    ) {
        runCatchingTelemetryLogSave {
            saveTelemetryLog(
                createdAt = createdAt,
                narrationOutcome = narrationOutcome,
                narrationSource = narrationSource,
                simulator = Simulator.LmuWindows,
                readoutItemKey = readoutItemKey,
                narratedText = narratedText,
                telemetryJson = telemetryJson,
            )
        }
    }
}
