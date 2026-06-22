package kurou.kodriver.core.gt7ps5data.datasource

import androidx.datastore.core.CorruptionException
import androidx.datastore.core.Serializer
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerializationException
import kotlinx.serialization.protobuf.ProtoBuf
import java.io.InputStream
import java.io.OutputStream

@OptIn(ExperimentalSerializationApi::class)
internal object ConsoleAddressSerializer : Serializer<ConsoleAddressPreferences> {
    override val defaultValue: ConsoleAddressPreferences = ConsoleAddressPreferences()

    override suspend fun readFrom(input: InputStream): ConsoleAddressPreferences =
        try {
            ProtoBuf.decodeFromByteArray(ConsoleAddressPreferences.serializer(), input.readBytes())
        } catch (e: SerializationException) {
            throw CorruptionException("Cannot read ConsoleAddressPreferences.", e)
        }

    override suspend fun writeTo(t: ConsoleAddressPreferences, output: OutputStream) {
        output.write(ProtoBuf.encodeToByteArray(ConsoleAddressPreferences.serializer(), t))
    }
}
