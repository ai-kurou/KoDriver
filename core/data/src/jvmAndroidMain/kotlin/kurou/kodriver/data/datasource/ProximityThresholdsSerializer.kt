package kurou.kodriver.data.datasource

import androidx.datastore.core.CorruptionException
import androidx.datastore.core.Serializer
import io.sentry.Sentry
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerializationException
import kotlinx.serialization.protobuf.ProtoBuf
import kurou.kodriver.data.model.ProximityThresholdsPreferences
import java.io.InputStream
import java.io.OutputStream

@OptIn(ExperimentalSerializationApi::class)
internal object ProximityThresholdsSerializer : Serializer<ProximityThresholdsPreferences> {
    override val defaultValue: ProximityThresholdsPreferences = ProximityThresholdsPreferences()

    override suspend fun readFrom(input: InputStream): ProximityThresholdsPreferences =
        try {
            ProtoBuf.decodeFromByteArray(ProximityThresholdsPreferences.serializer(), input.readBytes())
        } catch (e: SerializationException) {
            Sentry.captureException(e)
            throw CorruptionException("Cannot read ProximityThresholdsPreferences.", e)
        }

    override suspend fun writeTo(t: ProximityThresholdsPreferences, output: OutputStream) {
        output.write(ProtoBuf.encodeToByteArray(ProximityThresholdsPreferences.serializer(), t))
    }
}
