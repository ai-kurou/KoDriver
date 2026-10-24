package kurou.kodriver.core.narrator

import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class StartSoundResourcesTest {
    @Test
    fun `フォーミュララジオの開始音リソースは読み取り可能なWAVである`() {
        runTest {
            assertValidWav(readStartSoundBytes(FORMULA_RADIO_START_SOUND_PATH))
        }
    }

    @Test
    fun `電子ノイズの開始音リソースは読み取り可能なWAVである`() {
        runTest {
            assertValidWav(readStartSoundBytes(ELECTRONIC_NOISE_START_SOUND_PATH))
        }
    }

    private fun assertValidWav(bytes: ByteArray) {
        assertTrue(bytes.isNotEmpty())
        val durationMs = assertNotNull(wavDurationMs(bytes))
        assertTrue(durationMs > 0L)
    }
}
