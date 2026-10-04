package kurou.kodriver.domain.model

import kotlin.test.Test
import kotlin.test.assertEquals

class Gt7Ps5RemainingFuelReadoutTextTest {
    @Test
    fun `既定文言の残量パーセントを置換する`() {
        assertEquals(
            "燃料は残り30パーセント",
            formatGt7Ps5RemainingFuelReadoutText(
                GT7_PS5_REMAINING_FUEL_READOUT_TEXT_DEFAULT,
                30,
            ),
        )
    }

    @Test
    fun `プレースホルダーがなければ文言を維持する`() {
        assertEquals("燃料警告", formatGt7Ps5RemainingFuelReadoutText("燃料警告", 1))
    }

    @Test
    fun `残量パーセントの境界値を単純に置換する`() {
        listOf(Int.MIN_VALUE, -1, 0, 1, Int.MAX_VALUE).forEach { percent ->
            assertEquals(
                "残り${percent}パーセント",
                formatGt7Ps5RemainingFuelReadoutText("残り{percent}パーセント", percent),
            )
        }
    }

    @Test
    fun `複数のプレースホルダーを置換して未知のトークンを維持する`() {
        assertEquals(
            "2・2・{unknown}",
            formatGt7Ps5RemainingFuelReadoutText("{percent}・{percent}・{unknown}", 2),
        )
    }

    @Test
    fun `未知のトークンを出現順で重複なく返す`() {
        assertEquals(
            listOf("{lap}", "{x}", "{}"),
            findUnknownGt7Ps5RemainingFuelReadoutPlaceholders("{percent}{lap}パーセント{x}{lap}{}"),
        )
    }

    @Test
    fun `既知のトークンと閉じていない括弧は未知のトークンにならない`() {
        listOf("", "{percent}パーセント", "{percent}{percent}", "{", "}", "{lap", "文言").forEach {
            assertEquals(emptyList(), findUnknownGt7Ps5RemainingFuelReadoutPlaceholders(it))
        }
        assertEquals(listOf("{x}"), findUnknownGt7Ps5RemainingFuelReadoutPlaceholders("{{x}}"))
    }
}
