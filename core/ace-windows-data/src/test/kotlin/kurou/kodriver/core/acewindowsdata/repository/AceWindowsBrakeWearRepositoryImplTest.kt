package kurou.kodriver.core.acewindowsdata.repository

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kurou.kodriver.core.acewindowsdata.datasource.AceWindowsGraphicsSharedMemorySource
import kurou.kodriver.core.windowssharedmemory.datasource.FakeWindowsSharedMemoryReader
import kurou.kodriver.domain.model.WheelIndex
import kotlin.test.Test
import kotlin.test.assertEquals

class AceWindowsBrakeWearRepositoryImplTest {
    private fun makeSource(
        reader: FakeWindowsSharedMemoryReader,
        pollingIntervalMs: Long = 1L,
    ) = AceWindowsGraphicsSharedMemorySource(
        pollingIntervalMs = pollingIntervalMs,
        reconnectIntervalMs = 1L,
        reader = reader,
        scope = CoroutineScope(SupervisorJob()),
    )

    @Test
    fun `reader が open 済みのとき4輪分のpadLifeとdiscLifeを emit する`() =
        runTest {
            val fake = FakeWindowsSharedMemoryReader(initialOpen = true, bufferSize = 8_192)
            val repo = AceWindowsBrakeWearRepositoryImpl(source = makeSource(fake))

            val result = repo.brakeWearStream().first()

            assertEquals(WheelIndex.entries.toSet(), result.padLife.keys)
            assertEquals(WheelIndex.entries.toSet(), result.discLife.keys)
        }

    @Test
    fun `mapBrakeWearが変換した4輪分の実際の値をそのままemitする`() =
        runTest {
            val fake =
                FakeWindowsSharedMemoryReader(
                    initialOpen = true,
                    bufferSize = 8_192,
                    configureBuffer = { buffer ->
                        WheelIndex.entries.forEach { wheel ->
                            buffer.putFloat(OFF_PAD_LIFE + wheel.ordinal * WEAR_STRIDE, PAD_LIFE_BASE + wheel.ordinal)
                            buffer.putFloat(OFF_DISC_LIFE + wheel.ordinal * WEAR_STRIDE, DISC_LIFE_BASE + wheel.ordinal)
                        }
                    },
                )
            val repo = AceWindowsBrakeWearRepositoryImpl(source = makeSource(fake))

            val result = repo.brakeWearStream().first()

            WheelIndex.entries.forEach { wheel ->
                assertEquals((PAD_LIFE_BASE + wheel.ordinal).toDouble(), result.padLife[wheel]!!, TOLERANCE)
                assertEquals((DISC_LIFE_BASE + wheel.ordinal).toDouble(), result.discLife[wheel]!!, TOLERANCE)
            }
        }

    private companion object {
        const val OFF_PAD_LIFE = 740
        const val OFF_DISC_LIFE = 756
        const val WEAR_STRIDE = 4
        const val PAD_LIFE_BASE = 0.90f
        const val DISC_LIFE_BASE = 0.10f
        const val TOLERANCE = 0.0001
    }
}
