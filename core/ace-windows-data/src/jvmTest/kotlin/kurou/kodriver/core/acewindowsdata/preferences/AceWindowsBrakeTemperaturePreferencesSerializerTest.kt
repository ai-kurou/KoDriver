package kurou.kodriver.core.acewindowsdata.preferences

import kotlinx.coroutines.test.runTest
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import kotlin.test.Test
import kotlin.test.assertEquals

class AceWindowsBrakeTemperaturePreferencesSerializerTest {
    @Test
    fun `カスタム文言と空文言を他の設定とともに往復できる`() =
        runTest {
            listOf("注意{celsius}度", "", " ").forEach { text ->
                val original = AceWindowsBrakeTemperaturePreferences(900, mapOf("overheat_warning" to false), text)
                val output = ByteArrayOutputStream()
                AceWindowsBrakeTemperaturePreferencesSerializer.writeTo(original, output)
                assertEquals(
                    original,
                    AceWindowsBrakeTemperaturePreferencesSerializer.readFrom(
                        ByteArrayInputStream(output.toByteArray()),
                    ),
                )
            }
        }
}
