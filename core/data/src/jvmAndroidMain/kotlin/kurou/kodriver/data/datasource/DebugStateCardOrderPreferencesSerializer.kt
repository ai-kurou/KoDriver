package kurou.kodriver.data.datasource

import androidx.datastore.core.CorruptionException
import androidx.datastore.core.Serializer
import io.sentry.Sentry
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerializationException
import kotlinx.serialization.protobuf.ProtoBuf
import kurou.kodriver.data.model.DebugStateCardOrderPreferences
import java.io.InputStream
import java.io.OutputStream

@OptIn(ExperimentalSerializationApi::class)
internal object DebugStateCardOrderPreferencesSerializer : Serializer<DebugStateCardOrderPreferences> {
    override val defaultValue: DebugStateCardOrderPreferences = DebugStateCardOrderPreferences()

    override suspend fun readFrom(input: InputStream): DebugStateCardOrderPreferences =
        try {
            ProtoBuf.decodeFromByteArray(DebugStateCardOrderPreferences.serializer(), input.readBytes())
        } catch (e: SerializationException) {
            Sentry.captureException(e)
            throw CorruptionException("Cannot read DebugStateCardOrderPreferences.", e)
        }

    override suspend fun writeTo(
        t: DebugStateCardOrderPreferences,
        output: OutputStream,
    ) {
        output.write(ProtoBuf.encodeToByteArray(DebugStateCardOrderPreferences.serializer(), t))
    }
}
