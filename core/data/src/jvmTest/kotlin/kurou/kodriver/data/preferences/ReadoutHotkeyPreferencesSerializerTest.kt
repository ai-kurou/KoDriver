package kurou.kodriver.data.preferences

import androidx.datastore.core.CorruptionException
import kotlinx.coroutines.test.runTest
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class ReadoutHotkeyPreferencesSerializerTest {
    @Test
    fun `デフォルト値はF1`() {
        assertEquals(
            ReadoutHotkeyPreferences(virtualKeyCode = 0x70),
            ReadoutHotkeyPreferencesSerializer.defaultValue,
        )
    }

    @Test
    fun `書き込んだ値を読み出せる`() =
        runTest {
            val original = ReadoutHotkeyPreferences(virtualKeyCode = 0x87)
            val output = ByteArrayOutputStream()
            ReadoutHotkeyPreferencesSerializer.writeTo(original, output)

            val restored = ReadoutHotkeyPreferencesSerializer.readFrom(ByteArrayInputStream(output.toByteArray()))

            assertEquals(original, restored)
        }

    @Test
    fun `不正なバイト列で CorruptionException が発生する`() =
        runTest {
            val corrupt = ByteArrayInputStream(byteArrayOf(0x00, 0xFF.toByte(), 0x42))

            assertFailsWith<CorruptionException> {
                ReadoutHotkeyPreferencesSerializer.readFrom(corrupt)
            }
        }
}
