package kurou.kodriver.data.datasource

import androidx.datastore.core.CorruptionException
import androidx.datastore.core.Serializer
import io.sentry.Sentry
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerializationException
import kotlinx.serialization.protobuf.ProtoBuf
import kurou.kodriver.data.model.AceWindowsFlagPreferences
import java.io.InputStream
import java.io.OutputStream

@OptIn(ExperimentalSerializationApi::class)
internal object AceWindowsFlagPreferencesSerializer : Serializer<AceWindowsFlagPreferences> {
    override val defaultValue: AceWindowsFlagPreferences = AceWindowsFlagPreferences()

    override suspend fun readFrom(input: InputStream): AceWindowsFlagPreferences =
        try {
            ProtoBuf.decodeFromByteArray(AceWindowsFlagPreferences.serializer(), input.readBytes())
        } catch (e: SerializationException) {
            Sentry.captureException(e)
            throw CorruptionException("Cannot read AceWindowsFlagPreferences.", e)
        }

    override suspend fun writeTo(t: AceWindowsFlagPreferences, output: OutputStream) {
        output.write(ProtoBuf.encodeToByteArray(AceWindowsFlagPreferences.serializer(), t))
    }
}
