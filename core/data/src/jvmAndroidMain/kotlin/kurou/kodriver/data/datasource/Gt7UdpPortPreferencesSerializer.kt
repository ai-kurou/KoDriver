package kurou.kodriver.data.datasource

import androidx.datastore.core.CorruptionException
import androidx.datastore.core.Serializer
import io.sentry.Sentry
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerializationException
import kotlinx.serialization.protobuf.ProtoBuf
import kurou.kodriver.data.model.Gt7UdpPortPreferences
import java.io.InputStream
import java.io.OutputStream

@OptIn(ExperimentalSerializationApi::class)
internal object Gt7UdpPortPreferencesSerializer : Serializer<Gt7UdpPortPreferences> {
    override val defaultValue: Gt7UdpPortPreferences = Gt7UdpPortPreferences()

    override suspend fun readFrom(input: InputStream): Gt7UdpPortPreferences =
        try {
            ProtoBuf.decodeFromByteArray(Gt7UdpPortPreferences.serializer(), input.readBytes())
        } catch (e: SerializationException) {
            Sentry.captureException(e)
            throw CorruptionException("Cannot read Gt7UdpPortPreferences.", e)
        }

    override suspend fun writeTo(t: Gt7UdpPortPreferences, output: OutputStream) {
        output.write(ProtoBuf.encodeToByteArray(Gt7UdpPortPreferences.serializer(), t))
    }
}
