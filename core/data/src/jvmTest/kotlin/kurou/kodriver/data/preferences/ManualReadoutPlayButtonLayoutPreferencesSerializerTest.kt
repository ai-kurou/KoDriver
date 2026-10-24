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
class ManualReadoutPlayButtonLayoutPreferencesSerializerTest {
    @Test
    fun `正常なバイト列をデシリアライズできる`() =
        runTest {
            val original = ManualReadoutPlayButtonLayoutPreferences(enabled = false)
            val bytes = ProtoBuf.encodeToByteArray(ManualReadoutPlayButtonLayoutPreferences.serializer(), original)

            val result = ManualReadoutPlayButtonLayoutPreferencesSerializer.readFrom(ByteArrayInputStream(bytes))

            assertEquals(original, result)
        }

    @Test
    fun `不正なバイト列はCorruptionExceptionをスローする`() =
        runTest {
            val invalidBytes = byteArrayOf(0xFF.toByte(), 0xFE.toByte(), 0x00, 0x01)

            assertFailsWith<CorruptionException> {
                ManualReadoutPlayButtonLayoutPreferencesSerializer.readFrom(ByteArrayInputStream(invalidBytes))
            }
        }

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
