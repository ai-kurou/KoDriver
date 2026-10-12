package kurou.kodriver.data.preferences

import androidx.datastore.core.CorruptionException
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.protobuf.ProtoBuf
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

@OptIn(ExperimentalSerializationApi::class)
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

    @Test
    fun `正常なバイト列をデシリアライズできる`() =
        runTest {
            val original = OverlayTextColorPreferences(argb = 0x80123456L)
            val bytes = ProtoBuf.encodeToByteArray(OverlayTextColorPreferences.serializer(), original)

            val result = OverlayTextColorPreferencesSerializer.readFrom(ByteArrayInputStream(bytes))

            assertEquals(original, result)
        }

    @Test
    fun `不正なバイト列はCorruptionExceptionをスローする`() =
        runTest {
            val invalidBytes = byteArrayOf(0xFF.toByte(), 0xFE.toByte(), 0x00, 0x01)

            assertFailsWith<CorruptionException> {
                OverlayTextColorPreferencesSerializer.readFrom(ByteArrayInputStream(invalidBytes))
            }
        }

    @Test
    fun `writeToしたバイト列をreadFromで復元できる`() =
        runTest {
            val original = OverlayTextColorPreferences(argb = 0x80123456L)
            val output = ByteArrayOutputStream()
            OverlayTextColorPreferencesSerializer.writeTo(original, output)

            val result =
                OverlayTextColorPreferencesSerializer.readFrom(ByteArrayInputStream(output.toByteArray()))

            assertEquals(original, result)
        }
}
