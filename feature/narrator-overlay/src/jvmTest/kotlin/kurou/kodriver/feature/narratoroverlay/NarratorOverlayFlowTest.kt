package kurou.kodriver.feature.narratoroverlay

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kurou.kodriver.domain.model.AceWindowsReadoutItemKey
import kurou.kodriver.domain.model.Simulator
import kurou.kodriver.domain.overlay.model.OverlayTextSize
import kurou.kodriver.domain.speechrecognition.model.SpeechRecognitionTranscript
import kurou.kodriver.domain.telemetrylog.model.NarrationOutcome
import kurou.kodriver.domain.telemetrylog.model.NarrationSource
import kurou.kodriver.domain.telemetrylog.model.TelemetryLog
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalCoroutinesApi::class)
class NarratorOverlayFlowTest {
    @Test
    fun `初期状態は入力されたログと表示設定を合成する`() =
        runTest {
            val log = createTelemetryLog()
            val transcript = SpeechRecognitionTranscript(partialText = "ピット", recognizedText = "燃料")
            val flow =
                NarratorOverlayFlow(
                    latestTelemetryLog = MutableStateFlow(log),
                    overlayTextSize = MutableStateFlow(OverlayTextSize.LARGE),
                    overlayTextColor = MutableStateFlow(0xFFFFFFFFL),
                    backgroundOpacity = MutableStateFlow(80),
                    speechRecognitionTranscript = MutableStateFlow(transcript),
                )

            assertEquals(
                NarratorOverlayUiState(log, OverlayTextSize.LARGE, 0xFFFFFFFFL, 80, transcript),
                flow.uiState.first(),
            )
        }

    @Test
    fun `購読中は各入力の更新とログの解除を画面状態へ反映する`() =
        runTest {
            val latestTelemetryLog = MutableStateFlow<TelemetryLog?>(null)
            val overlayTextSize = MutableStateFlow(OverlayTextSize.MEDIUM)
            val overlayTextColor = MutableStateFlow(0xFFFFFFFFL)
            val backgroundOpacity = MutableStateFlow(50)
            val speechRecognitionTranscript = MutableStateFlow(SpeechRecognitionTranscript())
            val flow =
                NarratorOverlayFlow(
                    latestTelemetryLog,
                    overlayTextSize,
                    overlayTextColor,
                    backgroundOpacity,
                    speechRecognitionTranscript,
                )
            val states = mutableListOf<NarratorOverlayUiState>()
            backgroundScope.launch { flow.uiState.collect { states.add(it) } }
            runCurrent()
            var expected = NarratorOverlayUiState(overlayTextColor = 0xFFFFFFFFL)
            assertEquals(expected, states.last())

            val log = createTelemetryLog()
            latestTelemetryLog.update { log }
            runCurrent()
            expected = expected.copy(latestTelemetryLog = log)
            assertEquals(expected, states.last())

            overlayTextSize.update { OverlayTextSize.LARGE }
            runCurrent()
            expected = expected.copy(overlayTextSize = OverlayTextSize.LARGE)
            assertEquals(expected, states.last())

            overlayTextColor.update { 0xFF00FF00L }
            runCurrent()
            expected = expected.copy(overlayTextColor = 0xFF00FF00L)
            assertEquals(expected, states.last())

            backgroundOpacity.update { 80 }
            runCurrent()
            expected = expected.copy(backgroundOpacity = 80)
            assertEquals(expected, states.last())

            val transcript = SpeechRecognitionTranscript(partialText = "ピット", recognizedText = "燃料")
            speechRecognitionTranscript.update { transcript }
            runCurrent()
            expected = expected.copy(speechRecognitionTranscript = transcript)
            assertEquals(expected, states.last())

            latestTelemetryLog.update { null }
            runCurrent()
            assertEquals(expected.copy(latestTelemetryLog = null), states.last())
        }

    private fun createTelemetryLog() =
        TelemetryLog(
            id = 1L,
            createdAt = 1_000L,
            simulator = Simulator.AceWindows,
            readoutItemKey = AceWindowsReadoutItemKey.RemainingFuel.Root,
            narratedText = "コーナー進入注意",
            narrationOutcome = NarrationOutcome.INTERRUPTED,
            narrationSource = NarrationSource.AUTO,
            telemetryJson = "{}",
        )
}
