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
class VoiceSpeedPreferencesSerializerTest {
    @Test
    fun `デフォルト値は1_0`() {
        assertEquals(1.0f, VoiceSpeedPreferencesSerializer.defaultValue.voiceSpeed)
    }

    @Test
    fun `正常なバイト列をデシリアライズできる`() =
        runTest {
            val original = VoiceSpeedPreferences(voiceSpeed = 1.5f)
            val bytes = ProtoBuf.encodeToByteArray(VoiceSpeedPreferences.serializer(), original)

            val result = VoiceSpeedPreferencesSerializer.readFrom(ByteArrayInputStream(bytes))

            assertEquals(original, result)
        }

    @Test
    fun `不正なバイト列はCorruptionExceptionをスローする`() =
        runTest {
            val invalidBytes = byteArrayOf(0xFF.toByte(), 0xFE.toByte(), 0x00, 0x01)

            assertFailsWith<CorruptionException> {
                VoiceSpeedPreferencesSerializer.readFrom(ByteArrayInputStream(invalidBytes))
            }
        }

    @Test
    fun `writeToしたバイト列をreadFromで復元できる`() =
        runTest {
            val original = VoiceSpeedPreferences(voiceSpeed = 0.75f)
            val output = ByteArrayOutputStream()
            VoiceSpeedPreferencesSerializer.writeTo(original, output)

            val result = VoiceSpeedPreferencesSerializer.readFrom(ByteArrayInputStream(output.toByteArray()))

            assertEquals(original, result)
        }
}
