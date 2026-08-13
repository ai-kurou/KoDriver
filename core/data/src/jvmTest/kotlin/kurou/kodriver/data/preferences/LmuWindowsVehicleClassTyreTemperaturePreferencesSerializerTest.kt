@file:Suppress("FunctionNaming")

package kurou.kodriver.data.preferences

import androidx.datastore.core.CorruptionException
import kotlinx.coroutines.test.runTest
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class LmuWindowsVehicleClassTyreTemperaturePreferencesSerializerTest {
    @Test
    fun `デフォルト値は highThresholdCelsiusByVehicleClass が空Map`() {
        assertEquals(
            LmuWindowsVehicleClassTyreTemperaturePreferences(highThresholdCelsiusByVehicleClass = emptyMap()),
            LmuWindowsVehicleClassTyreTemperaturePreferencesSerializer.defaultValue,
        )
    }

    @Test
    fun `書き込んだ値を読み出せる`() =
        runTest {
            val original =
                LmuWindowsVehicleClassTyreTemperaturePreferences(
                    highThresholdCelsiusByVehicleClass = mapOf("GTE" to 110),
                )
            val output = ByteArrayOutputStream()
            LmuWindowsVehicleClassTyreTemperaturePreferencesSerializer.writeTo(original, output)

            val restored =
                LmuWindowsVehicleClassTyreTemperaturePreferencesSerializer.readFrom(
                    ByteArrayInputStream(output.toByteArray()),
                )

            assertEquals(original, restored)
        }

    @Test
    fun `不正なバイト列で CorruptionException が発生する`() =
        runTest {
            val corrupt = ByteArrayInputStream(byteArrayOf(0x00, 0xFF.toByte(), 0x42))

            assertFailsWith<CorruptionException> {
                LmuWindowsVehicleClassTyreTemperaturePreferencesSerializer.readFrom(corrupt)
            }
        }
}
