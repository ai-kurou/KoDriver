package kurou.kodriver.core.speechrecognitiondata.microphone

import kotlin.test.Test
import kotlin.test.assertNull

class JavaSoundAudioInputSourceTest {
    private val source = JavaSoundAudioInputSource()

    @Test
    fun `デバイス名の一覧を取得できる`() {
        // 実機のマイクの有無に依存するため、例外なく取得できて重複がないことだけを確認する
        val names = source.deviceNames()

        kotlin.test.assertEquals(names.distinct(), names)
    }

    @Test
    fun `存在しないデバイス名は開けずnullを返す`() {
        assertNull(source.open("存在しないマイク-6f1c0d7e"))
    }
}
