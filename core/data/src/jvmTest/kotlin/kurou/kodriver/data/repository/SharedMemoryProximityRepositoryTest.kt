package kurou.kodriver.data.repository

import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kurou.kodriver.data.datasource.MemoryReader
import kurou.kodriver.data.datasource.SharedLmuWindowsMemorySource
import kurou.kodriver.domain.repository.ProximityThresholdsRepository
import java.nio.ByteBuffer
import java.nio.ByteOrder
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * テスト座標系:
 *   playerOriRow2X=0.0, playerOriRow2Z=-1.0 → atan2(0,-1)=PI → plrOriYaw=0
 *   cosYaw=1.0(exact), sinYaw=0.0(exact)
 *   relX = opp.posX  (負=左, 正=右)
 *   relY = -opp.posZ (負=前, 正=後)
 */
class SharedMemoryProximityRepositoryTest {

    private fun makeSource(
        reader: MemoryReader,
        pollingIntervalMs: Long = 1L,
        reconnectIntervalMs: Long = 1L,
    ) = SharedLmuWindowsMemorySource(
        pollingIntervalMs = pollingIntervalMs,
        reconnectIntervalMs = reconnectIntervalMs,
        reader = reader,
        scope = CoroutineScope(SupervisorJob()),
    )

    // -------------------------------------------------------------------------
    // フロー制御
    // -------------------------------------------------------------------------

    @Test
    fun `フローがキャンセルされると reader の close が呼ばれる`() = runBlocking {
        val reader = FakeProximityMemoryReader(buildBuffer(activeVehicles = 1, playerIdx = 0))
        val repo = SharedMemoryProximityRepository(
            thresholdsRepository = FakeProximityThresholdsRepository(),
            source = makeSource(reader),
        )

        val job = launch { repo.proximityStream().collect { } }
        delay(50)
        job.cancelAndJoin()
        reader.closed.await()

        assertTrue(reader.closeCalled)
    }

    @Test
    fun `reader が open できない間は emit しない`() = runBlocking {
        val reader = FakeProximityMemoryReader(
            buffer = buildBuffer(activeVehicles = 1, playerIdx = 0),
            openResult = false,
        )
        val repo = SharedMemoryProximityRepository(
            thresholdsRepository = FakeProximityThresholdsRepository(),
            source = makeSource(reader),
        )
        var emitCount = 0

        val job = launch { repo.proximityStream().collect { emitCount++ } }
        delay(50)
        job.cancelAndJoin()
        reader.closed.await()

        assertTrue(reader.closeCalled)
        assertEqual(0, emitCount)
    }

    @Test
    fun `readBuffer が null の間は emit しない`() = runBlocking {
        val reader = FakeProximityMemoryReader(
            buffer = buildBuffer(activeVehicles = 1, playerIdx = 0),
            returnNullBuffer = true,
        )
        val repo = SharedMemoryProximityRepository(
            thresholdsRepository = FakeProximityThresholdsRepository(),
            source = makeSource(reader),
        )
        var emitCount = 0

        val job = launch { repo.proximityStream().collect { emitCount++ } }
        delay(50)
        job.cancelAndJoin()
        reader.closed.await()

        assertEquals(0, emitCount)
        assertTrue(reader.closeCalled)
    }

    // -------------------------------------------------------------------------
    // 並走判定: 左右の検出
    // -------------------------------------------------------------------------

    @Test
    fun `プレイヤーの左に並走車がいる場合 isSideBySideLeft が true になる`() = runBlocking {
        // relX = opp.posX = -3.0 (左), relY = 0
        val buffer = buildBuffer(
            activeVehicles = 2,
            playerIdx = 0,
            opponents = listOf(VehiclePos(posX = -3.0, posZ = 0.0)),
        )
        val repo = SharedMemoryProximityRepository(
            thresholdsRepository = FakeProximityThresholdsRepository(longitudinal = 4.5),
            source = makeSource(FakeProximityMemoryReader(buffer)),
        )

        val result = repo.proximityStream().first()

        assertTrue(result.isSideBySideLeft)
        assertFalse(result.isSideBySideRight)
    }

