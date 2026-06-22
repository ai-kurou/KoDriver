package kurou.kodriver.core.gt7ps5data.datasource

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
class ConsoleAddressSerializerTest {

    @Test
    fun `正常なバイト列をデシリアライズできる`() = runTest {
        val original = ConsoleAddressPreferences(address = "192.168.1.100")
        val bytes = ProtoBuf.encodeToByteArray(ConsoleAddressPreferences.serializer(), original)

        val result = ConsoleAddressSerializer.readFrom(ByteArrayInputStream(bytes))

        assertEquals(original, result)
    }

    @Test
    fun `不正なバイト列はCorruptionExceptionをスローする`() = runTest {
        val invalidBytes = byteArrayOf(0xFF.toByte(), 0xFE.toByte(), 0x00, 0x01)

        assertFailsWith<CorruptionException> {
            ConsoleAddressSerializer.readFrom(ByteArrayInputStream(invalidBytes))
        }
    }

    @Test
    fun `writeToしたバイト列をreadFromで復元できる`() = runTest {
        val original = ConsoleAddressPreferences(address = "10.0.0.1")
        val output = ByteArrayOutputStream()
        ConsoleAddressSerializer.writeTo(original, output)

        val result = ConsoleAddressSerializer.readFrom(ByteArrayInputStream(output.toByteArray()))

        assertEquals(original, result)
    }

    @Test
    fun `デフォルト値はaddressが空文字列である`() {
        assertEquals(ConsoleAddressPreferences(address = ""), ConsoleAddressSerializer.defaultValue)
    }
}
