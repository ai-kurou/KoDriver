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
import kurou.kodriver.core.windowssharedmemory.datasource.SharedMemoryReader
import kurou.kodriver.domain.lmuwindows.model.LmuWindowsNearbyVehicleData
import java.nio.ByteBuffer
import java.nio.ByteOrder
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * テスト座標系:
 *   playerOriRow2X=0.0, playerOriRow2Z=-1.0 → atan2(0,-1)=PI → plrOriYaw=0
 *   cosYaw=1.0(exact), sinYaw=0.0(exact)
 *   relX = opp.posX  (負=左, 正=右)
 *   relY = -opp.posZ (負=前, 正=後)
 */
class LmuWindowsVehicleApproachRepositoryImplTest {
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

    // -------------------------------------------------------------------------
    // フロー制御
    // -------------------------------------------------------------------------

    @Test
    fun `フローがキャンセルされると reader の close が呼ばれる`() =
        runBlocking {
            val reader = FakeVehicleApproachMemoryReader(buildBuffer(activeVehicles = 1, playerIdx = 0))
            val repo =
                LmuWindowsVehicleApproachRepositoryImpl(
                    source = makeSource(reader),
                )

            val job = launch { repo.nearbyVehiclesStream().collect { } }
            delay(50)
            job.cancelAndJoin()
            reader.closed.await()

            assertTrue(reader.closeCalled)
        }

    @Test
    fun `readBuffer が null の間は emit しない`() =
        runBlocking {
            val reader =
                FakeVehicleApproachMemoryReader(
                    buffer = buildBuffer(activeVehicles = 1, playerIdx = 0),
                    returnNullBuffer = true,
                )
            val repo =
                LmuWindowsVehicleApproachRepositoryImpl(
                    source = makeSource(reader),
                )
            var emitCount = 0

            val job = launch { repo.nearbyVehiclesStream().collect { emitCount++ } }
            delay(50)
            job.cancelAndJoin()
            reader.closed.await()

            assertEquals(0, emitCount)
            assertTrue(reader.closeCalled)
        }

    // -------------------------------------------------------------------------
    // 周辺車両の位置: relX = opp.posX, relY = -opp.posZ
    // -------------------------------------------------------------------------

    @Test
    fun `自車以外の車両の位置を車両IDつきで返す`() =
        runBlocking {
            val buffer =
                buildBuffer(
                    activeVehicles = 3,
                    playerIdx = 1,
                    opponents = listOf(VehiclePos(posX = -3.0, posZ = 2.0), VehiclePos(posX = 0.5, posZ = -4.0)),
                )
            val repo =
                LmuWindowsVehicleApproachRepositoryImpl(source = makeSource(FakeVehicleApproachMemoryReader(buffer)))

            val result = repo.nearbyVehiclesStream().first()

            assertEquals(
                listOf(
                    LmuWindowsNearbyVehicleData(vehicleId = 0, lateralMeters = -3.0, longitudinalMeters = -2.0),
                    LmuWindowsNearbyVehicleData(vehicleId = 2, lateralMeters = 0.5, longitudinalMeters = 4.0),
                ),
                result.vehicles,
            )
        }

    @Test
    fun `閾値の設定可能な上限より遠い車両は含めず上限ちょうどの車両は含める`() =
        runBlocking {
            val buffer =
                buildBuffer(
                    activeVehicles = 5,
                    playerIdx = 0,
                    opponents =
                        listOf(
                            VehiclePos(posX = 8.0, posZ = -10.0),
                            VehiclePos(posX = 8.1, posZ = 0.0),
                            VehiclePos(posX = -8.1, posZ = 0.0),
                            VehiclePos(posX = 0.0, posZ = -10.1),
                        ),
                    // 5台分のテレメトリが収まるサイズにする
                    bufferSize = 140_000,
                )
            val repo =
                LmuWindowsVehicleApproachRepositoryImpl(source = makeSource(FakeVehicleApproachMemoryReader(buffer)))

            val result = repo.nearbyVehiclesStream().first()

            assertEquals(listOf(1), result.vehicles.map { it.vehicleId })
        }

    // -------------------------------------------------------------------------
    // 境界チェック
    // -------------------------------------------------------------------------

    @Test
    fun `playerIdx が activeVehicles 以上の場合は emit しない`() =
        runBlocking {
            // activeVehicles=1, playerIdx=2 → 2 >= 1 → スキップ
            val buffer = buildBuffer(activeVehicles = 1, playerIdx = 2)
            val fakeReader = FakeVehicleApproachMemoryReader(buffer)
            val repo =
                LmuWindowsVehicleApproachRepositoryImpl(
                    source = makeSource(fakeReader),
                )

            val job = launch { repo.nearbyVehiclesStream().collect { } }
            delay(50)
            job.cancelAndJoin()
            // WhileSubscribed が IO スレッドへ cancellation を伝播するまで待機
            delay(100)

            assertTrue(fakeReader.closeCalled)
        }

