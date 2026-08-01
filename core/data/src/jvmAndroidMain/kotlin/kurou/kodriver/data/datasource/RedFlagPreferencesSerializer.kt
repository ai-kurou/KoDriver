package kurou.kodriver.data.datasource

import androidx.datastore.core.CorruptionException
import androidx.datastore.core.Serializer
import io.sentry.Sentry
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerializationException
import kotlinx.serialization.protobuf.ProtoBuf
import kurou.kodriver.data.model.RedFlagPreferences
import java.io.InputStream
import java.io.OutputStream

@OptIn(ExperimentalSerializationApi::class)
internal object RedFlagPreferencesSerializer : Serializer<RedFlagPreferences> {
    override val defaultValue: RedFlagPreferences = RedFlagPreferences()

    override suspend fun readFrom(input: InputStream): RedFlagPreferences =
        try {
            ProtoBuf.decodeFromByteArray(RedFlagPreferences.serializer(), input.readBytes())
        } catch (e: SerializationException) {
            Sentry.captureException(e)
            throw CorruptionException("Cannot read RedFlagPreferences.", e)
        }

    override suspend fun writeTo(
        t: RedFlagPreferences,
        output: OutputStream,
    ) {
        output.write(ProtoBuf.encodeToByteArray(RedFlagPreferences.serializer(), t))
    }
}
