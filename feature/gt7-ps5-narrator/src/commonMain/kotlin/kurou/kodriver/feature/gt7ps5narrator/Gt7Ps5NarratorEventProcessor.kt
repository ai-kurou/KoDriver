package kurou.kodriver.feature.gt7ps5narrator

import kurou.kodriver.domain.engine.SpeechEvent
import kurou.kodriver.domain.engine.TextToSpeechEngine
import kurou.kodriver.domain.model.Gt7Ps5TelemetryData
import kurou.kodriver.domain.model.MyBestLapVoiceType
import kurou.kodriver.domain.model.ReadoutItemKey
import kurou.kodriver.domain.model.Simulator
import kurou.kodriver.domain.usecase.Gt7Ps5NarratorReadoutSettings
import kurou.kodriver.domain.usecase.Gt7Ps5NarratorState
import kurou.kodriver.domain.usecase.SaveTelemetryLogUseCase

internal data class Gt7Ps5TelemetryLogContext(
    val state: Gt7Ps5NarratorState,
    val settings: Gt7Ps5NarratorReadoutSettings,
    val finalState: Gt7Ps5NarratorState,
)

internal class Gt7Ps5NarratorEventProcessor(
    private val ttsEngine: TextToSpeechEngine,
    private val saveTelemetryLog: SaveTelemetryLogUseCase,
) {
    private val previousTelemetry = mutableMapOf<ReadoutItemKey, Gt7Ps5TelemetryData>()

    suspend fun process(
        sourceKey: ReadoutItemKey,
        telemetry: Gt7Ps5TelemetryData,
        events: List<SpeechEvent>,
        readoutOrder: List<ReadoutItemKey>,
        queueEnabledStates: Map<ReadoutItemKey, Boolean>,
        observedAtMs: Long,
        logContext: Gt7Ps5TelemetryLogContext =
            Gt7Ps5TelemetryLogContext(
                state = Gt7Ps5NarratorState(),
                settings =
                    Gt7Ps5NarratorReadoutSettings(
                        enabledStates = emptyMap(),
                        myBestLapVoiceType = MyBestLapVoiceType.FORMAL,
                        remainingFuelLapsThreshold = 0,
                        remainingFuelLapsEnabled = false,
                        remainingFuelThresholdPercentage = 0,
                        remainingFuelEnabled = false,
                    ),
                finalState = Gt7Ps5NarratorState(),
            ),
    ) {
        val previous = previousTelemetry[sourceKey]
        events.forEach { event ->
            if (speakWithPriority(event, readoutOrder, queueEnabledStates)) {
                saveTelemetryLog(
                    createdAt = observedAtMs,
                    simulator = Simulator.Gt7Ps5,
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
        previousTelemetry[sourceKey] = telemetry
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
}

private fun buildTelemetryLogJson(
    state: Gt7Ps5NarratorState,
    previous: Gt7Ps5TelemetryData?,
    current: Gt7Ps5TelemetryData,
    settings: Gt7Ps5NarratorReadoutSettings,
    observedAtMs: Long,
    finalState: Gt7Ps5NarratorState,
): String =
    "{" +
        """"state":${state.toJsonString()},""" +
        """"previousTelemetry":${previous?.toJson() ?: "null"},""" +
        """"telemetry":${current.toJson()},""" +
        """"settings":${settings.toJsonString()},""" +
        """"observedAtMs":$observedAtMs,""" +
        """"finalState":${finalState.toJsonString()}""" +
        "}"

private fun Gt7Ps5NarratorReadoutSettings.toJsonString(): String = """{"raw":${toString().toJsonStringLiteral()}}"""

private fun Gt7Ps5NarratorState.toJsonString(): String = """{"raw":${toString().toJsonStringLiteral()}}"""

private fun Gt7Ps5TelemetryData.toJson(): String =
    "{" +
        """"lapCount":$lapCount,""" +
        """"lapsInRace":$lapsInRace,""" +
        """"bestLapTimeMs":$bestLapTimeMs,""" +
        """"gasLevel":$gasLevel,""" +
        """"gasCapacity":$gasCapacity""" +
        "}"

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
