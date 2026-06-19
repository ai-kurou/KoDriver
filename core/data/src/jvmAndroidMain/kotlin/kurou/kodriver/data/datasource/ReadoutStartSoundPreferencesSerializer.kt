package kurou.kodriver.data.datasource

import androidx.datastore.core.CorruptionException
import androidx.datastore.core.Serializer
import io.sentry.Sentry
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerializationException
import kotlinx.serialization.protobuf.ProtoBuf
import kurou.kodriver.data.model.ReadoutStartSoundPreferences
import java.io.InputStream
import java.io.OutputStream

@OptIn(ExperimentalSerializationApi::class)
internal object ReadoutStartSoundPreferencesSerializer : Serializer<ReadoutStartSoundPreferences> {
    override val defaultValue: ReadoutStartSoundPreferences = ReadoutStartSoundPreferences()

    override suspend fun readFrom(input: InputStream): ReadoutStartSoundPreferences =
        try {
            ProtoBuf.decodeFromByteArray(ReadoutStartSoundPreferences.serializer(), input.readBytes())
        } catch (e: SerializationException) {
            Sentry.captureException(e)
            throw CorruptionException("Cannot read ReadoutStartSoundPreferences.", e)
        }

    override suspend fun writeTo(t: ReadoutStartSoundPreferences, output: OutputStream) {
        output.write(ProtoBuf.encodeToByteArray(ReadoutStartSoundPreferences.serializer(), t))
    }
}
