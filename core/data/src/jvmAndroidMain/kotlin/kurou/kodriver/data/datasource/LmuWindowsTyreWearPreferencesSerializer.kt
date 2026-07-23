package kurou.kodriver.data.datasource

import androidx.datastore.core.CorruptionException
import androidx.datastore.core.Serializer
import io.sentry.Sentry
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerializationException
import kotlinx.serialization.protobuf.ProtoBuf
import kurou.kodriver.data.model.LmuWindowsTyreWearPreferences
import java.io.InputStream
import java.io.OutputStream

@OptIn(ExperimentalSerializationApi::class)
internal object LmuWindowsTyreWearPreferencesSerializer : Serializer<LmuWindowsTyreWearPreferences> {
    override val defaultValue: LmuWindowsTyreWearPreferences = LmuWindowsTyreWearPreferences()

    override suspend fun readFrom(input: InputStream): LmuWindowsTyreWearPreferences =
        try {
            ProtoBuf.decodeFromByteArray(LmuWindowsTyreWearPreferences.serializer(), input.readBytes())
        } catch (e: SerializationException) {
            Sentry.captureException(e)
            throw CorruptionException("Cannot read LmuWindowsTyreWearPreferences.", e)
        }

    override suspend fun writeTo(t: LmuWindowsTyreWearPreferences, output: OutputStream) {
        output.write(ProtoBuf.encodeToByteArray(LmuWindowsTyreWearPreferences.serializer(), t))
    }
}
