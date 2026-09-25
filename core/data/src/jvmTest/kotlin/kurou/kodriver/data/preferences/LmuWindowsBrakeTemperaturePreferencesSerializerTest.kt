@file:Suppress("FunctionNaming")

package kurou.kodriver.data.preferences

import androidx.datastore.core.CorruptionException
import kotlinx.coroutines.test.runTest
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class LmuWindowsBrakeTemperaturePreferencesSerializerTest {
    @Test
    fun `デフォルト値は highThresholdCelsius が 700`() {
        assertEquals(
            LmuWindowsBrakeTemperaturePreferences(highThresholdCelsius = 700),
            LmuWindowsBrakeTemperaturePreferencesSerializer.defaultValue,
        )
    }

    @Test
    fun `書き込んだ値を読み出せる`() =
        runTest {
            val original = LmuWindowsBrakeTemperaturePreferences(highThresholdCelsius = 600)
            val output = ByteArrayOutputStream()
            LmuWindowsBrakeTemperaturePreferencesSerializer.writeTo(original, output)

            val restored =
                LmuWindowsBrakeTemperaturePreferencesSerializer.readFrom(
                    ByteArrayInputStream(output.toByteArray()),
                )

            assertEquals(original, restored)
        }

    @Test
    fun `不正なバイト列で CorruptionException が発生する`() =
        runTest {
            val corrupt = ByteArrayInputStream(byteArrayOf(0x00, 0xFF.toByte(), 0x42))

            assertFailsWith<CorruptionException> {
                LmuWindowsBrakeTemperaturePreferencesSerializer.readFrom(corrupt)
            }
        }
}
