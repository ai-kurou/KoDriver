package kurou.kodriver.data.repository

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kurou.kodriver.data.datasource.MemoryReader
import kurou.kodriver.data.datasource.SharedMemoryReader
import kurou.kodriver.domain.model.ProximityData
import kurou.kodriver.domain.repository.ProximityRepository
import java.nio.ByteBuffer
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin

internal class SharedMemoryProximityRepository(
    private val pollingIntervalMs: Long = 16L,
    private val reconnectIntervalMs: Long = 1_000L,
    private val longitudinalThresholdMeters: Double = 1.0,
    private val lateralMinimumMeters: Double = 1.0,
    private val lateralMaximumMeters: Double = 5.0,
    private val reader: MemoryReader = SharedMemoryReader(
        segmentName = "LMU_Data",
        sizeBytes = 324_820,
    ),
) : ProximityRepository {

    override fun proximityStream(): Flow<ProximityData> = flow {
        try {
            while (true) {
                if (!reader.isOpen()) {
                    if (!reader.open()) {
                        delay(reconnectIntervalMs)
                        continue
                    }
                }
                reader.readBuffer()?.let { buffer ->
                    val maxCount = maxVehicleCount(buffer)
                    val activeVehicles = (buffer.get(TELEMETRY_BASE + OFF_ACTIVE_VEHICLES).toInt() and 0xFF)
                        .coerceAtMost(maxCount)
                    val playerIdx = buffer.get(TELEMETRY_BASE + OFF_PLAYER_VEHICLE_IDX).toInt() and 0xFF
                    if (activeVehicles > 0 && playerIdx < activeVehicles) {
                        emit(computeProximity(buffer, activeVehicles, playerIdx))
                    }
                }
                delay(pollingIntervalMs)
            }
        } finally {
            reader.close()
        }
    }.flowOn(Dispatchers.IO)

    private fun computeProximity(buffer: ByteBuffer, activeVehicles: Int, playerIdx: Int): ProximityData {
        val plrBase = TELEMETRY_BASE + OFF_TELEM_INFO + playerIdx * VEHICLE_STRIDE
        val plrPosX = buffer.getDouble(plrBase + OFF_POS_X)
        val plrPosY = -buffer.getDouble(plrBase + OFF_POS_Z)
        // mOri[2] は進行方向ベクトル。atan2(x, z) でヨー角を得る (TinyPedal: oriyaw2rad)
        val plrOriYaw = atan2(
            buffer.getDouble(plrBase + OFF_ORI_ROW2_X),
            buffer.getDouble(plrBase + OFF_ORI_ROW2_Z),
        ) - PI // TinyPedalと同じ: レーダー表示向けに180度回転

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

            // プレイヤー座標系へ回転 (TinyPedal: rotate_coordinate)
            val relX = cosYaw * dx - sinYaw * dy // 負=左, 正=右
            val relY = cosYaw * dy + sinYaw * dx // 負=前, 正=後

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

        // mPos オフセット (LmuMapper と同一)
        private const val OFF_POS_X = 160
        private const val OFF_POS_Z = 176

        // mOri[2] = 進行方向ベクトル行 (mPos=160+24, mLocalVel=184+24, mLocalAccel=208+24 → mOri=232)
        // mOri[2] は3行目: 232 + 2*24 = 280
        private const val OFF_ORI_ROW2_X = 280
        private const val OFF_ORI_ROW2_Z = 296

        fun maxVehicleCount(buffer: ByteBuffer): Int {
            val headerSize = TELEMETRY_BASE + OFF_TELEM_INFO + OFF_ORI_ROW2_Z + Double.SIZE_BYTES
            return maxOf(0, (buffer.limit() - headerSize) / VEHICLE_STRIDE)
        }
    }
}
