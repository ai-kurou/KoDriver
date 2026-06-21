package kurou.kodriver.core.gt7ps5data.mapper

import kurou.kodriver.domain.model.Gt7Ps5TelemetryData
import java.nio.ByteBuffer

/**
 * GT7 UDP テレメトリパケット（復号済み・296バイト）を Gt7Ps5TelemetryData に変換する。
 *
 * オフセットは SimulatorInterface パケット仕様（GT7 / PS5）に基づく:
 *   0x74 (116) : LapCount        int16
 *   0x76 (118) : LapsInRace      int16  (0 = フリー走行・予選)
 *   0x78 (120) : BestLapTimeMs   int32  (-1 = ベストラップなし)
 *   0x84 (132) : GasLevel        float  (リットル)
 *   0x88 (136) : GasCapacity     float  (リットル、非燃料車は 100.0)
 *   0xC4 (196) : EnergyRecovery  float  (0.0〜1.0 / ERS 非搭載車は 0.0)
 */
internal object Gt7Ps5Mapper {

    fun map(packet: ByteBuffer): Gt7Ps5TelemetryData = Gt7Ps5TelemetryData(
        lapCount = packet.getShort(LAP_COUNT_OFFSET).toInt(),
        lapsInRace = packet.getShort(LAPS_IN_RACE_OFFSET).toInt(),
        bestLapTimeMs = packet.getInt(BEST_LAP_TIME_OFFSET),
        gasLevel = packet.getFloat(GAS_LEVEL_OFFSET),
        gasCapacity = packet.getFloat(GAS_CAPACITY_OFFSET),
        energyRecovery = packet.getFloat(ENERGY_RECOVERY_OFFSET),
    )

    private const val LAP_COUNT_OFFSET = 0x74
    private const val LAPS_IN_RACE_OFFSET = 0x76
    private const val BEST_LAP_TIME_OFFSET = 0x78
    private const val GAS_LEVEL_OFFSET = 0x84
    private const val GAS_CAPACITY_OFFSET = 0x88
    private const val ENERGY_RECOVERY_OFFSET = 0xC4
}
