package kurou.kodriver.feature.manualreadoutlist

import androidx.compose.ui.test.junit4.v2.createComposeRule
import org.junit.Rule
import org.junit.Test
import kotlin.test.assertEquals

class SpeechRecognitionPlatformTest {
    @get:Rule
    val rule = createComposeRule()

    @Test
    fun `権限の要求は何も行わず結果も通知しない`() {
        val results = mutableListOf<Boolean>()
        rule.setContent { rememberRecordAudioPermissionRequester { results += it }() }

        rule.runOnIdle { assertEquals(emptyList(), results) }
    }
}
