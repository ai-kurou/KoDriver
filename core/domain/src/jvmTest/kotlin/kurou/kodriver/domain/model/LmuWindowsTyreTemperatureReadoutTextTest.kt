package kurou.kodriver.domain.model

import kotlin.test.Test
import kotlin.test.assertEquals

class LmuWindowsTyreTemperatureReadoutTextTest {
    @Test
    fun `過熱と低温の既定文言は温度プレースホルダーを含み温度に置換する`() {
        listOf(
            LMU_WINDOWS_TYRE_TEMPERATURE_OVERHEAT_READOUT_TEXT_DEFAULT to "タイヤ過熱",
            LMU_WINDOWS_TYRE_TEMPERATURE_COLD_READOUT_TEXT_DEFAULT to "タイヤ低温",
        ).forEach { (template, warning) ->
            assertEquals("$warning {celsius}度", template)
            assertEquals("$warning 30度", formatLmuWindowsTyreTemperatureReadoutText(template, 30))
            assertEquals(emptyList(), findUnknownLmuWindowsTyreTemperatureReadoutPlaceholders(template))
        }
    }

    @Test
    fun `プレースホルダーがなければ文言を維持する`() {
        assertEquals("タイヤ警告", formatLmuWindowsTyreTemperatureReadoutText("タイヤ警告", 1))
    }

    @Test
    fun `温度の境界値を単純に置換する`() {
        listOf(Int.MIN_VALUE, -1, 0, 1, Int.MAX_VALUE).forEach { celsius ->
            assertEquals(
                "温度$celsius℃",
                formatLmuWindowsTyreTemperatureReadoutText("温度{celsius}℃", celsius),
            )
        }
    }

    @Test
    fun `複数のプレースホルダーを置換して未知のトークンを維持する`() {
        assertEquals(
            "2・2・{unknown}",
            formatLmuWindowsTyreTemperatureReadoutText("{celsius}・{celsius}・{unknown}", 2),
        )
    }

    @Test
    fun `未知のトークンを出現順で重複なく返す`() {
        assertEquals(
            listOf("{lap}", "{x}", "{}"),
            findUnknownLmuWindowsTyreTemperatureReadoutPlaceholders("{celsius}{lap}%{x}{lap}{}"),
        )
    }

    @Test
    fun `既知のトークンと閉じていない括弧は未知のトークンにならない`() {
        listOf("", "{celsius}%", "{celsius}{celsius}", "{", "}", "{lap", "文言").forEach {
            assertEquals(emptyList(), findUnknownLmuWindowsTyreTemperatureReadoutPlaceholders(it))
        }
        assertEquals(listOf("{x}"), findUnknownLmuWindowsTyreTemperatureReadoutPlaceholders("{{x}}"))
    }
}
