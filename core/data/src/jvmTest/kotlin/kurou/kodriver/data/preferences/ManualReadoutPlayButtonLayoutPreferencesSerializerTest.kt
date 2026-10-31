package kurou.kodriver.data.preferences

import kotlinx.coroutines.test.runTest
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import kotlin.test.Test
import kotlin.test.assertEquals

class ManualReadoutPlayButtonLayoutPreferencesSerializerTest {
    @Test
    fun `writeToしたバイト列をreadFromで復元できる`() =
        runTest {
            val original = ManualReadoutPlayButtonLayoutPreferences(enabled = false)
            val output = ByteArrayOutputStream()
            ManualReadoutPlayButtonLayoutPreferencesSerializer.writeTo(original, output)

            val input = ByteArrayInputStream(output.toByteArray())
            val result = ManualReadoutPlayButtonLayoutPreferencesSerializer.readFrom(input)

            assertEquals(original, result)
        }
}
