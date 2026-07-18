package kurou.kodriver.feature.gt7ps5narrator

import kurou.kodriver.domain.engine.SpeechEvent
import kurou.kodriver.domain.engine.TextToSpeechEngine
import kurou.kodriver.domain.model.Gt7Ps5TelemetryData
import kurou.kodriver.domain.model.ReadoutItemKey
import kurou.kodriver.domain.model.Simulator
import kurou.kodriver.domain.usecase.SaveTelemetryLogUseCase

class Gt7Ps5NarratorEventProcessor(
    private val ttsEngine: TextToSpeechEngine,
    private val saveTelemetryLog: SaveTelemetryLogUseCase,
) {
    private val previousTelemetry = mutableMapOf<ReadoutItemKey, Gt7Ps5TelemetryData>()

    suspend fun process(
        sourceKey: ReadoutItemKey,
        telemetry: Gt7Ps5TelemetryData,
        events: List<SpeechEvent>,
        readoutOrder: List<ReadoutItemKey>,
        observedAtMs: Long,
    ) {
        val previous = previousTelemetry[sourceKey]
        events.forEach { event ->
            if (speakWithPriority(event, readoutOrder)) {
                saveTelemetryLog(
                    createdAt = observedAtMs,
                    simulatorId = Simulator.Gt7Ps5.id,
                    readoutItemKey = event.readoutItemKey.value,
                    telemetryJson = buildTelemetryLogJson(previous = previous, current = telemetry),
                )
            }
        }
        previousTelemetry[sourceKey] = telemetry
    }

    private fun speakWithPriority(event: SpeechEvent, readoutOrder: List<ReadoutItemKey>): Boolean {
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

private fun buildTelemetryLogJson(previous: Gt7Ps5TelemetryData?, current: Gt7Ps5TelemetryData): String =
    """{"previous":${if (previous == null) "null" else previous.toJson()},"current":${current.toJson()}}"""

private fun Gt7Ps5TelemetryData.toJson(): String =
    "{" +
        """"lapCount":$lapCount,""" +
        """"lapsInRace":$lapsInRace,""" +
        """"bestLapTimeMs":$bestLapTimeMs,""" +
        """"gasLevel":$gasLevel,""" +
        """"gasCapacity":$gasCapacity""" +
        "}"
