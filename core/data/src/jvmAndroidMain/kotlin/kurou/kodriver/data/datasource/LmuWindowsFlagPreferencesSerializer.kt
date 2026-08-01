package kurou.kodriver.data.datasource

import androidx.datastore.core.CorruptionException
import androidx.datastore.core.Serializer
import io.sentry.Sentry
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerializationException
import kotlinx.serialization.protobuf.ProtoBuf
import kurou.kodriver.data.model.LmuWindowsFlagPreferences
import java.io.InputStream
import java.io.OutputStream

@OptIn(ExperimentalSerializationApi::class)
internal object LmuWindowsFlagPreferencesSerializer : Serializer<LmuWindowsFlagPreferences> {
    override val defaultValue: LmuWindowsFlagPreferences = LmuWindowsFlagPreferences()

    override suspend fun readFrom(input: InputStream): LmuWindowsFlagPreferences =
        try {
            ProtoBuf.decodeFromByteArray(LmuWindowsFlagPreferences.serializer(), input.readBytes())
        } catch (e: SerializationException) {
            Sentry.captureException(e)
            throw CorruptionException("Cannot read LmuWindowsFlagPreferences.", e)
        }

    override suspend fun writeTo(
        t: LmuWindowsFlagPreferences,
        output: OutputStream,
    ) {
        output.write(ProtoBuf.encodeToByteArray(LmuWindowsFlagPreferences.serializer(), t))
    }
}
