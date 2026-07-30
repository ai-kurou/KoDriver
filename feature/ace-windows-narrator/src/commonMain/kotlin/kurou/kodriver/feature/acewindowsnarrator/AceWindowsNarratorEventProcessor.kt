package kurou.kodriver.feature.acewindowsnarrator

import kotlinx.coroutines.CancellationException
import kurou.kodriver.domain.engine.SpeechEvent
import kurou.kodriver.domain.engine.TextToSpeechEngine
import kurou.kodriver.domain.model.AceWindowsFlagData
import kurou.kodriver.domain.model.AceWindowsFuelData
import kurou.kodriver.domain.model.ReadoutItemKey
import kurou.kodriver.domain.model.Simulator
import kurou.kodriver.domain.usecase.AceWindowsNarratorReadoutSettings
import kurou.kodriver.domain.usecase.AceWindowsNarratorState
import kurou.kodriver.domain.usecase.SaveTelemetryLogUseCase

internal data class AceWindowsTelemetryLogContext(
    val state: AceWindowsNarratorState,
    val settings: AceWindowsNarratorReadoutSettings,
    val finalState: AceWindowsNarratorState,
)

internal class AceWindowsNarratorEventProcessor(
    private val ttsEngine: TextToSpeechEngine,
    private val saveTelemetryLog: SaveTelemetryLogUseCase,
) {
    private var previousFuel: AceWindowsFuelData? = null
    private var previousFlag: AceWindowsFlagData? = null

    suspend fun processFlag(
        flag: AceWindowsFlagData,
        events: List<SpeechEvent>,
        readoutOrder: List<ReadoutItemKey>,
        queueEnabledStates: Map<ReadoutItemKey, Boolean>,
        observedAtMs: Long,
        logContext: AceWindowsTelemetryLogContext,
    ) {
        val previous = previousFlag
        events.forEach { event ->
            if (speakWithPriority(event, readoutOrder, queueEnabledStates)) {
                saveTelemetryLogSafely(
                    createdAt = observedAtMs,
                    readoutItemKey = event.readoutItemKey,
                    telemetryJson = buildFlagTelemetryLogJson(
                        state = logContext.state,
                        previous = previous,
                        current = flag,
                        settings = logContext.settings,
                        observedAtMs = observedAtMs,
                        finalState = logContext.finalState,
                    ),
                )
            }
        }
        previousFlag = flag
    }

    suspend fun processRemainingFuel(
        fuel: AceWindowsFuelData,
        events: List<SpeechEvent>,
        readoutOrder: List<ReadoutItemKey>,
        queueEnabledStates: Map<ReadoutItemKey, Boolean>,
        observedAtMs: Long,
        logContext: AceWindowsTelemetryLogContext,
    ) {
        val previous = previousFuel
        events.forEach { event ->
            if (speakWithPriority(event, readoutOrder, queueEnabledStates)) {
                saveTelemetryLogSafely(
                    createdAt = observedAtMs,
                    readoutItemKey = event.readoutItemKey,
                    telemetryJson = buildTelemetryLogJson(
                        state = logContext.state,
                        previous = previous,
                        current = fuel,
                        settings = logContext.settings,
                        observedAtMs = observedAtMs,
                        finalState = logContext.finalState,
                    ),
                )
            }
        }
        previousFuel = fuel
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
                simulator = Simulator.AceWindows,
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
    state: AceWindowsNarratorState,
    previous: AceWindowsFuelData?,
    current: AceWindowsFuelData,
    settings: AceWindowsNarratorReadoutSettings,
    observedAtMs: Long,
    finalState: AceWindowsNarratorState,
): String =
    "{" +
        """"state":${state.toJsonString()},""" +
        """"previousFuel":${previous?.toJson() ?: "null"},""" +
        """"fuel":${current.toJson()},""" +
        """"settings":${settings.toJsonString()},""" +
        """"observedAtMs":$observedAtMs,""" +
        """"finalState":${finalState.toJsonString()}""" +
        "}"

private fun AceWindowsNarratorReadoutSettings.toJsonString(): String =
    """{"raw":${toString().toJsonStringLiteral()}}"""

private fun AceWindowsNarratorState.toJsonString(): String =
    """{"raw":${toString().toJsonStringLiteral()}}"""

private fun AceWindowsFuelData.toJson(): String =
    """{"remainingPercent":$remainingPercent}"""

private fun buildFlagTelemetryLogJson(
    state: AceWindowsNarratorState,
    previous: AceWindowsFlagData?,
    current: AceWindowsFlagData,
    settings: AceWindowsNarratorReadoutSettings,
    observedAtMs: Long,
    finalState: AceWindowsNarratorState,
): String =
    "{" +
        """"state":${state.toJsonString()},""" +
        """"previousFlag":${previous?.toJson() ?: "null"},""" +
        """"flag":${current.toJson()},""" +
        """"settings":${settings.toJsonString()},""" +
        """"observedAtMs":$observedAtMs,""" +
        """"finalState":${finalState.toJsonString()}""" +
        "}"

private fun AceWindowsFlagData.toJson(): String =
    """{"flag":"${flag.name}"}"""

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
