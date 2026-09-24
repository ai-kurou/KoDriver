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
}
