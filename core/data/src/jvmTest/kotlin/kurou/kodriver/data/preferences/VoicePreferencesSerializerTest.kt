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
class VoicePreferencesSerializerTest {
    @Test
    fun `正常なバイト列をデシリアライズできる`() =
        runTest {
            val original = VoicePreferences(voiceId = "Microsoft Haruka Desktop")
            val bytes = ProtoBuf.encodeToByteArray(VoicePreferences.serializer(), original)

            val result = VoicePreferencesSerializer.readFrom(ByteArrayInputStream(bytes))

            assertEquals(original, result)
        }

    @Test
    fun `不正なバイト列はCorruptionExceptionをスローする`() =
        runTest {
            val invalidBytes = byteArrayOf(0xFF.toByte(), 0xFE.toByte(), 0x00, 0x01)

            assertFailsWith<CorruptionException> {
                VoicePreferencesSerializer.readFrom(ByteArrayInputStream(invalidBytes))
            }
        }

    @Test
    fun `writeToしたバイト列をreadFromで復元できる`() =
        runTest {
            val original = VoicePreferences(voiceId = "ja-jp-x-jab-local")
            val output = ByteArrayOutputStream()
            VoicePreferencesSerializer.writeTo(original, output)

            val result = VoicePreferencesSerializer.readFrom(ByteArrayInputStream(output.toByteArray()))

            assertEquals(original, result)
        }
}
