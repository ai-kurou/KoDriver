package kurou.kodriver.domain.model

import kotlin.test.Test
import kotlin.test.assertEquals

class LmuWindowsBrakeWearReadoutTextTest {
    @Test
    fun `既定文言の閾値を置換する`() {
        assertEquals(
            "ブレーキ残量30%以下",
            formatLmuWindowsBrakeWearReadoutText(
                LMU_WINDOWS_BRAKE_WEAR_READOUT_TEXT_DEFAULT,
                30,
            ),
        )
    }

    @Test
    fun `プレースホルダーがなければ文言を維持する`() {
        assertEquals("ブレーキ警告", formatLmuWindowsBrakeWearReadoutText("ブレーキ警告", 1))
    }

    @Test
    fun `閾値の境界値を単純に置換する`() {
        listOf(Int.MIN_VALUE, -1, 0, 1, Int.MAX_VALUE).forEach { percent ->
            assertEquals(
                "残り$percent%",
                formatLmuWindowsBrakeWearReadoutText("残り{percent}%", percent),
            )
        }
    }

    @Test
    fun `複数のプレースホルダーを置換して未知のトークンを維持する`() {
        assertEquals(
            "2・2・{unknown}",
            formatLmuWindowsBrakeWearReadoutText("{percent}・{percent}・{unknown}", 2),
        )
    }

    @Test
    fun `未知のトークンを出現順で重複なく返す`() {
        assertEquals(
            listOf("{lap}", "{x}", "{}"),
            findUnknownLmuWindowsBrakeWearReadoutPlaceholders("{percent}{lap}%{x}{lap}{}"),
        )
    }

    @Test
    fun `既知のトークンと閉じていない括弧は未知のトークンにならない`() {
        listOf("", "{percent}%", "{percent}{percent}", "{", "}", "{lap", "文言").forEach {
            assertEquals(emptyList(), findUnknownLmuWindowsBrakeWearReadoutPlaceholders(it))
        }
        assertEquals(listOf("{x}"), findUnknownLmuWindowsBrakeWearReadoutPlaceholders("{{x}}"))
    }
}
