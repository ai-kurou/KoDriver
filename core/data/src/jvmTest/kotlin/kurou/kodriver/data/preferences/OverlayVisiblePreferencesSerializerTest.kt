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
class OverlayVisiblePreferencesSerializerTest {
    @Test
    fun `正常なバイト列をデシリアライズできる`() =
        runTest {
            val original = OverlayVisiblePreferences(visible = false)
            val bytes = ProtoBuf.encodeToByteArray(OverlayVisiblePreferences.serializer(), original)

            val result = OverlayVisiblePreferencesSerializer.readFrom(ByteArrayInputStream(bytes))

            assertEquals(original, result)
        }

    @Test
    fun `不正なバイト列はCorruptionExceptionをスローする`() =
        runTest {
            val invalidBytes = byteArrayOf(0xFF.toByte(), 0xFE.toByte(), 0x00, 0x01)

            assertFailsWith<CorruptionException> {
                OverlayVisiblePreferencesSerializer.readFrom(ByteArrayInputStream(invalidBytes))
            }
        }

    @Test
    fun `writeToしたバイト列をreadFromで復元できる`() =
        runTest {
            val original = OverlayVisiblePreferences(visible = false)
            val output = ByteArrayOutputStream()
            OverlayVisiblePreferencesSerializer.writeTo(original, output)

            val result = OverlayVisiblePreferencesSerializer.readFrom(ByteArrayInputStream(output.toByteArray()))

            assertEquals(original, result)
        }
}
