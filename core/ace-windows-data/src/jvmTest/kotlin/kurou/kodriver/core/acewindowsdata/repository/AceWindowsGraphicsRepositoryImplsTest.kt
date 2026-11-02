package kurou.kodriver.core.acewindowsdata.repository

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kurou.kodriver.core.acewindowsdata.datasource.AceWindowsGraphicsSharedMemorySource
import kurou.kodriver.core.windowssharedmemory.datasource.FakeWindowsSharedMemoryReader
import kurou.kodriver.domain.acewindows.model.AceWindowsGapData
import kurou.kodriver.domain.acewindows.model.AceWindowsOverallPositionData
import kurou.kodriver.domain.model.WheelIndex
import java.nio.ByteBuffer
import kotlin.test.Test
import kotlin.test.assertEquals

class AceWindowsGraphicsRepositoryImplsTest {
    private fun makeSource(configureBuffer: (ByteBuffer) -> Unit = {}) =
        AceWindowsGraphicsSharedMemorySource(
            pollingIntervalMs = 1L,
            reconnectIntervalMs = 1L,
            reader =
                FakeWindowsSharedMemoryReader(
                    initialOpen = true,
                    bufferSize = 8_192,
                    configureBuffer = configureBuffer,
                ),
            scope = CoroutineScope(SupervisorJob()),
        )

    @Test
    fun `Flagはreaderがopen済みのときデータをemitする`() =
        runTest {
            val repo = AceWindowsFlagRepositoryImpl(source = makeSource())

            repo.flagStream().first()
        }

    @Test
    fun `Statusはreaderがopen済みのときデータをemitする`() =
        runTest {
            val repo = AceWindowsStatusRepositoryImpl(source = makeSource())

            repo.statusStream().first()
        }

    @Test
    fun `BestLapTimeはreaderがopen済みのときデータをemitする`() =
        runTest {
            val repo = AceWindowsBestLapTimeRepositoryImpl(source = makeSource())

            repo.bestLapTimeStream().first()
        }

    @Test
    fun `RemainingFuelLapsはreaderがopen済みのときデータをemitする`() =
        runTest {
            val repo = AceWindowsRemainingFuelLapsRepositoryImpl(source = makeSource())

            repo.remainingFuelLapsStream().first()
        }

    @Test
    fun `Gapはreaderがopen済みのときデータをemitする`() =
        runTest {
            val repo = AceWindowsGapRepositoryImpl(source = makeSource())

            assertEquals(AceWindowsGapData(0f, 0f), repo.gapStream().first())
        }

    @Test
    fun `TyreCarcassTemperatureはreaderがopen済みのとき4輪分のカーカス平均温度をemitする`() =
        runTest {
            val repo = AceWindowsTyreCarcassTemperatureRepositoryImpl(source = makeSource())

            val result = repo.tyreCarcassTemperatureStream().first()

            assertEquals(WheelIndex.entries.toSet(), result.wheels.keys)
        }

    @Test
    fun `OverallPositionはreaderがopen済みのときデータをemitする`() =
        runTest {
            val repo =
                AceWindowsOverallPositionRepositoryImpl(
                    source =
                        makeSource {
                            it.putInt(2388, 2)
                            it.putInt(2392, 20)
                        },
                )

            assertEquals(AceWindowsOverallPositionData(2, 20), repo.overallPositionStream().first())
        }
}
