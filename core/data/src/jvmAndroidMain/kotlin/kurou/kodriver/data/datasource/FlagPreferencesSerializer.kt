package kurou.kodriver.data.datasource

import androidx.datastore.core.CorruptionException
import androidx.datastore.core.Serializer
import io.sentry.Sentry
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerializationException
import kotlinx.serialization.protobuf.ProtoBuf
import kurou.kodriver.data.model.FlagPreferences
import java.io.InputStream
import java.io.OutputStream

@OptIn(ExperimentalSerializationApi::class)
internal object FlagPreferencesSerializer : Serializer<FlagPreferences> {
    override val defaultValue: FlagPreferences = FlagPreferences()

    override suspend fun readFrom(input: InputStream): FlagPreferences =
        try {
            ProtoBuf.decodeFromByteArray(FlagPreferences.serializer(), input.readBytes())
        } catch (e: SerializationException) {
            Sentry.captureException(e)
            throw CorruptionException("Cannot read FlagPreferences.", e)
        }

    override suspend fun writeTo(t: FlagPreferences, output: OutputStream) {
        output.write(ProtoBuf.encodeToByteArray(FlagPreferences.serializer(), t))
    }
}
