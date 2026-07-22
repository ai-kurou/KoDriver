package kurou.kodriver.core.lmuwindowsdata.repository

import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kurou.kodriver.core.lmuwindowsdata.datasource.LmuWindowsSharedMemorySource
import kurou.kodriver.core.lmuwindowsdata.datasource.SharedMemoryReader
import java.nio.ByteBuffer
import java.nio.ByteOrder
import kotlin.math.abs
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * テスト座標系:
 *   playerOriRow2X=0.0, playerOriRow2Z=-1.0 → atan2(0,-1)=PI → plrOriYaw=0
 *   cosYaw=1.0(exact), sinYaw=0.0(exact)
 *   relX(横) = opp.posX  (負=左, 正=右)
 *   relY(前後) = -opp.posZ (負=前, 正=後)
 */
class LmuWindowsNearbyVehiclesRepositoryImplTest {

    private fun makeSource(
        reader: SharedMemoryReader,
        pollingIntervalMs: Long = 1L,
        reconnectIntervalMs: Long = 1L,
    ) = LmuWindowsSharedMemorySource(
        pollingIntervalMs = pollingIntervalMs,
        reconnectIntervalMs = reconnectIntervalMs,
        reader = reader,
        scope = CoroutineScope(SupervisorJob()),
    )

    @Test
    fun `フローがキャンセルされると reader の close が呼ばれる`() = runBlocking {
        val reader = FakeNearbyVehiclesMemoryReader(buildBuffer(activeVehicles = 1, playerIdx = 0))
        val repo = LmuWindowsNearbyVehiclesRepositoryImpl(source = makeSource(reader))

        val job = launch { repo.nearbyVehiclesStream().collect { } }
        delay(50)
        job.cancelAndJoin()
        reader.closed.await()

        assertTrue(reader.closeCalled)
    }

    @Test
    fun `activeVehiclesが0の場合は emit しない`() = runBlocking {
        val reader = FakeNearbyVehiclesMemoryReader(buildBuffer(activeVehicles = 0, playerIdx = 0))
        val repo = LmuWindowsNearbyVehiclesRepositoryImpl(source = makeSource(reader))
        var emitCount = 0

        val job = launch { repo.nearbyVehiclesStream().collect { emitCount++ } }
        delay(50)
        job.cancelAndJoin()

        assertEquals(0, emitCount)
    }

    @Test
    fun `playerIdxがactiveVehicles以上の場合は emit しない`() = runBlocking {
        val reader = FakeNearbyVehiclesMemoryReader(buildBuffer(activeVehicles = 1, playerIdx = 2))
        val repo = LmuWindowsNearbyVehiclesRepositoryImpl(source = makeSource(reader))
        var emitCount = 0

        val job = launch { repo.nearbyVehiclesStream().collect { emitCount++ } }
        delay(50)
        job.cancelAndJoin()

        assertEquals(0, emitCount)
    }

    @Test
    fun `他車両がいない場合は空リストを返す`() = runBlocking {
        val reader = FakeNearbyVehiclesMemoryReader(buildBuffer(activeVehicles = 1, playerIdx = 0))
        val repo = LmuWindowsNearbyVehiclesRepositoryImpl(source = makeSource(reader))

        val result = repo.nearbyVehiclesStream().first()

        assertEquals(emptyList(), result.vehicles)
    }

    @Test
    fun `10m以内の車両は結果に含まれる`() = runBlocking {
        // relX = 3.0, relY = -(-4.0) = 4.0 → いずれも10m以内
        val buffer = buildBuffer(
            activeVehicles = 2,
            playerIdx = 0,
            opponents = listOf(NearbyVehiclePos(posX = 3.0, posZ = -4.0)),
        )
        val repo = LmuWindowsNearbyVehiclesRepositoryImpl(source = makeSource(FakeNearbyVehiclesMemoryReader(buffer)))

        val result = repo.nearbyVehiclesStream().first()

        assertEquals(1, result.vehicles.size)
        val vehicle = result.vehicles.first()
        assertEquals(1, vehicle.vehicleId)
        assertTrue(abs(vehicle.lateralDistanceMeters - 3.0) < 1e-9)
        assertTrue(abs(vehicle.longitudinalDistanceMeters - 4.0) < 1e-9)
    }