    @Test
    fun `プレイヤーの右に並走車がいる場合 isSideBySideRight が true になる`() = runBlocking {
        // relX = opp.posX = +3.0 (右), relY = 0
        val buffer = buildBuffer(
            activeVehicles = 2,
            playerIdx = 0,
            opponents = listOf(VehiclePos(posX = 3.0, posZ = 0.0)),
        )
        val repo = SharedMemoryProximityRepository(
            thresholdsRepository = FakeProximityThresholdsRepository(longitudinal = 4.5),
            source = makeSource(FakeProximityMemoryReader(buffer)),
        )

        val result = repo.proximityStream().first()

        assertFalse(result.isSideBySideLeft)
        assertTrue(result.isSideBySideRight)
    }

    @Test
    fun `左右両方に並走車がいる場合 両方 true になる`() = runBlocking {
        val buffer = buildBuffer(
            activeVehicles = 3,
            playerIdx = 0,
            opponents = listOf(
                VehiclePos(posX = -3.0, posZ = 0.0),
                VehiclePos(posX = 3.0, posZ = 0.0),
            ),
        )
        val repo = SharedMemoryProximityRepository(
            thresholdsRepository = FakeProximityThresholdsRepository(longitudinal = 4.5),
            source = makeSource(FakeProximityMemoryReader(buffer)),
        )

        val result = repo.proximityStream().first()

        assertTrue(result.isSideBySideLeft)
        assertTrue(result.isSideBySideRight)
    }

    // -------------------------------------------------------------------------
    // 並走判定: 前後方向のしきい値 (relY = -opp.posZ)
    // -------------------------------------------------------------------------

    @Test
    fun `前後方向の距離がしきい値を超える車両は並走判定しない`() = runBlocking {
        // relY = -(-10.0) = 10.0 > 4.5*1.2=5.4 → スキップ
        val buffer = buildBuffer(
            activeVehicles = 2,
            playerIdx = 0,
            opponents = listOf(VehiclePos(posX = -3.0, posZ = -10.0)),
        )
        val repo = SharedMemoryProximityRepository(
            thresholdsRepository = FakeProximityThresholdsRepository(longitudinal = 4.5),
            source = makeSource(FakeProximityMemoryReader(buffer)),
        )

        val result = repo.proximityStream().first()

        assertFalse(result.isSideBySideLeft)
        assertFalse(result.isSideBySideRight)
    }

    @Test
    fun `前後方向の距離がしきい値ちょうどの車両は並走判定しない`() = runBlocking {
        val vehicleLength = 4.5
        // relY = 5.4 >= threshold=5.4 → スキップ (>= で判定)
        val buffer = buildBuffer(
            activeVehicles = 2,
            playerIdx = 0,
            opponents = listOf(VehiclePos(posX = -3.0, posZ = -5.4)),
        )
        val repo = SharedMemoryProximityRepository(
            thresholdsRepository = FakeProximityThresholdsRepository(longitudinal = vehicleLength),
            source = makeSource(FakeProximityMemoryReader(buffer)),
        )

        val result = repo.proximityStream().first()

        assertFalse(result.isSideBySideLeft)
    }

    // -------------------------------------------------------------------------
    // 境界チェック
    // -------------------------------------------------------------------------

    @Test
    fun `playerIdx が activeVehicles 以上の場合は emit しない`() = runBlocking {
        // activeVehicles=1, playerIdx=2 → 2 >= 1 → スキップ
        val buffer = buildBuffer(activeVehicles = 1, playerIdx = 2)
        val fakeReader = FakeProximityMemoryReader(buffer)
        val repo = SharedMemoryProximityRepository(
            thresholdsRepository = FakeProximityThresholdsRepository(),
            source = makeSource(fakeReader),
        )

        val job = launch { repo.proximityStream().collect { } }
        delay(50)
        job.cancelAndJoin()
        // WhileSubscribed が IO スレッドへ cancellation を伝播するまで待機
        delay(100)

        assertTrue(fakeReader.closeCalled)
    }

