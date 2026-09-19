package kurou.kodriver.data.preferences

import androidx.datastore.core.CorruptionException
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.protobuf.ProtoBuf
import kurou.kodriver.domain.model.OVERLAY_TEXT_SIZE_DEFAULT
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

@OptIn(ExperimentalSerializationApi::class)
class OverlayTextSizePreferencesSerializerTest {
    @Test
    fun `readFromでOverlayTextSizePreferencesを復元できる`() =
        runTest {
            val original = OverlayTextSizePreferences(size = "large")
            val bytes = ProtoBuf.encodeToByteArray(OverlayTextSizePreferences.serializer(), original)

            val result = OverlayTextSizePreferencesSerializer.readFrom(ByteArrayInputStream(bytes))

            assertEquals(original, result)
        }

    @Test
    fun `不正なバイト列はCorruptionExceptionになる`() =
        runTest {
            val exception =
                assertFailsWith<CorruptionException> {
                    OverlayTextSizePreferencesSerializer.readFrom(ByteArrayInputStream(byteArrayOf(1, 2, 3)))
                }

            assertEquals(
                "Cannot read ${OverlayTextSizePreferences.serializer().descriptor.serialName}.",
                exception.message,
            )
        }

    @Test
    fun `writeToでOverlayTextSizePreferencesを書き込める`() =
        runTest {
            val original = OverlayTextSizePreferences(size = "small")
            val output = ByteArrayOutputStream()

            OverlayTextSizePreferencesSerializer.writeTo(original, output)
            val result = OverlayTextSizePreferencesSerializer.readFrom(ByteArrayInputStream(output.toByteArray()))

            assertEquals(original, result)
        }

    @Test
    fun `デフォルト値はmedium`() {
        assertEquals(OVERLAY_TEXT_SIZE_DEFAULT.id, OverlayTextSizePreferencesSerializer.defaultValue.size)
    }
}
