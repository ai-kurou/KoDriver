package kurou.kodriver.data.datasource

import androidx.datastore.core.CorruptionException
import androidx.datastore.core.Serializer
import io.sentry.Sentry
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerializationException
import kotlinx.serialization.protobuf.ProtoBuf
import kurou.kodriver.data.model.LmuWindowsRemainingVirtualEnergyLapsPreferences
import java.io.InputStream
import java.io.OutputStream

@OptIn(ExperimentalSerializationApi::class)
internal object LmuWindowsRemainingVirtualEnergyLapsPreferencesSerializer :
    Serializer<LmuWindowsRemainingVirtualEnergyLapsPreferences> {
    override val defaultValue: LmuWindowsRemainingVirtualEnergyLapsPreferences =
        LmuWindowsRemainingVirtualEnergyLapsPreferences()

    override suspend fun readFrom(input: InputStream): LmuWindowsRemainingVirtualEnergyLapsPreferences =
        try {
            ProtoBuf.decodeFromByteArray(
                LmuWindowsRemainingVirtualEnergyLapsPreferences.serializer(),
                input.readBytes(),
            )
        } catch (e: SerializationException) {
            Sentry.captureException(e)
            throw CorruptionException("Cannot read LmuWindowsRemainingVirtualEnergyLapsPreferences.", e)
        }

    override suspend fun writeTo(t: LmuWindowsRemainingVirtualEnergyLapsPreferences, output: OutputStream) {
        output.write(ProtoBuf.encodeToByteArray(LmuWindowsRemainingVirtualEnergyLapsPreferences.serializer(), t))
    }
}
