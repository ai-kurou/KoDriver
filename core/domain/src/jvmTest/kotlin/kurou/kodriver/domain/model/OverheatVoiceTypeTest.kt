package kurou.kodriver.domain.model

import kotlin.test.Test
import kotlin.test.assertEquals

class OverheatVoiceTypeTest {
    @Test
    fun `fromId は gp2_gp2 を GP2_GP2 に変換する`() {
        assertEquals(OverheatVoiceType.GP2_GP2, OverheatVoiceType.fromId("gp2_gp2"))
    }

    @Test
    fun `fromId は standard を STANDARD に変換する`() {
        assertEquals(OverheatVoiceType.STANDARD, OverheatVoiceType.fromId("standard"))
    }

    @Test
    fun `fromId は未知の ID のとき GP2_GP2 を返す`() {
        assertEquals(OverheatVoiceType.GP2_GP2, OverheatVoiceType.fromId("unknown"))
    }
}
