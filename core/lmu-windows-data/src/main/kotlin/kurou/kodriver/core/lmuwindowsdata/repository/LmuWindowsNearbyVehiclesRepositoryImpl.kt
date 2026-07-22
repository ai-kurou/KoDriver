package kurou.kodriver.core.lmuwindowsdata.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.mapNotNull
import kurou.kodriver.core.lmuwindowsdata.datasource.LmuWindowsSharedMemorySource
import kurou.kodriver.domain.model.LmuWindowsNearbyVehicleData
import kurou.kodriver.domain.model.LmuWindowsNearbyVehiclesData
import kurou.kodriver.domain.repository.LmuWindowsNearbyVehiclesRepository
import java.nio.ByteBuffer
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin

internal class LmuWindowsNearbyVehiclesRepositoryImpl(
    private val nearbyThresholdMeters: Double = 10.0,
    private val source: LmuWindowsSharedMemorySource,
) : LmuWindowsNearbyVehiclesRepository {

    override fun nearbyVehiclesStream(): Flow<LmuWindowsNearbyVehiclesData> =
        source.bufferFlow.mapNotNull { buffer ->
            val maxCount = maxVehicleCount(buffer)
            val activeVehicles = (buffer.get(TELEMETRY_BASE + OFF_ACTIVE_VEHICLES).toInt() and 0xFF)
                .coerceAtMost(maxCount)
            val playerIdx = buffer.get(TELEMETRY_BASE + OFF_PLAYER_VEHICLE_IDX).toInt() and 0xFF
            if (activeVehicles > 0 && playerIdx < activeVehicles) {
                computeNearbyVehicles(buffer, activeVehicles, playerIdx)
            } else {
                null
            }
        }

    private fun computeNearbyVehicles(
        buffer: ByteBuffer,
        activeVehicles: Int,
        playerIdx: Int,
    ): LmuWindowsNearbyVehiclesData {
        val plrBase = TELEMETRY_BASE + OFF_TELEM_INFO + playerIdx * VEHICLE_STRIDE
        val plrPosX = buffer.getDouble(plrBase + OFF_POS_X)
        val plrPosY = -buffer.getDouble(plrBase + OFF_POS_Z)
        val plrOriYaw = atan2(
            buffer.getDouble(plrBase + OFF_ORI_ROW2_X),
            buffer.getDouble(plrBase + OFF_ORI_ROW2_Z),
        ) - PI

        val sinYaw = sin(plrOriYaw)
        val cosYaw = cos(plrOriYaw)

        val nearbyVehicles = buildList {
            for (i in 0 until activeVehicles) {
                if (i == playerIdx) continue

                val optBase = TELEMETRY_BASE + OFF_TELEM_INFO + i * VEHICLE_STRIDE
                val optPosX = buffer.getDouble(optBase + OFF_POS_X)
                val optPosY = -buffer.getDouble(optBase + OFF_POS_Z)

                val dx = optPosX - plrPosX
                val dy = optPosY - plrPosY

                val relX = cosYaw * dx - sinYaw * dy
                val relY = cosYaw * dy + sinYaw * dx

                if (abs(relX) > nearbyThresholdMeters || abs(relY) > nearbyThresholdMeters) continue

                add(
                    LmuWindowsNearbyVehicleData(
                        vehicleId = i,
                        longitudinalDistanceMeters = relY,
                        lateralDistanceMeters = relX,
                    ),
                )
            }
        }

        return LmuWindowsNearbyVehiclesData(vehicles = nearbyVehicles)
    }

    companion object {
        private const val TELEMETRY_BASE = 128_464

        private const val OFF_ACTIVE_VEHICLES = 0
        private const val OFF_PLAYER_VEHICLE_IDX = 1
        private const val OFF_TELEM_INFO = 4
        private const val VEHICLE_STRIDE = 1_888

        private const val OFF_POS_X = 160
        private const val OFF_POS_Z = 176

        private const val OFF_ORI_ROW2_X = 280
        private const val OFF_ORI_ROW2_Z = 296

        fun maxVehicleCount(buffer: ByteBuffer): Int {
            val headerSize = TELEMETRY_BASE + OFF_TELEM_INFO + OFF_ORI_ROW2_Z + Double.SIZE_BYTES
            return maxOf(0, (buffer.limit() - headerSize) / VEHICLE_STRIDE)
        }
    }
}
