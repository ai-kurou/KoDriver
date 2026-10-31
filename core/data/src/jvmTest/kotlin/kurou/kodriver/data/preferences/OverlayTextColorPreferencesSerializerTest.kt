package kurou.kodriver.data.preferences

import kotlinx.coroutines.test.runTest
import java.io.ByteArrayInputStream
import kotlin.test.Test
import kotlin.test.assertEquals

class OverlayTextColorPreferencesSerializerTest {
    @Test
    fun `既定値と空のProtoBufは既定の文字色を返す`() =
        runTest {
            assertEquals(0xFFFFC107L, OverlayTextColorPreferencesSerializer.defaultValue.argb)
            assertEquals(
                OverlayTextColorPreferences(),
                OverlayTextColorPreferencesSerializer.readFrom(ByteArrayInputStream(byteArrayOf())),
            )
        }
}
