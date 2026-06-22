package kurou.kodriver.core.gt7ps5data.datasource

import androidx.datastore.core.CorruptionException
import androidx.datastore.core.Serializer
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerializationException
import kotlinx.serialization.protobuf.ProtoBuf
import java.io.InputStream
import java.io.OutputStream

@OptIn(ExperimentalSerializationApi::class)
internal object Gt7Ps5AddressSerializer : Serializer<Gt7Ps5AddressPreferences> {
    override val defaultValue: Gt7Ps5AddressPreferences = Gt7Ps5AddressPreferences()

    override suspend fun readFrom(input: InputStream): Gt7Ps5AddressPreferences =
        try {
            ProtoBuf.decodeFromByteArray(Gt7Ps5AddressPreferences.serializer(), input.readBytes())
        } catch (e: SerializationException) {
            throw CorruptionException("Cannot read Gt7Ps5AddressPreferences.", e)
        }

    override suspend fun writeTo(t: Gt7Ps5AddressPreferences, output: OutputStream) {
        output.write(ProtoBuf.encodeToByteArray(Gt7Ps5AddressPreferences.serializer(), t))
    }
}
