package kurou.kodriver.core.lmuwindowsdata.preferences

import kotlinx.coroutines.test.runTest
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import kotlin.test.Test
import kotlin.test.assertEquals

class LmuWindowsVehicleClassBrakeWearPreferencesSerializerTest {
    @Test
    fun `デフォルト値は lowThresholdPercentByVehicleClass が空Map`() {
        assertEquals(
            LmuWindowsVehicleClassBrakeWearPreferences(lowThresholdPercentByVehicleClass = emptyMap()),
            LmuWindowsVehicleClassBrakeWearPreferencesSerializer.defaultValue,
        )
    }

    @Test
    fun `保存した空欄は復元後も既定文言へ戻らない`() =
        runTest {
            val original = LmuWindowsVehicleClassBrakeWearPreferences(readoutText = "")
            val output = ByteArrayOutputStream()
            LmuWindowsVehicleClassBrakeWearPreferencesSerializer.writeTo(original, output)
            assertEquals(
                original,
                LmuWindowsVehicleClassBrakeWearPreferencesSerializer.readFrom(
                    ByteArrayInputStream(output.toByteArray()),
                ),
            )
        }
}
