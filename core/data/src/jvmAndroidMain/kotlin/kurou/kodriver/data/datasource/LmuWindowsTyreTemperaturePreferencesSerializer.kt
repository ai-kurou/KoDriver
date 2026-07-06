package kurou.kodriver.data.datasource

import androidx.datastore.core.CorruptionException
import androidx.datastore.core.Serializer
import io.sentry.Sentry
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerializationException
import kotlinx.serialization.protobuf.ProtoBuf
import kurou.kodriver.data.model.LmuWindowsTyreTemperaturePreferences
import java.io.InputStream
import java.io.OutputStream

@OptIn(ExperimentalSerializationApi::class)
internal object LmuWindowsTyreTemperaturePreferencesSerializer :
    Serializer<LmuWindowsTyreTemperaturePreferences> {
    override val defaultValue: LmuWindowsTyreTemperaturePreferences =
        LmuWindowsTyreTemperaturePreferences()

    override suspend fun readFrom(input: InputStream): LmuWindowsTyreTemperaturePreferences =
        try {
            ProtoBuf.decodeFromByteArray(LmuWindowsTyreTemperaturePreferences.serializer(), input.readBytes())
        } catch (e: SerializationException) {
            Sentry.captureException(e)
            throw CorruptionException("Cannot read LmuWindowsTyreTemperaturePreferences.", e)
        }

    override suspend fun writeTo(t: LmuWindowsTyreTemperaturePreferences, output: OutputStream) {
        output.write(ProtoBuf.encodeToByteArray(LmuWindowsTyreTemperaturePreferences.serializer(), t))
    }
}
