package kurou.kodriver.data.preferences

import androidx.datastore.core.CorruptionException
import kotlinx.coroutines.test.runTest
import kurou.kodriver.domain.model.ACE_WINDOWS_TYRE_TEMPERATURE_HIGH_THRESHOLD_CELSIUS_DEFAULT
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class AceWindowsTyreTemperaturePreferencesSerializerTest {
    @Test
    fun `デフォルト値は90度`() {
        assertEquals(
            AceWindowsTyreTemperaturePreferences(
                highThresholdCelsius = ACE_WINDOWS_TYRE_TEMPERATURE_HIGH_THRESHOLD_CELSIUS_DEFAULT.value,
            ),
            AceWindowsTyreTemperaturePreferencesSerializer.defaultValue,
        )
    }

    @Test
    fun `書き込んだ値を読み出せる`() =
        runTest {
            val original = AceWindowsTyreTemperaturePreferences(highThresholdCelsius = 100)
            val output = ByteArrayOutputStream()
            AceWindowsTyreTemperaturePreferencesSerializer.writeTo(original, output)

            val restored =
                AceWindowsTyreTemperaturePreferencesSerializer.readFrom(
                    ByteArrayInputStream(output.toByteArray()),
                )

            assertEquals(original, restored)
        }

    @Test
    fun `不正なバイト列で CorruptionException が発生する`() =
        runTest {
            val corrupt = ByteArrayInputStream(byteArrayOf(0x00, 0xFF.toByte(), 0x42))

            assertFailsWith<CorruptionException> {
                AceWindowsTyreTemperaturePreferencesSerializer.readFrom(corrupt)
            }
        }

    @Test
    fun `カスタム文言と空文言を他の設定とともに往復できる`() =
        runTest {
            listOf("注意{celsius}度", "", " ").forEach { text ->
                val original = AceWindowsTyreTemperaturePreferences(105, mapOf("overheat_warning" to false), text)
                val output = ByteArrayOutputStream()
                AceWindowsTyreTemperaturePreferencesSerializer.writeTo(original, output)
                assertEquals(
                    original,
                    AceWindowsTyreTemperaturePreferencesSerializer.readFrom(ByteArrayInputStream(output.toByteArray())),
                )
            }
        }
}
