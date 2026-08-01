package kurou.kodriver.data.datasource

import androidx.datastore.core.CorruptionException
import androidx.datastore.core.Serializer
import io.sentry.Sentry
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerializationException
import kotlinx.serialization.protobuf.ProtoBuf
import kurou.kodriver.data.model.LmuWindowsVehicleDamagePreferences
import java.io.InputStream
import java.io.OutputStream

@OptIn(ExperimentalSerializationApi::class)
internal object LmuWindowsVehicleDamagePreferencesSerializer : Serializer<LmuWindowsVehicleDamagePreferences> {
    override val defaultValue: LmuWindowsVehicleDamagePreferences = LmuWindowsVehicleDamagePreferences()

    override suspend fun readFrom(input: InputStream): LmuWindowsVehicleDamagePreferences =
        try {
            ProtoBuf.decodeFromByteArray(LmuWindowsVehicleDamagePreferences.serializer(), input.readBytes())
        } catch (e: SerializationException) {
            Sentry.captureException(e)
            throw CorruptionException("Cannot read LmuWindowsVehicleDamagePreferences.", e)
        }

    override suspend fun writeTo(
        t: LmuWindowsVehicleDamagePreferences,
        output: OutputStream,
    ) {
        output.write(ProtoBuf.encodeToByteArray(LmuWindowsVehicleDamagePreferences.serializer(), t))
    }
}
