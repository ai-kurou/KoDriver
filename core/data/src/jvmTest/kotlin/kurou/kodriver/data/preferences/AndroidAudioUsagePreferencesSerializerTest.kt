package kurou.kodriver.data.preferences

import kotlinx.coroutines.test.runTest
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.protobuf.ProtoBuf
import kurou.kodriver.domain.model.ANDROID_AUDIO_USAGE_DEFAULT
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import kotlin.test.Test
import kotlin.test.assertEquals

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
