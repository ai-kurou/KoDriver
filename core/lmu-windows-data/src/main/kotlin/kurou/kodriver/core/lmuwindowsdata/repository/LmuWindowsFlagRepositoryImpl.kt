package kurou.kodriver.core.lmuwindowsdata.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.mapNotNull
import kurou.kodriver.core.lmuwindowsdata.datasource.LmuWindowsSharedMemorySource
import kurou.kodriver.domain.model.LmuWindowsRaceFlagsData
import kurou.kodriver.domain.model.PrimaryFlag
import kurou.kodriver.domain.model.SectorFlagState
import kurou.kodriver.domain.model.SessionPhase
import kurou.kodriver.domain.model.SessionYellowFlagState
import kurou.kodriver.domain.repository.LmuWindowsFlagRepository
import java.nio.ByteBuffer

internal class LmuWindowsFlagRepositoryImpl(
    private val source: LmuWindowsSharedMemorySource,
) : LmuWindowsFlagRepository {
    override fun flagStream(): Flow<LmuWindowsRaceFlagsData> = source.bufferFlow.mapNotNull { readFlags(it) }

    private fun readFlags(buffer: ByteBuffer): LmuWindowsRaceFlagsData? {
        val vehicleCount = buffer.getInt(SCORING_BASE + OFF_NUM_VEHICLES).coerceIn(0, MAX_VEHICLES)
        val playerVehicleBase = findPlayerVehicleBase(buffer, vehicleCount) ?: return null

        return LmuWindowsRaceFlagsData(
            gamePhase = SessionPhase.fromRaw(buffer.get(SCORING_BASE + OFF_GAME_PHASE).toInt() and 0xFF),
            yellowFlagState = SessionYellowFlagState.fromRaw(buffer.get(SCORING_BASE + OFF_YELLOW_FLAG_STATE).toInt()),
            sectorFlags =
                listOf(
                    SectorFlagState.fromRaw(buffer.get(SCORING_BASE + OFF_SECTOR_FLAGS).toInt()),
                    SectorFlagState.fromRaw(buffer.get(SCORING_BASE + OFF_SECTOR_FLAGS + 1).toInt()),
                    SectorFlagState.fromRaw(buffer.get(SCORING_BASE + OFF_SECTOR_FLAGS + 2).toInt()),
                ),
            playerFlag = PrimaryFlag.fromRaw(buffer.get(playerVehicleBase + OFF_PLAYER_FLAG).toInt() and 0xFF),
            playerUnderYellow = buffer.get(playerVehicleBase + OFF_PLAYER_UNDER_YELLOW).toInt() != 0,
        )
    }

    private fun findPlayerVehicleBase(
        buffer: ByteBuffer,
        vehicleCount: Int,
    ): Int? {
        for (index in 0 until vehicleCount) {
            val vehicleBase = VEHICLE_SCORING_BASE + index * VEHICLE_SCORING_STRIDE
            if (buffer.get(vehicleBase + OFF_IS_PLAYER).toInt() != 0) {
                return vehicleBase
            }
        }
        return null
    }

    companion object {
        private const val SCORING_BASE = 1_632
        private const val VEHICLE_SCORING_BASE = 2_192
        private const val VEHICLE_SCORING_STRIDE = 584
        private const val MAX_VEHICLES = 104

        private const val OFF_NUM_VEHICLES = 104
        private const val OFF_GAME_PHASE = 108
        private const val OFF_YELLOW_FLAG_STATE = 109
        private const val OFF_SECTOR_FLAGS = 110

        private const val OFF_IS_PLAYER = 196
        private const val OFF_PLAYER_FLAG = 504
        private const val OFF_PLAYER_UNDER_YELLOW = 505
    }
}
