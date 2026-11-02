package kurou.kodriver.core.lmuwindowsdata.preferences

import kotlinx.coroutines.test.runTest
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import kotlin.test.Test
import kotlin.test.assertEquals

class LmuWindowsTyreWearPreferencesSerializerTest {
    @Test
    fun `保存した空欄文言は読み出しても既定値に戻らない`() =
        runTest {
            val original = LmuWindowsTyreWearPreferences(readoutText = "")
            val output = ByteArrayOutputStream()
            LmuWindowsTyreWearPreferencesSerializer.writeTo(original, output)

            assertEquals(
                original,
                LmuWindowsTyreWearPreferencesSerializer.readFrom(
                    ByteArrayInputStream(output.toByteArray()),
                ),
            )
        }
}
