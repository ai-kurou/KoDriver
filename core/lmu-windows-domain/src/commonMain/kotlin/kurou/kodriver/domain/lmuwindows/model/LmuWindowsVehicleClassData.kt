package kurou.kodriver.domain.lmuwindows.model

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

/**
 * LMU 共有メモリの `rF2VehicleScoring.mVehicleClass` が返す人間可読なクラス名（2026年8月時点の実測値:
 * "GT3", "GTE", "LMP3", "LMP2", "LMP2_ELMS", "Hyper"）を表すクラス別閾値設定などに使う。
 * WebSocket 経由の JSON では従来と同じ `{"name": "..."}` の形で送受信される（[LmuWindowsVehicleClassDataSerializer]）。
 */
@Serializable(with = LmuWindowsVehicleClassDataSerializer::class)
public sealed class LmuWindowsVehicleClassData(
    public val name: String,
) {
    public data object Hypercar : LmuWindowsVehicleClassData("Hyper")

    public data object P2 : LmuWindowsVehicleClassData("LMP2")

    public data object P2Elms : LmuWindowsVehicleClassData("LMP2_ELMS")

    public data object P3 : LmuWindowsVehicleClassData("LMP3")

    public data object Gte : LmuWindowsVehicleClassData("GTE")

    public data object Gt3 : LmuWindowsVehicleClassData("GT3")

    /** 未知のクラス文字列、またはプレイヤー車両が見つからず空文字列だった場合。 */
    public data class Unknown(
        val raw: String,
    ) : LmuWindowsVehicleClassData(raw)

    public companion object {
        public fun fromRawValue(raw: String): LmuWindowsVehicleClassData =
            when (raw) {
                Hypercar.name -> Hypercar
                P2.name -> P2
                P2Elms.name -> P2Elms
                P3.name -> P3
                Gte.name -> Gte
                Gt3.name -> Gt3
                else -> Unknown(raw)
            }
    }
}

internal object LmuWindowsVehicleClassDataSerializer : KSerializer<LmuWindowsVehicleClassData> {
    @Serializable
    private data class Surrogate(
        val name: String,
    )

    override val descriptor: SerialDescriptor = Surrogate.serializer().descriptor

    override fun serialize(
        encoder: Encoder,
        value: LmuWindowsVehicleClassData,
    ) {
        encoder.encodeSerializableValue(Surrogate.serializer(), Surrogate(value.name))
    }

    override fun deserialize(decoder: Decoder): LmuWindowsVehicleClassData {
        val surrogate = decoder.decodeSerializableValue(Surrogate.serializer())
        return LmuWindowsVehicleClassData.fromRawValue(surrogate.name)
    }
}