    @Test
    fun `activeVehicles が0の場合は emit しない`() = runBlocking {
        val fakeReader = FakeProximityMemoryReader(buildBuffer(activeVehicles = 0, playerIdx = 0))
        val repo = SharedMemoryProximityRepository(
            thresholdsRepository = FakeProximityThresholdsRepository(),
            source = makeSource(fakeReader),
        )
        var emitCount = 0

        val job = launch { repo.proximityStream().collect { emitCount++ } }
        delay(50)
        job.cancelAndJoin()

        assertEquals(0, emitCount)
    }

    @Test
    fun `activeVehicles がバッファ範囲を超える値でもクランプされ例外が出ない`() = runBlocking {
        // バッファ135_000バイト → maxVehicleCount=3。activeVehicles=255はクランプされる
        // BufferUnderflowException が発生しないことを確認
        val buffer = buildBuffer(activeVehicles = 255, playerIdx = 0)
        val repo = SharedMemoryProximityRepository(
            thresholdsRepository = FakeProximityThresholdsRepository(),
            source = makeSource(FakeProximityMemoryReader(buffer)),
        )

        repo.proximityStream().first()
        Unit
    }

    @Test
    fun `ヘッダー未満のバッファでは最大車両数は0になる`() {
        assertEquals(0, SharedMemoryProximityRepository.maxVehicleCount(ByteBuffer.allocate(1)))
    }

    // -------------------------------------------------------------------------
    // 横方向距離
    // -------------------------------------------------------------------------

    @Test
    fun `左の並走車の横方向距離が正しく返る`() = runBlocking {
        // relX = -3.0 → lateralDistanceLeftMeters = 3.0
        val buffer = buildBuffer(
            activeVehicles = 2,
            playerIdx = 0,
            opponents = listOf(VehiclePos(posX = -3.0, posZ = 0.0)),
        )
        val repo = SharedMemoryProximityRepository(
            thresholdsRepository = FakeProximityThresholdsRepository(longitudinal = 4.5),
            source = makeSource(FakeProximityMemoryReader(buffer)),
        )

        val result = repo.proximityStream().first()

        assertTrue(kotlin.math.abs(result.lateralDistanceLeftMeters - 3.0) < 1e-9)
        assertTrue(result.lateralDistanceRightMeters == Double.MAX_VALUE)
    }

    @Test
    fun `左に複数台いる場合は最近接の横距離を返す`() = runBlocking {
        // posX=-3 → absRelX=3, posX=-5 → absRelX=5 → 最近接=3.0
        val buffer = buildBuffer(
            activeVehicles = 3,
            playerIdx = 0,
            opponents = listOf(
                VehiclePos(posX = -3.0, posZ = 0.0),
                VehiclePos(posX = -5.0, posZ = 0.0),
            ),
        )
        val repo = SharedMemoryProximityRepository(
            thresholdsRepository = FakeProximityThresholdsRepository(longitudinal = 4.5),
            source = makeSource(FakeProximityMemoryReader(buffer)),
        )

        val result = repo.proximityStream().first()

        assertTrue(kotlin.math.abs(result.lateralDistanceLeftMeters - 3.0) < 1e-9)
    }

    @Test
    fun `右に複数台いる場合は最近接の横距離を返す`() = runBlocking {
        val buffer = buildBuffer(
            activeVehicles = 3,
            playerIdx = 0,
            opponents = listOf(
                VehiclePos(posX = 3.0, posZ = 0.0),
                VehiclePos(posX = 5.0, posZ = 0.0),
            ),
        )
        val repo = SharedMemoryProximityRepository(
            thresholdsRepository = FakeProximityThresholdsRepository(longitudinal = 4.5),
            source = makeSource(FakeProximityMemoryReader(buffer)),
        )

        val result = repo.proximityStream().first()

        assertEquals(3.0, result.lateralDistanceRightMeters)
    }

