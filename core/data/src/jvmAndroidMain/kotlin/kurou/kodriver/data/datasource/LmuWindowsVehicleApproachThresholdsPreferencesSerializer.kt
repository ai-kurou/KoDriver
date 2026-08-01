package kurou.kodriver.data.datasource

import androidx.datastore.core.CorruptionException
import androidx.datastore.core.Serializer
import io.sentry.Sentry
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerializationException
import kotlinx.serialization.protobuf.ProtoBuf
import kurou.kodriver.data.model.LmuWindowsVehicleApproachThresholdsPreferences
import java.io.InputStream
import java.io.OutputStream

@OptIn(ExperimentalSerializationApi::class)
internal object LmuWindowsVehicleApproachThresholdsPreferencesSerializer :
    Serializer<LmuWindowsVehicleApproachThresholdsPreferences> {
    override val defaultValue: LmuWindowsVehicleApproachThresholdsPreferences =
        LmuWindowsVehicleApproachThresholdsPreferences()

    override suspend fun readFrom(input: InputStream): LmuWindowsVehicleApproachThresholdsPreferences =
        try {
            ProtoBuf.decodeFromByteArray(LmuWindowsVehicleApproachThresholdsPreferences.serializer(), input.readBytes())
        } catch (e: SerializationException) {
            Sentry.captureException(e)
            throw CorruptionException("Cannot read LmuWindowsVehicleApproachThresholdsPreferences.", e)
        }

    override suspend fun writeTo(
        t: LmuWindowsVehicleApproachThresholdsPreferences,
        output: OutputStream,
    ) {
        output.write(ProtoBuf.encodeToByteArray(LmuWindowsVehicleApproachThresholdsPreferences.serializer(), t))
    }
}
