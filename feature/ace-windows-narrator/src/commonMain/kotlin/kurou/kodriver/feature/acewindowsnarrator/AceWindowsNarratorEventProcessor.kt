package kurou.kodriver.feature.acewindowsnarrator

import kotlinx.coroutines.CancellationException
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kurou.kodriver.core.narrator.TelemetryLogJson
import kurou.kodriver.core.narrator.speakWithPriority
import kurou.kodriver.core.narrator.toJsonStringLiteral
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
        isOnTrack: Boolean,
    ) {
        val previous = previousFlag
        if (isOnTrack) {
            events.forEach { event ->
                if (speakWithPriority(event, readoutOrder, queueEnabledStates)) {
                    saveTelemetryLogSafely(
                        createdAt = observedAtMs,
                        readoutItemKey = event.readoutItemKey,
                        telemetryJson =
                            buildFlagTelemetryLogJson(
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
        isOnTrack: Boolean,
    ) {
        val previous = previousFuel
        if (isOnTrack) {
            events.forEach { event ->
                if (speakWithPriority(event, readoutOrder, queueEnabledStates)) {
                    saveTelemetryLogSafely(
                        createdAt = observedAtMs,
                        readoutItemKey = event.readoutItemKey,
                        telemetryJson =
                            buildTelemetryLogJson(
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
        }
        previousFuel = fuel
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

/**
 * ACE の燃料残量判定入力（[AceWindowsFuelData]）は判定ロジック（
 * [kurou.kodriver.domain.usecase.DetermineAceWindowsNarratorReadoutUseCase.determineRemainingFuel]）と
 * 共有しているため、フィールドを手動で選ばず [telemetryLogJson] でシリアライズしてそのまま記録する。
 * これにより判定に使う入力が増えても記録側の更新漏れが構造的に起こらない。
 */
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
        """"previousFuel":${previous?.let { telemetryLogJson.encodeToString(it) } ?: "null"},""" +
        """"fuel":${telemetryLogJson.encodeToString(current)},""" +
        """"settings":${settings.toJsonString()},""" +
        """"observedAtMs":$observedAtMs,""" +
        """"finalState":${finalState.toJsonString()}""" +
        "}"

private fun AceWindowsNarratorReadoutSettings.toJsonString(): String = """{"raw":${toString().toJsonStringLiteral()}}"""

private fun AceWindowsNarratorState.toJsonString(): String = """{"raw":${toString().toJsonStringLiteral()}}"""

/**
 * NaN/Infinity を含む可能性のある燃料残量（[AceWindowsFuelData.remainingPercent]）を
 * encode 失敗させないため、[TelemetryLogJson] に対して非有限値の encode を許可するよう拡張する。
 */
private val telemetryLogJson =
    Json(TelemetryLogJson) {
        allowSpecialFloatingPointValues = true
    }

/**
 * ACE のフラグ判定入力（[AceWindowsFlagData]）は判定ロジック（
 * [kurou.kodriver.domain.usecase.DetermineAceWindowsNarratorReadoutUseCase.determineFlag]）と
 * 共有しているため、フィールドを手動で選ばず [telemetryLogJson] でシリアライズしてそのまま記録する。
 * これにより判定に使う入力が増えても記録側の更新漏れが構造的に起こらない。
 */
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
        """"previousFlag":${previous?.let { telemetryLogJson.encodeToString(it) } ?: "null"},""" +
        """"flag":${telemetryLogJson.encodeToString(current)},""" +
        """"settings":${settings.toJsonString()},""" +
        """"observedAtMs":$observedAtMs,""" +
        """"finalState":${finalState.toJsonString()}""" +
        "}"
