package kurou.kodriver.feature.acewindowsnarrator

import kotlinx.coroutines.CancellationException
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kurou.kodriver.core.narrator.TelemetryLogJson
import kurou.kodriver.core.narrator.TelemetryLogJsonCurrentField
import kurou.kodriver.core.narrator.TelemetryLogJsonPreviousField
import kurou.kodriver.core.narrator.buildTelemetryLogJson
import kurou.kodriver.core.narrator.speakWithPriority
import kurou.kodriver.core.narrator.toJsonStringLiteral
import kurou.kodriver.domain.engine.SpeechEvent
import kurou.kodriver.domain.engine.TextToSpeechEngine
import kurou.kodriver.domain.model.AceWindowsBestLapTimeData
import kurou.kodriver.domain.model.AceWindowsFlagData
import kurou.kodriver.domain.model.AceWindowsFuelData
import kurou.kodriver.domain.model.AceWindowsTyreCarcassTemperatureData
import kurou.kodriver.domain.model.AceWindowsVehicleApproachData
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
    private var previousTyreCarcassTemperature: AceWindowsTyreCarcassTemperatureData? = null
    private var previousVehicleApproach: AceWindowsVehicleApproachData? = null
    private var previousBestLapTime: AceWindowsBestLapTimeData? = null

    suspend fun processMyBestLap(
        bestLapTime: AceWindowsBestLapTimeData,
        events: List<SpeechEvent>,
        readoutOrder: List<ReadoutItemKey>,
        queueEnabledStates: Map<ReadoutItemKey, Boolean>,
        observedAtMs: Long,
        logContext: AceWindowsTelemetryLogContext,
        isOnTrack: Boolean,
    ) {
        val previous = previousBestLapTime
        if (isOnTrack) {
            events.forEach { event ->
                if (speakWithPriority(event, readoutOrder, queueEnabledStates)) {
                    saveTelemetryLogSafely(
                        createdAt = observedAtMs,
                        readoutItemKey = event.readoutItemKey,
                        telemetryJson =
                            buildMyBestLapTelemetryLogJson(
                                state = logContext.state,
                                previous = previous,
                                current = bestLapTime,
                                settings = logContext.settings,
                                observedAtMs = observedAtMs,
                                finalState = logContext.finalState,
                            ),
                    )
                }
            }
        }
        previousBestLapTime = bestLapTime
    }

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

    suspend fun processTyreTemperature(
        tyreCarcassTemperature: AceWindowsTyreCarcassTemperatureData,
        events: List<SpeechEvent>,
        readoutOrder: List<ReadoutItemKey>,
        queueEnabledStates: Map<ReadoutItemKey, Boolean>,
        observedAtMs: Long,
        logContext: AceWindowsTelemetryLogContext,
        isOnTrack: Boolean,
    ) {
        val previous = previousTyreCarcassTemperature
        if (isOnTrack) {
            events.forEach { event ->
                if (speakWithPriority(event, readoutOrder, queueEnabledStates)) {
                    saveTelemetryLogSafely(
                        createdAt = observedAtMs,
                        readoutItemKey = event.readoutItemKey,
                        telemetryJson =
                            buildTyreTemperatureTelemetryLogJson(
                                state = logContext.state,
                                previous = previous,
                                current = tyreCarcassTemperature,
                                settings = logContext.settings,
                                observedAtMs = observedAtMs,
                                finalState = logContext.finalState,
                            ),
                    )
                }
            }
        }
        previousTyreCarcassTemperature = tyreCarcassTemperature
    }

    suspend fun processVehicleApproach(
        vehicleApproach: AceWindowsVehicleApproachData,
        events: List<SpeechEvent>,
        readoutOrder: List<ReadoutItemKey>,
        queueEnabledStates: Map<ReadoutItemKey, Boolean>,
        observedAtMs: Long,
        logContext: AceWindowsTelemetryLogContext,
        isOnTrack: Boolean,
    ) {
        val previous = previousVehicleApproach
        if (isOnTrack) {
            events.forEach { event ->
                if (speakWithPriority(event, readoutOrder, queueEnabledStates)) {
                    saveTelemetryLogSafely(
                        createdAt = observedAtMs,
                        readoutItemKey = event.readoutItemKey,
                        telemetryJson =
                            buildVehicleApproachTelemetryLogJson(
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
        }
        previousVehicleApproach = vehicleApproach
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
    buildTelemetryLogJson(
        stateJson = state.toJsonString(),
        previous =
            TelemetryLogJsonPreviousField(
                name = "previousFuel",
                json = previous?.let { telemetryLogJson.encodeToString(it) },
            ),
        current = TelemetryLogJsonCurrentField(name = "fuel", json = telemetryLogJson.encodeToString(current)),
        settingsJson = settings.toJsonString(),
        observedAtMs = observedAtMs,
        finalStateJson = finalState.toJsonString(),
    )

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
 * ACE の自己ベストラップ判定入力（[AceWindowsBestLapTimeData]）は判定ロジック（
 * [kurou.kodriver.domain.usecase.DetermineAceWindowsNarratorReadoutUseCase.determineMyBestLap]）と
 * 共有しているため、フィールドを手動で選ばず [telemetryLogJson] でシリアライズしてそのまま記録する。
 * これにより判定に使う入力が増えても記録側の更新漏れが構造的に起こらない。
 */
private fun buildMyBestLapTelemetryLogJson(
    state: AceWindowsNarratorState,
    previous: AceWindowsBestLapTimeData?,
    current: AceWindowsBestLapTimeData,
    settings: AceWindowsNarratorReadoutSettings,
    observedAtMs: Long,
    finalState: AceWindowsNarratorState,
): String =
    buildTelemetryLogJson(
        stateJson = state.toJsonString(),
        previous =
            TelemetryLogJsonPreviousField(
                name = "previousBestLapTime",
                json = previous?.let { telemetryLogJson.encodeToString(it) },
            ),
        current = TelemetryLogJsonCurrentField(name = "bestLapTime", json = telemetryLogJson.encodeToString(current)),
        settingsJson = settings.toJsonString(),
        observedAtMs = observedAtMs,
        finalStateJson = finalState.toJsonString(),
    )

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
    buildTelemetryLogJson(
        stateJson = state.toJsonString(),
        previous =
            TelemetryLogJsonPreviousField(
                name = "previousFlag",
                json = previous?.let { telemetryLogJson.encodeToString(it) },
            ),
        current = TelemetryLogJsonCurrentField(name = "flag", json = telemetryLogJson.encodeToString(current)),
        settingsJson = settings.toJsonString(),
        observedAtMs = observedAtMs,
        finalStateJson = finalState.toJsonString(),
    )

/**
 * ACE のタイヤカーカス温度判定入力（[AceWindowsTyreCarcassTemperatureData]）は判定ロジック（
 * [kurou.kodriver.domain.usecase.DetermineAceWindowsNarratorReadoutUseCase.determineTyreTemperatureOverheat]）と
 * 共有しているため、フィールドを手動で選ばず [telemetryLogJson] でシリアライズしてそのまま記録する。
 * これにより判定に使う入力が増えても記録側の更新漏れが構造的に起こらない。
 */
private fun buildTyreTemperatureTelemetryLogJson(
    state: AceWindowsNarratorState,
    previous: AceWindowsTyreCarcassTemperatureData?,
    current: AceWindowsTyreCarcassTemperatureData,
    settings: AceWindowsNarratorReadoutSettings,
    observedAtMs: Long,
    finalState: AceWindowsNarratorState,
): String =
    buildTelemetryLogJson(
        stateJson = state.toJsonString(),
        previous =
            TelemetryLogJsonPreviousField(
                name = "previousTyreCarcassTemperature",
                json =
                    previous?.let {
                        telemetryLogJson.encodeToString(it)
                    },
            ),
        current =
            TelemetryLogJsonCurrentField(
                name = "tyreCarcassTemperature",
                json = telemetryLogJson.encodeToString(current),
            ),
        settingsJson = settings.toJsonString(),
        observedAtMs = observedAtMs,
        finalStateJson = finalState.toJsonString(),
    )

/**
 * ACE の車両接近判定入力（[AceWindowsVehicleApproachData]）は判定ロジック（
 * [kurou.kodriver.domain.usecase.DetermineAceWindowsNarratorReadoutUseCase.determineVehicleApproach]）と
 * 共有しているため、フィールドを手動で選ばず [telemetryLogJson] でシリアライズしてそのまま記録する。
 * これにより判定に使う入力が増えても記録側の更新漏れが構造的に起こらない。
 */
private fun buildVehicleApproachTelemetryLogJson(
    state: AceWindowsNarratorState,
    previous: AceWindowsVehicleApproachData?,
    current: AceWindowsVehicleApproachData,
    settings: AceWindowsNarratorReadoutSettings,
    observedAtMs: Long,
    finalState: AceWindowsNarratorState,
): String =
    buildTelemetryLogJson(
        stateJson = state.toJsonString(),
        previous =
            TelemetryLogJsonPreviousField(
                name = "previousVehicleApproach",
                json = previous?.let { telemetryLogJson.encodeToString(it) },
            ),
        current =
            TelemetryLogJsonCurrentField(
                name = "vehicleApproach",
                json = telemetryLogJson.encodeToString(current),
            ),
        settingsJson = settings.toJsonString(),
        observedAtMs = observedAtMs,
        finalStateJson = finalState.toJsonString(),
    )
