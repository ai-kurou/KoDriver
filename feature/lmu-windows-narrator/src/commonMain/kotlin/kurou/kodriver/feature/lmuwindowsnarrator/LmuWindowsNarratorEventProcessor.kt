package kurou.kodriver.feature.lmuwindowsnarrator

import kotlinx.coroutines.CancellationException
import kotlinx.serialization.encodeToString
import kurou.kodriver.core.model.LmuWindowsRaceFlagsData
import kurou.kodriver.core.model.LmuWindowsTelemetryData
import kurou.kodriver.core.model.LmuWindowsTyreWearData
import kurou.kodriver.core.model.LmuWindowsVehicleApproachData
import kurou.kodriver.core.model.LmuWindowsVehicleDamageData
import kurou.kodriver.core.model.LmuWindowsVirtualEnergyData
import kurou.kodriver.core.model.ReadoutItemKey
import kurou.kodriver.core.model.Simulator
import kurou.kodriver.core.narrator.TelemetryLogJson
import kurou.kodriver.core.narrator.speakWithPriority
import kurou.kodriver.domain.engine.SpeechEvent
import kurou.kodriver.domain.engine.TextToSpeechEngine
import kurou.kodriver.domain.usecase.LmuWindowsNarratorReadoutSettings
import kurou.kodriver.domain.usecase.LmuWindowsNarratorState
import kurou.kodriver.domain.usecase.SaveTelemetryLogUseCase
import kurou.kodriver.domain.usecase.TyreTemperatureReadoutInput

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
) {
    private var previousTelemetry: LmuWindowsTelemetryData? = null
    private var previousVehicleApproach: LmuWindowsVehicleApproachData? = null
    private var previousVehicleDamage: LmuWindowsVehicleDamageData? = null
    private var previousRaceFlags: LmuWindowsRaceFlagsData? = null
    private var previousTyreWear: LmuWindowsTyreWearData? = null
    private var previousRemainingVirtualEnergy: LmuWindowsVirtualEnergyData? = null

    suspend fun processTelemetry(
        telemetry: LmuWindowsTelemetryData,
        events: List<SpeechEvent>,
        readoutOrder: List<ReadoutItemKey>,
        queueEnabledStates: Map<ReadoutItemKey, Boolean>,
        observedAtMs: Long,
        logContext: LmuWindowsTelemetryLogContext,
    ) {
        val previous = previousTelemetry
        events.forEach { event ->
            if (speakWithPriority(event, readoutOrder, queueEnabledStates)) {
                saveTelemetryLogSafely(
                    createdAt = observedAtMs,
                    readoutItemKey = event.readoutItemKey,
                    telemetryJson =
                        buildTelemetryLogJson(
                            state = logContext.state,
                            previous = previous,
                            current = telemetry,
                            settings = logContext.settings,
                            observedAtMs = observedAtMs,
                            finalState = logContext.finalState,
                        ),
                )
            }
        }
        previousTelemetry = telemetry
    }

    suspend fun processVehicleApproach(
        vehicleApproach: LmuWindowsVehicleApproachData,
        events: List<SpeechEvent>,
        readoutOrder: List<ReadoutItemKey>,
        queueEnabledStates: Map<ReadoutItemKey, Boolean>,
        observedAtMs: Long,
        logContext: LmuWindowsTelemetryLogContext,
    ) {
        val previous = previousVehicleApproach
        events.forEach { event ->
            if (speakWithPriority(event, readoutOrder, queueEnabledStates)) {
                saveTelemetryLogSafely(
                    createdAt = observedAtMs,
                    readoutItemKey = event.readoutItemKey,
                    telemetryJson =
                        buildTelemetryLogJson(
                            state = logContext.state,
                            previous = previous,
                            current = vehicleApproach,
                            settings = logContext.settings,
                            observedAtMs = observedAtMs,
                            finalState = logContext.finalState,
                        ),
                )
            }
        }
        previousVehicleApproach = vehicleApproach
    }

    suspend fun processVehicleDamage(
        vehicleDamage: LmuWindowsVehicleDamageData,
        events: List<SpeechEvent>,
        readoutOrder: List<ReadoutItemKey>,
        queueEnabledStates: Map<ReadoutItemKey, Boolean>,
        observedAtMs: Long,
        logContext: LmuWindowsTelemetryLogContext,
    ) {
        val previous = previousVehicleDamage
        events.forEach { event ->
            if (speakWithPriority(event, readoutOrder, queueEnabledStates)) {
                saveTelemetryLogSafely(
                    createdAt = observedAtMs,
                    readoutItemKey = event.readoutItemKey,
                    telemetryJson =
                        buildTelemetryLogJson(
                            state = logContext.state,
                            previous = previous,
                            current = vehicleDamage,
                            settings = logContext.settings,
                            observedAtMs = observedAtMs,
                            finalState = logContext.finalState,
                        ),
                )
            }
        }
        previousVehicleDamage = vehicleDamage
    }

    suspend fun processRaceFlags(
        raceFlags: LmuWindowsRaceFlagsData,
        events: List<SpeechEvent>,
        readoutOrder: List<ReadoutItemKey>,
        queueEnabledStates: Map<ReadoutItemKey, Boolean>,
        observedAtMs: Long,
        logContext: LmuWindowsTelemetryLogContext,
    ) {
        val previous = previousRaceFlags
        events.forEach { event ->
            if (speakWithPriority(event, readoutOrder, queueEnabledStates)) {
                saveTelemetryLogSafely(
                    createdAt = observedAtMs,
                    readoutItemKey = event.readoutItemKey,
                    telemetryJson =
                        buildTelemetryLogJson(
                            state = logContext.state,
                            previous = previous,
                            current = raceFlags,
                            settings = logContext.settings,
                            observedAtMs = observedAtMs,
                            finalState = logContext.finalState,
                        ),
                )
            }
        }
        previousRaceFlags = raceFlags
    }

    suspend fun processTyreWear(
        tyreWear: LmuWindowsTyreWearData,
        events: List<SpeechEvent>,
        readoutOrder: List<ReadoutItemKey>,
        queueEnabledStates: Map<ReadoutItemKey, Boolean>,
        observedAtMs: Long,
        logContext: LmuWindowsTelemetryLogContext,
    ) {
        val previous = previousTyreWear
        events.forEach { event ->
            if (speakWithPriority(event, readoutOrder, queueEnabledStates)) {
                saveTelemetryLogSafely(
                    createdAt = observedAtMs,
                    readoutItemKey = event.readoutItemKey,
                    telemetryJson =
                        buildTelemetryLogJson(
                            state = logContext.state,
                            previous = previous,
                            current = tyreWear,
                            settings = logContext.settings,
                            observedAtMs = observedAtMs,
                            finalState = logContext.finalState,
                        ),
                )
            }
        }
        previousTyreWear = tyreWear
    }

    suspend fun processRemainingVirtualEnergy(
        remainingVirtualEnergy: LmuWindowsVirtualEnergyData,
        events: List<SpeechEvent>,
        readoutOrder: List<ReadoutItemKey>,
        queueEnabledStates: Map<ReadoutItemKey, Boolean>,
        observedAtMs: Long,
        logContext: LmuWindowsTelemetryLogContext,
    ) {
        val previous = previousRemainingVirtualEnergy
        events.forEach { event ->
            if (speakWithPriority(event, readoutOrder, queueEnabledStates)) {
                saveTelemetryLogSafely(
                    createdAt = observedAtMs,
                    readoutItemKey = event.readoutItemKey,
                    telemetryJson =
                        buildTelemetryLogJson(
                            state = logContext.state,
                            previous = previous,
                            current = remainingVirtualEnergy,
                            settings = logContext.settings,
                            observedAtMs = observedAtMs,
                            finalState = logContext.finalState,
                        ),
                )
            }
        }
        previousRemainingVirtualEnergy = remainingVirtualEnergy
    }

    suspend fun processTyreTemperature(
        input: TyreTemperatureReadoutInput,
        events: List<SpeechEvent>,
        readoutOrder: List<ReadoutItemKey>,
        queueEnabledStates: Map<ReadoutItemKey, Boolean>,
        observedAtMs: Long,
        logContext: LmuWindowsTyreTemperatureLogContext,
    ) {
        events.forEach { event ->
            if (speakWithPriority(event, readoutOrder, queueEnabledStates)) {
                saveTelemetryLogSafely(
                    createdAt = observedAtMs,
                    readoutItemKey = event.readoutItemKey,
                    telemetryJson =
                        buildTelemetryLogJson(
                            state = logContext.state,
                            input = input,
                            settings = logContext.settings,
                            observedAtMs = observedAtMs,
                            overheatState = logContext.overheatState,
                            finalState = logContext.finalState,
                        ),
                )
            }
        }
    }

    suspend fun processPitTiming(
        snapshot: LmuWindowsPitTimingSnapshot,
        events: List<SpeechEvent>,
        readoutOrder: List<ReadoutItemKey>,
        queueEnabledStates: Map<ReadoutItemKey, Boolean>,
        observedAtMs: Long,
        logContext: LmuWindowsPitTimingLogContext,
    ) {
        events.forEach { event ->
            if (speakWithPriority(event, readoutOrder, queueEnabledStates)) {
                saveTelemetryLogSafely(
                    createdAt = observedAtMs,
                    readoutItemKey = event.readoutItemKey,
                    telemetryJson =
                        buildPitTimingTelemetryLogJson(
                            state = logContext.state,
                            telemetry = snapshot.telemetry,
                            virtualEnergy = snapshot.virtualEnergy,
                            tyreWear = snapshot.tyreWear,
                            settings = logContext.settings,
                            observedAtMs = observedAtMs,
                            finalState = logContext.finalState,
                        ),
                )
            }
        }
    }

    private fun speakWithPriority(
        event: SpeechEvent,
        readoutOrder: List<ReadoutItemKey>,
        queueEnabledStates: Map<ReadoutItemKey, Boolean>,
    ): Boolean =
        speakWithPriority(
            eventKey = event.readoutItemKey,
            currentKey = { ttsEngine.currentReadoutItemKey },
            readoutOrder = readoutOrder,
            queueEnabled = queueEnabledStates[event.readoutItemKey] == true,
            speak = { queue -> ttsEngine.speak(event, queue) },
            stop = { ttsEngine.stop() },
        )

    private suspend fun saveTelemetryLogSafely(
        createdAt: Long,
        readoutItemKey: ReadoutItemKey,
        telemetryJson: String,
    ) {
        try {
            saveTelemetryLog(
                createdAt = createdAt,
                simulator = Simulator.LmuWindows,
                readoutItemKey = readoutItemKey,
                telemetryJson = telemetryJson,
            )
        } catch (e: CancellationException) {
            throw e
        } catch (_: Exception) {
            // ログ保存は読み上げの補助機能のため、保存失敗で以後の読み上げを止めない。
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
    "{" +
        """"state":${TelemetryLogJson.encodeToString(state)},""" +
        """"previousVehicleApproach":${previous?.let { TelemetryLogJson.encodeToString(it) } ?: "null"},""" +
        """"vehicleApproach":${TelemetryLogJson.encodeToString(current)},""" +
        """"settings":${TelemetryLogJson.encodeToString(settings)},""" +
        """"observedAtMs":$observedAtMs,""" +
        """"finalState":${TelemetryLogJson.encodeToString(finalState)}""" +
        "}"

private fun buildTelemetryLogJson(
    state: LmuWindowsNarratorState,
    previous: LmuWindowsTelemetryData?,
    current: LmuWindowsTelemetryData,
    settings: LmuWindowsNarratorReadoutSettings,
    observedAtMs: Long,
    finalState: LmuWindowsNarratorState,
): String =
    "{" +
        """"state":${TelemetryLogJson.encodeToString(state)},""" +
        """"previousTelemetry":${previous?.toJson() ?: "null"},""" +
        """"telemetry":${current.toJson()},""" +
        """"settings":${TelemetryLogJson.encodeToString(settings)},""" +
        """"observedAtMs":$observedAtMs,""" +
        """"finalState":${TelemetryLogJson.encodeToString(finalState)}""" +
        "}"

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
    "{" +
        """"state":${TelemetryLogJson.encodeToString(state)},""" +
        """"previousVehicleDamage":${previous?.let { TelemetryLogJson.encodeToString(it) } ?: "null"},""" +
        """"vehicleDamage":${TelemetryLogJson.encodeToString(current)},""" +
        """"settings":${TelemetryLogJson.encodeToString(settings)},""" +
        """"observedAtMs":$observedAtMs,""" +
        """"finalState":${TelemetryLogJson.encodeToString(finalState)}""" +
        "}"

/**
 * タイヤ摩耗判定入力（[LmuWindowsTyreWearData]）は判定ロジック（
 * [kurou.kodriver.domain.usecase.DetermineLmuWindowsNarratorReadoutUseCase.determineTyreWear]）と
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
    "{" +
        """"state":${TelemetryLogJson.encodeToString(state)},""" +
        """"previousTyreWear":${previous?.let { TelemetryLogJson.encodeToString(it) } ?: "null"},""" +
        """"tyreWear":${TelemetryLogJson.encodeToString(current)},""" +
        """"settings":${TelemetryLogJson.encodeToString(settings)},""" +
        """"observedAtMs":$observedAtMs,""" +
        """"finalState":${TelemetryLogJson.encodeToString(finalState)}""" +
        "}"

/**
 * バーチャルエナジー残量判定入力（[LmuWindowsVirtualEnergyData]）は判定ロジック（
 * [kurou.kodriver.domain.usecase.DetermineLmuWindowsNarratorReadoutUseCase.determineRemainingVirtualEnergy]）と
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
    "{" +
        """"state":${TelemetryLogJson.encodeToString(state)},""" +
        """"previousRemainingVirtualEnergy":${previous?.let { TelemetryLogJson.encodeToString(it) } ?: "null"},""" +
        """"remainingVirtualEnergy":${TelemetryLogJson.encodeToString(current)},""" +
        """"settings":${TelemetryLogJson.encodeToString(settings)},""" +
        """"observedAtMs":$observedAtMs,""" +
        """"finalState":${TelemetryLogJson.encodeToString(finalState)}""" +
        "}"

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
 * フラグ判定入力（[LmuWindowsRaceFlagsData]）は判定ロジック（
 * [kurou.kodriver.domain.usecase.DetermineLmuWindowsNarratorReadoutUseCase.determineRaceFlags]）と
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
    "{" +
        """"state":${TelemetryLogJson.encodeToString(state)},""" +
        """"previousRaceFlags":${previous?.let { TelemetryLogJson.encodeToString(it) } ?: "null"},""" +
        """"raceFlags":${TelemetryLogJson.encodeToString(current)},""" +
        """"settings":${TelemetryLogJson.encodeToString(settings)},""" +
        """"observedAtMs":$observedAtMs,""" +
        """"finalState":${TelemetryLogJson.encodeToString(finalState)}""" +
        "}"

/**
 * タイヤ温度読み上げの入力は [TyreTemperatureReadoutInput] で判定ロジック（
 * [kurou.kodriver.domain.usecase.DetermineLmuWindowsNarratorReadoutUseCase.determineTyreTemperatureOverheat] /
 * [kurou.kodriver.domain.usecase.DetermineLmuWindowsNarratorReadoutUseCase.determineTyreTemperatureLow]）と
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
