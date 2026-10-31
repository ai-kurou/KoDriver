package kurou.kodriver.core.acewindowsdata.preferences

import kotlinx.coroutines.test.runTest
import kurou.kodriver.domain.acewindows.model.ACE_WINDOWS_VEHICLE_APPROACH_THRESHOLD_METERS_DEFAULT
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import kotlin.test.Test
import kotlin.test.assertEquals

class AceWindowsVehicleApproachPreferencesSerializerTest {
    @Test
    fun `デフォルト値は各Defaults定数と一致する`() {
        assertEquals(
            AceWindowsVehicleApproachPreferences(
                thresholdMeters = ACE_WINDOWS_VEHICLE_APPROACH_THRESHOLD_METERS_DEFAULT,
            ),
            AceWindowsVehicleApproachPreferencesSerializer.defaultValue,
        )
    }

    @Test
    fun `空文言もそのまま往復できる`() =
        runTest {
            val original = AceWindowsVehicleApproachPreferences(readoutText = "")
            val output = ByteArrayOutputStream()
            AceWindowsVehicleApproachPreferencesSerializer.writeTo(original, output)
            assertEquals(
                original,
                AceWindowsVehicleApproachPreferencesSerializer.readFrom(ByteArrayInputStream(output.toByteArray())),
            )
        }
}
