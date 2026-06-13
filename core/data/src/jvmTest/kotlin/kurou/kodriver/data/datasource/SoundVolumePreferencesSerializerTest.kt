package kurou.kodriver.data.datasource

import androidx.datastore.core.CorruptionException
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.protobuf.ProtoBuf
import kurou.kodriver.data.model.SoundVolumePreferences
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

@OptIn(ExperimentalSerializationApi::class)
class SoundVolumePreferencesSerializerTest {

    @Test
    fun `正常なバイト列をデシリアライズできる`() = runTest {
        val original = SoundVolumePreferences(volume = 75)
        val bytes = ProtoBuf.encodeToByteArray(SoundVolumePreferences.serializer(), original)

        val result = SoundVolumePreferencesSerializer.readFrom(ByteArrayInputStream(bytes))

        assertEquals(original, result)
    }

    @Test
    fun `不正なバイト列はCorruptionExceptionをスローする`() = runTest {
        val invalidBytes = byteArrayOf(0xFF.toByte(), 0xFE.toByte(), 0x00, 0x01)

        assertFailsWith<CorruptionException> {
            SoundVolumePreferencesSerializer.readFrom(ByteArrayInputStream(invalidBytes))
        }
    }

    @Test
    fun `writeToしたバイト列をreadFromで復元できる`() = runTest {
        val original = SoundVolumePreferences(volume = 42)
        val output = ByteArrayOutputStream()
        SoundVolumePreferencesSerializer.writeTo(original, output)

        val result = SoundVolumePreferencesSerializer.readFrom(ByteArrayInputStream(output.toByteArray()))

        assertEquals(original, result)
    }
}
