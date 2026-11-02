package kurou.kodriver.core.lmuwindowsdata.preferences

import kotlinx.coroutines.test.runTest
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import kotlin.test.Test
import kotlin.test.assertEquals

class LmuWindowsVehicleClassBrakeTemperaturePreferencesSerializerTest {
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
