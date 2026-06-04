package kurou.kodriver.data.datasource

import androidx.datastore.core.CorruptionException
import androidx.datastore.core.Serializer
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerializationException
import kotlinx.serialization.protobuf.ProtoBuf
import kurou.kodriver.data.model.ReadoutPreferences
import java.io.InputStream
import java.io.OutputStream

@OptIn(ExperimentalSerializationApi::class)
internal object ReadoutPreferencesSerializer : Serializer<ReadoutPreferences> {
    override val defaultValue: ReadoutPreferences = ReadoutPreferences()

    override suspend fun readFrom(input: InputStream): ReadoutPreferences =
        try {
            ProtoBuf.decodeFromByteArray(ReadoutPreferences.serializer(), input.readBytes())
        } catch (e: SerializationException) {
            throw CorruptionException("Cannot read ReadoutPreferences.", e)
        }

    override suspend fun writeTo(t: ReadoutPreferences, output: OutputStream) {
        output.write(ProtoBuf.encodeToByteArray(ReadoutPreferences.serializer(), t))
    }
}
