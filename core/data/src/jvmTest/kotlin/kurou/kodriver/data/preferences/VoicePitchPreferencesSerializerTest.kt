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
class VoicePitchPreferencesSerializerTest {
    @Test
    fun `デフォルト値は1_0`() {
        assertEquals(1.0f, VoicePitchPreferencesSerializer.defaultValue.voicePitch)
    }

    @Test
    fun `正常なバイト列をデシリアライズできる`() =
        runTest {
            val original = VoicePitchPreferences(voicePitch = 1.5f)
            val bytes = ProtoBuf.encodeToByteArray(VoicePitchPreferences.serializer(), original)

            val result = VoicePitchPreferencesSerializer.readFrom(ByteArrayInputStream(bytes))

            assertEquals(original, result)
        }

    @Test
    fun `不正なバイト列はCorruptionExceptionをスローする`() =
        runTest {
            val invalidBytes = byteArrayOf(0xFF.toByte(), 0xFE.toByte(), 0x00, 0x01)

            assertFailsWith<CorruptionException> {
                VoicePitchPreferencesSerializer.readFrom(ByteArrayInputStream(invalidBytes))
            }
        }

    @Test
    fun `writeToしたバイト列をreadFromで復元できる`() =
        runTest {
            val original = VoicePitchPreferences(voicePitch = 0.75f)
            val output = ByteArrayOutputStream()
            VoicePitchPreferencesSerializer.writeTo(original, output)

            val result = VoicePitchPreferencesSerializer.readFrom(ByteArrayInputStream(output.toByteArray()))

            assertEquals(original, result)
        }
}
