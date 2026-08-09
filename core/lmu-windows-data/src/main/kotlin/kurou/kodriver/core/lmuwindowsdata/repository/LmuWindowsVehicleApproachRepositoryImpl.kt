package kurou.kodriver.core.lmuwindowsdata.repository

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.mapNotNull
import kurou.kodriver.core.lmuwindowsdata.datasource.LmuWindowsSharedMemorySource
import kurou.kodriver.core.lmuwindowsdata.mapper.LmuWindowsMapper
import kurou.kodriver.domain.model.LmuWindowsVehicleApproachData
import kurou.kodriver.domain.repository.LmuWindowsVehicleApproachRepository
import kurou.kodriver.domain.repository.LmuWindowsVehicleApproachThresholdsPreferencesRepository
import java.nio.ByteBuffer
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin

@OptIn(ExperimentalCoroutinesApi::class)
internal class LmuWindowsVehicleApproachRepositoryImpl(
    private val thresholdsRepository: LmuWindowsVehicleApproachThresholdsPreferencesRepository,
    private val lateralMinimumMeters: Double = 1.0,
    private val source: LmuWindowsSharedMemorySource,
) : LmuWindowsVehicleApproachRepository {
    override fun vehicleApproachStream(): Flow<LmuWindowsVehicleApproachData> =
        combine(
            thresholdsRepository.observeLongitudinalThresholdMeters(),
            thresholdsRepository.observeLateralThresholdMeters(),
        ) { longitudinal, lateral -> longitudinal to lateral }
            .flatMapLatest { (longitudinalThreshold, lateralMaximum) ->
                rawVehicleApproachFlow(longitudinalThreshold, lateralMaximum)
            }

    private fun rawVehicleApproachFlow(
        longitudinalThresholdMeters: Double,
        lateralMaximumMeters: Double,
    ): Flow<LmuWindowsVehicleApproachData> =
        source.bufferFlow.mapNotNull { buffer ->
            val maxCount = maxVehicleCount(buffer)
            val activeVehicles = LmuWindowsMapper.readActiveVehicleCount(buffer).coerceAtMost(maxCount)
            val playerIdx = LmuWindowsMapper.readPlayerVehicleIdx(buffer)
            if (activeVehicles > 0 && playerIdx < activeVehicles) {
                computeVehicleApproach(
                    buffer,
                    activeVehicles,
                    playerIdx,
                    longitudinalThresholdMeters,
                    lateralMaximumMeters,
                )
            } else {
                null
            }
        }

    private fun computeVehicleApproach(
        buffer: ByteBuffer,
        activeVehicles: Int,
        playerIdx: Int,
        longitudinalThresholdMeters: Double,
        lateralMaximumMeters: Double,
    ): LmuWindowsVehicleApproachData {
        val plrBase = LmuWindowsMapper.vehicleTelemetryBase(playerIdx)
        val plrPosX = buffer.getDouble(plrBase + OFF_POS_X)
        val plrPosY = -buffer.getDouble(plrBase + OFF_POS_Z)
        val plrOriYaw =
            atan2(
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

            val optBase = LmuWindowsMapper.vehicleTelemetryBase(i)
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

        return LmuWindowsVehicleApproachData(
            sideBySideLeftVehicleIds = leftVehicleIds,
            sideBySideRightVehicleIds = rightVehicleIds,
            lateralDistanceLeftMeters = nearestLeftMeters,
            lateralDistanceRightMeters = nearestRightMeters,
        )
    }

    companion object {
        private const val OFF_POS_X = 160
        private const val OFF_POS_Z = 176

        private const val OFF_ORI_ROW2_X = 280
        private const val OFF_ORI_ROW2_Z = 296

        fun maxVehicleCount(buffer: ByteBuffer): Int =
            LmuWindowsMapper.maxVehicleCount(buffer, headerSizePerVehicle = OFF_ORI_ROW2_Z + Double.SIZE_BYTES)
    }
}
