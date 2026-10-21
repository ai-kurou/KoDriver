package kurou.kodriver.data.preferences

import androidx.datastore.core.CorruptionException
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.protobuf.ProtoBuf
import kurou.kodriver.domain.model.ANDROID_AUDIO_USAGE_DEFAULT
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

@OptIn(ExperimentalSerializationApi::class)
class AndroidAudioUsagePreferencesSerializerTest {
    @Test
    fun `readFromで保存値を復元できる`() =
        runTest {
            val original = AndroidAudioUsagePreferences(usage = "alarm")
            val result =
                AndroidAudioUsagePreferencesSerializer.readFrom(
                    ByteArrayInputStream(
                        ProtoBuf.encodeToByteArray(
                            AndroidAudioUsagePreferences.serializer(),
                            original,
                        ),
                    ),
                )

            assertEquals(original, result)
        }

    @Test
    fun `不正なバイト列はCorruptionExceptionになる`() =
        runTest {
            val exception =
                assertFailsWith<CorruptionException> {
                    AndroidAudioUsagePreferencesSerializer.readFrom(ByteArrayInputStream(byteArrayOf(1, 2, 3)))
                }

            assertEquals(
                "Cannot read ${AndroidAudioUsagePreferences.serializer().descriptor.serialName}.",
                exception.message,
            )
        }

    @Test
    fun `writeToで保存値を書き込める`() =
        runTest {
            val original = AndroidAudioUsagePreferences(usage = "notification")
            val output = ByteArrayOutputStream()

            AndroidAudioUsagePreferencesSerializer.writeTo(original, output)

            assertEquals(
                original,
                AndroidAudioUsagePreferencesSerializer.readFrom(ByteArrayInputStream(output.toByteArray())),
            )
        }

    @Test
    fun `デフォルト値はメディア`() {
        assertEquals(ANDROID_AUDIO_USAGE_DEFAULT.id, AndroidAudioUsagePreferencesSerializer.defaultValue.usage)
    }
}
