package kurou.kodriver.domain.model

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

/**
 * LMU 共有メモリの `rF2VehicleScoring.mVehicleClass` が返す人間可読なクラス名（2026年8月時点の実測値:
 * "GT3", "GTE", "LMP3", "LMP2", "LMP2_ELMS", "Hyper"）を表すクラス別しきい値設定などに使う。
 * WebSocket 経由の JSON では従来と同じ `{"name": "..."}` の形で送受信される（[LmuWindowsVehicleClassDataSerializer]）。
 */
@Serializable(with = LmuWindowsVehicleClassDataSerializer::class)
sealed class LmuWindowsVehicleClassData(
    val name: String,
) {
    data object Hypercar : LmuWindowsVehicleClassData("Hyper")

    data object Lmp2 : LmuWindowsVehicleClassData("LMP2")

    data object Lmp2Elms : LmuWindowsVehicleClassData("LMP2_ELMS")

    data object Lmp3 : LmuWindowsVehicleClassData("LMP3")

    data object Gte : LmuWindowsVehicleClassData("GTE")

    data object Lmgt3 : LmuWindowsVehicleClassData("GT3")

    /** 未知のクラス文字列、またはプレイヤー車両が見つからず空文字列だった場合。 */
    data class Unknown(
        val raw: String,
    ) : LmuWindowsVehicleClassData(raw)

    companion object {
        fun fromRawValue(raw: String): LmuWindowsVehicleClassData =
            when (raw) {
                Hypercar.name -> Hypercar
                Lmp2.name -> Lmp2
                Lmp2Elms.name -> Lmp2Elms
                Lmp3.name -> Lmp3
                Gte.name -> Gte
                Lmgt3.name -> Lmgt3
                else -> Unknown(raw)
            }
    }
}

object LmuWindowsVehicleClassDataSerializer : KSerializer<LmuWindowsVehicleClassData> {
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
