package kurou.kodriver.data.datasource

import androidx.datastore.core.CorruptionException
import androidx.datastore.core.Serializer
import io.sentry.Sentry
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerializationException
import kotlinx.serialization.protobuf.ProtoBuf
import kurou.kodriver.data.model.QueuePreferences
import java.io.InputStream
import java.io.OutputStream

@OptIn(ExperimentalSerializationApi::class)
internal object QueuePreferencesSerializer : Serializer<QueuePreferences> {
    override val defaultValue: QueuePreferences = QueuePreferences()

    override suspend fun readFrom(input: InputStream): QueuePreferences =
        try {
            ProtoBuf.decodeFromByteArray(QueuePreferences.serializer(), input.readBytes())
        } catch (e: SerializationException) {
            Sentry.captureException(e)
            throw CorruptionException("Cannot read QueuePreferences.", e)
        }

    override suspend fun writeTo(t: QueuePreferences, output: OutputStream) {
        output.write(ProtoBuf.encodeToByteArray(QueuePreferences.serializer(), t))
    }
}
