package kurou.kodriver.data.datasource

import androidx.datastore.core.CorruptionException
import androidx.datastore.core.Serializer
import io.sentry.Sentry
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerializationException
import kotlinx.serialization.protobuf.ProtoBuf
import kurou.kodriver.data.model.LmuWindowsPitTimingPreferences
import java.io.InputStream
import java.io.OutputStream

@OptIn(ExperimentalSerializationApi::class)
internal object LmuWindowsPitTimingPreferencesSerializer : Serializer<LmuWindowsPitTimingPreferences> {
    override val defaultValue: LmuWindowsPitTimingPreferences = LmuWindowsPitTimingPreferences()

    override suspend fun readFrom(input: InputStream): LmuWindowsPitTimingPreferences =
        try {
            ProtoBuf.decodeFromByteArray(LmuWindowsPitTimingPreferences.serializer(), input.readBytes())
        } catch (e: SerializationException) {
            Sentry.captureException(e)
            throw CorruptionException("Cannot read LmuWindowsPitTimingPreferences.", e)
        }

    override suspend fun writeTo(
        t: LmuWindowsPitTimingPreferences,
        output: OutputStream,
    ) {
        output.write(ProtoBuf.encodeToByteArray(LmuWindowsPitTimingPreferences.serializer(), t))
    }
}
