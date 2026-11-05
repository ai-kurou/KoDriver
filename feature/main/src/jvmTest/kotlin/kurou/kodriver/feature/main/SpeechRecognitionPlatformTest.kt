package kurou.kodriver.feature.main

import androidx.compose.ui.test.junit4.v2.createComposeRule
import org.junit.Rule
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class SpeechRecognitionPlatformTest {
    @get:Rule
    val rule = createComposeRule()

    @Test
    fun `権限の要求は何も行わず結果も通知しない`() {
        val results = mutableListOf<Boolean>()
        rule.setContent { rememberRecordAudioPermissionRequester { results += it }() }

        rule.runOnIdle { assertEquals(emptyList(), results) }
    }

    @Test
    fun `デスクトップでは音声認識を提供せずマイク入力の確認を提供する`() {
        assertFalse(isSpeechRecognitionSupportedOnPlatform())
        assertTrue(isMicrophoneInputSupportedOnPlatform())
    }
}
