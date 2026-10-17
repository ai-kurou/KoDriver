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

class AceWindowsTyreCarcassTemperatureRepositoryImplTest {
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
    fun `reader が open 済みのとき4輪分のカーカス平均温度を emit する`() =
        runTest {
            val fake = FakeWindowsSharedMemoryReader(initialOpen = true, bufferSize = 8_192)
            val repo = AceWindowsTyreCarcassTemperatureRepositoryImpl(source = makeSource(fake))

            val result = repo.tyreCarcassTemperatureStream().first()

            assertEquals(WheelIndex.entries.toSet(), result.wheels.keys)
        }
}
