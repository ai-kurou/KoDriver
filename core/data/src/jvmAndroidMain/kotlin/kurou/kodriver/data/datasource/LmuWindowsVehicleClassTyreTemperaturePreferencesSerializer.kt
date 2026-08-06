package kurou.kodriver.data.datasource

import androidx.datastore.core.CorruptionException
import androidx.datastore.core.Serializer
import io.sentry.Sentry
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerializationException
import kotlinx.serialization.protobuf.ProtoBuf
import kurou.kodriver.data.model.LmuWindowsVehicleClassTyreTemperaturePreferences
import java.io.InputStream
import java.io.OutputStream

@OptIn(ExperimentalSerializationApi::class)
internal object LmuWindowsVehicleClassTyreTemperaturePreferencesSerializer :
    Serializer<LmuWindowsVehicleClassTyreTemperaturePreferences> {
    override val defaultValue: LmuWindowsVehicleClassTyreTemperaturePreferences =
        LmuWindowsVehicleClassTyreTemperaturePreferences()

    override suspend fun readFrom(input: InputStream): LmuWindowsVehicleClassTyreTemperaturePreferences =
        try {
            ProtoBuf.decodeFromByteArray(
                LmuWindowsVehicleClassTyreTemperaturePreferences.serializer(),
                input.readBytes(),
            )
        } catch (e: SerializationException) {
            Sentry.captureException(e)
            throw CorruptionException("Cannot read LmuWindowsVehicleClassTyreTemperaturePreferences.", e)
        }

    override suspend fun writeTo(
        t: LmuWindowsVehicleClassTyreTemperaturePreferences,
        output: OutputStream,
    ) {
        output.write(
            ProtoBuf.encodeToByteArray(LmuWindowsVehicleClassTyreTemperaturePreferences.serializer(), t),
        )
    }
}
