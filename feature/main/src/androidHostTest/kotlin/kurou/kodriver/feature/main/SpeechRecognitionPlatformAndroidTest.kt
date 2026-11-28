@file:Suppress("FunctionNaming")

package kurou.kodriver.feature.main

import org.junit.Test
import kotlin.test.assertTrue

class SpeechRecognitionPlatformAndroidTest {
    @Test
    fun `見えなくなると聞き取りを止める判定がAndroidではtrueになる`() {
        assertTrue(isSpeechRecognitionStoppedInBackgroundOnPlatform())
    }
}
