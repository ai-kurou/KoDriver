package kurou.kodriver.data.datasource

import androidx.datastore.core.CorruptionException
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.protobuf.ProtoBuf
import kurou.kodriver.data.model.ReadoutPreferences
import kurou.kodriver.data.model.SimulatorReadoutState
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.io.InputStream
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

@OptIn(ExperimentalSerializationApi::class)
class ReadoutPreferencesSerializerTest {

    @Test
    fun `正常なバイト列をデシリアライズできる`() = runTest {
        val original = ReadoutPreferences(
            simulatorStates = mapOf(
                "lmu" to SimulatorReadoutState(enabledStates = mapOf("車両接近" to true)),
            ),
        )
        val bytes = ProtoBuf.encodeToByteArray(ReadoutPreferences.serializer(), original)

        val result = ReadoutPreferencesSerializer.readFrom(ByteArrayInputStream(bytes))

        assertEquals(original, result)
    }

    @Test
    fun `不正なバイト列はCorruptionExceptionをスローする`() = runTest {
        val invalidBytes = byteArrayOf(0xFF.toByte(), 0xFE.toByte(), 0x00, 0x01)

        assertFailsWith<CorruptionException> {
            ReadoutPreferencesSerializer.readFrom(ByteArrayInputStream(invalidBytes))
        }
    }

    @Test
    fun `IOExceptionはCorruptionExceptionをスローする`() = runTest {
        val throwingStream = object : InputStream() {
            override fun read(): Int = throw IOException("read error")
        }

        assertFailsWith<CorruptionException> {
            ReadoutPreferencesSerializer.readFrom(throwingStream)
        }
    }

    @Test
    fun `writeToしたバイト列をreadFromで復元できる`() = runTest {
        val original = ReadoutPreferences(
            simulatorStates = mapOf(
                "lmu" to SimulatorReadoutState(enabledStates = mapOf("車両接近" to true, "残りラップ数" to false)),
            ),
        )
        val output = ByteArrayOutputStream()
        ReadoutPreferencesSerializer.writeTo(original, output)

        val result = ReadoutPreferencesSerializer.readFrom(ByteArrayInputStream(output.toByteArray()))

        assertEquals(original, result)
    }
}
