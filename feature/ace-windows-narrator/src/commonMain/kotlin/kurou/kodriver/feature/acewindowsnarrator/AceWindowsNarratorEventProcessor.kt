package kurou.kodriver.feature.acewindowsnarrator

import kotlinx.serialization.encodeToString
import kurou.kodriver.core.narrator.SpeakOutcomes
import kurou.kodriver.core.narrator.TelemetryLogJsonAllowingSpecialFloatingPoint
import kurou.kodriver.core.narrator.TelemetryLogJsonCurrentField
import kurou.kodriver.core.narrator.TelemetryLogJsonPreviousField
import kurou.kodriver.core.narrator.buildTelemetryLogJson
import kurou.kodriver.core.narrator.runCatchingNarratorError
import kurou.kodriver.core.narrator.runCatchingTelemetryLogSave
import kurou.kodriver.core.narrator.speakWithOutcome
import kurou.kodriver.core.narrator.toRawTelemetryLogJson
import kurou.kodriver.domain.acewindows.model.AceWindowsBestLapTimeData
import kurou.kodriver.domain.acewindows.model.AceWindowsFlagData
import kurou.kodriver.domain.acewindows.model.AceWindowsFuelData
import kurou.kodriver.domain.acewindows.model.AceWindowsRemainingFuelLapsData
import kurou.kodriver.domain.acewindows.model.AceWindowsTyreCarcassTemperatureData
import kurou.kodriver.domain.acewindows.model.AceWindowsVehicleApproachData
import kurou.kodriver.domain.acewindows.usecase.AceWindowsNarratorReadoutSettings
import kurou.kodriver.domain.acewindows.usecase.AceWindowsNarratorState
import kurou.kodriver.domain.engine.DebugReadoutText
import kurou.kodriver.domain.engine.ReadoutTextEvent
import kurou.kodriver.domain.engine.SpeechEvent
import kurou.kodriver.domain.engine.TextToSpeechEngine
import kurou.kodriver.domain.model.DebugReadoutRequest
import kurou.kodriver.domain.model.NarrationOutcome
import kurou.kodriver.domain.model.NarrationSource
import kurou.kodriver.domain.model.ReadoutItemKey
import kurou.kodriver.domain.model.Simulator
import kurou.kodriver.domain.usecase.SaveTelemetryLogUseCase

internal data class AceWindowsTelemetryLogContext(
    val state: AceWindowsNarratorState,
    val settings: AceWindowsNarratorReadoutSettings,
    val finalState: AceWindowsNarratorState,
)

