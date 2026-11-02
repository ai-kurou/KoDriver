package kurou.kodriver.core.acewindowsdata.preferences

import kotlinx.coroutines.test.runTest
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import kotlin.test.Test
import kotlin.test.assertEquals

class MyBestLapPreferencesSerializerTest {
    @Test
    fun `空白文言もそのまま復元される`() =
        runTest {
            val original =
                MyBestLapPreferences(aceWindowsReadoutText = " ")
            val output = ByteArrayOutputStream()
            MyBestLapPreferencesSerializer.writeTo(original, output)
            assertEquals(original, MyBestLapPreferencesSerializer.readFrom(ByteArrayInputStream(output.toByteArray())))
        }
}
