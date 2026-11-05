@file:Suppress("FunctionNaming")

package kurou.kodriver.core.speechrecognitiondata.speech

import kotlin.test.Test
import kotlin.test.assertEquals

class SpeechRecognitionLevelTest {
    @Test
    fun `下限以下は無音、上限以上は最大に丸める`() {
        assertEquals(0f, normalizeRecognitionRmsDb(RECOGNITION_RMS_DB_MIN))
        assertEquals(0f, normalizeRecognitionRmsDb(-50f))
        assertEquals(1f, normalizeRecognitionRmsDb(RECOGNITION_RMS_DB_MAX))
        assertEquals(1f, normalizeRecognitionRmsDb(40f))
    }

    @Test
    fun `範囲内は直線的に正規化する`() {
        assertEquals(0.5f, normalizeRecognitionRmsDb(4f))
        assertEquals(0.25f, normalizeRecognitionRmsDb(1f))
    }

    @Test
    fun `NaNは無音として扱う`() {
        assertEquals(0f, normalizeRecognitionRmsDb(Float.NaN))
    }
}
