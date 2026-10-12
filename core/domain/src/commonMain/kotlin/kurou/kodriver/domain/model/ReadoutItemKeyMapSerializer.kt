package kurou.kodriver.domain.model

import kotlinx.serialization.KSerializer
import kotlinx.serialization.builtins.MapSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

/**
 * [ReadoutItemKey] の実装型に依存せず、安定識別子を使ってシリアライズする。
 * DataStore キーと同じ [ReadoutItemKey.value] を Map キーとして使うことで、
 * テレメトリログ上でも読み上げ設定を読める形で記録する。
 */
abstract class ReadoutItemKeyMapSerializer(
    private val entries: () -> List<ReadoutItemKey>,
) : KSerializer<Map<ReadoutItemKey, Boolean>> {
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
                entries().find { it.value == value }?.let { it to enabled }
            }.toMap()
}
