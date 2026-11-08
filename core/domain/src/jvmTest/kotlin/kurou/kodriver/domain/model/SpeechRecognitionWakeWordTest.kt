package kurou.kodriver.domain.model

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class SpeechRecognitionWakeWordTest {
    @Test
    fun `カタカナのウェイクワードを含む候補を検出する`() {
        assertTrue(containsSpeechRecognitionWakeWord(listOf("コドライバー")))
    }

    @Test
    fun `ひらがなのウェイクワードを含む候補を検出する`() {
        assertTrue(containsSpeechRecognitionWakeWord(listOf("このどらいばー")))
    }

    @Test
    fun `コが「この」に分かれて認識されても検出する`() {
        assertTrue(containsSpeechRecognitionWakeWord(listOf("このドライバー")))
    }

    @Test
    fun `後続の語が付いていても検出する`() {
        assertTrue(containsSpeechRecognitionWakeWord(listOf("コドライバー燃料")))
    }

    @Test
    fun `2番目以降の候補だけに含まれていても検出する`() {
        assertTrue(containsSpeechRecognitionWakeWord(listOf("ドライ", "このドライバー")))
    }

    @Test
    fun `単独のドライバーは普通の発話にも出るため検出しない`() {
        assertFalse(containsSpeechRecognitionWakeWord(listOf("ドライバー")))
        assertFalse(containsSpeechRecognitionWakeWord(listOf("どらいばー", "ドライバーがピットイン")))
    }

    @Test
    fun `ウェイクワードを含まない候補は検出しない`() {
        assertFalse(containsSpeechRecognitionWakeWord(listOf("燃料", "タイヤ")))
    }

    @Test
    fun `候補が空なら検出しない`() {
        assertFalse(containsSpeechRecognitionWakeWord(emptyList()))
    }
}
