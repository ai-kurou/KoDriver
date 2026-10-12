package kurou.kodriver.domain.model

import kotlin.test.Test
import kotlin.test.assertEquals

class LmuWindowsTyreWearReadoutTextTest {
    @Test
    fun `既定文言の閾値を置換する`() {
        assertEquals(
            "タイヤ残存率30%以下",
            formatLmuWindowsTyreWearReadoutText(
                LMU_WINDOWS_TYRE_WEAR_READOUT_TEXT_DEFAULT,
                30,
            ),
        )
    }

    @Test
    fun `プレースホルダーがなければ文言を維持する`() {
        assertEquals("タイヤ警告", formatLmuWindowsTyreWearReadoutText("タイヤ警告", 1))
    }

    @Test
    fun `閾値の境界値を単純に置換する`() {
        listOf(Int.MIN_VALUE, -1, 0, 1, Int.MAX_VALUE).forEach { percentage ->
            assertEquals(
                "残り$percentage%",
                formatLmuWindowsTyreWearReadoutText("残り{percent}%", percentage),
            )
        }
    }

    @Test
    fun `複数のプレースホルダーを置換して未知のトークンを維持する`() {
        assertEquals(
            "2・2・{unknown}",
            formatLmuWindowsTyreWearReadoutText("{percent}・{percent}・{unknown}", 2),
        )
    }

    @Test
    fun `未知のトークンを出現順で重複なく返す`() {
        assertEquals(
            listOf("{lap}", "{x}", "{}"),
            findUnknownLmuWindowsTyreWearReadoutPlaceholders("{percent}{lap}%{x}{lap}{}"),
        )
    }

    @Test
    fun `既知のトークンと閉じていない括弧は未知のトークンにならない`() {
        listOf("", "{percent}%", "{percent}{percent}", "{", "}", "{lap", "文言").forEach {
            assertEquals(emptyList(), findUnknownLmuWindowsTyreWearReadoutPlaceholders(it))
        }
        assertEquals(listOf("{x}"), findUnknownLmuWindowsTyreWearReadoutPlaceholders("{{x}}"))
    }
}
