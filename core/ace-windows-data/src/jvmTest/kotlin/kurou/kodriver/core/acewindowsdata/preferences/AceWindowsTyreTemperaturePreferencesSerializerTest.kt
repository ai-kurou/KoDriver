package kurou.kodriver.core.acewindowsdata.preferences

import kotlinx.coroutines.test.runTest
import kurou.kodriver.domain.acewindows.model.ACE_WINDOWS_TYRE_TEMPERATURE_HIGH_THRESHOLD_CELSIUS_DEFAULT
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import kotlin.test.Test
import kotlin.test.assertEquals

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
