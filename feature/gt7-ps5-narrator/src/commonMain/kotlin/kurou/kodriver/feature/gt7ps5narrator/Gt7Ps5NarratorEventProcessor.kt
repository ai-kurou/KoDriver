package kurou.kodriver.feature.gt7ps5narrator

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kurou.kodriver.core.model.Gt7Ps5TelemetryData
import kurou.kodriver.core.model.MyBestLapVoiceType
import kurou.kodriver.core.model.ReadoutItemKey
import kurou.kodriver.core.model.Simulator
import kurou.kodriver.core.narrator.TelemetryLogJson
import kurou.kodriver.core.narrator.speakWithPriority
import kurou.kodriver.core.narrator.toJsonStringLiteral
import kurou.kodriver.domain.engine.SpeechEvent
import kurou.kodriver.domain.engine.TextToSpeechEngine
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
    ): Boolean =
        speakWithPriority(
            eventKey = event.readoutItemKey,
            currentKey = { ttsEngine.currentReadoutItemKey },
            readoutOrder = readoutOrder,
            queueEnabled = queueEnabledStates[event.readoutItemKey] == true,
            speak = { queue -> ttsEngine.speak(event, queue) },
            stop = { ttsEngine.stop() },
        )
}

/**
 * GT7 の読み上げ判定入力（[Gt7Ps5TelemetryData]）は判定ロジック（
 * [kurou.kodriver.domain.usecase.DetermineGt7Ps5NarratorReadoutUseCase] の各 determine* 関数）と
 * 共有しているため、フィールドを手動で選ばず [telemetryLogJson] でシリアライズしてそのまま記録する。
 * これにより判定に使う入力が増えても記録側の更新漏れが構造的に起こらない。
 */
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
        """"previousTelemetry":${previous?.let { telemetryLogJson.encodeToString(it) } ?: "null"},""" +
        """"telemetry":${telemetryLogJson.encodeToString(current)},""" +
        """"settings":${settings.toJsonString()},""" +
        """"observedAtMs":$observedAtMs,""" +
        """"finalState":${finalState.toJsonString()}""" +
        "}"

private fun Gt7Ps5NarratorReadoutSettings.toJsonString(): String = """{"raw":${toString().toJsonStringLiteral()}}"""

private fun Gt7Ps5NarratorState.toJsonString(): String = """{"raw":${toString().toJsonStringLiteral()}}"""

/**
 * UDP テレメトリの Float フィールド（gasLevel/gasCapacity 等）が NaN/Infinity を
 * 取りうるため、[TelemetryLogJson] に対して通常は encode を拒否する非有限値も許可するよう拡張する。
 */
private val telemetryLogJson =
    Json(TelemetryLogJson) {
        allowSpecialFloatingPointValues = true
    }
