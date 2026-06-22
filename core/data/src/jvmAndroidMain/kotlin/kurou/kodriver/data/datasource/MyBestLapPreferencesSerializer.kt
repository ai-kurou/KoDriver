package kurou.kodriver.data.datasource

import androidx.datastore.core.CorruptionException
import androidx.datastore.core.Serializer
import io.sentry.Sentry
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerializationException
import kotlinx.serialization.protobuf.ProtoBuf
import kurou.kodriver.data.model.MyBestLapPreferences
import java.io.InputStream
import java.io.OutputStream

@OptIn(ExperimentalSerializationApi::class)
internal object MyBestLapPreferencesSerializer : Serializer<MyBestLapPreferences> {
    override val defaultValue: MyBestLapPreferences = MyBestLapPreferences()

    override suspend fun readFrom(input: InputStream): MyBestLapPreferences =
        try {
            ProtoBuf.decodeFromByteArray(MyBestLapPreferences.serializer(), input.readBytes())
        } catch (e: SerializationException) {
            Sentry.captureException(e)
            throw CorruptionException("Cannot read MyBestLapPreferences.", e)
        }

    override suspend fun writeTo(t: MyBestLapPreferences, output: OutputStream) {
        output.write(ProtoBuf.encodeToByteArray(MyBestLapPreferences.serializer(), t))
    }
}