    @Test
    fun `playerHasVehicle が false の場合は emit しない`() =
        runBlocking {
            val buffer = buildBuffer(activeVehicles = 2, playerIdx = 0, opponents = listOf(VehiclePos(posX = 3.0)))
            buffer.put(TELEMETRY_BASE + OFF_PLAYER_HAS_VEHICLE, 0)
            val fakeReader = FakeVehicleApproachMemoryReader(buffer)
            val repo =
                LmuWindowsVehicleApproachRepositoryImpl(
                    source = makeSource(fakeReader),
                )
            var emitCount = 0

            val job = launch { repo.nearbyVehiclesStream().collect { emitCount++ } }
            delay(50)
            job.cancelAndJoin()

            assertEquals(0, emitCount)
        }

    @Test
    fun `activeVehicles が0の場合は emit しない`() =
        runBlocking {
            val fakeReader = FakeVehicleApproachMemoryReader(buildBuffer(activeVehicles = 0, playerIdx = 0))
            val repo =
                LmuWindowsVehicleApproachRepositoryImpl(
                    source = makeSource(fakeReader),
                )
            var emitCount = 0

            val job = launch { repo.nearbyVehiclesStream().collect { emitCount++ } }
            delay(50)
            job.cancelAndJoin()

            assertEquals(0, emitCount)
        }

    @Test
    fun `activeVehicles がバッファ範囲を超える値でもクランプされ例外が出ない`() =
        runBlocking {
            // バッファ135_000バイト → maxVehicleCount=4。activeVehicles=255はクランプされる
            // BufferUnderflowException が発生しないことを確認
            val buffer = buildBuffer(activeVehicles = 255, playerIdx = 0)
            val repo =
                LmuWindowsVehicleApproachRepositoryImpl(
                    source = makeSource(FakeVehicleApproachMemoryReader(buffer)),
                )

            repo.nearbyVehiclesStream().first()
            Unit
        }

    @Test
    fun `最大車両数は共有メモリの実サイズでは104になりヘッダー未満のバッファでは0になる`() {
        assertEquals(104, LmuWindowsVehicleApproachRepositoryImpl.maxVehicleCount(ByteBuffer.allocate(324_820)))
        assertEquals(0, LmuWindowsVehicleApproachRepositoryImpl.maxVehicleCount(ByteBuffer.allocate(1)))
    }
}

// -----------------------------------------------------------------------------
// ヘルパー
// -----------------------------------------------------------------------------

private data class VehiclePos(
    val posX: Double = 0.0,
    val posZ: Double = 0.0,
)

private const val TELEMETRY_BASE = 128_464
private const val OFF_ACTIVE_VEHICLES = 0
private const val OFF_PLAYER_VEHICLE_IDX = 1
private const val OFF_PLAYER_HAS_VEHICLE = 2
private const val OFF_TELEM_INFO = 4
private const val VEHICLE_STRIDE = 1_888
private const val OFF_POS_X = 160
private const val OFF_POS_Z = 176
private const val OFF_ORI_ROW2_X = 280
private const val OFF_ORI_ROW2_Z = 296

/**
 * oriRow2X=0.0, oriRow2Z=-1.0 → cosYaw=1.0(exact), sinYaw=0.0(exact)
 * relX = opp.posX, relY = -opp.posZ
 */
private fun buildBuffer(
    activeVehicles: Int,
    playerIdx: Int,
    opponents: List<VehiclePos> = emptyList(),
    bufferSize: Int = 135_000,
): ByteBuffer {
    val buffer = ByteBuffer.allocate(bufferSize).order(ByteOrder.LITTLE_ENDIAN)

    buffer.put(TELEMETRY_BASE + OFF_ACTIVE_VEHICLES, activeVehicles.toByte())
    buffer.put(TELEMETRY_BASE + OFF_PLAYER_VEHICLE_IDX, playerIdx.toByte())
    buffer.put(TELEMETRY_BASE + OFF_PLAYER_HAS_VEHICLE, 1)

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

private class FakeVehicleApproachMemoryReader(
    private val buffer: ByteBuffer,
    private val openResult: Boolean = true,
    private val returnNullBuffer: Boolean = false,
) : SharedMemoryReader {
    var closeCalled = false
    val closed = CompletableDeferred<Unit>()
    private var opened = openResult

    override fun open(): Boolean {
        opened = openResult
        return openResult
    }

    override fun readBuffer(): ByteBuffer? {
        if (!opened || returnNullBuffer) return null
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
