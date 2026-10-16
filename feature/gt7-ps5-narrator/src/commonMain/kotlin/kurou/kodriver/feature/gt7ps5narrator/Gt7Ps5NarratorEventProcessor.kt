package kurou.kodriver.feature.gt7ps5narrator

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
import kurou.kodriver.domain.engine.DebugReadoutText
import kurou.kodriver.domain.engine.ReadoutTextEvent
import kurou.kodriver.domain.engine.SpeechEvent
import kurou.kodriver.domain.engine.TextToSpeechEngine
import kurou.kodriver.domain.model.DebugReadoutRequest
import kurou.kodriver.domain.model.Gt7Ps5TelemetryData
import kurou.kodriver.domain.model.NarrationOutcome
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
    private val readoutText: suspend (SpeechEvent) -> String?,
) {
    private val previousTelemetry = mutableMapOf<ReadoutItemKey, Gt7Ps5TelemetryData>()

    /** 判定・テンプレート置換を通さず、通常の優先度・音声・ログ経路で処理する。 */
    suspend fun processDebugReadout(
        request: DebugReadoutRequest,
        autoReadoutOrder: List<ReadoutItemKey>,
        queueEnabledStates: Map<ReadoutItemKey, Boolean>,
        observedAtMs: Long,
        logContext: Gt7Ps5TelemetryLogContext,
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
        logContext: Gt7Ps5TelemetryLogContext,
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
                telemetryJson =
                    """{"state":${logContext.state.toRawTelemetryLogJson()},""" +
                        """"settings":${logContext.settings.toRawTelemetryLogJson()},""" +
                        """"observedAtMs":$observedAtMs}""",
            )
        }
    }

    suspend fun process(
        sourceKey: ReadoutItemKey,
        telemetry: Gt7Ps5TelemetryData,
        events: List<ReadoutTextEvent>,
        autoReadoutOrder: List<ReadoutItemKey>,
        queueEnabledStates: Map<ReadoutItemKey, Boolean>,
        observedAtMs: Long,
        logContext: Gt7Ps5TelemetryLogContext,
    ) {
        val previous = previousTelemetry[sourceKey]
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
        previousTelemetry[sourceKey] = telemetry
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
        telemetryJson: String,
    ) {
        runCatchingTelemetryLogSave {
            saveTelemetryLog(
                createdAt = createdAt,
                narrationOutcome = narrationOutcome,
                simulator = Simulator.Gt7Ps5,
                readoutItemKey = readoutItemKey,
                narratedText = narratedText,
                telemetryJson = telemetryJson,
            )
        }
    }
}

/**
 * GT7 の読み上げ判定入力（[Gt7Ps5TelemetryData]）は判定ロジック（
 * [kurou.kodriver.domain.usecase.DetermineGt7Ps5NarratorReadoutUseCase] の各 determine* 関数）と
 * 共有しているため、フィールドを手動で選ばず [TelemetryLogJsonAllowingSpecialFloatingPoint] でシリアライズしてそのまま記録する。
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
    buildTelemetryLogJson(
        stateJson = state.toRawTelemetryLogJson(),
        previous =
            TelemetryLogJsonPreviousField(
                name = "previousTelemetry",
                json =
                    previous?.let {
                        TelemetryLogJsonAllowingSpecialFloatingPoint.encodeToString(it)
                    },
            ),
        current =
            TelemetryLogJsonCurrentField(
                name = "telemetry",
                json = TelemetryLogJsonAllowingSpecialFloatingPoint.encodeToString(current),
            ),
        settingsJson = settings.toRawTelemetryLogJson(),
        observedAtMs = observedAtMs,
        finalStateJson = finalState.toRawTelemetryLogJson(),
    )
