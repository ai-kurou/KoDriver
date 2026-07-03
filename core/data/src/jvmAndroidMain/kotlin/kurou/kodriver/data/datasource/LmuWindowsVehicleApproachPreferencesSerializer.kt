package kurou.kodriver.data.datasource

import androidx.datastore.core.CorruptionException
import androidx.datastore.core.Serializer
import io.sentry.Sentry
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerializationException
import kotlinx.serialization.protobuf.ProtoBuf
import kurou.kodriver.data.model.LmuWindowsVehicleApproachPreferences
import java.io.InputStream
import java.io.OutputStream

@OptIn(ExperimentalSerializationApi::class)
internal object LmuWindowsVehicleApproachPreferencesSerializer : Serializer<LmuWindowsVehicleApproachPreferences> {
    override val defaultValue: LmuWindowsVehicleApproachPreferences = LmuWindowsVehicleApproachPreferences()

    override suspend fun readFrom(input: InputStream): LmuWindowsVehicleApproachPreferences =
        try {
            ProtoBuf.decodeFromByteArray(LmuWindowsVehicleApproachPreferences.serializer(), input.readBytes())
        } catch (e: SerializationException) {
            Sentry.captureException(e)
            throw CorruptionException("Cannot read LmuWindowsVehicleApproachPreferences.", e)
        }

    override suspend fun writeTo(t: LmuWindowsVehicleApproachPreferences, output: OutputStream) {
        output.write(ProtoBuf.encodeToByteArray(LmuWindowsVehicleApproachPreferences.serializer(), t))
    }
}
