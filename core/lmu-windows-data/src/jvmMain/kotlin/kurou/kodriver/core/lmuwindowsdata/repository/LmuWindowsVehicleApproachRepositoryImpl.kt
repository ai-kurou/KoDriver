package kurou.kodriver.core.lmuwindowsdata.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.mapNotNull
import kurou.kodriver.core.lmuwindowsdata.datasource.LmuWindowsSharedMemorySource
import kurou.kodriver.core.lmuwindowsdata.mapper.LmuWindowsMapper
import kurou.kodriver.domain.lmuwindows.model.LMU_WINDOWS_VEHICLE_APPROACH_LATERAL_THRESHOLD_METERS_MAX
import kurou.kodriver.domain.lmuwindows.model.LMU_WINDOWS_VEHICLE_APPROACH_LONGITUDINAL_THRESHOLD_METERS_MAX
import kurou.kodriver.domain.lmuwindows.model.LmuWindowsNearbyVehicleData
import kurou.kodriver.domain.lmuwindows.model.LmuWindowsNearbyVehiclesData
import kurou.kodriver.domain.lmuwindows.repository.LmuWindowsVehicleApproachRepository
import java.nio.ByteBuffer
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin

internal class LmuWindowsVehicleApproachRepositoryImpl(
    private val source: LmuWindowsSharedMemorySource,
) : LmuWindowsVehicleApproachRepository {
    override fun nearbyVehiclesStream(): Flow<LmuWindowsNearbyVehiclesData> =
        source.telemetryBufferFlow.mapNotNull { buffer ->
            val maxCount = maxVehicleCount(buffer)
            val activeVehicles = LmuWindowsMapper.readActiveVehicleCount(buffer).coerceAtMost(maxCount)
            val playerIdx = LmuWindowsMapper.readPlayerVehicleIdx(buffer)
            if (LmuWindowsMapper.readPlayerHasVehicle(buffer) && playerIdx < activeVehicles) {
                readNearbyVehicles(buffer, activeVehicles, playerIdx)
            } else {
                null
            }
        }

    private fun readNearbyVehicles(
        buffer: ByteBuffer,
        activeVehicles: Int,
        playerIdx: Int,
    ): LmuWindowsNearbyVehiclesData {
        val player = readPlayerPose(buffer, playerIdx)
        return LmuWindowsNearbyVehiclesData(
            vehicles =
                (0 until activeVehicles)
                    .filter { it != playerIdx }
                    .mapNotNull { i -> nearbyVehicleOrNull(buffer, i, player) },
        )
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

    /** 閾値の設定可能な上限より遠い車両は、どの設定でも並走にならないため null を返す。 */
    private fun nearbyVehicleOrNull(
        buffer: ByteBuffer,
        vehicleIdx: Int,
        player: PlayerPose,
    ): LmuWindowsNearbyVehicleData? {
        val optBase = LmuWindowsMapper.vehicleTelemetryBase(vehicleIdx)
        val dx = buffer.getDouble(optBase + OFF_POS_X) - player.posX
        val dy = -buffer.getDouble(optBase + OFF_POS_Z) - player.posY

        // 自車基準へ回転した relX は左右（負が左）、relY は前後。高さ方向は使わない。
        val relX = player.cosYaw * dx - player.sinYaw * dy
        val relY = player.cosYaw * dy + player.sinYaw * dx

        if (abs(relY) > LMU_WINDOWS_VEHICLE_APPROACH_LONGITUDINAL_THRESHOLD_METERS_MAX) return null
        if (abs(relX) > LMU_WINDOWS_VEHICLE_APPROACH_LATERAL_THRESHOLD_METERS_MAX) return null
        // インデックスは Narrator の接近継続時間を車両ごとに追跡するキーとして渡す。
        return LmuWindowsNearbyVehicleData(vehicleId = vehicleIdx, lateralMeters = relX, longitudinalMeters = relY)
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
