package kurou.kodriver.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.mapNotNull
import kurou.kodriver.data.datasource.SharedLmuWindowsMemorySource
import kurou.kodriver.domain.model.CountLapFlag
import kurou.kodriver.domain.model.PrimaryFlag
import kurou.kodriver.domain.model.RaceFlagsData
import kurou.kodriver.domain.model.SectorFlagState
import kurou.kodriver.domain.model.SessionPhase
import kurou.kodriver.domain.model.SessionYellowFlagState
import kurou.kodriver.domain.repository.FlagRepository
import java.nio.ByteBuffer

internal class SharedMemoryFlagRepository(
    private val source: SharedLmuWindowsMemorySource,
) : FlagRepository {

    override fun flagStream(): Flow<RaceFlagsData> =
        source.bufferFlow.mapNotNull { readFlags(it) }

    private fun readFlags(buffer: ByteBuffer): RaceFlagsData? {
        val vehicleCount = buffer.getInt(SCORING_BASE + OFF_NUM_VEHICLES).coerceIn(0, MAX_VEHICLES)
        val playerVehicleBase = findPlayerVehicleBase(buffer, vehicleCount) ?: return null

        return RaceFlagsData(
            gamePhase = SessionPhase.fromRaw(buffer.get(SCORING_BASE + OFF_GAME_PHASE).toInt() and 0xFF),
            yellowFlagState = SessionYellowFlagState.fromRaw(buffer.get(SCORING_BASE + OFF_YELLOW_FLAG_STATE).toInt()),
            sectorFlags = listOf(
                SectorFlagState.fromRaw(buffer.get(SCORING_BASE + OFF_SECTOR_FLAGS).toInt()),
                SectorFlagState.fromRaw(buffer.get(SCORING_BASE + OFF_SECTOR_FLAGS + 1).toInt()),
                SectorFlagState.fromRaw(buffer.get(SCORING_BASE + OFF_SECTOR_FLAGS + 2).toInt()),
            ),
            startLight = buffer.get(SCORING_BASE + OFF_START_LIGHT).toInt() and 0xFF,
            numRedLights = buffer.get(SCORING_BASE + OFF_NUM_RED_LIGHTS).toInt() and 0xFF,
            playerFlag = PrimaryFlag.fromRaw(buffer.get(playerVehicleBase + OFF_PLAYER_FLAG).toInt() and 0xFF),
            playerUnderYellow = buffer.get(playerVehicleBase + OFF_PLAYER_UNDER_YELLOW).toInt() != 0,
            playerCountLapFlag = CountLapFlag.fromRaw(
                buffer.get(playerVehicleBase + OFF_PLAYER_COUNT_LAP_FLAG).toInt() and 0xFF,
            ),
        )
    }

    private fun findPlayerVehicleBase(buffer: ByteBuffer, vehicleCount: Int): Int? {
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
        private const val OFF_START_LIGHT = 113
        private const val OFF_NUM_RED_LIGHTS = 114

        private const val OFF_IS_PLAYER = 196
        private const val OFF_PLAYER_FLAG = 504
        private const val OFF_PLAYER_UNDER_YELLOW = 505
        private const val OFF_PLAYER_COUNT_LAP_FLAG = 506
    }
}
