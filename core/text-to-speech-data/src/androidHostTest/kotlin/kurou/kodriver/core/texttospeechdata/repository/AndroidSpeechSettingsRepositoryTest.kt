@file:Suppress("FunctionNaming")

package kurou.kodriver.core.texttospeechdata.repository

import kotlin.test.Test
import kotlin.test.assertEquals

class AndroidSpeechSettingsRepositoryTest {
    @Test
    fun `Windowsの音声設定を開く処理は何もしない`() {
        assertEquals(Unit, AndroidSpeechSettingsRepository().openWindowsSpeechSettings())
    }
}
