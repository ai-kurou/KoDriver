package kurou.kodriver.data.datasource

import androidx.datastore.core.CorruptionException
import androidx.datastore.core.Serializer
import io.sentry.Sentry
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerializationException
import kotlinx.serialization.protobuf.ProtoBuf
import kurou.kodriver.data.model.AceWindowsRemainingFuelPreferences
import java.io.InputStream
import java.io.OutputStream

@OptIn(ExperimentalSerializationApi::class)
internal object AceWindowsRemainingFuelPreferencesSerializer : Serializer<AceWindowsRemainingFuelPreferences> {
    override val defaultValue: AceWindowsRemainingFuelPreferences = AceWindowsRemainingFuelPreferences()

    override suspend fun readFrom(input: InputStream): AceWindowsRemainingFuelPreferences =
        try {
            ProtoBuf.decodeFromByteArray(
                AceWindowsRemainingFuelPreferences.serializer(),
                input.readBytes(),
            )
        } catch (e: SerializationException) {
            Sentry.captureException(e)
            throw CorruptionException("Cannot read AceWindowsRemainingFuelPreferences.", e)
        }

    override suspend fun writeTo(t: AceWindowsRemainingFuelPreferences, output: OutputStream) {
        output.write(ProtoBuf.encodeToByteArray(AceWindowsRemainingFuelPreferences.serializer(), t))
    }
}
