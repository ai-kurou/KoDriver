package kurou.kodriver.feature.lmuwindowsnarrator

import kotlinx.coroutines.CancellationException
import kurou.kodriver.domain.engine.SpeechEvent
import kurou.kodriver.domain.engine.TextToSpeechEngine
import kurou.kodriver.domain.model.LmuWindowsRaceFlagsData
import kurou.kodriver.domain.model.LmuWindowsTelemetryData
import kurou.kodriver.domain.model.LmuWindowsTyreCarcassTemperatureData
import kurou.kodriver.domain.model.LmuWindowsTyreWearData
import kurou.kodriver.domain.model.LmuWindowsVehicleApproachData
import kurou.kodriver.domain.model.LmuWindowsVehicleDamageData
import kurou.kodriver.domain.model.LmuWindowsVirtualEnergyData
import kurou.kodriver.domain.model.ReadoutItemKey
import kurou.kodriver.domain.model.Simulator
import kurou.kodriver.domain.usecase.LmuWindowsNarratorReadoutSettings
import kurou.kodriver.domain.usecase.LmuWindowsNarratorState
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
        tyreCarcassTemperature: LmuWindowsTyreCarcassTemperatureData,
        raceFlags: LmuWindowsRaceFlagsData,
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
                        tyreCarcassTemperature = tyreCarcassTemperature,
                        raceFlags = raceFlags,
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
    ): Boolean {
        if (queueEnabledStates[event.readoutItemKey] == true) {
            ttsEngine.speak(event, queue = true)
            return true
        }
        val currentKey = ttsEngine.currentReadoutItemKey
        if (currentKey != null) {
            val currentIndex = readoutOrder.indexOf(currentKey).takeIf { it != -1 } ?: Int.MAX_VALUE
            val newIndex = readoutOrder.indexOf(event.readoutItemKey).takeIf { it != -1 } ?: Int.MAX_VALUE
            if (newIndex >= currentIndex) return false
            ttsEngine.stop()
        }
        ttsEngine.speak(event)
        return true
    }

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
        """"state":${state.toJsonString()},""" +
        """"previousVehicleApproach":${previous?.toJson() ?: "null"},""" +
        """"vehicleApproach":${current.toJson()},""" +
        """"settings":${settings.toJsonString()},""" +
        """"observedAtMs":$observedAtMs,""" +
        """"finalState":${finalState.toJsonString()}""" +
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
        """"state":${state.toJsonString()},""" +
        """"previousTelemetry":${previous?.toJson() ?: "null"},""" +
        """"telemetry":${current.toJson()},""" +
        """"settings":${settings.toJsonString()},""" +
        """"observedAtMs":$observedAtMs,""" +
        """"finalState":${finalState.toJsonString()}""" +
        "}"

private fun LmuWindowsNarratorReadoutSettings.toJsonString(): String =
    """{"raw":${toString().toJsonStringLiteral()}}"""

private fun LmuWindowsNarratorState.toJsonString(): String =
    """{"raw":${toString().toJsonStringLiteral()}}"""

private fun LmuWindowsTelemetryData.toJson(): String =
    "{" +
        """"currentLapTimeMs":${timing.currentLapTimeMs},""" +
        """"lastLapTimeMs":${timing.lastLapTimeMs},""" +
        """"bestLapTimeMs":${timing.bestLapTimeMs},""" +
        """"currentLap":${timing.currentLap},""" +
        """"maxLaps":${timing.maxLaps}""" +
        "}"

private fun LmuWindowsVehicleApproachData.toJson(): String =
    "{" +
        """"sideBySideLeftVehicleIds":${sideBySideLeftVehicleIds.sorted()},""" +
        """"sideBySideRightVehicleIds":${sideBySideRightVehicleIds.sorted()},""" +
        """"lateralDistanceLeftMeters":$lateralDistanceLeftMeters,""" +
        """"lateralDistanceRightMeters":$lateralDistanceRightMeters""" +
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
        """"state":${state.toJsonString()},""" +
        """"previousVehicleDamage":${previous?.toJson() ?: "null"},""" +
        """"vehicleDamage":${current.toJson()},""" +
        """"settings":${settings.toJsonString()},""" +
        """"observedAtMs":$observedAtMs,""" +
        """"finalState":${finalState.toJsonString()}""" +
        "}"

private fun buildTelemetryLogJson(
    state: LmuWindowsNarratorState,
    previous: LmuWindowsTyreWearData?,
    current: LmuWindowsTyreWearData,
    settings: LmuWindowsNarratorReadoutSettings,
    observedAtMs: Long,
    finalState: LmuWindowsNarratorState,
): String =
    "{" +
        """"state":${state.toJsonString()},""" +
        """"previousTyreWear":${previous?.toJson() ?: "null"},""" +
        """"tyreWear":${current.toJson()},""" +
        """"settings":${settings.toJsonString()},""" +
        """"observedAtMs":$observedAtMs,""" +
        """"finalState":${finalState.toJsonString()}""" +
        "}"

