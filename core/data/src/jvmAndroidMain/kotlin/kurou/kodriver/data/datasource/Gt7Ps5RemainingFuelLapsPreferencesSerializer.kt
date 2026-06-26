package kurou.kodriver.data.datasource

import androidx.datastore.core.CorruptionException
import androidx.datastore.core.Serializer
import io.sentry.Sentry
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerializationException
import kotlinx.serialization.protobuf.ProtoBuf
import kurou.kodriver.data.model.Gt7Ps5RemainingFuelLapsPreferences
import java.io.InputStream
import java.io.OutputStream

@OptIn(ExperimentalSerializationApi::class)
internal object Gt7Ps5RemainingFuelLapsPreferencesSerializer : Serializer<Gt7Ps5RemainingFuelLapsPreferences> {
    override val defaultValue: Gt7Ps5RemainingFuelLapsPreferences = Gt7Ps5RemainingFuelLapsPreferences()

    override suspend fun readFrom(input: InputStream): Gt7Ps5RemainingFuelLapsPreferences =
        try {
            ProtoBuf.decodeFromByteArray(Gt7Ps5RemainingFuelLapsPreferences.serializer(), input.readBytes())
        } catch (e: SerializationException) {
            Sentry.captureException(e)
            throw CorruptionException("Cannot read Gt7Ps5RemainingFuelLapsPreferences.", e)
        }

    override suspend fun writeTo(t: Gt7Ps5RemainingFuelLapsPreferences, output: OutputStream) {
        output.write(ProtoBuf.encodeToByteArray(Gt7Ps5RemainingFuelLapsPreferences.serializer(), t))
    }
}
