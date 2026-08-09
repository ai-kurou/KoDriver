package kurou.kodriver.data.datasource

import androidx.datastore.core.CorruptionException
import androidx.datastore.core.Serializer
import io.sentry.Sentry
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerializationException
import kotlinx.serialization.protobuf.ProtoBuf
import java.io.InputStream
import java.io.OutputStream

/**
 * ProtoBuf ベースの DataStore [Serializer] を組み立てる汎用ファクトリ。
 *
 * 各 Preferences の Serializer は readFrom / writeTo の実装が同一であるため、
 * このファクトリで [defaultValue] と [kSerializer] だけを渡す形に共通化する。
 * エラーメッセージの型名は [kSerializer] の `descriptor.serialName` から取得するため、
 * 別途文字列で渡す必要はない。
 */
@OptIn(ExperimentalSerializationApi::class)
internal fun <T> protoBufPreferencesSerializer(
    defaultValue: T,
    kSerializer: KSerializer<T>,
): Serializer<T> =
    object : Serializer<T> {
        override val defaultValue: T = defaultValue

        override suspend fun readFrom(input: InputStream): T =
            try {
                ProtoBuf.decodeFromByteArray(kSerializer, input.readBytes())
            } catch (e: SerializationException) {
                Sentry.captureException(e)
                throw CorruptionException("Cannot read ${kSerializer.descriptor.serialName}.", e)
            }

        override suspend fun writeTo(
            t: T,
            output: OutputStream,
        ) {
            output.write(ProtoBuf.encodeToByteArray(kSerializer, t))
        }
    }
