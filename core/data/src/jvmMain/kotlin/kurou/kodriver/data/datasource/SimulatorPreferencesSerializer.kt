package kurou.kodriver.data.datasource

import androidx.datastore.core.CorruptionException
import androidx.datastore.core.Serializer
import io.sentry.Sentry
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerializationException
import kotlinx.serialization.protobuf.ProtoBuf
import kurou.kodriver.data.model.SimulatorPreferences
import java.io.InputStream
import java.io.OutputStream

@OptIn(ExperimentalSerializationApi::class)
internal object SimulatorPreferencesSerializer : Serializer<SimulatorPreferences> {
    override val defaultValue: SimulatorPreferences = SimulatorPreferences()

    override suspend fun readFrom(input: InputStream): SimulatorPreferences =
        try {
            ProtoBuf.decodeFromByteArray(SimulatorPreferences.serializer(), input.readBytes())
        } catch (e: SerializationException) {
            Sentry.captureException(e)
            throw CorruptionException("Cannot read SimulatorPreferences.", e)
        }

    override suspend fun writeTo(t: SimulatorPreferences, output: OutputStream) {
        output.write(ProtoBuf.encodeToByteArray(SimulatorPreferences.serializer(), t))
    }
}
