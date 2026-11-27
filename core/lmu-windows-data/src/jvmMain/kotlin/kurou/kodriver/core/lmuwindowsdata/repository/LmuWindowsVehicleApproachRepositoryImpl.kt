package kurou.kodriver.core.lmuwindowsdata.repository

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.mapNotNull
import kurou.kodriver.core.lmuwindowsdata.datasource.LmuWindowsSharedMemorySource
import kurou.kodriver.core.lmuwindowsdata.mapper.LmuWindowsMapper
import kurou.kodriver.domain.lmuwindows.model.LMU_WINDOWS_VEHICLE_APPROACH_LATERAL_MINIMUM_METERS_DEFAULT
import kurou.kodriver.domain.lmuwindows.model.LmuWindowsVehicleApproachData
import kurou.kodriver.domain.lmuwindows.repository.LmuWindowsVehicleApproachRepository
import kurou.kodriver.domain.lmuwindows.repository.LmuWindowsVehicleApproachThresholdsPreferencesRepository
import kurou.kodriver.domain.model.LateralDistanceMeters
import java.nio.ByteBuffer
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin

@OptIn(ExperimentalCoroutinesApi::class)
internal class LmuWindowsVehicleApproachRepositoryImpl(
    private val thresholdsRepository: LmuWindowsVehicleApproachThresholdsPreferencesRepository,
    private val lateralMinimumMeters: Double = LMU_WINDOWS_VEHICLE_APPROACH_LATERAL_MINIMUM_METERS_DEFAULT,
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
        source.telemetryBufferFlow.mapNotNull { buffer ->
            val maxCount = maxVehicleCount(buffer)
            val activeVehicles = LmuWindowsMapper.readActiveVehicleCount(buffer).coerceAtMost(maxCount)
            val playerIdx = LmuWindowsMapper.readPlayerVehicleIdx(buffer)
            if (LmuWindowsMapper.readPlayerHasVehicle(buffer) && playerIdx < activeVehicles) {
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
        val player = readPlayerPose(buffer, playerIdx)

        // インデックスは Narrator の接近継続時間を車両ごとに追跡するキーとして渡す。
        val sideBySideVehicles =
            (0 until activeVehicles)
                .filter { it != playerIdx }
                .mapNotNull { i ->
                    sideBySideRelX(buffer, i, player, longitudinalThresholdMeters, lateralMaximumMeters)
                        ?.let { relX -> i to relX }
                }
        val (leftVehicles, rightVehicles) = sideBySideVehicles.partition { (_, relX) -> relX < 0 }

        return LmuWindowsVehicleApproachData(
            sideBySideLeftVehicleIds = leftVehicles.mapTo(mutableSetOf()) { (id, _) -> id },
            sideBySideRightVehicleIds = rightVehicles.mapTo(mutableSetOf()) { (id, _) -> id },
            lateralDistanceLeftMeters = LateralDistanceMeters(nearestLateralMeters(leftVehicles)),
            lateralDistanceRightMeters = LateralDistanceMeters(nearestLateralMeters(rightVehicles)),
        )
    }

    private fun nearestLateralMeters(vehicles: List<Pair<Int, Double>>): Double =
        vehicles.fold(Double.MAX_VALUE) { nearest, (_, relX) ->
            val absRelX = abs(relX)
            if (absRelX < nearest) absRelX else nearest
        }

    private fun readPlayerPose(
        buffer: ByteBuffer,
        playerIdx: Int,
    ): PlayerPose {
        val plrBase = LmuWindowsMapper.vehicleTelemetryBase(playerIdx)
        // LMU の地面上の座標 (X, Z) を (X, -Z) に投影し、姿勢行列の第3行から自車の yaw を求める。
        val yaw =
            atan2(
                buffer.getDouble(plrBase + OFF_ORI_ROW2_X),
                buffer.getDouble(plrBase + OFF_ORI_ROW2_Z),
            ) - PI
        return PlayerPose(
            posX = buffer.getDouble(plrBase + OFF_POS_X),
            posY = -buffer.getDouble(plrBase + OFF_POS_Z),
            sinYaw = sin(yaw),
            cosYaw = cos(yaw),
        )
    }

    /** 並走と判定した場合は自車基準の左右位置 relX（負が左）を返し、対象外なら null を返す。 */
    private fun sideBySideRelX(
        buffer: ByteBuffer,
        vehicleIdx: Int,
        player: PlayerPose,
        longitudinalThresholdMeters: Double,
        lateralMaximumMeters: Double,
    ): Double? {
        val optBase = LmuWindowsMapper.vehicleTelemetryBase(vehicleIdx)
        val dx = buffer.getDouble(optBase + OFF_POS_X) - player.posX
        val dy = -buffer.getDouble(optBase + OFF_POS_Z) - player.posY

        // 自車基準へ回転した relX は左右（負が左）、relY は前後。高さ方向は並走判定に使わない。
        val relX = player.cosYaw * dx - player.sinYaw * dy
        val relY = player.cosYaw * dy + player.sinYaw * dx

        if (abs(relY) >= longitudinalThresholdMeters) return null

        val absRelX = abs(relX)
        if (absRelX < lateralMinimumMeters || absRelX > lateralMaximumMeters) return null
        return relX
    }

    private data class PlayerPose(
        val posX: Double,
        val posY: Double,
        val sinYaw: Double,
        val cosYaw: Double,
    )

    companion object {
        private const val OFF_POS_X = 160
        private const val OFF_POS_Z = 176

        private const val OFF_ORI_ROW2_X = 280
        private const val OFF_ORI_ROW2_Z = 296

        fun maxVehicleCount(buffer: ByteBuffer): Int =
            LmuWindowsMapper.maxVehicleCount(buffer, headerSizePerVehicle = OFF_ORI_ROW2_Z + Double.SIZE_BYTES)
    }
}