internal class AceWindowsNarratorEventProcessor(
    private val ttsEngine: TextToSpeechEngine,
    private val saveTelemetryLog: SaveTelemetryLogUseCase,
    private val readoutText: suspend (SpeechEvent) -> String?,
) {
    private var previousFuel: AceWindowsFuelData? = null
    private var previousRemainingFuelLaps: AceWindowsRemainingFuelLapsData? = null
    private var previousFlag: AceWindowsFlagData? = null
    private var previousTyreCarcassTemperature: AceWindowsTyreCarcassTemperatureData? = null
    private var previousVehicleApproach: AceWindowsVehicleApproachData? = null
    private var previousBestLapTime: AceWindowsBestLapTimeData? = null

    /** 判定・テンプレート置換を通さず、通常の優先度・音声・ログ経路で処理する。 */
    suspend fun processDebugReadout(
        request: DebugReadoutRequest,
        autoReadoutOrder: List<ReadoutItemKey>,
        queueEnabledStates: Map<ReadoutItemKey, Boolean>,
        observedAtMs: Long,
        logContext: AceWindowsTelemetryLogContext,
    ) {
        val event = DebugReadoutText(request.readoutItemKey, request.text)
        val text = readoutTextSafely(event)?.takeIf { it.isNotBlank() }
        val outcome =
            if (text == null) {
                NarrationOutcome.SKIPPED
            } else {
                speakWithPriority(event.withResolvedText(text), autoReadoutOrder, queueEnabledStates)
            }
        saveTelemetryLogSafely(
            createdAt = observedAtMs,
            readoutItemKey = event.readoutItemKey,
            narratedText = text.orEmpty(),
            narrationOutcome = outcome,
            narrationSource = NarrationSource.AUTO,
            telemetryJson =
                """{"state":${logContext.state.toRawTelemetryLogJson()},""" +
                    """"settings":${logContext.settings.toRawTelemetryLogJson()},""" +
                    """"observedAtMs":$observedAtMs,"debugReadout":true}""",
        )
    }

    suspend fun processManualReadout(
        events: List<ReadoutTextEvent>,
        autoReadoutOrder: List<ReadoutItemKey>,
        queueEnabledStates: Map<ReadoutItemKey, Boolean>,
        observedAtMs: Long,
        logContext: AceWindowsTelemetryLogContext,
    ) {
        events.forEach { event ->
            val text = readoutTextSafely(event)?.takeIf { it.isNotBlank() }
            val outcome =
                if (text == null) {
                    NarrationOutcome.SKIPPED
                } else {
                    speakWithPriority(event.withResolvedText(text), autoReadoutOrder, queueEnabledStates)
                }
            saveTelemetryLogSafely(
                createdAt = observedAtMs,
                readoutItemKey = event.readoutItemKey,
                narratedText = text.orEmpty(),
                narrationOutcome = outcome,
                narrationSource = NarrationSource.MANUAL,
                telemetryJson =
                    """{"state":${logContext.state.toRawTelemetryLogJson()},""" +
                        """"settings":${logContext.settings.toRawTelemetryLogJson()},""" +
                        """"observedAtMs":$observedAtMs}""",
            )
        }
    }

    suspend fun processMyBestLap(
        bestLapTime: AceWindowsBestLapTimeData,
        events: List<ReadoutTextEvent>,
        autoReadoutOrder: List<ReadoutItemKey>,
        queueEnabledStates: Map<ReadoutItemKey, Boolean>,
        observedAtMs: Long,
        logContext: AceWindowsTelemetryLogContext,
    ) {
        val previous = previousBestLapTime
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
                narrationSource = NarrationSource.AUTO,
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
        previousBestLapTime = bestLapTime
    }

    suspend fun processFlag(
        flag: AceWindowsFlagData,
        events: List<ReadoutTextEvent>,
        autoReadoutOrder: List<ReadoutItemKey>,
        queueEnabledStates: Map<ReadoutItemKey, Boolean>,
        observedAtMs: Long,
        logContext: AceWindowsTelemetryLogContext,
    ) {
        val previous = previousFlag
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
                narrationSource = NarrationSource.AUTO,
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
        previousFlag = flag
    }

    suspend fun processRemainingFuel(
        fuel: AceWindowsFuelData,
        events: List<ReadoutTextEvent>,
        autoReadoutOrder: List<ReadoutItemKey>,
        queueEnabledStates: Map<ReadoutItemKey, Boolean>,
        observedAtMs: Long,
        logContext: AceWindowsTelemetryLogContext,
    ) {
        val previous = previousFuel
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
                narrationSource = NarrationSource.AUTO,
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
        previousFuel = fuel
    }

    suspend fun processRemainingFuelLaps(
        remainingFuelLaps: AceWindowsRemainingFuelLapsData,
        events: List<ReadoutTextEvent>,
        autoReadoutOrder: List<ReadoutItemKey>,
        queueEnabledStates: Map<ReadoutItemKey, Boolean>,
        observedAtMs: Long,
        logContext: AceWindowsTelemetryLogContext,
    ) {
        val previous = previousRemainingFuelLaps
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
                narrationSource = NarrationSource.AUTO,
                telemetryJson =
                    buildRemainingFuelLapsTelemetryLogJson(
                        state = logContext.state,
                        previous = previous,
                        current = remainingFuelLaps,
                        settings = logContext.settings,
                        observedAtMs = observedAtMs,
                        finalState = logContext.finalState,
                    ),
            )
        }
        previousRemainingFuelLaps = remainingFuelLaps
    }

    suspend fun processTyreTemperature(
        tyreCarcassTemperature: AceWindowsTyreCarcassTemperatureData,
        events: List<ReadoutTextEvent>,
        autoReadoutOrder: List<ReadoutItemKey>,
        queueEnabledStates: Map<ReadoutItemKey, Boolean>,
        observedAtMs: Long,
        logContext: AceWindowsTelemetryLogContext,
    ) {
        val previous = previousTyreCarcassTemperature
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
                narrationSource = NarrationSource.AUTO,
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
        previousTyreCarcassTemperature = tyreCarcassTemperature
    }

    suspend fun processVehicleApproach(
        vehicleApproach: AceWindowsVehicleApproachData,
        events: List<ReadoutTextEvent>,
        autoReadoutOrder: List<ReadoutItemKey>,
        queueEnabledStates: Map<ReadoutItemKey, Boolean>,
        observedAtMs: Long,
        logContext: AceWindowsTelemetryLogContext,
    ) {
        val previous = previousVehicleApproach
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
                narrationSource = NarrationSource.AUTO,
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
        previousVehicleApproach = vehicleApproach
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
        narrationSource: NarrationSource,
        telemetryJson: String,
    ) {
        runCatchingTelemetryLogSave {
            saveTelemetryLog(
                createdAt = createdAt,
                narrationOutcome = narrationOutcome,
                narrationSource = narrationSource,
                simulator = Simulator.AceWindows,
                readoutItemKey = readoutItemKey,
                narratedText = narratedText,
                telemetryJson = telemetryJson,
            )
        }
    }
}

