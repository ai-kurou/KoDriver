@file:Suppress("FunctionNaming")

package kurou.kodriver.data.speech

import android.speech.SpeechRecognizer
import kurou.kodriver.domain.model.SpeechRecognitionFailureReason
import kotlin.test.Test
import kotlin.test.assertEquals

class SpeechRecognitionErrorPolicyTest {
    @Test
    fun `権限不足は権限未許可として停止する`() {
        assertEquals(
            SpeechRecognitionErrorAction.Stop(SpeechRecognitionFailureReason.PermissionDenied),
            speechRecognitionErrorAction(SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS),
        )
    }

    @Test
    fun `言語が未対応または利用不可の場合は利用不可として停止する`() {
        val expected = SpeechRecognitionErrorAction.Stop(SpeechRecognitionFailureReason.Unavailable)

        assertEquals(expected, speechRecognitionErrorAction(SpeechRecognizer.ERROR_LANGUAGE_NOT_SUPPORTED))
        assertEquals(expected, speechRecognitionErrorAction(SpeechRecognizer.ERROR_LANGUAGE_UNAVAILABLE))
    }

    @Test
    fun `無音と認識不能は短い待ち時間で再開する`() {
        val expected = SpeechRecognitionErrorAction.Restart(SPEECH_RECOGNITION_QUICK_RESTART_DELAY_MS)

        assertEquals(expected, speechRecognitionErrorAction(SpeechRecognizer.ERROR_NO_MATCH))
        assertEquals(expected, speechRecognitionErrorAction(SpeechRecognizer.ERROR_SPEECH_TIMEOUT))
    }

    @Test
    fun `その他のエラーは一時的な失敗として扱う`() {
        val expected = SpeechRecognitionErrorAction.Retry

        listOf(
            SpeechRecognizer.ERROR_NETWORK_TIMEOUT,
            SpeechRecognizer.ERROR_NETWORK,
            SpeechRecognizer.ERROR_AUDIO,
            SpeechRecognizer.ERROR_SERVER,
            SpeechRecognizer.ERROR_CLIENT,
            SpeechRecognizer.ERROR_RECOGNIZER_BUSY,
            SpeechRecognizer.ERROR_SERVER_DISCONNECTED,
            SpeechRecognizer.ERROR_TOO_MANY_REQUESTS,
            SpeechRecognizer.ERROR_CANNOT_CHECK_SUPPORT,
        ).forEach { assertEquals(expected, speechRecognitionErrorAction(it), "errorCode=$it") }
    }

    @Test
    fun `再開待ち時間は連続失敗のたびに倍にして上限で止める`() {
        assertEquals(1_000L, speechRecognitionRetryDelayMs(1))
        assertEquals(2_000L, speechRecognitionRetryDelayMs(2))
        assertEquals(4_000L, speechRecognitionRetryDelayMs(3))
        assertEquals(16_000L, speechRecognitionRetryDelayMs(5))
        assertEquals(SPEECH_RECOGNITION_RETRY_DELAY_MAX_MS, speechRecognitionRetryDelayMs(6))
        assertEquals(SPEECH_RECOGNITION_RETRY_DELAY_MAX_MS, speechRecognitionRetryDelayMs(1_000))
    }

    @Test
    fun `連続失敗が0回以下でも1回目の待ち時間にする`() {
        assertEquals(SPEECH_RECOGNITION_RETRY_DELAY_MS, speechRecognitionRetryDelayMs(0))
        assertEquals(SPEECH_RECOGNITION_RETRY_DELAY_MS, speechRecognitionRetryDelayMs(-1))
    }
}
