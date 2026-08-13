package kurou.kodriver.data.preferences

import androidx.datastore.core.CorruptionException
import kotlinx.coroutines.test.runTest
import kurou.kodriver.domain.model.GT7_PS5_TYRE_TEMPERATURE_HIGH_THRESHOLD_CELSIUS_DEFAULT
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class Gt7Ps5TyreTemperaturePreferencesSerializerTest {
    @Test
    fun `デフォルト値は95度`() {
        assertEquals(
            Gt7Ps5TyreTemperaturePreferences(
                highThresholdCelsius = GT7_PS5_TYRE_TEMPERATURE_HIGH_THRESHOLD_CELSIUS_DEFAULT,
            ),
            Gt7Ps5TyreTemperaturePreferencesSerializer.defaultValue,
        )
    }

    @Test
    fun `書き込んだ値を読み出せる`() =
        runTest {
            val original = Gt7Ps5TyreTemperaturePreferences(highThresholdCelsius = 100)
            val output = ByteArrayOutputStream()
            Gt7Ps5TyreTemperaturePreferencesSerializer.writeTo(original, output)

            val restored =
                Gt7Ps5TyreTemperaturePreferencesSerializer.readFrom(
                    ByteArrayInputStream(output.toByteArray()),
                )

            assertEquals(original, restored)
        }

    @Test
    fun `不正なバイト列で CorruptionException が発生する`() =
        runTest {
            val corrupt = ByteArrayInputStream(byteArrayOf(0x00, 0xFF.toByte(), 0x42))

            assertFailsWith<CorruptionException> {
                Gt7Ps5TyreTemperaturePreferencesSerializer.readFrom(corrupt)
            }
        }
}
