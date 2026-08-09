package kurou.kodriver.core.gt7ps5data.mapper

import kurou.kodriver.core.model.Gt7Ps5TelemetryData
import java.nio.ByteBuffer
import java.nio.charset.StandardCharsets

/**
 * GT7 UDP テレメトリパケット（復号済み・368バイト以上）を Gt7Ps5TelemetryData に変換する。
 *
 * オフセットは SimulatorInterface パケット仕様（GT7 / PS5）に基づく:
 *   0x44 (68)  : GasLevel        float  (リットル)
 *   0x48 (72)  : GasCapacity     float  (リットル、非燃料車は 100.0)
 *   0x74 (116) : LapCount        int16
 *   0x76 (118) : LapsInRace      int16  (0 = フリー走行・予選)
 *   0x78 (120) : BestLapTimeMs   int32  (-1 = ベストラップなし)
 *   0x16C (364): CarCategory     char[4] (NULL終端文字列、例: "GR3", "GRX")
 */
internal object Gt7Ps5Mapper {
    fun map(packet: ByteBuffer): Gt7Ps5TelemetryData =
        Gt7Ps5TelemetryData(
            lapCount = packet.getShort(LAP_COUNT_OFFSET).toInt(),
            lapsInRace = packet.getShort(LAPS_IN_RACE_OFFSET).toInt(),
            bestLapTimeMs = packet.getInt(BEST_LAP_TIME_OFFSET),
            gasLevel = packet.getFloat(GAS_LEVEL_OFFSET),
            gasCapacity = packet.getFloat(GAS_CAPACITY_OFFSET),
            carCategory = readCarCategory(packet),
        )

    private fun readCarCategory(packet: ByteBuffer): String {
        val bytes = ByteArray(CAR_CATEGORY_LENGTH)
        for (i in 0 until CAR_CATEGORY_LENGTH) {
            bytes[i] = packet.get(CAR_CATEGORY_OFFSET + i)
        }
        val nullIndex = bytes.indexOf(0).let { if (it == -1) bytes.size else it }
        return String(bytes, 0, nullIndex, StandardCharsets.US_ASCII)
    }

    private const val GAS_LEVEL_OFFSET = 0x44
    private const val GAS_CAPACITY_OFFSET = 0x48
    private const val LAP_COUNT_OFFSET = 0x74
    private const val LAPS_IN_RACE_OFFSET = 0x76
    private const val BEST_LAP_TIME_OFFSET = 0x78
    private const val CAR_CATEGORY_OFFSET = 0x16C
    private const val CAR_CATEGORY_LENGTH = 4
}
