package kurou.kodriver.data.datasource

import androidx.datastore.core.CorruptionException
import androidx.datastore.core.Serializer
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerializationException
import kotlinx.serialization.protobuf.ProtoBuf
import kurou.kodriver.data.model.VehicleDamagePreferences
import java.io.InputStream
import java.io.OutputStream

@OptIn(ExperimentalSerializationApi::class)
internal object VehicleDamagePreferencesSerializer : Serializer<VehicleDamagePreferences> {
    override val defaultValue: VehicleDamagePreferences = VehicleDamagePreferences()

    override suspend fun readFrom(input: InputStream): VehicleDamagePreferences =
        try {
            ProtoBuf.decodeFromByteArray(VehicleDamagePreferences.serializer(), input.readBytes())
        } catch (e: SerializationException) {
            throw CorruptionException("Cannot read VehicleDamagePreferences.", e)
        }

    override suspend fun writeTo(t: VehicleDamagePreferences, output: OutputStream) {
        output.write(ProtoBuf.encodeToByteArray(VehicleDamagePreferences.serializer(), t))
    }
}
