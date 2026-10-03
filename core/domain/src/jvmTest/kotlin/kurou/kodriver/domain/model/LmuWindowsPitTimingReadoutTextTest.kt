package kurou.kodriver.domain.model

import kotlin.test.Test
import kotlin.test.assertEquals

class LmuWindowsPitTimingReadoutTextTest {
    @Test
    fun `バーチャルエナジーの1以上の周回数は通常既定文言を置換する`() {
        listOf(1, 3).forEach { laps ->
            assertEquals(
                LMU_WINDOWS_PIT_TIMING_VIRTUAL_ENERGY_READOUT_TEXT_DEFAULT.replace("{laps}", laps.toString()),
                defaultLmuWindowsPitTimingReadoutText(PitTimingSource.VirtualEnergy, laps),
            )
        }
    }

    @Test
    fun `バーチャルエナジーの0以下の周回数は切迫既定文言を返す`() {
        listOf(0, -1).forEach { laps ->
            assertEquals(
                LMU_WINDOWS_PIT_TIMING_VIRTUAL_ENERGY_IMMINENT_READOUT_TEXT_DEFAULT,
                defaultLmuWindowsPitTimingReadoutText(PitTimingSource.VirtualEnergy, laps),
            )
        }
    }

    @Test
    fun `タイヤ摩耗の1以上の周回数は通常既定文言を置換する`() {
        listOf(1, 3).forEach { laps ->
            assertEquals(
                LMU_WINDOWS_PIT_TIMING_TYRE_WEAR_READOUT_TEXT_DEFAULT.replace("{laps}", laps.toString()),
                defaultLmuWindowsPitTimingReadoutText(PitTimingSource.TyreWear, laps),
            )
        }
    }

    @Test
    fun `タイヤ摩耗の0以下の周回数は切迫既定文言を返す`() {
        listOf(0, -1).forEach { laps ->
            assertEquals(
                LMU_WINDOWS_PIT_TIMING_TYRE_WEAR_IMMINENT_READOUT_TEXT_DEFAULT,
                defaultLmuWindowsPitTimingReadoutText(PitTimingSource.TyreWear, laps),
            )
        }
    }

    @Test
    fun `プレースホルダーがなければ文言を維持する`() {
        assertEquals("ピットイン", formatLmuWindowsPitTimingReadoutText("ピットイン", 1))
    }

    @Test
    fun `周回数の境界値を単純に置換する`() {
        listOf(Int.MIN_VALUE, -1, 0, 1, Int.MAX_VALUE).forEach { laps ->
            assertEquals("残り${laps}周", formatLmuWindowsPitTimingReadoutText("残り{laps}周", laps))
        }
    }

    @Test
    fun `複数のプレースホルダーを置換して未知のトークンを維持する`() {
        assertEquals("2・2・{unknown}", formatLmuWindowsPitTimingReadoutText("{laps}・{laps}・{unknown}", 2))
    }

    @Test
    fun `未知のトークンを出現順で重複なく返す`() {
        assertEquals(
            listOf("{lap}", "{x}", "{}"),
            findUnknownLmuWindowsPitTimingReadoutPlaceholders("{laps}{lap}周{x}{lap}{}"),
        )
    }

    @Test
    fun `既知のトークンと閉じていない括弧は未知のトークンにならない`() {
        listOf("", "{laps}周", "{laps}{laps}", "{", "}", "{lap", "文言").forEach {
            assertEquals(emptyList(), findUnknownLmuWindowsPitTimingReadoutPlaceholders(it))
        }
        assertEquals(listOf("{x}"), findUnknownLmuWindowsPitTimingReadoutPlaceholders("{{x}}"))
    }
}
