package kurou.kodriver.data.preferences

import androidx.datastore.core.CorruptionException
import kotlinx.coroutines.test.runTest
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class LmuWindowsVehicleClassBrakeTemperaturePreferencesSerializerTest {
    @Test
    fun `デフォルト値は highThresholdCelsiusByVehicleClass が空Map`() {
        assertEquals(
            LmuWindowsVehicleClassBrakeTemperaturePreferences(highThresholdCelsiusByVehicleClass = emptyMap()),
            LmuWindowsVehicleClassBrakeTemperaturePreferencesSerializer.defaultValue,
        )
    }

    @Test
    fun `書き込んだ値を読み出せる`() =
        runTest {
            val original =
                LmuWindowsVehicleClassBrakeTemperaturePreferences(
                    highThresholdCelsiusByVehicleClass = mapOf("GTE" to 750),
                    readoutText = "温度{celsius}℃",
                    selectedVehicleClassKey = "GTE",
                )
            val output = ByteArrayOutputStream()
            LmuWindowsVehicleClassBrakeTemperaturePreferencesSerializer.writeTo(original, output)

            val restored =
                LmuWindowsVehicleClassBrakeTemperaturePreferencesSerializer.readFrom(
                    ByteArrayInputStream(output.toByteArray()),
                )

            assertEquals(original, restored)
        }

    @Test
    fun `不正なバイト列で CorruptionException が発生する`() =
        runTest {
            val corrupt = ByteArrayInputStream(byteArrayOf(0x00, 0xFF.toByte(), 0x42))

            assertFailsWith<CorruptionException> {
                LmuWindowsVehicleClassBrakeTemperaturePreferencesSerializer.readFrom(corrupt)
            }
        }

    @Test
    fun `保存した空欄は復元後も既定文言へ戻らない`() =
        runTest {
            val original = LmuWindowsVehicleClassBrakeTemperaturePreferences(readoutText = "")
            val output = ByteArrayOutputStream()
            LmuWindowsVehicleClassBrakeTemperaturePreferencesSerializer.writeTo(original, output)
            assertEquals(
                original,
                LmuWindowsVehicleClassBrakeTemperaturePreferencesSerializer.readFrom(
                    ByteArrayInputStream(output.toByteArray()),
                ),
            )
        }
}
