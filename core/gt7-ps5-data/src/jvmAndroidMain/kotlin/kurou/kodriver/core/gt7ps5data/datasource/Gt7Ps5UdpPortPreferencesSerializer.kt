package kurou.kodriver.core.gt7ps5data.datasource

import androidx.datastore.core.CorruptionException
import androidx.datastore.core.Serializer
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerializationException
import kotlinx.serialization.protobuf.ProtoBuf
import kurou.kodriver.core.gt7ps5data.model.Gt7Ps5UdpPortPreferences
import kurou.kodriver.domain.captureError
import java.io.InputStream
import java.io.OutputStream

@OptIn(ExperimentalSerializationApi::class)
internal object Gt7Ps5UdpPortPreferencesSerializer : Serializer<Gt7Ps5UdpPortPreferences> {
    override val defaultValue: Gt7Ps5UdpPortPreferences = Gt7Ps5UdpPortPreferences()

    override suspend fun readFrom(input: InputStream): Gt7Ps5UdpPortPreferences =
        try {
            ProtoBuf.decodeFromByteArray(Gt7Ps5UdpPortPreferences.serializer(), input.readBytes())
        } catch (e: SerializationException) {
            captureError(e)
            throw CorruptionException("Cannot read Gt7Ps5UdpPortPreferences.", e)
        }

    override suspend fun writeTo(
        t: Gt7Ps5UdpPortPreferences,
        output: OutputStream,
    ) {
        output.write(ProtoBuf.encodeToByteArray(Gt7Ps5UdpPortPreferences.serializer(), t))
    }
}
