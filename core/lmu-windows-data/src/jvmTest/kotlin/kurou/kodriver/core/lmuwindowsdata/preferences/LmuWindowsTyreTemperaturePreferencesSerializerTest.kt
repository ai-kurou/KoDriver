package kurou.kodriver.core.lmuwindowsdata.preferences

import kotlinx.coroutines.test.runTest
import kurou.kodriver.domain.lmuwindows.model.LMU_WINDOWS_TYRE_TEMPERATURE_COLD_READOUT_TEXT_DEFAULT
import kurou.kodriver.domain.lmuwindows.model.LMU_WINDOWS_TYRE_TEMPERATURE_OVERHEAT_READOUT_TEXT_DEFAULT
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import kotlin.test.Test
import kotlin.test.assertEquals

class LmuWindowsTyreTemperaturePreferencesSerializerTest {
    @Test
    fun `デフォルト値は空Mapと既定文言`() {
        assertEquals(
            LmuWindowsTyreTemperaturePreferences(
                enabledStates = emptyMap(),
                lowWarningPhases = emptyMap(),
                overheatReadoutText = LMU_WINDOWS_TYRE_TEMPERATURE_OVERHEAT_READOUT_TEXT_DEFAULT,
                coldReadoutText = LMU_WINDOWS_TYRE_TEMPERATURE_COLD_READOUT_TEXT_DEFAULT,
            ),
            LmuWindowsTyreTemperaturePreferencesSerializer.defaultValue,
        )
    }

    @Test
    fun `保存した空欄文言は読み出しても既定値に戻らない`() =
        runTest {
            val original = LmuWindowsTyreTemperaturePreferences(overheatReadoutText = "")
            val output = ByteArrayOutputStream()
            LmuWindowsTyreTemperaturePreferencesSerializer.writeTo(original, output)

            assertEquals(
                original,
                LmuWindowsTyreTemperaturePreferencesSerializer.readFrom(
                    ByteArrayInputStream(output.toByteArray()),
                ),
            )
        }

    @Test
    fun `保存した空欄の低温警告文言は読み出しても既定値に戻らない`() =
        runTest {
            val original =
                LmuWindowsTyreTemperaturePreferences(
                    enabledStates = mapOf("lmu_windows_auto_tyre_temperature_overheat_warning" to false),
                    lowWarningPhases = mapOf(5 to true),
                    overheatReadoutText = "タイヤが過熱しています",
                    coldReadoutText = "",
                )
            val output = ByteArrayOutputStream()
            LmuWindowsTyreTemperaturePreferencesSerializer.writeTo(original, output)

            assertEquals(
                original,
                LmuWindowsTyreTemperaturePreferencesSerializer.readFrom(
                    ByteArrayInputStream(output.toByteArray()),
                ),
            )
        }

    @Test
    fun `廃止したProtoNumber 1を含む既存データから現行設定を読み出せる`() =
        runTest {
            val original =
                LmuWindowsTyreTemperaturePreferences(
                    enabledStates = mapOf("lmu_windows_auto_tyre_temperature_overheat_warning" to false),
                    lowWarningPhases = mapOf(5 to true),
                    overheatReadoutText = "タイヤ過熱注意",
                    coldReadoutText = "",
                )
            val output = ByteArrayOutputStream()
            // 旧単一高温閾値（ProtoNumber 1、110℃）を現行設定の前に付加する。
            output.write(byteArrayOf(0x08, 0x6E))
            LmuWindowsTyreTemperaturePreferencesSerializer.writeTo(original, output)

            assertEquals(
                original,
                LmuWindowsTyreTemperaturePreferencesSerializer.readFrom(
                    ByteArrayInputStream(output.toByteArray()),
                ),
            )
        }
}
