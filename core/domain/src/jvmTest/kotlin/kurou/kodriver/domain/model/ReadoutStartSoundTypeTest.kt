package kurou.kodriver.domain.model

import kotlin.test.Test
import kotlin.test.assertEquals

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
    fun `fromId は未知の ID のとき FORMULA_RADIO を返す`() {
        assertEquals(
            ReadoutStartSoundType.FORMULA_RADIO,
            ReadoutStartSoundType.fromId("unknown"),
        )
    }
}
