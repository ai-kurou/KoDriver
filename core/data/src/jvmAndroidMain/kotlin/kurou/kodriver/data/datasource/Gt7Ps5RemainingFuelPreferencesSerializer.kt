package kurou.kodriver.data.datasource

import androidx.datastore.core.CorruptionException
import androidx.datastore.core.Serializer
import io.sentry.Sentry
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerializationException
import kotlinx.serialization.protobuf.ProtoBuf
import kurou.kodriver.data.model.Gt7Ps5RemainingFuelPreferences
import java.io.InputStream
import java.io.OutputStream

@OptIn(ExperimentalSerializationApi::class)
internal object Gt7Ps5RemainingFuelPreferencesSerializer : Serializer<Gt7Ps5RemainingFuelPreferences> {
    override val defaultValue: Gt7Ps5RemainingFuelPreferences = Gt7Ps5RemainingFuelPreferences()

    override suspend fun readFrom(input: InputStream): Gt7Ps5RemainingFuelPreferences =
        try {
            ProtoBuf.decodeFromByteArray(Gt7Ps5RemainingFuelPreferences.serializer(), input.readBytes())
        } catch (e: SerializationException) {
            Sentry.captureException(e)
            throw CorruptionException("Cannot read Gt7Ps5RemainingFuelPreferences.", e)
        }

    override suspend fun writeTo(
        t: Gt7Ps5RemainingFuelPreferences,
        output: OutputStream,
    ) {
        output.write(ProtoBuf.encodeToByteArray(Gt7Ps5RemainingFuelPreferences.serializer(), t))
    }
}
