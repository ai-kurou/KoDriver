@file:Suppress("FunctionNaming")

package kurou.kodriver.data.datasource

import androidx.datastore.core.CorruptionException
import kotlinx.coroutines.test.runTest
import kurou.kodriver.data.model.LmuWindowsTyreTemperaturePreferences
import kurou.kodriver.domain.model.LMU_WINDOWS_TYRE_TEMPERATURE_HIGH_THRESHOLD_CELSIUS_DEFAULT
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class LmuWindowsTyreTemperaturePreferencesSerializerTest {
    @Test
    fun `デフォルト値は highThresholdCelsius が 95、enabledStates が空Map`() {
        assertEquals(
            LmuWindowsTyreTemperaturePreferences(
                highThresholdCelsius = LMU_WINDOWS_TYRE_TEMPERATURE_HIGH_THRESHOLD_CELSIUS_DEFAULT,
                enabledStates = emptyMap(),
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
}
