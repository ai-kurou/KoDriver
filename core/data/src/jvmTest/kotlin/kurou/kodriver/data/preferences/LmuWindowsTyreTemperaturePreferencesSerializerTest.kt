package kurou.kodriver.data.preferences

import androidx.datastore.core.CorruptionException
import kotlinx.coroutines.test.runTest
import kurou.kodriver.domain.model.LMU_WINDOWS_TYRE_TEMPERATURE_COLD_READOUT_TEXT_DEFAULT
import kurou.kodriver.domain.model.LMU_WINDOWS_TYRE_TEMPERATURE_HIGH_THRESHOLD_CELSIUS_DEFAULT
import kurou.kodriver.domain.model.LMU_WINDOWS_TYRE_TEMPERATURE_OVERHEAT_READOUT_TEXT_DEFAULT
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class LmuWindowsTyreTemperaturePreferencesSerializerTest {
    @Test
    fun `デフォルト値は閾値95と空Mapと既定文言`() {
        assertEquals(
            LmuWindowsTyreTemperaturePreferences(
                highThresholdCelsius = LMU_WINDOWS_TYRE_TEMPERATURE_HIGH_THRESHOLD_CELSIUS_DEFAULT.value,
                enabledStates = emptyMap(),
                lowWarningPhases = emptyMap(),
                overheatReadoutText = LMU_WINDOWS_TYRE_TEMPERATURE_OVERHEAT_READOUT_TEXT_DEFAULT,
                coldReadoutText = LMU_WINDOWS_TYRE_TEMPERATURE_COLD_READOUT_TEXT_DEFAULT,
            ),
            LmuWindowsTyreTemperaturePreferencesSerializer.defaultValue,
        )
    }

    @Test
    fun `書き込んだ値を読み出せる`() =
        runTest {
            val original =
                LmuWindowsTyreTemperaturePreferences(
                    highThresholdCelsius = 110,
                    enabledStates = mapOf("lmu_windows_tyre_temperature_overheat_warning" to false),
                    lowWarningPhases = mapOf(5 to true),
                    overheatReadoutText = "タイヤが過熱しています",
                    coldReadoutText = "タイヤが冷えています",
                )
            val output = ByteArrayOutputStream()
            LmuWindowsTyreTemperaturePreferencesSerializer.writeTo(original, output)

            val restored =
                LmuWindowsTyreTemperaturePreferencesSerializer.readFrom(
                    ByteArrayInputStream(output.toByteArray()),
                )

            assertEquals(original, restored)
        }

    @Test
    fun `不正なバイト列で CorruptionException が発生する`() =
        runTest {
            val corrupt = ByteArrayInputStream(byteArrayOf(0x00, 0xFF.toByte(), 0x42))

            assertFailsWith<CorruptionException> {
                LmuWindowsTyreTemperaturePreferencesSerializer.readFrom(corrupt)
            }
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
                    highThresholdCelsius = 110,
                    enabledStates = mapOf("lmu_windows_tyre_temperature_overheat_warning" to false),
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
}