    @Test
    fun `横方向10mを超える車両は結果から除外される`() = runBlocking {
        val buffer = buildBuffer(
            activeVehicles = 2,
            playerIdx = 0,
            opponents = listOf(NearbyVehiclePos(posX = 10.1, posZ = 0.0)),
        )
        val repo = LmuWindowsNearbyVehiclesRepositoryImpl(source = makeSource(FakeNearbyVehiclesMemoryReader(buffer)))

        val result = repo.nearbyVehiclesStream().first()

        assertEquals(emptyList(), result.vehicles)
    }

    @Test
    fun `前後方向10mを超える車両は結果から除外される`() = runBlocking {
        val buffer = buildBuffer(
            activeVehicles = 2,
            playerIdx = 0,
            opponents = listOf(NearbyVehiclePos(posX = 0.0, posZ = -10.1)),
        )
        val repo = LmuWindowsNearbyVehiclesRepositoryImpl(source = makeSource(FakeNearbyVehiclesMemoryReader(buffer)))

        val result = repo.nearbyVehiclesStream().first()

        assertEquals(emptyList(), result.vehicles)
    }

    @Test
    fun `複数台が10m以内にいる場合は複数件返る`() = runBlocking {
        val buffer = buildBuffer(
            activeVehicles = 3,
            playerIdx = 0,
            opponents = listOf(
                NearbyVehiclePos(posX = -2.0, posZ = 1.0),
                NearbyVehiclePos(posX = 5.0, posZ = -5.0),
            ),
        )
        val repo = LmuWindowsNearbyVehiclesRepositoryImpl(source = makeSource(FakeNearbyVehiclesMemoryReader(buffer)))

        val result = repo.nearbyVehiclesStream().first()

        assertEquals(2, result.vehicles.size)
        assertEquals(setOf(1, 2), result.vehicles.map { it.vehicleId }.toSet())
    }

    @Test
    fun `ヘッダー未満のバッファでは最大車両数は0になる`() {
        assertEquals(0, LmuWindowsNearbyVehiclesRepositoryImpl.maxVehicleCount(ByteBuffer.allocate(1)))
    }
}

private data class NearbyVehiclePos(val posX: Double = 0.0, val posZ: Double = 0.0)

private const val TELEMETRY_BASE = 128_464
private const val OFF_ACTIVE_VEHICLES = 0
private const val OFF_PLAYER_VEHICLE_IDX = 1
private const val OFF_TELEM_INFO = 4
private const val VEHICLE_STRIDE = 1_888
private const val OFF_POS_X = 160
private const val OFF_POS_Z = 176
private const val OFF_ORI_ROW2_X = 280
private const val OFF_ORI_ROW2_Z = 296

private fun buildBuffer(
    activeVehicles: Int,
    playerIdx: Int,
    opponents: List<NearbyVehiclePos> = emptyList(),
): ByteBuffer {
    val buffer = ByteBuffer.allocate(135_000).order(ByteOrder.LITTLE_ENDIAN)

    buffer.put(TELEMETRY_BASE + OFF_ACTIVE_VEHICLES, activeVehicles.toByte())
    buffer.put(TELEMETRY_BASE + OFF_PLAYER_VEHICLE_IDX, playerIdx.toByte())

    val plrBase = TELEMETRY_BASE + OFF_TELEM_INFO + playerIdx * VEHICLE_STRIDE
    buffer.putDouble(plrBase + OFF_ORI_ROW2_X, 0.0) // atan2(0,-1)=PI
    buffer.putDouble(plrBase + OFF_ORI_ROW2_Z, -1.0)

    for ((i, opp) in opponents.withIndex()) {
        val oppIdx = if (i < playerIdx) i else i + 1
        val optBase = TELEMETRY_BASE + OFF_TELEM_INFO + oppIdx * VEHICLE_STRIDE
        buffer.putDouble(optBase + OFF_POS_X, opp.posX)
        buffer.putDouble(optBase + OFF_POS_Z, opp.posZ)
    }

    return buffer
}

private class FakeNearbyVehiclesMemoryReader(
    private val buffer: ByteBuffer,
) : SharedMemoryReader {

    var closeCalled = false
    val closed = CompletableDeferred<Unit>()
    private var opened = true

    override fun open(): Boolean {
        opened = true
        return true
    }

    override fun readBuffer(): ByteBuffer? {
        if (!opened) return null
        // duplicate() はバッキングアレイを共有するが、スレッド間の可視性が不確定なため
        // 毎回独立したコピーを返す
        return ByteBuffer.wrap(buffer.array().copyOf()).order(ByteOrder.LITTLE_ENDIAN)
    }

    override fun isOpen(): Boolean = opened

    override fun close() {
        closeCalled = true
        opened = false
        closed.complete(Unit)
    }
}
