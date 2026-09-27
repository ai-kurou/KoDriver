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
class FeedbackCooldownSerializerTest {
    @Test
    fun `正常なバイト列をデシリアライズできる`() =
        runTest {
            val original = FeedbackCooldownPreferences(lastSentAtEpochMillis = 1_700_000_000_000L)
            val bytes = ProtoBuf.encodeToByteArray(FeedbackCooldownPreferences.serializer(), original)

            val result = FeedbackCooldownSerializer.readFrom(ByteArrayInputStream(bytes))

            assertEquals(original, result)
        }

    @Test
    fun `不正なバイト列はCorruptionExceptionをスローする`() =
        runTest {
            val invalidBytes = byteArrayOf(0xFF.toByte(), 0xFE.toByte(), 0x00, 0x01)

            assertFailsWith<CorruptionException> {
                FeedbackCooldownSerializer.readFrom(ByteArrayInputStream(invalidBytes))
            }
        }

    @Test
    fun `writeToしたバイト列をreadFromで復元できる`() =
        runTest {
            val original = FeedbackCooldownPreferences(lastSentAtEpochMillis = 1_600_000_000_000L)
            val output = ByteArrayOutputStream()
            FeedbackCooldownSerializer.writeTo(original, output)

            val result = FeedbackCooldownSerializer.readFrom(ByteArrayInputStream(output.toByteArray()))

            assertEquals(original, result)
        }

    @Test
    fun `デフォルト値はlastSentAtEpochMillisが0である`() {
        assertEquals(FeedbackCooldownPreferences(lastSentAtEpochMillis = 0L), FeedbackCooldownSerializer.defaultValue)
    }
}
