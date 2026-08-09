package kurou.kodriver.core.model

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class MyBestLapVoiceTypeTest {
    @Test
    fun `fromId は formal を FORMAL に変換する`() {
        assertEquals(MyBestLapVoiceType.FORMAL, MyBestLapVoiceType.fromId("formal"))
    }

    @Test
    fun `fromId は casual を CASUAL に変換する`() {
        assertEquals(MyBestLapVoiceType.CASUAL, MyBestLapVoiceType.fromId("casual"))
    }

    @Test
    fun `fromId は未知の ID のとき null を返す`() {
        assertNull(MyBestLapVoiceType.fromId("unknown"))
    }
}
