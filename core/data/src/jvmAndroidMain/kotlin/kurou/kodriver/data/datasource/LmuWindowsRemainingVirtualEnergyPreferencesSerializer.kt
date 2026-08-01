package kurou.kodriver.data.datasource

import androidx.datastore.core.CorruptionException
import androidx.datastore.core.Serializer
import io.sentry.Sentry
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerializationException
import kotlinx.serialization.protobuf.ProtoBuf
import kurou.kodriver.data.model.LmuWindowsRemainingVirtualEnergyPreferences
import java.io.InputStream
import java.io.OutputStream

@OptIn(ExperimentalSerializationApi::class)
internal object LmuWindowsRemainingVirtualEnergyPreferencesSerializer :
    Serializer<LmuWindowsRemainingVirtualEnergyPreferences> {
    override val defaultValue: LmuWindowsRemainingVirtualEnergyPreferences =
        LmuWindowsRemainingVirtualEnergyPreferences()

    override suspend fun readFrom(input: InputStream): LmuWindowsRemainingVirtualEnergyPreferences =
        try {
            ProtoBuf.decodeFromByteArray(
                LmuWindowsRemainingVirtualEnergyPreferences.serializer(),
                input.readBytes(),
            )
        } catch (e: SerializationException) {
            Sentry.captureException(e)
            throw CorruptionException("Cannot read LmuWindowsRemainingVirtualEnergyPreferences.", e)
        }

    override suspend fun writeTo(
        t: LmuWindowsRemainingVirtualEnergyPreferences,
        output: OutputStream,
    ) {
        output.write(ProtoBuf.encodeToByteArray(LmuWindowsRemainingVirtualEnergyPreferences.serializer(), t))
    }
}
