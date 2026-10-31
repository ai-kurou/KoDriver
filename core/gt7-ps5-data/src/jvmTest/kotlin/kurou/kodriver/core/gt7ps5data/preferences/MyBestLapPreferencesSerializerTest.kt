package kurou.kodriver.core.gt7ps5data.preferences

import kotlinx.coroutines.test.runTest
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import kotlin.test.Test
import kotlin.test.assertEquals

class MyBestLapPreferencesSerializerTest {
    @Test
    fun `デフォルト値は初期設定を返す`() {
        assertEquals(
            MyBestLapPreferences(),
            MyBestLapPreferencesSerializer.defaultValue,
        )
    }

    @Test
    fun `空白文言もそのまま復元される`() =
        runTest {
            val original =
                MyBestLapPreferences(readoutText = " ")
            val output = ByteArrayOutputStream()
            MyBestLapPreferencesSerializer.writeTo(original, output)
            assertEquals(original, MyBestLapPreferencesSerializer.readFrom(ByteArrayInputStream(output.toByteArray())))
        }
}
