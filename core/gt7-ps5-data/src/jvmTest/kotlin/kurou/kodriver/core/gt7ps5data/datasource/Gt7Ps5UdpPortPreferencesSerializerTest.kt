package kurou.kodriver.core.gt7ps5data.datasource

import androidx.datastore.core.CorruptionException
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.protobuf.ProtoBuf
import kurou.kodriver.core.gt7ps5data.model.Gt7Ps5UdpPortPreferences
import kurou.kodriver.domain.model.GT7_PS5_UDP_PORT_ALTERNATE
import kurou.kodriver.domain.model.GT7_PS5_UDP_PORT_DEFAULT
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

@OptIn(ExperimentalSerializationApi::class)
class Gt7Ps5UdpPortPreferencesSerializerTest {

    @Test
    fun `正常なバイト列をデシリアライズできる`() =
        runTest {
        val original = Gt7Ps5UdpPortPreferences(port = GT7_PS5_UDP_PORT_ALTERNATE)
        val bytes = ProtoBuf.encodeToByteArray(Gt7Ps5UdpPortPreferences.serializer(), original)

        val result = Gt7Ps5UdpPortPreferencesSerializer.readFrom(ByteArrayInputStream(bytes))

        assertEquals(original, result)
    }

    @Test
    fun `不正なバイト列はCorruptionExceptionをスローする`() =
        runTest {
        val invalidBytes = byteArrayOf(0xFF.toByte(), 0xFE.toByte(), 0x00, 0x01)

        assertFailsWith<CorruptionException> {
            Gt7Ps5UdpPortPreferencesSerializer.readFrom(ByteArrayInputStream(invalidBytes))
        }
    }

    @Test
    fun `writeToしたバイト列をreadFromで復元できる`() =
        runTest {
        val original = Gt7Ps5UdpPortPreferences(port = GT7_PS5_UDP_PORT_DEFAULT)
        val output = ByteArrayOutputStream()
        Gt7Ps5UdpPortPreferencesSerializer.writeTo(original, output)

        val result = Gt7Ps5UdpPortPreferencesSerializer.readFrom(ByteArrayInputStream(output.toByteArray()))

        assertEquals(original, result)
    }
}
