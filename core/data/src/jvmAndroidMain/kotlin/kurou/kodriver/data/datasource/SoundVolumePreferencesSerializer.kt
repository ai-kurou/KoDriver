package kurou.kodriver.data.datasource

import androidx.datastore.core.CorruptionException
import androidx.datastore.core.Serializer
import io.sentry.Sentry
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerializationException
import kotlinx.serialization.protobuf.ProtoBuf
import kurou.kodriver.data.model.SoundVolumePreferences
import java.io.InputStream
import java.io.OutputStream

@OptIn(ExperimentalSerializationApi::class)
internal object SoundVolumePreferencesSerializer : Serializer<SoundVolumePreferences> {
    override val defaultValue: SoundVolumePreferences = SoundVolumePreferences()

    override suspend fun readFrom(input: InputStream): SoundVolumePreferences =
        try {
            ProtoBuf.decodeFromByteArray(SoundVolumePreferences.serializer(), input.readBytes())
        } catch (e: SerializationException) {
            Sentry.captureException(e)
            throw CorruptionException("Cannot read SoundVolumePreferences.", e)
        }

    override suspend fun writeTo(t: SoundVolumePreferences, output: OutputStream) {
        output.write(ProtoBuf.encodeToByteArray(SoundVolumePreferences.serializer(), t))
    }
}
