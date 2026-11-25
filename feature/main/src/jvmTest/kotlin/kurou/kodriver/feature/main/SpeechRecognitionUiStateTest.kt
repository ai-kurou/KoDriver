package kurou.kodriver.feature.main

import kotlin.test.Test
import kotlin.test.assertEquals

class SpeechRecognitionUiStateTest {
    @Test
    fun `ウェイクワード待ちではカットの閾値をそのまま使う`() {
        val state = SpeechRecognitionUiState(status = SpeechRecognitionStatus.Listening, cutThreshold = 0.3f)

        assertEquals(0.3f, state.effectiveCutThreshold)
    }

    @Test
    fun `コマンド待ちでコマンド受付中もカットする設定がオフなら閾値を使わない`() {
        val state =
            SpeechRecognitionUiState(
                status = SpeechRecognitionStatus.CommandWaiting,
                cutThreshold = 0.3f,
                cutInCommand = false,
            )

        assertEquals(0f, state.effectiveCutThreshold)
    }

    @Test
    fun `コマンド待ちでコマンド受付中もカットする設定がオンなら閾値をそのまま使う`() {
        val state =
            SpeechRecognitionUiState(
                status = SpeechRecognitionStatus.CommandWaiting,
                cutThreshold = 0.3f,
                cutInCommand = true,
            )

        assertEquals(0.3f, state.effectiveCutThreshold)
    }
}