    @Test
    fun `横方向の最小値未満と最大値超過の車両は並走判定しない`() = runBlocking {
        val buffer = buildBuffer(
            activeVehicles = 3,
            playerIdx = 0,
            opponents = listOf(
                VehiclePos(posX = -0.5, posZ = 0.0),
                VehiclePos(posX = 6.0, posZ = 0.0),
            ),
        )
        val repo = SharedMemoryProximityRepository(
            thresholdsRepository = FakeProximityThresholdsRepository(
                longitudinal = 4.5,
                lateral = 5.0,
            ),
            source = makeSource(FakeProximityMemoryReader(buffer)),
        )

        val result = repo.proximityStream().first()

        assertFalse(result.isSideBySideLeft)
        assertFalse(result.isSideBySideRight)
    }

    @Test
    fun `横方向閾値が更新されると新しい閾値で判定する`() = runBlocking {
        val thresholds = MutableProximityThresholdsRepository(lateral = 2.0)
        val repo = SharedMemoryProximityRepository(
            thresholdsRepository = thresholds,
            source = makeSource(
                FakeProximityMemoryReader(
                    buildBuffer(
                        activeVehicles = 2,
                        playerIdx = 0,
                        opponents = listOf(VehiclePos(posX = 3.0, posZ = 0.0)),
                    ),
                ),
            ),
        )
        val results = mutableListOf<Boolean>()
        val firstResultReceived = CompletableDeferred<Unit>()

        val job = launch {
            repo.proximityStream()
                .map { it.isSideBySideRight }
                .distinctUntilChanged()
                .onEach {
                    results += it
                    if (results.size == 1) firstResultReceived.complete(Unit)
                }
                .take(2)
                .toList()
        }
        firstResultReceived.await()
        thresholds.saveLateralThresholdMeters(4.0)
        job.join()

        assertEquals(listOf(false, true), results)
    }

    @Test
    fun `並走車がいない場合は横距離が MAX_VALUE になる`() = runBlocking {
        val buffer = buildBuffer(activeVehicles = 1, playerIdx = 0)
        val repo = SharedMemoryProximityRepository(
            thresholdsRepository = FakeProximityThresholdsRepository(),
            source = makeSource(FakeProximityMemoryReader(buffer)),
        )

        val result = repo.proximityStream().first()

        assertTrue(result.lateralDistanceLeftMeters == Double.MAX_VALUE)
        assertTrue(result.lateralDistanceRightMeters == Double.MAX_VALUE)
    }
}

// -----------------------------------------------------------------------------
// ヘルパー
// -----------------------------------------------------------------------------

private fun assertEqual(expected: Int, actual: Int) = assertTrue(expected == actual)

private class FakeProximityThresholdsRepository(
    private val longitudinal: Double = 1.0,
    private val lateral: Double = 5.0,
) : ProximityThresholdsRepository {
    override fun observeLongitudinalThresholdMeters(): Flow<Double> = flowOf(longitudinal)
    override fun observeLateralThresholdMeters(): Flow<Double> = flowOf(lateral)
    override suspend fun saveLongitudinalThresholdMeters(meters: Double) = Unit
    override suspend fun saveLateralThresholdMeters(meters: Double) = Unit
}

private class MutableProximityThresholdsRepository(
    longitudinal: Double = 1.0,
    lateral: Double = 5.0,
) : ProximityThresholdsRepository {
    private val longitudinalFlow = MutableStateFlow(longitudinal)
    private val lateralFlow = MutableStateFlow(lateral)

    override fun observeLongitudinalThresholdMeters(): Flow<Double> = longitudinalFlow
    override fun observeLateralThresholdMeters(): Flow<Double> = lateralFlow

    override suspend fun saveLongitudinalThresholdMeters(meters: Double) {
        longitudinalFlow.value = meters
    }

    override suspend fun saveLateralThresholdMeters(meters: Double) {
        lateralFlow.value = meters
    }
}

private data class VehiclePos(val posX: Double = 0.0, val posZ: Double = 0.0)

private const val TELEMETRY_BASE = 128_464
private const val OFF_ACTIVE_VEHICLES = 0
private const val OFF_PLAYER_VEHICLE_IDX = 1
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

private class FakeProximityMemoryReader(
    private val buffer: ByteBuffer,
    private val openResult: Boolean = true,
    private val returnNullBuffer: Boolean = false,
) : MemoryReader {

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
