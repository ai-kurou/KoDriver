package kurou.kodriver.data.repository

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.mapNotNull
import kurou.kodriver.data.datasource.SharedLmuWindowsMemorySource
import kurou.kodriver.domain.model.ProximityData
import kurou.kodriver.domain.repository.ProximityRepository
import kurou.kodriver.domain.repository.ProximityThresholdsRepository
import java.nio.ByteBuffer
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin

@OptIn(ExperimentalCoroutinesApi::class)
internal class SharedMemoryProximityRepository(
    private val thresholdsRepository: ProximityThresholdsRepository,
    private val lateralMinimumMeters: Double = 1.0,
    private val source: SharedLmuWindowsMemorySource,
) : ProximityRepository {

    override fun proximityStream(): Flow<ProximityData> =
        combine(
            thresholdsRepository.observeLongitudinalThresholdMeters(),
            thresholdsRepository.observeLateralThresholdMeters(),
        ) { longitudinal, lateral -> longitudinal to lateral }
            .flatMapLatest { (longitudinalThreshold, lateralMaximum) ->
                rawProximityFlow(longitudinalThreshold, lateralMaximum)
            }

    private fun rawProximityFlow(
        longitudinalThresholdMeters: Double,
        lateralMaximumMeters: Double,
    ): Flow<ProximityData> = source.bufferFlow.mapNotNull { buffer ->
        val maxCount = maxVehicleCount(buffer)
        val activeVehicles = (buffer.get(TELEMETRY_BASE + OFF_ACTIVE_VEHICLES).toInt() and 0xFF)
            .coerceAtMost(maxCount)
        val playerIdx = buffer.get(TELEMETRY_BASE + OFF_PLAYER_VEHICLE_IDX).toInt() and 0xFF
        if (activeVehicles > 0 && playerIdx < activeVehicles) {
            computeProximity(buffer, activeVehicles, playerIdx, longitudinalThresholdMeters, lateralMaximumMeters)
        } else {
            null
        }
    }

    private fun computeProximity(
        buffer: ByteBuffer,
        activeVehicles: Int,
        playerIdx: Int,
        longitudinalThresholdMeters: Double,
        lateralMaximumMeters: Double,
    ): ProximityData {
        val plrBase = TELEMETRY_BASE + OFF_TELEM_INFO + playerIdx * VEHICLE_STRIDE
        val plrPosX = buffer.getDouble(plrBase + OFF_POS_X)
        val plrPosY = -buffer.getDouble(plrBase + OFF_POS_Z)
        val plrOriYaw = atan2(
            buffer.getDouble(plrBase + OFF_ORI_ROW2_X),
            buffer.getDouble(plrBase + OFF_ORI_ROW2_Z),
        ) - PI

        val sinYaw = sin(plrOriYaw)
        val cosYaw = cos(plrOriYaw)
        val sideBySideThreshold = longitudinalThresholdMeters

        var nearestLeftMeters = Double.MAX_VALUE
        var nearestRightMeters = Double.MAX_VALUE
        val leftVehicleIds = mutableSetOf<Int>()
        val rightVehicleIds = mutableSetOf<Int>()

        for (i in 0 until activeVehicles) {
            if (i == playerIdx) continue

            val optBase = TELEMETRY_BASE + OFF_TELEM_INFO + i * VEHICLE_STRIDE
            val optPosX = buffer.getDouble(optBase + OFF_POS_X)
            val optPosY = -buffer.getDouble(optBase + OFF_POS_Z)

            val dx = optPosX - plrPosX
            val dy = optPosY - plrPosY

            val relX = cosYaw * dx - sinYaw * dy
            val relY = cosYaw * dy + sinYaw * dx

            if (abs(relY) >= sideBySideThreshold) continue

            val absRelX = abs(relX)
            if (absRelX < lateralMinimumMeters) continue
            if (absRelX > lateralMaximumMeters) continue
            if (relX < 0) {
                leftVehicleIds.add(i)
                if (absRelX < nearestLeftMeters) nearestLeftMeters = absRelX
            } else {
                rightVehicleIds.add(i)
                if (absRelX < nearestRightMeters) nearestRightMeters = absRelX
            }
        }

        return ProximityData(
            sideBySideLeftVehicleIds = leftVehicleIds,
            sideBySideRightVehicleIds = rightVehicleIds,
            lateralDistanceLeftMeters = nearestLeftMeters,
            lateralDistanceRightMeters = nearestRightMeters,
        )
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
