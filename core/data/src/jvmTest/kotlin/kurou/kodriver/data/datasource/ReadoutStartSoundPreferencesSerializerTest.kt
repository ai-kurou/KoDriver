package kurou.kodriver.data.datasource

import androidx.datastore.core.CorruptionException
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.protobuf.ProtoBuf
import kurou.kodriver.data.model.ReadoutStartSoundPreferences
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

@OptIn(ExperimentalSerializationApi::class)
class ReadoutStartSoundPreferencesSerializerTest {

    @Test
    fun `正常なバイト列をデシリアライズできる`() = runTest {
        val original = ReadoutStartSoundPreferences(type = "formula_radio")
        val bytes = ProtoBuf.encodeToByteArray(ReadoutStartSoundPreferences.serializer(), original)

        val result = ReadoutStartSoundPreferencesSerializer.readFrom(ByteArrayInputStream(bytes))

        assertEquals(original, result)
    }

    @Test
    fun `不正なバイト列はCorruptionExceptionをスローする`() = runTest {
        val invalidBytes = byteArrayOf(0xFF.toByte(), 0xFE.toByte(), 0x00, 0x01)

        assertFailsWith<CorruptionException> {
            ReadoutStartSoundPreferencesSerializer.readFrom(ByteArrayInputStream(invalidBytes))
        }
    }

    @Test
    fun `writeToしたバイト列をreadFromで復元できる`() = runTest {
        val original = ReadoutStartSoundPreferences(type = "electronic_noise")
        val output = ByteArrayOutputStream()
        ReadoutStartSoundPreferencesSerializer.writeTo(original, output)

        val result = ReadoutStartSoundPreferencesSerializer.readFrom(ByteArrayInputStream(output.toByteArray()))

        assertEquals(original, result)
    }

    @Test
    fun `デフォルト値は electronic_noise`() {
        assertEquals("electronic_noise", ReadoutStartSoundPreferencesSerializer.defaultValue.type)
    }
}
