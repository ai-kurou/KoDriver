package kurou.kodriver.domain.model

import kotlin.test.Test
import kotlin.test.assertEquals

class Gt7Ps5TyreTemperatureReadoutTextTest {
    @Test
    fun `過熱の既定文言は温度プレースホルダーを含み温度に置換する`() {
        val template = GT7_PS5_TYRE_TEMPERATURE_OVERHEAT_READOUT_TEXT_DEFAULT
        assertEquals("タイヤ過熱 {celsius}度", template)
        assertEquals("タイヤ過熱 30度", formatGt7Ps5TyreTemperatureReadoutText(template, 30))
        assertEquals(emptyList(), findUnknownGt7Ps5TyreTemperatureReadoutPlaceholders(template))
    }

    @Test
    fun `プレースホルダーがなければ文言を維持する`() {
        assertEquals("タイヤ警告", formatGt7Ps5TyreTemperatureReadoutText("タイヤ警告", 1))
    }

    @Test
    fun `温度の境界値を単純に置換する`() {
        listOf(Int.MIN_VALUE, -1, 0, 1, Int.MAX_VALUE).forEach { celsius ->
            assertEquals(
                "温度$celsius℃",
                formatGt7Ps5TyreTemperatureReadoutText("温度{celsius}℃", celsius),
            )
        }
    }

    @Test
    fun `複数のプレースホルダーを置換して未知のトークンを維持する`() {
        assertEquals(
            "2・2・{unknown}",
            formatGt7Ps5TyreTemperatureReadoutText("{celsius}・{celsius}・{unknown}", 2),
        )
    }

    @Test
    fun `未知のトークンを出現順で重複なく返す`() {
        assertEquals(
            listOf("{lap}", "{x}", "{}"),
            findUnknownGt7Ps5TyreTemperatureReadoutPlaceholders("{celsius}{lap}%{x}{lap}{}"),
        )
    }

    @Test
    fun `既知のトークンと閉じていない括弧は未知のトークンにならない`() {
        listOf("", "{celsius}%", "{celsius}{celsius}", "{", "}", "{lap", "文言").forEach {
            assertEquals(emptyList(), findUnknownGt7Ps5TyreTemperatureReadoutPlaceholders(it))
        }
        assertEquals(listOf("{x}"), findUnknownGt7Ps5TyreTemperatureReadoutPlaceholders("{{x}}"))
    }
}
