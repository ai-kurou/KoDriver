package kurou.kodriver.core.acewindowsdata.preferences

import kotlinx.coroutines.test.runTest
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import kotlin.test.Test
import kotlin.test.assertEquals

class AceWindowsRemainingFuelPreferencesSerializerTest {
    @Test
    fun `空欄文言は読み出しても既定値に戻らない`() =
        runTest {
            val original = AceWindowsRemainingFuelPreferences(readoutText = "")
            val output = ByteArrayOutputStream()
            AceWindowsRemainingFuelPreferencesSerializer.writeTo(original, output)

            assertEquals(
                original,
                AceWindowsRemainingFuelPreferencesSerializer.readFrom(ByteArrayInputStream(output.toByteArray())),
            )
        }
}
