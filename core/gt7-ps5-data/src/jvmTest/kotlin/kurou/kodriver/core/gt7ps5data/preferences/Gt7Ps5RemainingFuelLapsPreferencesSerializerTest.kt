package kurou.kodriver.core.gt7ps5data.preferences

import kotlinx.coroutines.test.runTest
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import kotlin.test.Test
import kotlin.test.assertEquals

class Gt7Ps5RemainingFuelLapsPreferencesSerializerTest {
    @Test
    fun `空欄文言は読み出しても既定値に戻らない`() =
        runTest {
            val original = Gt7Ps5RemainingFuelLapsPreferences(readoutText = "", emptyReadoutText = "")
            val output = ByteArrayOutputStream()
            Gt7Ps5RemainingFuelLapsPreferencesSerializer.writeTo(original, output)

            assertEquals(
                original,
                Gt7Ps5RemainingFuelLapsPreferencesSerializer.readFrom(ByteArrayInputStream(output.toByteArray())),
            )
        }
}
