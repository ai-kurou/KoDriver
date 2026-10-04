package kurou.kodriver.domain.model

import kotlin.test.Test
import kotlin.test.assertEquals

class LmuWindowsBrakeTemperatureReadoutTextTest {
    @Test
    fun `既定文言の閾値を置換する`() {
        assertEquals(
            "ブレーキ温度30℃以上",
            formatLmuWindowsBrakeTemperatureReadoutText(
                LMU_WINDOWS_BRAKE_TEMPERATURE_READOUT_TEXT_DEFAULT,
                30,
            ),
        )
    }

    @Test
    fun `プレースホルダーがなければ文言を維持する`() {
        assertEquals("ブレーキ警告", formatLmuWindowsBrakeTemperatureReadoutText("ブレーキ警告", 1))
    }

    @Test
    fun `閾値の境界値を単純に置換する`() {
        listOf(Int.MIN_VALUE, -1, 0, 1, Int.MAX_VALUE).forEach { celsius ->
            assertEquals(
                "残り$celsius%",
                formatLmuWindowsBrakeTemperatureReadoutText("残り{celsius}%", celsius),
            )
        }
    }

    @Test
    fun `複数のプレースホルダーを置換して未知のトークンを維持する`() {
        assertEquals(
            "2・2・{unknown}",
            formatLmuWindowsBrakeTemperatureReadoutText("{celsius}・{celsius}・{unknown}", 2),
        )
    }

    @Test
    fun `未知のトークンを出現順で重複なく返す`() {
        assertEquals(
            listOf("{lap}", "{x}", "{}"),
            findUnknownLmuWindowsBrakeTemperatureReadoutPlaceholders("{celsius}{lap}%{x}{lap}{}"),
        )
    }

    @Test
    fun `既知のトークンと閉じていない括弧は未知のトークンにならない`() {
        listOf("", "{celsius}%", "{celsius}{celsius}", "{", "}", "{lap", "文言").forEach {
            assertEquals(emptyList(), findUnknownLmuWindowsBrakeTemperatureReadoutPlaceholders(it))
        }
        assertEquals(listOf("{x}"), findUnknownLmuWindowsBrakeTemperatureReadoutPlaceholders("{{x}}"))
    }
}