private fun LmuWindowsTyreWearData.toJson(): String =
    """{"wheels":{${wheels.entries.joinToString(",") { (k, v) -> """"$k":$v""" }}}}"""

private fun buildTelemetryLogJson(
    state: LmuWindowsNarratorState,
    previous: LmuWindowsVirtualEnergyData?,
    current: LmuWindowsVirtualEnergyData,
    settings: LmuWindowsNarratorReadoutSettings,
    observedAtMs: Long,
    finalState: LmuWindowsNarratorState,
): String =
    "{" +
        """"state":${state.toJsonString()},""" +
        """"previousRemainingVirtualEnergy":${previous?.toJson() ?: "null"},""" +
        """"remainingVirtualEnergy":${current.toJson()},""" +
        """"settings":${settings.toJsonString()},""" +
        """"observedAtMs":$observedAtMs,""" +
        """"finalState":${finalState.toJsonString()}""" +
        "}"

private fun LmuWindowsVirtualEnergyData.toJson(): String =
    "{" +
        """"remainingRatio":$remainingRatio,""" +
        """"session":$session""" +
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
        """"state":${state.toJsonString()},""" +
        """"telemetry":${telemetry.toJson()},""" +
        """"virtualEnergy":${virtualEnergy.toJson()},""" +
        """"tyreWear":${tyreWear.toJson()},""" +
        """"settings":${settings.toJsonString()},""" +
        """"observedAtMs":$observedAtMs,""" +
        """"finalState":${finalState.toJsonString()}""" +
        "}"

private fun LmuWindowsVehicleDamageData.toJson(): String =
    "{" +
        """"overheating":$overheating,""" +
        """"partDetached":$partDetached,""" +
        """"lastImpactMagnitude":$lastImpactMagnitude""" +
        "}"

private fun buildTelemetryLogJson(
    state: LmuWindowsNarratorState,
    previous: LmuWindowsRaceFlagsData?,
    current: LmuWindowsRaceFlagsData,
    settings: LmuWindowsNarratorReadoutSettings,
    observedAtMs: Long,
    finalState: LmuWindowsNarratorState,
): String =
    "{" +
        """"state":${state.toJsonString()},""" +
        """"previousRaceFlags":${previous?.toJson() ?: "null"},""" +
        """"raceFlags":${current.toJson()},""" +
        """"settings":${settings.toJsonString()},""" +
        """"observedAtMs":$observedAtMs,""" +
        """"finalState":${finalState.toJsonString()}""" +
        "}"

private fun LmuWindowsRaceFlagsData.toJson(): String =
    "{" +
        """"gamePhase":"$gamePhase",""" +
        """"yellowFlagState":"$yellowFlagState",""" +
        """"sectorFlags":[${sectorFlags.joinToString(",") { """"$it"""" }}],""" +
        """"startLight":$startLight,""" +
        """"numRedLights":$numRedLights,""" +
        """"playerFlag":"$playerFlag",""" +
        """"playerUnderYellow":$playerUnderYellow,""" +
        """"playerCountLapFlag":"$playerCountLapFlag"""" +
        "}"

private fun buildTelemetryLogJson(
    state: LmuWindowsNarratorState,
    tyreCarcassTemperature: LmuWindowsTyreCarcassTemperatureData,
    raceFlags: LmuWindowsRaceFlagsData,
    settings: LmuWindowsNarratorReadoutSettings,
    observedAtMs: Long,
    overheatState: LmuWindowsNarratorState,
    finalState: LmuWindowsNarratorState,
): String =
    "{" +
        """"state":${state.toJsonString()},""" +
        """"tyreCarcassTemperature":${tyreCarcassTemperature.toJson()},""" +
        """"raceFlags":${raceFlags.toJson()},""" +
        """"settings":${settings.toJsonString()},""" +
        """"observedAtMs":$observedAtMs,""" +
        """"overheatState":${overheatState.toJsonString()},""" +
        """"finalState":${finalState.toJsonString()}""" +
        "}"

private fun LmuWindowsTyreCarcassTemperatureData.toJson(): String =
    """{"wheels":{${wheels.entries.joinToString(",") { (k, v) -> """"$k":$v""" }}}}"""

private fun String.toJsonStringLiteral(): String =
    buildString {
        append('"')
        this@toJsonStringLiteral.forEach { char ->
            when (char) {
                '\\' -> append("\\\\")
                '"' -> append("\\\"")
                '\n' -> append("\\n")
                '\r' -> append("\\r")
                '\t' -> append("\\t")
                else -> append(char)
            }
        }
        append('"')
    }
