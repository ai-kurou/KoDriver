package kurou.kodriver.domain.model

import kotlin.test.Test
import kotlin.test.assertEquals

class AceWindowsRemainingFuelReadoutTextTest {
    @Test
    fun `既定文言の残量パーセントを置換する`() {
        assertEquals(
            "燃料は残り30パーセント",
            formatAceWindowsRemainingFuelReadoutText(
                ACE_WINDOWS_REMAINING_FUEL_READOUT_TEXT_DEFAULT,
                30,
            ),
        )
    }

    @Test
    fun `プレースホルダーがなければ文言を維持する`() {
        assertEquals("燃料警告", formatAceWindowsRemainingFuelReadoutText("燃料警告", 1))
    }

    @Test
    fun `残量パーセントの境界値を単純に置換する`() {
        listOf(Int.MIN_VALUE, -1, 0, 1, Int.MAX_VALUE).forEach { percent ->
            assertEquals(
                "残り${percent}パーセント",
                formatAceWindowsRemainingFuelReadoutText("残り{percent}パーセント", percent),
            )
        }
    }

    @Test
    fun `複数のプレースホルダーを置換して未知のトークンを維持する`() {
        assertEquals(
            "2・2・{unknown}",
            formatAceWindowsRemainingFuelReadoutText("{percent}・{percent}・{unknown}", 2),
        )
    }

    @Test
    fun `未知のトークンを出現順で重複なく返す`() {
        assertEquals(
            listOf("{lap}", "{x}", "{}"),
            findUnknownAceWindowsRemainingFuelReadoutPlaceholders("{percent}{lap}パーセント{x}{lap}{}"),
        )
    }

    @Test
    fun `既知のトークンと閉じていない括弧は未知のトークンにならない`() {
        listOf("", "{percent}パーセント", "{percent}{percent}", "{", "}", "{lap", "文言").forEach {
            assertEquals(emptyList(), findUnknownAceWindowsRemainingFuelReadoutPlaceholders(it))
        }
        assertEquals(listOf("{x}"), findUnknownAceWindowsRemainingFuelReadoutPlaceholders("{{x}}"))
    }
}
