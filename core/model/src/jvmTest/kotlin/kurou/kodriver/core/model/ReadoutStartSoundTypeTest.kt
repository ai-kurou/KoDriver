package kurou.kodriver.core.model

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class ReadoutStartSoundTypeTest {
    @Test
    fun `fromId は一致する種別を返す`() {
        assertEquals(
            ReadoutStartSoundType.ELECTRONIC_NOISE,
            ReadoutStartSoundType.fromId("electronic_noise"),
        )
        assertEquals(
            ReadoutStartSoundType.FORMULA_RADIO,
            ReadoutStartSoundType.fromId("formula_radio"),
        )
    }

    @Test
    fun `fromId は未知の ID のとき null を返す`() {
        assertNull(ReadoutStartSoundType.fromId("unknown"))
    }
}
