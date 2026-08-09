package kurou.kodriver.data.datasource

import androidx.datastore.core.CorruptionException
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.Serializable
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class PreferencesSerializerFactoryTest {
    @Serializable
    private data class FakePreferences(
        val value: Int = 0,
    )

    private val serializer =
        protoBufPreferencesSerializer(
            defaultValue = FakePreferences(),
            kSerializer = FakePreferences.serializer(),
        )

    @Test
    fun `defaultValueは指定した値を返す`() {
        assertEquals(FakePreferences(value = 0), serializer.defaultValue)
    }

    @Test
    fun `書き込んだ値を読み出せる`() =
        runTest {
            val original = FakePreferences(value = 42)
            val output = ByteArrayOutputStream()
            serializer.writeTo(original, output)

            val restored = serializer.readFrom(ByteArrayInputStream(output.toByteArray()))

            assertEquals(original, restored)
        }

    @Test
    fun `不正なバイト列で CorruptionException が発生する`() =
        runTest {
            val corrupt = ByteArrayInputStream(byteArrayOf(0x00, 0xFF.toByte(), 0x42))

            assertFailsWith<CorruptionException> {
                serializer.readFrom(corrupt)
            }
        }

    @Test
    fun `CorruptionExceptionのメッセージにkSerializerのserialNameが含まれる`() =
        runTest {
            val corrupt = ByteArrayInputStream(byteArrayOf(0x00, 0xFF.toByte(), 0x42))

            val exception =
                assertFailsWith<CorruptionException> {
                    serializer.readFrom(corrupt)
                }

            assertEquals(
                "Cannot read ${FakePreferences.serializer().descriptor.serialName}.",
                exception.message,
            )
        }
}
