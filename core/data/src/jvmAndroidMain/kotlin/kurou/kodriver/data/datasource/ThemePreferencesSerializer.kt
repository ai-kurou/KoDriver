package kurou.kodriver.data.datasource

import androidx.datastore.core.CorruptionException
import androidx.datastore.core.Serializer
import io.sentry.Sentry
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerializationException
import kotlinx.serialization.protobuf.ProtoBuf
import kurou.kodriver.data.model.ThemePreferences
import java.io.InputStream
import java.io.OutputStream

@OptIn(ExperimentalSerializationApi::class)
internal object ThemePreferencesSerializer : Serializer<ThemePreferences> {
    override val defaultValue: ThemePreferences = ThemePreferences()

    override suspend fun readFrom(input: InputStream): ThemePreferences =
        try {
            ProtoBuf.decodeFromByteArray(ThemePreferences.serializer(), input.readBytes())
        } catch (e: SerializationException) {
            Sentry.captureException(e)
            throw CorruptionException("Cannot read ThemePreferences.", e)
        }

    override suspend fun writeTo(t: ThemePreferences, output: OutputStream) {
        output.write(ProtoBuf.encodeToByteArray(ThemePreferences.serializer(), t))
    }
}