/**
 * ACE の燃料残量判定入力（[AceWindowsFuelData]）は判定ロジック（
 * [kurou.kodriver.domain.usecase.DetermineAceWindowsNarratorReadoutUseCase.determineRemainingFuel]）と
 * 共有しているため、フィールドを手動で選ばず [TelemetryLogJsonAllowingSpecialFloatingPoint] でシリアライズしてそのまま記録する。
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
        stateJson = state.toRawTelemetryLogJson(),
        previous =
            TelemetryLogJsonPreviousField(
                name = "previousFuel",
                json = previous?.let { TelemetryLogJsonAllowingSpecialFloatingPoint.encodeToString(it) },
            ),
        current =
            TelemetryLogJsonCurrentField(
                name = "fuel",
                json = TelemetryLogJsonAllowingSpecialFloatingPoint.encodeToString(current),
            ),
        settingsJson = settings.toRawTelemetryLogJson(),
        observedAtMs = observedAtMs,
        finalStateJson = finalState.toRawTelemetryLogJson(),
    )

private fun buildRemainingFuelLapsTelemetryLogJson(
    state: AceWindowsNarratorState,
    previous: AceWindowsRemainingFuelLapsData?,
    current: AceWindowsRemainingFuelLapsData,
    settings: AceWindowsNarratorReadoutSettings,
    observedAtMs: Long,
    finalState: AceWindowsNarratorState,
): String =
    buildTelemetryLogJson(
        stateJson = state.toRawTelemetryLogJson(),
        previous =
            TelemetryLogJsonPreviousField(
                name = "previousRemainingFuelLaps",
                json = previous?.let { TelemetryLogJsonAllowingSpecialFloatingPoint.encodeToString(it) },
            ),
        current =
            TelemetryLogJsonCurrentField(
                name = "remainingFuelLaps",
                json = TelemetryLogJsonAllowingSpecialFloatingPoint.encodeToString(current),
            ),
        settingsJson = settings.toRawTelemetryLogJson(),
        observedAtMs = observedAtMs,
        finalStateJson = finalState.toRawTelemetryLogJson(),
    )

/**
 * ACE の自己ベストラップ判定入力（[AceWindowsBestLapTimeData]）は判定ロジック（
 * [kurou.kodriver.domain.usecase.DetermineAceWindowsNarratorReadoutUseCase.determineMyBestLap]）と
 * 共有しているため、フィールドを手動で選ばず [TelemetryLogJsonAllowingSpecialFloatingPoint] でシリアライズしてそのまま記録する。
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
        stateJson = state.toRawTelemetryLogJson(),
        previous =
            TelemetryLogJsonPreviousField(
                name = "previousBestLapTime",
                json = previous?.let { TelemetryLogJsonAllowingSpecialFloatingPoint.encodeToString(it) },
            ),
        current =
            TelemetryLogJsonCurrentField(
                name = "bestLapTime",
                json = TelemetryLogJsonAllowingSpecialFloatingPoint.encodeToString(current),
            ),
        settingsJson = settings.toRawTelemetryLogJson(),
        observedAtMs = observedAtMs,
        finalStateJson = finalState.toRawTelemetryLogJson(),
    )

/**
 * ACE のフラッグ判定入力（[AceWindowsFlagData]）は判定ロジック（
 * [kurou.kodriver.domain.usecase.DetermineAceWindowsNarratorReadoutUseCase.determineFlag]）と
 * 共有しているため、フィールドを手動で選ばず [TelemetryLogJsonAllowingSpecialFloatingPoint] でシリアライズしてそのまま記録する。
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
        stateJson = state.toRawTelemetryLogJson(),
        previous =
            TelemetryLogJsonPreviousField(
                name = "previousFlag",
                json = previous?.let { TelemetryLogJsonAllowingSpecialFloatingPoint.encodeToString(it) },
            ),
        current =
            TelemetryLogJsonCurrentField(
                name = "flag",
                json = TelemetryLogJsonAllowingSpecialFloatingPoint.encodeToString(current),
            ),
        settingsJson = settings.toRawTelemetryLogJson(),
        observedAtMs = observedAtMs,
        finalStateJson = finalState.toRawTelemetryLogJson(),
    )

/**
 * ACE のタイヤカーカス温度判定入力（[AceWindowsTyreCarcassTemperatureData]）は判定ロジック（
 * [kurou.kodriver.domain.usecase.DetermineAceWindowsNarratorReadoutUseCase.determineTyreTemperatureOverheat]）と
 * 共有しているため、フィールドを手動で選ばず [TelemetryLogJsonAllowingSpecialFloatingPoint] でシリアライズしてそのまま記録する。
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
        stateJson = state.toRawTelemetryLogJson(),
        previous =
            TelemetryLogJsonPreviousField(
                name = "previousTyreCarcassTemperature",
                json =
                    previous?.let {
                        TelemetryLogJsonAllowingSpecialFloatingPoint.encodeToString(it)
                    },
            ),
        current =
            TelemetryLogJsonCurrentField(
                name = "tyreCarcassTemperature",
                json = TelemetryLogJsonAllowingSpecialFloatingPoint.encodeToString(current),
            ),
        settingsJson = settings.toRawTelemetryLogJson(),
        observedAtMs = observedAtMs,
        finalStateJson = finalState.toRawTelemetryLogJson(),
    )

/**
 * ACE の車両接近判定入力（[AceWindowsVehicleApproachData]）は判定ロジック（
 * [kurou.kodriver.domain.usecase.DetermineAceWindowsNarratorReadoutUseCase.determineVehicleApproach]）と
 * 共有しているため、フィールドを手動で選ばず [TelemetryLogJsonAllowingSpecialFloatingPoint] でシリアライズしてそのまま記録する。
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
        stateJson = state.toRawTelemetryLogJson(),
        previous =
            TelemetryLogJsonPreviousField(
                name = "previousVehicleApproach",
                json = previous?.let { TelemetryLogJsonAllowingSpecialFloatingPoint.encodeToString(it) },
            ),
        current =
            TelemetryLogJsonCurrentField(
                name = "vehicleApproach",
                json = TelemetryLogJsonAllowingSpecialFloatingPoint.encodeToString(current),
            ),
        settingsJson = settings.toRawTelemetryLogJson(),
        observedAtMs = observedAtMs,
        finalStateJson = finalState.toRawTelemetryLogJson(),
    )
