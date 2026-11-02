package kurou.kodriver.core.lmuwindowsdata.preferences

import kotlinx.coroutines.test.runTest
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import kotlin.test.Test
import kotlin.test.assertEquals

class LmuWindowsRemainingFuelPreferencesSerializerTest {
    @Test
    fun `保存した空欄文言は読み出しても既定値に戻らない`() =
        runTest {
            val original = LmuWindowsRemainingFuelPreferences(readoutText = "")
            val output = ByteArrayOutputStream()
            LmuWindowsRemainingFuelPreferencesSerializer.writeTo(original, output)

            assertEquals(
                original,
                LmuWindowsRemainingFuelPreferencesSerializer.readFrom(ByteArrayInputStream(output.toByteArray())),
            )
        }
}
