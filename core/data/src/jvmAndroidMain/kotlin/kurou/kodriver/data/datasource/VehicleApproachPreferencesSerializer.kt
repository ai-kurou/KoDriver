package kurou.kodriver.data.datasource

import androidx.datastore.core.CorruptionException
import androidx.datastore.core.Serializer
import io.sentry.Sentry
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerializationException
import kotlinx.serialization.protobuf.ProtoBuf
import kurou.kodriver.data.model.VehicleApproachPreferences
import java.io.InputStream
import java.io.OutputStream

@OptIn(ExperimentalSerializationApi::class)
internal object VehicleApproachPreferencesSerializer : Serializer<VehicleApproachPreferences> {
    override val defaultValue: VehicleApproachPreferences = VehicleApproachPreferences()

    override suspend fun readFrom(input: InputStream): VehicleApproachPreferences =
        try {
            ProtoBuf.decodeFromByteArray(VehicleApproachPreferences.serializer(), input.readBytes())
        } catch (e: SerializationException) {
            Sentry.captureException(e)
            throw CorruptionException("Cannot read VehicleApproachPreferences.", e)
        }

    override suspend fun writeTo(t: VehicleApproachPreferences, output: OutputStream) {
        output.write(ProtoBuf.encodeToByteArray(VehicleApproachPreferences.serializer(), t))
    }
}
