package kurou.kodriver.core.acewindowsdata.preferences

import kotlinx.coroutines.test.runTest
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import kotlin.test.Test
import kotlin.test.assertEquals

class AceWindowsVehicleApproachPreferencesSerializerTest {
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
