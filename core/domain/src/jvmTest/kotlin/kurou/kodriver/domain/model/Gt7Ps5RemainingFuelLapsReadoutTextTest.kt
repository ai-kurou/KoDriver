package kurou.kodriver.domain.model

import kotlin.test.Test
import kotlin.test.assertEquals

class Gt7Ps5RemainingFuelLapsReadoutTextTest {
    @Test
    fun `既定文言の周回数を置換する`() {
        assertEquals(
            "燃料は残り約30周",
            formatGt7Ps5RemainingFuelLapsReadoutText(
                GT7_PS5_REMAINING_FUEL_LAPS_READOUT_TEXT_DEFAULT,
                30,
            ),
        )
    }

    @Test
    fun `プレースホルダーがなければ文言を維持する`() {
        assertEquals("燃料警告", formatGt7Ps5RemainingFuelLapsReadoutText("燃料警告", 1))
    }

    @Test
    fun `周回数の境界値を単純に置換する`() {
        listOf(Int.MIN_VALUE, -1, 0, 1, Int.MAX_VALUE).forEach { laps ->
            assertEquals(
                "残り${laps}周",
                formatGt7Ps5RemainingFuelLapsReadoutText("残り{laps}周", laps),
            )
        }
    }

    @Test
    fun `複数のプレースホルダーを置換して未知のトークンを維持する`() {
        assertEquals(
            "2・2・{unknown}",
            formatGt7Ps5RemainingFuelLapsReadoutText("{laps}・{laps}・{unknown}", 2),
        )
    }

    @Test
    fun `未知のトークンを出現順で重複なく返す`() {
        assertEquals(
            listOf("{lap}", "{x}", "{}"),
            findUnknownGt7Ps5RemainingFuelLapsReadoutPlaceholders("{laps}{lap}周{x}{lap}{}"),
        )
    }

    @Test
    fun `既知のトークンと閉じていない括弧は未知のトークンにならない`() {
        listOf("", "{laps}周", "{laps}{laps}", "{", "}", "{lap", "文言").forEach {
            assertEquals(emptyList(), findUnknownGt7Ps5RemainingFuelLapsReadoutPlaceholders(it))
        }
        assertEquals(listOf("{x}"), findUnknownGt7Ps5RemainingFuelLapsReadoutPlaceholders("{{x}}"))
    }
}
