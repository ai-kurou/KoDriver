package kurou.kodriver.domain.model

import kotlinx.serialization.KSerializer
import kotlinx.serialization.builtins.MapSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

/**
 * [ReadoutItemKey] は sealed interface のため既定のシリアライズ対象にならない。
 * DataStore キーと同じ [ReadoutItemKey.value] を Map キーとして使うことで、
 * テレメトリログ上でも読み上げ設定を読める形で記録する。
 */
object ReadoutItemKeyMapSerializer : KSerializer<Map<ReadoutItemKey, Boolean>> {
    private val delegate = MapSerializer(String.serializer(), Boolean.serializer())

    override val descriptor: SerialDescriptor = delegate.descriptor

    override fun serialize(
        encoder: Encoder,
        value: Map<ReadoutItemKey, Boolean>,
    ) {
        delegate.serialize(encoder, value.mapKeys { it.key.value })
    }

    override fun deserialize(decoder: Decoder): Map<ReadoutItemKey, Boolean> =
        delegate
            .deserialize(decoder)
            .mapNotNull { (value, enabled) ->
                ReadoutItemKey.fromValue(value)?.let { it to enabled }
            }.toMap()
}
